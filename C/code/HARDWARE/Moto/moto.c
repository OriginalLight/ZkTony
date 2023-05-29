#include "pwm.h"
#include "moto.h"
#include "exti.h"
#include "can.h"
#include "delay.h"
#include "usart.h"
#include "exti.h"
#include "time.h"
#include <math.h>

u8 MResFlag[MOTONUM] = {0};

u32 RSACC[ACCNUM] = {0xffff};
// u32 QSACC[ACCNUM]={0xffff};
// u32 WSACC[ACCNUM]={0xffff};
// u32 ESACC[ACCNUM]={0xffff};
//
// u32 TSACC[ACCNUM]={0xffff};
// u32 YSACC[ACCNUM]={0xffff};
// u32 USACC[ACCNUM]={0xffff};
// u32 ISACC[ACCNUM]={0xffff};
// u32 OSACC[ACCNUM]={0xffff};
// u32 PSACC[ACCNUM]={0xffff};
// u32 ASACC[ACCNUM]={0xffff};
// u32 SSACC[ACCNUM]={0xffff};
// u32 DSACC[ACCNUM]={0xffff};
// u32 FSACC[ACCNUM]={0xffff};
// u32 GSACC[ACCNUM]={0xffff};
// u32 HSACC[ACCNUM]={0xffff};

u8 Para_Save[32] = {0x01, 0x01, 0xC2, 0x00, 0x01, 0x01, 0x01, 0x01, 0x10, 0x02, 0x58, 0x64, 0x64, 0x01, 0x00, 0x00, 0x10, 0x02, 0x58, 0x64, 0x64, 0x01, 0x00, 0x00, 0x10, 0x02, 0x58, 0x64, 0x64, 0x00, 0x00, 0x00};

speedRampData srd[MOTONUM] = {0};

Moto_Struct Moto[MOTONUM] = {0};
Device_Struct DeviceA;

u8 Moto_State = 0; // 0 λ����  1 ��λ  2 �˶���  3 ��ͣ  4 �˶�ֹͣ

void AxisMove1(u32 step, u32 accel, u32 decel, u32 speed)
{
	//! Number of steps before we hit max speed.
	u32 max_s_lim;
	//! Number of steps before we must start deceleration (if accel does not hit max speed).
	u32 accel_lim;

	Moto[1].Mstate = 1;

	if (step == 1)
	{
		// Move one step...
		srd[1].accel_count = -1;
		// ...in DECEL state.
		srd[1].run_state = DECEL;
		// Just a short delay so main() can act on 'running'.
		srd[1].step_delay = 1000; // 100

		TIM1->CCR3 = 1000;
		TIM1->ARR = 2000;

		// ԽСƵ��Խ��  8000 000
		TIMxCHxOutControl(TIM1, 3, 1);
		//		TIM_CtrlPWMOutputs(TIM3,ENABLE);
		//	  TIM_Cmd(TIM3, ENABLE);
	}
	// Only move if number of steps to move is not zero.
	else if (step != 0)
	{
		// Refer to documentation for detailed information about these calculations.

		// Set max speed limit, by calc min_delay to use in timer.
		// min_delay = (alpha / tt)/ w
		srd[1].min_delay = T1_FREQ_2 / speed; // sudu

		// Set accelration by calc the first (c0) step delay .
		// step_delay = 1/tt * sqrt(2*alpha/accel)
		// step_delay = ( tfreq*0.676/100 )*100 * sqrt( (2*alpha*10000000000) / (accel*100) )/10000

		// srd[1].step_delay = ((long)T1_FREQ*0.676* sqrt(2000000 / accel))/1000/2;  //�����ٶ� �����ȷ��һ��

		srd[1].step_delay = 338 * sqrt(2000000 / accel); // �����ٶ� �����ȷ��һ��
		// Find out after how many steps does the speed hit the max speed limit.
		// max_s_lim = speed^2 / (2*alpha*accel)
		max_s_lim = speed * speed / (2 * accel);
		// If we hit max speed limit before 0,5 step it will round to 0.
		// But in practice we need to move atleast 1 step to get any speed at all.
		if (max_s_lim == 0)
		{
			max_s_lim = 1;
		}

		// Find out after how many steps we must start deceleration.
		// n1 = (n1+n2)decel / (accel + decel)
		//     if((accel+decel)>step)
		//		{
		//			accel_lim = step*decel/(accel+decel);

		accel_lim = ((float)step * decel) / (accel + decel);
		//		}
		//		else
		//		{
		//			accel_lim = step/(accel+decel)*decel;
		//		}
		// We must accelrate at least 1 step before we can start deceleration.
		if (accel_lim == 0)
		{
			accel_lim = 1;
		}

		// Use the limit we hit first to calc decel.
		if (accel_lim <= max_s_lim)
		{
			srd[1].decel_val = accel_lim - step;
		}
		else
		{
			srd[1].decel_val = -(s32)(max_s_lim * accel / decel);
		}
		// We must decelrate at least 1 step to stop.
		if (srd[1].decel_val == 0)
		{
			srd[1].decel_val = -1;
		}

		// Find step to start decleration.
		srd[1].decel_start = step + srd[1].decel_val;

		// If the maximum speed is so low that we dont need to go via accelration state.
		if (srd[1].step_delay <= srd[1].min_delay)
		{
			srd[1].step_delay = srd[1].min_delay;
			srd[1].run_state = RUN;
		}
		else
		{
			srd[1].run_state = ACCEL;
		}

		// Reset counter.
		srd[1].accel_count = 0;

		TIM1->CCR3 = srd[1].step_delay / 2;
		TIM1->ARR = srd[1].step_delay;
		TIMxCHxOutControl(TIM1, 3, 1);
		//		TIM_ITConfig(TIM3,TIM_IT_Update,ENABLE); //ENABLE TIM1 PWM INTERUPT
		//		// Set Timer/Counter to divide clock by 8
		//		//TCCR1B |= ((0<<CS12)|(1<<CS11)|(0<<CS10));
		//		TIM_CtrlPWMOutputs(TIM3,ENABLE);
		//		TIM_Cmd(TIM3, ENABLE);
	}
	// while(srd[1].run_state !=STOP) ; //WAIT FOR MOTO
}

void AxisMove2(u32 step, u32 accel, u32 decel, u32 speed)
{
	//! Number of steps before we hit max speed.
	u32 max_s_lim;
	//! Number of steps before we must start deceleration (if accel does not hit max speed).
	u32 accel_lim;

	Moto[2].Mstate = 1;

	if (step == 1)
	{
		// Move one step...
		srd[2].accel_count = -1;
		// ...in DECEL state.
		srd[2].run_state = DECEL;
		// Just a short delay so main() can act on 'running'.
		srd[2].step_delay = 1000; // 100

		TIM1->CCR4 = 1000;
		TIM1->ARR = 2000;

		// ԽСƵ��Խ��  8000 000
		TIMxCHxOutControl(TIM1, 4, 1);
		//		TIM_CtrlPWMOutputs(TIM3,ENABLE);
		//	  TIM_Cmd(TIM3, ENABLE);
	}
	// Only move if number of steps to move is not zero.
	else if (step != 0)
	{
		// Refer to documentation for detailed information about these calculations.

		// Set max speed limit, by calc min_delay to use in timer.
		// min_delay = (alpha / tt)/ w
		srd[2].min_delay = T1_FREQ_2 / speed; // sudu

		// Set accelration by calc the first (c0) step delay .
		// step_delay = 1/tt * sqrt(2*alpha/accel)
		// step_delay = ( tfreq*0.676/100 )*100 * sqrt( (2*alpha*10000000000) / (accel*100) )/10000

		// srd[2].step_delay = ((long)T1_FREQ*0.676* sqrt(2000000 / accel))/1000/2;  //�����ٶ� �����ȷ��һ��

		srd[2].step_delay = 338 * sqrt(2000000 / accel); // �����ٶ� �����ȷ��һ��
		// Find out after how many steps does the speed hit the max speed limit.
		// max_s_lim = speed^2 / (2*alpha*accel)
		max_s_lim = speed * speed / (2 * accel);
		// If we hit max speed limit before 0,5 step it will round to 0.
		// But in practice we need to move atleast 1 step to get any speed at all.
		if (max_s_lim == 0)
		{
			max_s_lim = 1;
		}

		// Find out after how many steps we must start deceleration.
		// n1 = (n1+n2)decel / (accel + decel)
		//     if((accel+decel)>step)
		//		{
		//			accel_lim = step*decel/(accel+decel);

		accel_lim = ((float)step * decel) / (accel + decel);
		//		}
		//		else
		//		{
		//			accel_lim = step/(accel+decel)*decel;
		//		}
		// We must accelrate at least 1 step before we can start deceleration.
		if (accel_lim == 0)
		{
			accel_lim = 1;
		}

		// Use the limit we hit first to calc decel.
		if (accel_lim <= max_s_lim)
		{
			srd[2].decel_val = accel_lim - step;
		}
		else
		{
			srd[2].decel_val = -(s32)(max_s_lim * accel / decel);
		}
		// We must decelrate at least 1 step to stop.
		if (srd[2].decel_val == 0)
		{
			srd[2].decel_val = -1;
		}

		// Find step to start decleration.
		srd[2].decel_start = step + srd[2].decel_val;

		// If the maximum speed is so low that we dont need to go via accelration state.
		if (srd[2].step_delay <= srd[2].min_delay)
		{
			srd[2].step_delay = srd[2].min_delay;
			srd[2].run_state = RUN;
		}
		else
		{
			srd[2].run_state = ACCEL;
		}

		// Reset counter.
		srd[2].accel_count = 0;

		TIM1->CCR4 = srd[2].step_delay / 2;
		TIM1->ARR = srd[2].step_delay;
		TIMxCHxOutControl(TIM1, 4, 1);
		//		TIM_ITConfig(TIM3,TIM_IT_Update,ENABLE); //ENABLE TIM1 PWM INTERUPT
		//		// Set Timer/Counter to divide clock by 8
		//		//TCCR1B |= ((0<<CS12)|(1<<CS11)|(0<<CS10));
		//		TIM_CtrlPWMOutputs(TIM3,ENABLE);
		//		TIM_Cmd(TIM3, ENABLE);
	}
	// while(srd[2].run_state !=STOP) ; //WAIT FOR MOTO
}

void AxisSpeed(u32 num, u32 accel, u32 speed)
{
	//! Number of steps before we hit max speed.
	//  u32 max_s_lim;
	//  //! Number of steps before we must start deceleration (if accel does not hit max speed).
	//  u32 accel_lim;

	float fremin, fremax, flexible;
	//	int len;
	float deno, melo, delt;
	// float yrange;
	u16 i;

	Moto[num].Mstate = 2;

	fremin = 400;

	//	fremax= 25600;
	fremax = speed; // 3200

	if (speed < 400)
		fremax = 600;

	delt = fremax - fremin;
	flexible = 6;
	//	Fre=1000000;
	// len =100;

	for (i = 0; i < ACCNUM; i++)
	{
		melo = (float)(flexible * (i - ACCNUM / 2) / (ACCNUM / 2));
		deno = (float)(1.0 / (1 + expf(-melo))); // expf is a library function of exponential(e)
		//			fre[i] = delt * deno + fremin;
		//			period[i] = (int)(Fre / fre[i]);    // 10000000 is the timer driver frequency
		//		..\HARDWARE\Moto\moto.c(302): error:  #268: declaration may not appear after executable statement in block

		//		fre[i] = delt * deno + fremin;
		//	period[i] = (int)(Fre / (delt * deno + fremin));    // 10000000 is the timer driver frequency
		RSACC[i] = (int)(T1_FREQ_2 / (delt * deno + fremin));
	}

	srd[num].run_state = ACCEL;

	srd[num].step_delay = RSACC[0];

	//		TIM3->CCR2=srd[num].step_delay/2;
	//		TIM3->ARR=srd[num].step_delay;

	//		TIM_ITConfig(TIM3,TIM_IT_Update,ENABLE); //ENABLE TIM1 PWM INTERUPT
	//		// Set Timer/Counter to divide clock by 8
	//		//TCCR1B |= ((0<<CS12)|(1<<CS11)|(0<<CS10));
	//		TIM_CtrlPWMOutputs(TIM3,ENABLE);
	//		TIM_Cmd(TIM3, ENABLE);

	CompareValue(num, 50);
}

/**************************************
 AxisMove
 Input:step  �ƶ�����  >0 ��ת  <0 ��ת
	  :accel ���ٶ�
			:decel ���ٶ�
			:speed �ٶ�
			num :motoID  ID == 0~15

 ***********************************/

void AxisMove(u32 num, s32 step, u32 accel, u32 decel, u32 speed)
{
	//! Number of steps before we hit max speed.
	u32 max_s_lim;
	//! Number of steps before we must start deceleration (if accel does not hit max speed).
	u32 accel_lim;

	Moto[num].Mstate = 1;

	MResFlag[num] = 0;

	if (srd[num].run_state != STOP)
	{ // ֻ�������������ֹͣ��ʱ��ż���
		return;
	}

	if (step < 0) // ����Ϊ���� ��ת Ĭ��Ϊ��ת
	{
		Moto[num].MotoDir = 0;
		step = -step;
	}

	if (step == 1)
	{
		// Move one step...
		// srd[num].accel_count = -1;
		srd[num].accel_count = -1;
		// ...in DECEL state.
		srd[num].run_state = DECEL;
		// Just a short delay so main() can act on 'running'.
		srd[num].step_delay = 1000; // 100
	}
	// Only move if number of steps to move is not zero.
	else if (step != 0)
	{
		// Refer to documentation for detailed information about these calculations.

		// Set max speed limit, by calc min_delay to use in timer.
		// min_delay = (alpha / tt)/ w
		srd[num].min_delay = T1_FREQ_2 / speed; // sudu

		// Set accelration by calc the first (c0) step delay .
		// step_delay = 1/tt * sqrt(2*alpha/accel)
		// step_delay = ( tfreq*0.676/100 )*100 * sqrt( (2*alpha*10000000000) / (accel*100) )/10000

		// srd9.step_delay = ((long)T1_FREQ*0.676* sqrt(2000000 / accel))/1000/2;  //�����ٶ� �����ȷ��һ��

		srd[num].step_delay = 338 * sqrt(2000000 / accel); // �����ٶ� �����ȷ��һ��
		// Find out after how many steps does the speed hit the max speed limit.
		// max_s_lim = speed^2 / (2*alpha*accel)
		max_s_lim = speed * speed / (2 * accel);
		// If we hit max speed limit before 0,5 step it will round to 0.
		// But in practice we need to move atleast 1 step to get any speed at all.
		if (max_s_lim == 0)
		{
			max_s_lim = 1;
		}

		// Find out after how many steps we must start deceleration.
		// n1 = (n1+n2)decel / (accel + decel)
		//     if((accel+decel)>step)
		//		{
		//			accel_lim = step*decel/(accel+decel);

		accel_lim = ((float)step * decel) / (accel + decel);
		//		}
		//		else
		//		{
		//			accel_lim = step/(accel+decel)*decel;
		//		}
		// We must accelrate at least 1 step before we can start deceleration.
		if (accel_lim == 0)
		{
			accel_lim = 1;
		}

		// Use the limit we hit first to calc decel.
		if (accel_lim <= max_s_lim)
		{
			srd[num].decel_val = accel_lim - step;
		}
		else
		{
			srd[num].decel_val = -(s32)(max_s_lim * accel / decel);
		}
		// We must decelrate at least 1 step to stop.
		if (srd[num].decel_val == 0)
		{
			srd[num].decel_val = -1;
		}

		// Find step to start decleration.
		srd[num].decel_start = step + srd[num].decel_val;

		// If the maximum speed is so low that we dont need to go via accelration state.
		if (srd[num].step_delay <= srd[num].min_delay)
		{
			srd[num].step_delay = srd[num].min_delay;
			srd[num].run_state = RUN;
		}
		else
		{
			srd[num].run_state = ACCEL;
		}

		// Reset counter.
		srd[num].accel_count = 0;
	}
	CompareValue(num, step);
	//			switch(num)
	//				{
	//					case 1:
	//						 if(step == 1)
	//						 {TIM1->CCR3=1000;
	//							TIM1->ARR=2000;}
	//						 else
	//						 {TIM1->CCR3=srd[num].step_delay/2;
	//							TIM1->ARR=srd[num].step_delay;
	//							}
	//						 TIMxCH3OutControl(TIM1,1);
	//							break;
	//					case 2:
	//						 if(step == 1)
	//						 {TIM1->CCR4=1000;
	//							TIM1->ARR=2000;}
	//						 else
	//						 {TIM1->CCR4=srd[num].step_delay/2;
	//							TIM1->ARR=srd[num].step_delay;
	//							}
	//						  TIMxCH4OutControl(TIM1,1);
	//							break;
	//					case 3:
	//						 if(step == 1)
	//						 {TIM1->CCR2=1000;
	//							TIM1->ARR=2000;}
	//						 else
	//						 {TIM1->CCR2=srd[num].step_delay/2;
	//							TIM1->ARR=srd[num].step_delay;
	//							}
	//						 TIMxCH2OutControl(TIM1,1);
	//							break;
	//				case 4:
	//						 if(step == 1)
	//						 {TIM1->CCR1=1000;
	//							TIM1->ARR=2000;}
	//						 else
	//						 {TIM1->CCR1=srd[num].step_delay/2;
	//							TIM1->ARR=srd[num].step_delay;
	//							}
	//						 TIMxCH1OutControl(TIM1,1);
	//							break;
	//				case 5:
	//						 if(step == 1)
	//						 {TIM3->CCR4=1000;
	//							TIM3->ARR=2000;}
	//						 else
	//						 {TIM3->CCR4=srd[num].step_delay/2;
	//							TIM3->ARR=srd[num].step_delay;
	//							}
	//						  TIMxCH4OutControl(TIM3,1);
	//							break;
	//				case 6:
	//					 if(step == 1)
	//					 {TIM3->CCR3=1000;
	//						TIM3->ARR=2000;}
	//					 else
	//					 {TIM3->CCR3=srd[num].step_delay/2;
	//						TIM3->ARR=srd[num].step_delay;
	//						}
	//					  TIMxCH3OutControl(TIM3,1);
	//						break;
	//				case 7:
	//					 if(step == 1)
	//					 {TIM2->CCR4=1000;
	//						TIM2->ARR=2000;}
	//					 else
	//					 {TIM2->CCR4=srd[num].step_delay/2;
	//						TIM2->ARR=srd[num].step_delay;
	//						}
	//					  TIMxCH4OutControl(TIM2,1);
	//						break;
	//				case 8:
	//					 if(step == 1)
	//					 {TIM2->CCR3=1000;
	//						TIM2->ARR=2000;}
	//					 else
	//					 {TIM2->CCR3=srd[num].step_delay/2;
	//						TIM2->ARR=srd[num].step_delay;
	//						}
	//					  TIMxCH3OutControl(TIM2,1);
	//						break;

	//				case 9:
	//					 if(step == 1)
	//					 {TIM2->CCR1=1000;
	//						TIM2->ARR=2000;}
	//					 else
	//					 {TIM2->CCR1=srd[num].step_delay/2;
	//						TIM2->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH1OutControl(TIM2,1);
	//						break;
	//
	//				case 10:
	//					 if(step == 1)
	//					 {TIM2->CCR2=1000;
	//						TIM2->ARR=2000;}
	//					 else
	//					 {TIM2->CCR2=srd[num].step_delay/2;
	//						TIM2->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH2OutControl(TIM2,1);
	//						break;
	//				case 11:
	//					 if(step == 1)
	//					 {TIM3->CCR2=1000;
	//						TIM3->ARR=2000;}
	//					 else
	//					 {TIM3->CCR2=srd[num].step_delay/2;
	//						TIM3->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH2OutControl(TIM3,1);
	//						break;
	//				case 12:
	//					 if(step == 1)
	//					 {TIM3->CCR1=1000;
	//						TIM3->ARR=2000;}
	//					 else
	//					 {TIM3->CCR1=srd[num].step_delay/2;
	//						TIM3->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH1OutControl(TIM3,1);
	//						break;
	//				case 13:
	//					 if(step == 1)
	//					 {TIM8->CCR1=1000;
	//						TIM8->ARR=2000;}
	//					 else
	//					 {TIM8->CCR1=srd[num].step_delay/2;
	//						TIM8->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH1OutControl(TIM8,1);
	//						break;
	//				case 14:
	//					 if(step == 1)
	//					 {TIM8->CCR2=1000;
	//						TIM8->ARR=2000;}
	//					 else
	//					 {TIM8->CCR2=srd[num].step_delay/2;
	//						TIM8->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH2OutControl(TIM8,1);
	//						break;
	//				case 15:
	//					 if(step == 1)
	//					 {TIM8->CCR3=1000;
	//						TIM8->ARR=2000;}
	//					 else
	//					 {TIM8->CCR3=srd[num].step_delay/2;
	//						TIM8->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH3OutControl(TIM8,1);
	//						break;
	//				case 16:
	//					 if(step == 1)
	//					 {TIM8->CCR4=1000;
	//						TIM8->ARR=2000;}
	//					 else
	//					 {TIM8->CCR4=srd[num].step_delay/2;
	//						TIM8->ARR=srd[num].step_delay;
	//						}
	//					 TIMxCH4OutControl(TIM8,1);
	//						break;
	//				}
	//				//while((srd[num].run_state != STOP));
}

/**
  * ��������: ���λ���˶����˶������Ĳ���
  * �������: step���ƶ��Ĳ��� (����Ϊ˳ʱ�룬����Ϊ��ʱ��).
			  accel  ���ٶ�,ʵ��ֵΪaccel*0.025*rad/sec^2
			  decel  ���ٶ�,ʵ��ֵΪdecel*0.025*rad/sec^2
			  speed  ����ٶ�,ʵ��ֵΪspeed*0.05*rad/sec
  * �� �� ֵ: ��
  * ˵    ��: �Ը����Ĳ����ƶ�����������ȼ��ٵ�����ٶȣ�Ȼ���ں���λ�ÿ�ʼ
  *           ������ֹͣ��ʹ�������˶�����Ϊָ���Ĳ���������Ӽ��ٽ׶κ̲ܶ���
  *           �ٶȺ������ǻ�û�ﵽ����ٶȾ�Ҫ��ʼ����
  */
void STEPMOTOR_AxisMoveRel(uint32_t num, int32_t step, uint32_t accel, uint32_t decel, uint32_t speed)
{
	__IO uint16_t tim_count;
	// �ﵽ����ٶ�ʱ�Ĳ���
	__IO uint32_t max_s_lim;
	// ����Ҫ��ʼ���ٵĲ������������û�дﵽ����ٶȣ�
	__IO uint32_t accel_lim;

	Moto[num].Mstate = 1;

	if (srd[num].run_state != STOP) // ֻ�������������ֹͣ��ʱ��ż���
		return;
	if (step < 0) // ����Ϊ����
	{
		srd[num].dir = Moto_Back; // ��ʱ�뷽����ת

		step = -step; // ��ȡ��������ֵ
	}
	else
	{
		srd[num].dir = Moto_For; // ˳ʱ�뷽����ת
	}

	if (step == 1) // ����Ϊ1
	{
		srd[num].accel_count = -1;	// ֻ�ƶ�һ��
		srd[num].run_state = DECEL; // ����״̬.
		srd[num].step_delay = 1000; // ����ʱ
	}
	else if (step != 0) // ���Ŀ���˶�������Ϊ0
	{
		// ���ǵĵ������ר��ָ���ֲ�����ϸ�ļ��㼰�Ƶ�����

		// ��������ٶȼ���, ����õ�min_delay���ڶ�ʱ���ļ�������ֵ��
		// min_delay = (alpha / tt)/ w
		srd[num].min_delay = (int32_t)(A_T_x10 / speed);

		// ͨ�������һ��(c0) �Ĳ�����ʱ���趨���ٶȣ�����accel��λΪ0.1rad/sec^2
		// step_delay = 1/tt * sqrt(2*alpha/accel)
		// step_delay = ( tfreq*0.676/10 )*10 * sqrt( (2*alpha*100000) / (accel*10) )/100
		srd[num].step_delay = (int32_t)((T1_FREQ_148 * sqrt(A_SQ / accel)) / 10);

		// ������ٲ�֮��ﵽ����ٶȵ�����
		// max_s_lim = speed^2 / (2*alpha*accel)
		max_s_lim = speed * speed / (2 * accel);
		// ����ﵽ����ٶ�С��0.5�������ǽ���������Ϊ0
		// ��ʵ�����Ǳ����ƶ�����һ�����ܴﵽ��Ҫ���ٶ�
		if (max_s_lim == 0)
		{
			max_s_lim = 1;
		}

		// ������ٲ�֮�����Ǳ��뿪ʼ����
		// n1 = (n1+n2)decel / (accel + decel)
		accel_lim = (uint32_t)(step * decel / (accel + decel));
		// ���Ǳ����������1�����ܲ��ܿ�ʼ����.
		if (accel_lim == 0)
		{
			accel_lim = 1;
		}

		// ʹ�������������ǿ��Լ�������ٽ׶β���
		if (accel_lim <= max_s_lim)
		{
			srd[num].decel_val = accel_lim - step;
		}
		else
		{
			srd[num].decel_val = -(max_s_lim * accel / decel);
		}
		// ��ֻʣ��һ�����Ǳ������
		if (srd[num].decel_val == 0)
		{
			srd[num].decel_val = -1;
		}

		// ���㿪ʼ����ʱ�Ĳ���
		srd[num].decel_start = step + srd[num].decel_val;

		// �������ٶȺ��������ǾͲ���Ҫ���м����˶�
		if (srd[num].step_delay <= srd[num].min_delay)
		{
			srd[num].step_delay = srd[num].min_delay;
			srd[num].run_state = RUN;
		}
		else
		{
			srd[num].run_state = ACCEL;
		}
		// ��λ���ٶȼ���ֵ
		srd[num].accel_count = 0;
	}
	//  MotionStatus = 1; // ���Ϊ�˶�״̬
	//  tim_count=__HAL_TIM_GET_COUNTER(&htimx_STEPMOTOR);
	//  __HAL_TIM_SET_COMPARE(&htimx_STEPMOTOR,STEPMOTOR_TIM_CHANNEL_x,tim_count+srd[num].step_delay); // ���ö�ʱ���Ƚ�ֵ
	//  TIM_CCxChannelCmd(STEPMOTOR_TIMx, STEPMOTOR_TIM_CHANNEL_x, TIM_CCx_ENABLE);// ʹ�ܶ�ʱ��ͨ��
	//  STEPMOTOR_OUTPUT_ENABLE();
	CompareValue(num, step);
}

void CompareValue(u32 num, int32_t step)
{
	switch (num)
	{
	case 0:
		if (step == 1)
		{
			TIM1->CCR3 = 1000;
			TIM1->ARR = 2000;
		}
		else
		{
			TIM1->CCR3 = srd[num].step_delay / 2;
			TIM1->ARR = srd[num].step_delay;
		}
		TIMxCH3OutControl(TIM1, 1);
		break;
	case 1:
		if (step == 1)
		{
			TIM1->CCR4 = 1000;
			TIM1->ARR = 2000;
		}
		else
		{
			TIM1->CCR4 = srd[num].step_delay / 2;
			TIM1->ARR = srd[num].step_delay;
		}
		TIMxCH4OutControl(TIM1, 1);
		break;
	case 2:
		if (step == 1)
		{
			TIM1->CCR2 = 1000;
			TIM1->ARR = 2000;
		}
		else
		{
			TIM1->CCR2 = srd[num].step_delay / 2;
			TIM1->ARR = srd[num].step_delay;
		}
		TIMxCH2OutControl(TIM1, 1);
		break;
	case 3:
		if (step == 1)
		{
			TIM1->CCR1 = 1000;
			TIM1->ARR = 2000;
		}
		else
		{
			TIM1->CCR1 = srd[num].step_delay / 2;
			TIM1->ARR = srd[num].step_delay;
		}
		TIMxCH1OutControl(TIM1, 1);
		break;
	case 4:
		if (step == 1)
		{
			TIM3->CCR4 = 1000;
			TIM3->ARR = 2000;
		}
		else
		{
			TIM3->CCR4 = srd[num].step_delay / 2;
			TIM3->ARR = srd[num].step_delay;
		}
		TIMxCH4OutControl(TIM3, 1);
		break;
	case 5:
		if (step == 1)
		{
			TIM3->CCR3 = 1000;
			TIM3->ARR = 2000;
		}
		else
		{
			TIM3->CCR3 = srd[num].step_delay / 2;
			TIM3->ARR = srd[num].step_delay;
		}
		TIMxCH3OutControl(TIM3, 1);
		break;
	case 6:
		if (step == 1)
		{
			TIM2->CCR4 = 1000;
			TIM2->ARR = 2000;
		}
		else
		{
			TIM2->CCR4 = srd[num].step_delay / 2;
			TIM2->ARR = srd[num].step_delay;
		}
		TIMxCH4OutControl(TIM2, 1);
		break;
	case 7:
		if (step == 1)
		{
			TIM2->CCR3 = 1000;
			TIM2->ARR = 2000;
		}
		else
		{
			TIM2->CCR3 = srd[num].step_delay / 2;
			TIM2->ARR = srd[num].step_delay;
		}
		TIMxCH3OutControl(TIM2, 1);
		break;

	case 8:
		if (step == 1)
		{
			TIM2->CCR1 = 1000;
			TIM2->ARR = 2000;
		}
		else
		{
			TIM2->CCR1 = srd[num].step_delay / 2;
			TIM2->ARR = srd[num].step_delay;
		}
		TIMxCH1OutControl(TIM2, 1);
		break;

	case 9:
		if (step == 1)
		{
			TIM2->CCR2 = 1000;
			TIM2->ARR = 2000;
		}
		else
		{
			TIM2->CCR2 = srd[num].step_delay / 2;
			TIM2->ARR = srd[num].step_delay;
		}
		TIMxCH2OutControl(TIM2, 1);
		break;
	case 10:
		if (step == 1)
		{
			TIM3->CCR2 = 1000;
			TIM3->ARR = 2000;
		}
		else
		{
			TIM3->CCR2 = srd[num].step_delay / 2;
			TIM3->ARR = srd[num].step_delay;
		}
		TIMxCH2OutControl(TIM3, 1);
		break;
	case 11:
		if (step == 1)
		{
			TIM3->CCR1 = 1000;
			TIM3->ARR = 2000;
		}
		else
		{
			TIM3->CCR1 = srd[num].step_delay / 2;
			TIM3->ARR = srd[num].step_delay;
		}
		TIMxCH1OutControl(TIM3, 1);
		break;
	case 12:
		if (step == 1)
		{
			TIM8->CCR1 = 1000;
			TIM8->ARR = 2000;
		}
		else
		{
			TIM8->CCR1 = srd[num].step_delay / 2;
			TIM8->ARR = srd[num].step_delay;
		}
		TIMxCH1OutControl(TIM8, 1);
		break;
	case 13:
		if (step == 1)
		{
			TIM8->CCR2 = 1000;
			TIM8->ARR = 2000;
		}
		else
		{
			TIM8->CCR2 = srd[num].step_delay / 2;
			TIM8->ARR = srd[num].step_delay;
		}
		TIMxCH2OutControl(TIM8, 1);
		break;
	case 14:
		if (step == 1)
		{
			TIM8->CCR3 = 1000;
			TIM8->ARR = 2000;
		}
		else
		{
			TIM8->CCR3 = srd[num].step_delay / 2;
			TIM8->ARR = srd[num].step_delay;
		}
		TIMxCH3OutControl(TIM8, 1);
		break;
	case 15:
		if (step == 1)
		{
			TIM8->CCR4 = 1000;
			TIM8->ARR = 2000;
		}
		else
		{
			TIM8->CCR4 = srd[num].step_delay / 2;
			TIM8->ARR = srd[num].step_delay;
		}
		TIMxCH4OutControl(TIM8, 1);
		break;
	}
}

void TIMControl(u8 num, u8 operation)
{
	switch (num)
	{
	case 1:

		TIMxCH3OutControl(TIM1, operation);
		break;
	case 2:

		TIMxCH4OutControl(TIM1, operation);
		break;
	case 3:

		TIMxCH2OutControl(TIM1, operation);
		break;
	case 4:

		TIMxCH1OutControl(TIM1, operation);
		break;
	case 5:

		TIMxCH4OutControl(TIM3, operation);
		break;
	case 6:

		TIMxCH3OutControl(TIM3, operation);
		break;
	case 7:

		TIMxCH4OutControl(TIM2, operation);
		break;
	case 8:

		TIMxCH3OutControl(TIM2, operation);
		break;

	case 9:

		TIMxCH1OutControl(TIM2, operation);
		break;

	case 10:

		TIMxCH2OutControl(TIM2, operation);
		break;
	case 11:

		TIMxCH2OutControl(TIM3, operation);
		break;
	case 12:

		TIMxCH1OutControl(TIM3, operation);
		break;
	case 13:

		TIMxCH1OutControl(TIM8, operation);
		break;
	case 14:

		TIMxCH2OutControl(TIM8, operation);
		break;
	case 15:

		TIMxCH3OutControl(TIM8, operation);
		break;
	case 16:

		TIMxCH4OutControl(TIM8, operation);
		break;
	}
}

/*
 �����Ӧ������ʼ��

 */
void MotoInitConfig()
{
	u8 i;
	DeviceA.addr = cmd_DA[0];

	for (i = 0; i < MOTONUM; i++)
	{
		Moto[i].Mxf = cmd_DA[8];
		Moto[i].MotoRpmSpeed = (cmd_DA[9] << 8) + cmd_DA[10];
		Moto[i].MAccfactor = cmd_DA[11];
		Moto[i].MDelfactor = cmd_DA[12];
		Moto[i].Mmode = cmd_DA[13];
		Moto[i].MdelayTime = (cmd_DA[14] << 8) + cmd_DA[15];

		Moto[i].MotoSpeed = Moto[i].MotoRpmSpeed * Moto[i].Mxf * 10 / 3;

		Moto[i].Maccel = Moto[i].MotoSpeed * Moto[i].MAccfactor / 50; // 1600
		Moto[i].Mdecel = Moto[i].MotoSpeed * Moto[i].MDelfactor / 50;

		Moto[i].Maction = 0;

		Moto[i].Mlevel = 1;
		Moto[i].MotoDir = 1;
	}

	// ���ʹ��
	M1_EN = 0;
	M2_EN = 0;
	M3_EN = 0;
	M4_EN = 0;
	M5_EN = 0;
	M6_EN = 0;
	M7_EN = 0;
	M8_EN = 0;
	M9_EN = 0;

	M11_EN = 0;
	M12_EN = 0;
	M13_EN = 0;
	M14_EN = 0;
	M15_EN = 0;
	M16_EN = 0;
	M10_EN = 0;

	// ����˶�����
	M1_DIR = 1;
	M2_DIR = 1;
	M3_DIR = 1;
	M4_DIR = 1;
	M5_DIR = 1;
	M6_DIR = 1;
	M7_DIR = 1;
	M8_DIR = 1;
	M9_DIR = 1;
	M10_DIR = 1;
	M11_DIR = 1;
	M12_DIR = 1;
	M13_DIR = 1;
	M14_DIR = 1;
	M15_DIR = 1;
	M16_DIR = 1;
}
