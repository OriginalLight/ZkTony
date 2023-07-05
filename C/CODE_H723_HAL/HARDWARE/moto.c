#include "moto.h"
#include "sys.h" 
#include "math.h"

SpeedRampData srd[MOTONUM] = {0};
Moto_Struct Moto[MOTONUM] = {0};

void TIM_SetCompare(uint8_t num, uint16_t val)
{
	switch (num)
	{
	case 0:
		TIM1->CCR3 = val; // TIMx->CCR1
		break;
	case 1:
		TIM1->CCR4 = val;
		break;
	case 2:
		TIM1->CCR2 = val;
		break;
	case 3:
		TIM1->CCR1 = val;
		break;
	case 4:
		TIM3->CCR4 = val;
		break;
	case 5:
		TIM3->CCR3 = val;
		break;
	case 6:
		TIM2->CCR4 = val;
		break;
	case 7:
		TIM2->CCR3 = val;
		break;
	case 8:
		TIM2->CCR1 = val;
		break;
	case 9:
		TIM2->CCR2 = val;
		break;
	case 10:
		TIM3->CCR2 = val;
		break;
	case 11:
		TIM3->CCR1 = val;
		break;
	case 12:
		TIM8->CCR1 = val;
		break;
	case 13:
		TIM8->CCR2 = val;
		break;
	case 14:
		TIM8->CCR3 = val;
		break;
	case 15:
		TIM8->CCR4 = val;
		break;
	}
}
uint16_t TIM_GetCompare(uint8_t num)
{
	uint16_t val;
	switch (num)
	{
	case 0:
		val = TIM1->CCR3; // TIMx->CCR1
		break;
	case 1:
		val = TIM1->CCR4;
		break;
	case 2:
		val = TIM1->CCR2;
		break;
	case 3:
		val = TIM1->CCR1;
		break;
	case 4:
		val = TIM3->CCR4;
		break;
	case 5:
		val = TIM3->CCR3;
		break;
	case 6:
		val = TIM2->CCR4;
		break;
	case 7:
		val = TIM2->CCR3;
		break;
	case 8:
		val = TIM2->CCR1;
		break;
	case 9:
		val = TIM2->CCR2;
		break;
	case 10:
		val = TIM3->CCR2;
		break;
	case 11:
		val = TIM3->CCR1;
		break;
	case 12:
		val = TIM8->CCR1;
		break;
	case 13:
		val = TIM8->CCR2;
		break;
	case 14:
		val = TIM8->CCR3;
		break;
	case 15:
		val = TIM8->CCR4;
		break;
	}
	return val;
}

/*TIMx: where x can be 1 to 14 to select the TIMx peripheral.
operation :1 enable
					:0 disable
channelx: where x can be 1 to 4 to select

*/
void TIMxCHxOutControl(TIM_TypeDef *TIMx, uint8_t channelx, uint8_t operation)
{
	TIM_HandleTypeDef htimx ;
	htimx.Instance = TIMx;
	uint16_t TIM_Channel, TIM_IT;
	if (channelx == 1)
	{
		TIM_Channel = TIM_CHANNEL_1;
		TIM_IT = TIM_IT_CC1;
	}
	if (channelx == 2)
	{
		TIM_Channel = TIM_CHANNEL_2;
		TIM_IT = TIM_IT_CC2;
	}
	if (channelx == 3)
	{
		TIM_Channel = TIM_CHANNEL_3;
		TIM_IT = TIM_IT_CC3;
	}
	if (channelx == 4)
	{
		TIM_Channel = TIM_CHANNEL_4;
		TIM_IT = TIM_IT_CC4;
	}

	if (!operation)
	{ // operation = 0   open
		TIM_CCxChannelCmd(TIMx, TIM_Channel, TIM_CCx_DISABLE);
		__HAL_TIM_DISABLE_IT(&htimx, TIM_IT);
	}
	else
	{ // operation = 1  close
		TIM_CCxChannelCmd(TIMx, TIM_Channel, TIM_CCx_ENABLE);
		__HAL_TIM_ENABLE_IT(&htimx, TIM_IT);
	}
}

void TIMControl(uint8_t num, uint8_t operation)
{
	switch (num)
	{
	case 0:

		TIMxCHxOutControl(TIM1, 3, operation);
		break;
	case 1:

		TIMxCHxOutControl(TIM1, 4, operation);
		break;
	case 2:

		TIMxCHxOutControl(TIM1, 2, operation);
		break;
	case 3:

		TIMxCHxOutControl(TIM1, 1, operation);
		break;
	case 4:

		TIMxCHxOutControl(TIM3, 4, operation);
		break;
	case 5:

		TIMxCHxOutControl(TIM3, 3, operation);
		break;
	case 6:

		TIMxCHxOutControl(TIM2, 4, operation);
		break;
	case 7:

		TIMxCHxOutControl(TIM2, 3, operation);
		break;

	case 8:

		TIMxCHxOutControl(TIM2, 1, operation);
		break;

	case 9:

		TIMxCHxOutControl(TIM2, 2, operation);
		break;
	case 10:

		TIMxCHxOutControl(TIM3, 2, operation);
		break;
	case 11:

		TIMxCHxOutControl(TIM3, 1, operation);
		break;
	case 12:

		TIMxCHxOutControl(TIM8, 1, operation);
		break;
	case 13:

		TIMxCHxOutControl(TIM8, 2, operation);
		break;
	case 14:

		TIMxCHxOutControl(TIM8, 3, operation);
		break;
	case 15:
		TIMxCHxOutControl(TIM8, 4, operation);
		break;
	}
}

/**
  * 函数功能: 相对位置运动：运动给定的步数
  * 输入参数: step：移动的步数 (正数为顺时针，负数为逆时针).
			  accel  加速度,实际值为accel*0.025*rad/sec^2
			  decel  减速度,实际值为decel*0.025*rad/sec^2
			  speed  最大速度,实际值为speed*0.05*rad/sec
  * 返 回 值: 无
  * 说    明: 以给定的步数移动步进电机，先加速到最大速度，然后在合适位置开始
  *           减速至停止，使得整个运动距离为指定的步数。如果加减速阶段很短并且
  *           速度很慢，那还没达到最大速度就要开始减速
  */
void STEPMOTOR_AxisMoveRel(uint8_t num, int32_t step, uint32_t accel, uint32_t decel, uint32_t speed)
{
	__IO uint16_t tim_count;
	// 达到最大速度时的步数
	__IO uint32_t max_s_lim;
	// 必须要开始减速的步数（如果加速没有达到最大速度）
	__IO uint32_t accel_lim;

	float faccel, fdecel, fspeed;
	memcpy(&faccel, &accel, sizeof(float));
	memcpy(&fdecel, &decel, sizeof(float));
	memcpy(&fspeed, &speed, sizeof(float));

	//Moto[num].Mstate = 1;

	if (Moto[num].MotionStatus != STOP) // 只允许步进电机在停止的时候才继续
		return;
	if (step < 0) // 步数为负数
	{
		Moto[num].MotoDir = Moto_Back; // 逆时针方向旋转

		step = -step; // 获取步数绝对值
	}
	else
	{
		Moto[num].MotoDir = Moto_For; // 顺时针方向旋转
	}

	if (step == 1) // 步数为1
	{
		srd[num].accel_count = -1;	// 只移动一步
		srd[num].run_state = DECEL; // 减速状态.
		srd[num].step_delay = 1000; // 短延时
	}
	else if (step != 0) // 如果目标运动步数不为0
	{
		// ?????????????????§??????é?????????????????????????????????????¨???????¨?
	//srd[num].step_delay = round((((float)SystemCoreClock/(STEPMOTOR_TIM_PRESCALER+1)) * sqrt(A_SQ / accel))/10);
		///////////////////////////
		// 设置最大速度极限, 计算得到min_delay用于定时器的计数器的值。
		// min_delay = (alpha / tt)/ w
		// srd[num].min_delay = (int32_t)(A_T_x10 / f_speed);
		srd[num].min_delay = (int32_t)(A_T_x10 / fspeed);
		// 通过计算第一个(c0) 的步进延时来设定加速度，其中accel单位为0.1rad/sec^2
		// step_delay = 1/tt * sqrt(2*alpha/accel)
		// step_delay = ( tfreq*0.676/10 )*10 * sqrt( (2*alpha*100000) / (accel*10) )/100
		srd[num].step_delay = (int32_t)((T1_FREQ_148 * sqrt(A_SQ / faccel)) / 10);

		// 计算多少步之后达到最大速度的限制
		// max_s_lim = speed^2 / (2*alpha*accel)
		max_s_lim = (uint32_t)(fspeed * fspeed / (A_x200 * faccel / 10));
		// 如果达到最大速度小于0.5步，我们将四舍五入为0
		// 但实际我们必须移动至少一步才能达到想要的速度
		if (max_s_lim == 0)
		{
			max_s_lim = 1;
		}

		// 计算多少步之后我们必须开始减速
		// n1 = (n1+n2)decel / (accel + decel)
		accel_lim = (uint32_t)(step * fdecel / (faccel + fdecel));
		// 我们必须加速至少1步才能才能开始减速
		if (accel_lim == 0)
		{
			accel_lim = 1;
		}

		// 使用限制条件我们可以计算出减速阶段步数
		if (accel_lim <= max_s_lim)
		{
			srd[num].decel_val = accel_lim - step;
		}
		else
		{
			srd[num].decel_val = -(max_s_lim * faccel / fdecel);
		}
		// 当只剩下一步我们必须减速
		if (srd[num].decel_val == 0)
		{
			srd[num].decel_val = -1;
		}

		// 计算开始减速时的步数
		srd[num].decel_start = step + srd[num].decel_val;

		// 如果最大速度很慢，我们就不需要进行加速运动
		if (srd[num].step_delay <= srd[num].min_delay)
		{
			srd[num].step_delay = srd[num].min_delay;
			srd[num].run_state = RUN;
		}
		else
		{
			srd[num].run_state = ACCEL;
		}
		// 复位加速度计数值
		srd[num].accel_count = 0;
	}
	Moto[num].MotionStatus = 1; // 电机为运动状态
	tim_count = TIM_GetCompare(num);
	tim_count += (srd[num].step_delay / 2);
	TIM_SetCompare(num, tim_count);
	TIMControl(num, 1);
}




/*
设置电机运动方向 正转为1 ，反转为0
num：电机ID号 0~15
set：正转为1 ，反转为0
*/
void Moto_Dir_Set(uint8_t num,uint8_t set)
{
	GPIO_PinState status; 
	if(set == 0)
	{
		status = GPIO_PIN_RESET;
	}
	else
	{
		status = GPIO_PIN_SET;
	}

	switch (num)
	{
	case 0:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_15, status);
		break;
	case 1:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_0, status);
		break;
	case 2:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_1, status);
		break;
	case 3:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_2, status);
		break;
	case 4:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_3, status);
		break;
	case 5:
		HAL_GPIO_WritePin(GPIOA, GPIO_PIN_4, status);
		break;
	case 6:
		HAL_GPIO_WritePin(GPIOA, GPIO_PIN_5, status);
		break;
	case 7:
		HAL_GPIO_WritePin(GPIOA, GPIO_PIN_6, status);
		break;
	case 8:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, status);
		break;
	case 9:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, status);
		break;
	case 10:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, status);
		break;
	case 11:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, status);
		break;
	case 12:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, status);
		break;
	case 13:
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_6, status);
		break;
	case 14:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, status);
		break;
	case 15:
		HAL_GPIO_WritePin(GPIOC, GPIO_PIN_14, status);
		break;
	}
}

/*
获取电机运动方向 正转为1 ，反转为0
num：电机ID号 0~15
 正转为1 ，反转为0
*/
uint8_t Moto_Dir_Get(uint8_t num)
{
	uint8_t val;
	switch (num)
	{
	case 0:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_15);
		break;
	case 1:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_0);
		break;
	case 2:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_1);
		break;
	case 3:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_2);
		break;
	case 4:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_3);
		break;
	case 5:
		val = HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_4);
		break;
	case 6:
		val = HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_5);
		break;
	case 7:
		val = HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_6);
		break;
	case 8:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_1);
		break;
	case 9:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_2);
		break;
	case 10:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_3);
		break;
	case 11:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_4);
		break;
	case 12:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_5);
		break;
	case 13:
		val = HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_6);
		break;
	case 14:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_13);
		break;
	case 15:
		val = HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_14);
		break;
	}
	return val;
}

/**
  * ?????°??????: ??????????????????
  * ???????????°: ???
  * ??? ??? ???: ???
  * ???    ???: ??????????????¨???????????????????????????é?¤??????é??????????°???
  *             
  */
void StopMotor(uint8_t num)
{

	//srd[num].run_state = STOP;
	
//		TIMControl(num, 0);
//		srd[num].accel_count = 0;
//		srd[num].step_delay = 0;
//		srd[num].min_delay = 0;
//		//Moto[num].MotionStatus = STOP;
		srd[num].run_state = STOP;
	
//		Moto[num].Mflag = 0;
//		srd[num].lock = 0;
}
