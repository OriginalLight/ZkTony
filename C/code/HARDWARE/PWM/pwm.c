#include "pwm.h"
#include "usart.h"
#include "moto.h"
#include "SEGGER_RTT.h"
#include "cmd_process.h"
#include "timer.h"

extern SpeedRampData srd[MOTONUM];
extern Moto_Struct Moto[MOTONUM];

__IO int32_t  step_position[MOTONUM] = {0};           // ��ǰλ��


/*
TIM GOPIO init
EN GOPIO init
DIR GOPIO init
*/
void TIM_GPIO_Config()
{

	GPIO_InitTypeDef GPIO_InitStructure;

	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA | RCC_AHB1Periph_GPIOC | RCC_AHB1Periph_GPIOD | RCC_AHB1Periph_GPIOB | RCC_AHB1Periph_GPIOE, ENABLE); // ʹ��PORTFʱ��
	// TIM1

	GPIO_PinAFConfig(GPIOE, GPIO_PinSource14, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource13, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource11, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource9, GPIO_AF_TIM1);	 //

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14 | GPIO_Pin_13 | GPIO_Pin_11 | GPIO_Pin_9; // GPIOF9
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;										// ���ù���
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;									// �ٶ�100MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;										// ���츴�����?
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;										// ����
	GPIO_Init(GPIOE, &GPIO_InitStructure);

	// TIM3
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource0, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource1, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource4, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource5, GPIO_AF_TIM3);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_4 | GPIO_Pin_5; // GPIOF9
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_Init(GPIOB, &GPIO_InitStructure);

	// TIM2
	GPIO_PinAFConfig(GPIOA, GPIO_PinSource0, GPIO_AF_TIM2);
	GPIO_PinAFConfig(GPIOA, GPIO_PinSource1, GPIO_AF_TIM2);
	GPIO_PinAFConfig(GPIOA, GPIO_PinSource2, GPIO_AF_TIM2);
	GPIO_PinAFConfig(GPIOA, GPIO_PinSource3, GPIO_AF_TIM2);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_3;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;	   //
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; //
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;	   //
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;	   //
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	// TIM8
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource6, GPIO_AF_TIM8);
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource7, GPIO_AF_TIM8);
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource8, GPIO_AF_TIM8);
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource9, GPIO_AF_TIM8);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6 | GPIO_Pin_7 | GPIO_Pin_8 | GPIO_Pin_9;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;	   //
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; //
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;	   //
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;	   //
	GPIO_Init(GPIOC, &GPIO_InitStructure);

	// EN GPIO
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6 | GPIO_Pin_7 | GPIO_Pin_8 | GPIO_Pin_9; // GPIOF9  //DIR AND ENABLE
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;									 // ���ģ�?
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;								 // �ٶ�100MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;									 // ���츴�����?
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;									 //
	GPIO_Init(GPIOB, &GPIO_InitStructure);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0;		   // GPIOF9  //DIR AND ENABLE
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;	   //
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; //
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;	   //
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;	   //
	GPIO_Init(GPIOE, &GPIO_InitStructure);

	// DIR GPIO
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_3 | GPIO_Pin_13 | GPIO_Pin_14 | GPIO_Pin_15; // GPIO  //DIR AND ENABLE
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;																			   //
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;																		   //
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;																			   //
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;																			   //
	GPIO_Init(GPIOC, &GPIO_InitStructure);																					   //

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5 | GPIO_Pin_6; // DIR AND ENABLE
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;						//
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;					//
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;						//
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;						//
	GPIO_Init(GPIOA, &GPIO_InitStructure);								//

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_3 | GPIO_Pin_4 | GPIO_Pin_5 | GPIO_Pin_6; // GPIOF9  //DIR AND ENABLE
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;															   //
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;														   //
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;															   //
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;															   //
	GPIO_Init(GPIOE, &GPIO_InitStructure);																	   //
}
//arr ֵ�ѱ��̶�д����������arrֵ��Ч
void TIM1_PWM_Init(u32 arr, u32 psc)
{

	// �˲������ֶ��޸�IO������
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM1, ENABLE); // TIM1ʱ��ʹ��

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					// ��ʱ����Ƶ
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; // ���ϼ���ģʽ

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; // �Զ���װ��
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM1, &TIM_TimeBaseStructure); // ��ʼ����ʱ��

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;

	// ��ʼ��TIM1 Channel 1 2 3 4
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // ѡ��ʱ��ģʽ:toggleģʽ
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; // �Ƚ����ʹ��?
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  // �������?:TIM����Ƚϼ��Ե�?
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM1, &TIM_OCInitStructure); // ����Tָ���Ĳ�����ʼ������TIM3 4OC1
	TIM_OC2Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC3Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC4Init(TIM1, &TIM_OCInitStructure); //

	TIM_OC1PreloadConfig(TIM1, TIM_OCPreload_Disable); // ʧ��TIM1��CCR1�ϵ�Ԥװ�ؼĴ���
	TIM_OC2PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC3PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM1, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM1, DISABLE); // ARPEʧ��

	TIM_CCxCmd(TIM1, TIM_Channel_1, TIM_CCx_Disable); // TIMͨ������Ƚ�ʧ��?
	TIM_CCxCmd(TIM1, TIM_Channel_2, TIM_CCx_Disable);
	TIM_CCxCmd(TIM1, TIM_Channel_3, TIM_CCx_Disable);
	TIM_CCxCmd(TIM1, TIM_Channel_4, TIM_CCx_Disable);

	TIM_ITConfig(TIM1, TIM_IT_CC1, DISABLE); //
	TIM_ITConfig(TIM1, TIM_IT_CC2, DISABLE); //
	TIM_ITConfig(TIM1, TIM_IT_CC3, DISABLE); //
	TIM_ITConfig(TIM1, TIM_IT_CC4, DISABLE); //

	NVIC_InitStructure.NVIC_IRQChannel = TIM1_CC_IRQn;			 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x00;		 //
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_CtrlPWMOutputs(TIM1, ENABLE);
	TIM_Cmd(TIM1, ENABLE); // ʹ��TIM1
}
void TIM3_PWM_Init(u32 arr, u32 psc)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					//
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;
	//  Channel 1/2/3/4 toggleģʽ

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  //
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; //
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  //
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM3, &TIM_OCInitStructure); //  OC1
	TIM_OC2Init(TIM3, &TIM_OCInitStructure);
	TIM_OC3Init(TIM3, &TIM_OCInitStructure);
	TIM_OC4Init(TIM3, &TIM_OCInitStructure);

	TIM_OC1PreloadConfig(TIM3, TIM_OCPreload_Disable); //
	TIM_OC2PreloadConfig(TIM3, TIM_OCPreload_Disable);
	TIM_OC3PreloadConfig(TIM3, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM3, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM3, DISABLE); //

	TIM_CCxCmd(TIM3, TIM_Channel_1, TIM_CCx_Disable);
	TIM_CCxCmd(TIM3, TIM_Channel_2, TIM_CCx_Disable);
	TIM_CCxCmd(TIM3, TIM_Channel_3, TIM_CCx_Disable);
	TIM_CCxCmd(TIM3, TIM_Channel_4, TIM_CCx_Disable);

	TIM_ITConfig(TIM3, TIM_IT_CC1, DISABLE); //
	TIM_ITConfig(TIM3, TIM_IT_CC2, DISABLE); //
	TIM_ITConfig(TIM3, TIM_IT_CC3, DISABLE); //
	TIM_ITConfig(TIM3, TIM_IT_CC4, DISABLE); //

	NVIC_InitStructure.NVIC_IRQChannel = TIM3_IRQn;				 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;		 //
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_Cmd(TIM3, ENABLE);
}
void TIM2_PWM_Init(u32 arr, u32 psc)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					//
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;
	//

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // :toggleģʽ
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; //
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  //
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM2, &TIM_OCInitStructure); //
	TIM_OC2Init(TIM2, &TIM_OCInitStructure);
	TIM_OC3Init(TIM2, &TIM_OCInitStructure);
	TIM_OC4Init(TIM2, &TIM_OCInitStructure);

	TIM_OC1PreloadConfig(TIM2, TIM_OCPreload_Disable); //
	TIM_OC2PreloadConfig(TIM2, TIM_OCPreload_Disable); //
	TIM_OC3PreloadConfig(TIM2, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM2, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM2, DISABLE); //

	TIM_ITConfig(TIM2, TIM_IT_CC1, DISABLE); //
	TIM_ITConfig(TIM2, TIM_IT_CC2, DISABLE); //
	TIM_ITConfig(TIM2, TIM_IT_CC3, DISABLE); //
	TIM_ITConfig(TIM2, TIM_IT_CC4, DISABLE); //

	TIM_CCxCmd(TIM2, TIM_Channel_1, TIM_CCx_Disable);
	TIM_CCxCmd(TIM2, TIM_Channel_2, TIM_CCx_Disable);
	TIM_CCxCmd(TIM2, TIM_Channel_3, TIM_CCx_Disable);
	TIM_CCxCmd(TIM2, TIM_Channel_4, TIM_CCx_Disable);

	NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn;				 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;		 //
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_CtrlPWMOutputs(TIM2, ENABLE);
	TIM_Cmd(TIM2, ENABLE);
}

void TIM8_PWM_Init(u32 arr, u32 psc)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM8, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					//
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM8, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;
	//  Channel

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  //
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; //
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  //
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM8, &TIM_OCInitStructure); //
	TIM_OC2Init(TIM8, &TIM_OCInitStructure);
	TIM_OC3Init(TIM8, &TIM_OCInitStructure);
	TIM_OC4Init(TIM8, &TIM_OCInitStructure);

	TIM_OC1PreloadConfig(TIM8, TIM_OCPreload_Disable); //
	TIM_OC2PreloadConfig(TIM8, TIM_OCPreload_Disable);
	TIM_OC3PreloadConfig(TIM8, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM8, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM8, DISABLE); //

	TIM_ITConfig(TIM8, TIM_IT_CC1, DISABLE); //
	TIM_ITConfig(TIM8, TIM_IT_CC2, DISABLE); //
	TIM_ITConfig(TIM8, TIM_IT_CC3, DISABLE); //
	TIM_ITConfig(TIM8, TIM_IT_CC4, DISABLE); //

	TIM_CCxCmd(TIM8, TIM_Channel_1, TIM_CCx_Disable);
	TIM_CCxCmd(TIM8, TIM_Channel_2, TIM_CCx_Disable);
	TIM_CCxCmd(TIM8, TIM_Channel_3, TIM_CCx_Disable);
	TIM_CCxCmd(TIM8, TIM_Channel_4, TIM_CCx_Disable);

	NVIC_InitStructure.NVIC_IRQChannel = TIM8_CC_IRQn;			 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x00;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_CtrlPWMOutputs(TIM8, ENABLE);
	TIM_Cmd(TIM8, ENABLE);
}

void TIM1_CC_IRQHandler(void)
{
	if (TIM_GetITStatus(TIM1, TIM_IT_CC1) != RESET)
	{
		/* Clear TIM1 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM1, TIM_IT_CC1);

		if (Moto[3].MotoDir == 0)
		{
			M4_DIR = 0;
		}

		if (Moto[3].Mstate == 1)
		{
			TIM_Callback(3);
		}
		
	}
	if (TIM_GetITStatus(TIM1, TIM_IT_CC2) != RESET)
	{
		/* Clear TIM1 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM1, TIM_IT_CC2);

		if (Moto[2].MotoDir == 0)
		{
			M3_DIR = 0;
		}

		if (Moto[2].Mstate == 1)
		{
			TIM_Callback(2);
		}
		
	}
	if (TIM_GetITStatus(TIM1, TIM_IT_CC3) != RESET)
	{
		/* Clear TIM1 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM1, TIM_IT_CC3);

		if (Moto[0].MotoDir == 0)
		{
			M1_DIR = Moto_Back;
		}

		if (Moto[0].Mstate == 1)
		{
			TIM_Callback(0);
		}
		
	}

	if (TIM_GetITStatus(TIM1, TIM_IT_CC4) != RESET)
	{
		/* Clear TIM1 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM1, TIM_IT_CC4);

		if (Moto[1].MotoDir == 0)
		{
			M2_DIR = Moto_Back;
		}
		if (Moto[1].Mstate == 1)
		{
			TIM_Callback(1);
		}
		
	}
}

void TIM8_CC_IRQHandler(void)
{

	if (TIM_GetITStatus(TIM8, TIM_IT_CC1) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM8, TIM_IT_CC1);

		if (Moto[12].MotoDir == 0)
		{
			M13_DIR = 0;
		}
		if (Moto[12].Mstate == 1)
		{

			TIM_Callback(12);
		}
		
	}
	if (TIM_GetITStatus(TIM8, TIM_IT_CC2) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM8, TIM_IT_CC2);

		if (Moto[13].MotoDir == 0)
		{
			M14_DIR = 0;
		}
		if (Moto[13].Mstate == 1)
		{
			TIM_Callback(13);	
		}
	}

	if (TIM_GetITStatus(TIM8, TIM_IT_CC3) != RESET)
	{

		TIM_ClearITPendingBit(TIM8, TIM_IT_CC3);

		if (Moto[14].MotoDir == 0)
		{
			M15_DIR = 0;
		}

		if (Moto[14].Mstate == 1)
		{
			TIM_Callback(14);
		}
		
	}
	if (TIM_GetITStatus(TIM8, TIM_IT_CC4) != RESET)
	{

		TIM_ClearITPendingBit(TIM8, TIM_IT_CC4);

		if (Moto[15].MotoDir == 0)
		{
			M16_DIR = 0;
		}

		if (Moto[15].Mstate == 1)
		{
			TIM_Callback(15);
		}
		
	}
}
void TIM3_IRQHandler(void)
{

	if (TIM_GetITStatus(TIM3, TIM_IT_CC1) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM3, TIM_IT_CC1);

		if (Moto[11].MotoDir == 0)
		{
			M12_DIR = 0;
		}

		if (Moto[11].Mstate == 1)
		{
			TIM_Callback(11);
		}
		
	}
	if (TIM_GetITStatus(TIM3, TIM_IT_CC2) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM3, TIM_IT_CC2);

		if (Moto[10].MotoDir == 0)
		{
			M11_DIR = 0;
		}
		if (Moto[10].Mstate == 1)
		{

			TIM_Callback(10);
		}
		
	}
	if (TIM_GetITStatus(TIM3, TIM_IT_CC3) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM3, TIM_IT_CC3);

		if (Moto[5].MotoDir == 0)
		{
			M6_DIR = 0;
		}
		if (Moto[5].Mstate == 1)
		{

			TIM_Callback(5);
		}
		
	}
	if (TIM_GetITStatus(TIM3, TIM_IT_CC4) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM3, TIM_IT_CC4);

		if (Moto[4].MotoDir == 0)
		{
			M5_DIR = 0;
		}
		if (Moto[4].Mstate == 1)
		{
			TIM_Callback(4);
		}
		
	}
}

void TIM2_IRQHandler(void)
{

	if (TIM_GetITStatus(TIM2, TIM_IT_CC1) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM2, TIM_IT_CC1);

		if (Moto[8].MotoDir == 0)
		{
			M9_DIR = 0;
		}

		if (Moto[8].Mstate == 1)
		{

			TIM_Callback(8);
		}

	}

	if (TIM_GetITStatus(TIM2, TIM_IT_CC2) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM2, TIM_IT_CC2);

		if (Moto[9].MotoDir == 0)
		{
			M10_DIR = 0;
		}
		if (Moto[9].Mstate == 1)
		{
			TIM_Callback(9);
		}

	}

	if (TIM_GetITStatus(TIM2, TIM_IT_CC4) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM2, TIM_IT_CC4);

		if (Moto[6].MotoDir == 0)
		{
			M7_DIR = 0;
		}
		if (Moto[6].Mstate == 1)
		{
				TIM_Callback(6);
		}

	}
	if (TIM_GetITStatus(TIM2, TIM_IT_CC3) != RESET)
	{
		/* Clear TIM3 Capture Compare1 interrupt pending bit*/
		TIM_ClearITPendingBit(TIM2, TIM_IT_CC3);

		if (Moto[7].MotoDir == 0)
		{
			M8_DIR = 0;
		}
		if (Moto[7].Mstate == 1)
		{
			 TIM_Callback(7);
		}
		
	}
}

/*
function: TIM control channel open or close

TIMx: where x can be 1 to 14 to select the TIMx peripheral.
operation :1 enable
					:0 disable
channelx: where x can be 1 to 4 to select

*/
void TIMxCHxOutControl(TIM_TypeDef *TIMx, uint8_t channelx, uint8_t operation)
{
	uint16_t TIM_Channel, TIM_IT;
	if (channelx == 1)
	{
		TIM_Channel = TIM_Channel_1;
		TIM_IT = TIM_IT_CC1;
	}
	if (channelx == 2)
	{
		TIM_Channel = TIM_Channel_2;
		TIM_IT = TIM_IT_CC2;
	}
	if (channelx == 3)
	{
		TIM_Channel = TIM_Channel_3;
		TIM_IT = TIM_IT_CC3;
	}
	if (channelx == 4)
	{
		TIM_Channel = TIM_Channel_4;
		TIM_IT = TIM_IT_CC4;
	}

	if (!operation)
	{ // operation = 0   open
		TIM_CCxCmd(TIMx, TIM_Channel, TIM_CCx_Disable);
		TIM_ITConfig(TIMx, TIM_IT, DISABLE);
	}
	else
	{ // operation = 1  close
		TIM_CCxCmd(TIMx, TIM_Channel, TIM_CCx_Enable);
		TIM_ITConfig(TIMx, TIM_IT, ENABLE);
	}
}

/*
function: TIMͨ������ control channel open or close

TIMx: where x can be 1 to 14 to select the TIMx peripheral.
operation :1 enable
			:0 disable

*/
void TIMxCH1OutControl(TIM_TypeDef *TIMx, uint8_t operation)
{
	if (!operation)
	{ // operation = 0   open
		TIM_CCxCmd(TIMx, TIM_Channel_1, TIM_CCx_Disable);
		TIM_ITConfig(TIMx, TIM_IT_CC1, DISABLE);
	}
	else
	{ // operation = 1  close
		TIM_CCxCmd(TIMx, TIM_Channel_1, TIM_CCx_Enable);
		TIM_ITConfig(TIMx, TIM_IT_CC1, ENABLE);
	}
}
void TIMxCH2OutControl(TIM_TypeDef *TIMx, uint8_t operation)
{
	if (!operation)
	{ // operation = 0   open
		TIM_CCxCmd(TIMx, TIM_Channel_2, TIM_CCx_Disable);
		TIM_ITConfig(TIMx, TIM_IT_CC2, DISABLE);
	}
	else
	{ // operation = 1  close
		TIM_CCxCmd(TIMx, TIM_Channel_2, TIM_CCx_Enable);
		TIM_ITConfig(TIMx, TIM_IT_CC2, ENABLE);
	}
}
void TIMxCH3OutControl(TIM_TypeDef *TIMx, uint8_t operation)
{
	if (!operation)
	{ // operation = 0   open
		TIM_CCxCmd(TIMx, TIM_Channel_3, TIM_CCx_Disable);
		TIM_ITConfig(TIMx, TIM_IT_CC3, DISABLE);
	}
	else
	{ // operation = 1  close
		TIM_CCxCmd(TIMx, TIM_Channel_3, TIM_CCx_Enable);
		TIM_ITConfig(TIMx, TIM_IT_CC3, ENABLE);
	}
}
void TIMxCH4OutControl(TIM_TypeDef *TIMx, uint8_t operation)
{
	if (!operation)
	{ // operation = 0   open
		TIM_CCxCmd(TIMx, TIM_Channel_4, TIM_CCx_Disable);
		TIM_ITConfig(TIMx, TIM_IT_CC4, DISABLE);
	}
	else
	{ // operation = 1  close
		TIM_CCxCmd(TIMx, TIM_Channel_4, TIM_CCx_Enable);
		TIM_ITConfig(TIMx, TIM_IT_CC4, ENABLE);
	}
}


//************************************************************************
/**
  * ��������: ��ʱ���жϻص�����
  * �������: ��
  * �� �� ֵ: ��
  * ˵    ��: ʵ�ּӼ��ٹ���

Ӳʯ�汾�㷨
  */

void TIM_Callback(uint8_t num)
{
	__IO static uint16_t tim_count = 0;
  __IO  uint32_t new_step_delay = 0;
  __IO static uint8_t  i[MOTONUM]={0};
	__IO static uint16_t last_accel_delay[MOTONUM] = {0};
	// ���ƶ�����������
	__IO static uint32_t step_count[MOTONUM] = {0};
	// ��¼new_step_delay�е������������һ������ľ���
	__IO static int32_t rest[MOTONUM] = {0};
	//��ʱ��ʹ�÷�תģʽ����Ҫ���������жϲ����һ����������
  

    
    // ���ñȽ�ֵ

		tim_count = TIM_GetCompare(num);
		tim_count += (srd[num].step_delay/2);
		TIM_SetCompare(num,tim_count);

    i[num]++;     // ��ʱ���жϴ�������ֵ
    if(i[num] == 2) // 2�Σ�˵���Ѿ����һ����������
    {
      i[num] = 0;   // ���㶨ʱ���жϴ�������ֵ
      switch(srd[num].run_state) // �Ӽ������߽׶�
      {
        case STOP:

		Moto[num].MotionStatus = STOP;  		//  ���Ϊֹͣ״̬
        step_count[num] = 0;  // ���㲽��������
        rest[num] = 0;        // ������ֵ
        last_accel_delay[num] = 0;
        srd[num].accel_count = 0;
        srd[num].step_delay = 0;
        srd[num].min_delay = 0;
          // �ر�ͨ��	
			TIMControl(num,0);	

				//////////////////////////////////////////////////
			if (srd[num].lock == 1)
			{
				Moto[num].Mflag = 0;
				srd[num].lock = 0;
			}
			///////////////////////////////////////////////////				
          break;

        case ACCEL:
				
//////////////////////////////////////////////////
		if (srd[num].lock == 0)
		{
			Moto[num].Mflag = 1;
			srd[num].lock = 1;
		}
		///////////////////////////////////////////////				
				
          step_count[num]++;      // ������1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // ����λ�ü�1
          }
          else
          {
            step_position[num]--; // ����λ�ü�1
          }
          srd[num].accel_count++; // ���ټ���ֵ��1
          
					new_step_delay = srd[num].step_delay - (((2 *srd[num].step_delay) + rest[num])/(4 * srd[num].accel_count + 1));//������(��)һ����������(ʱ����)
          rest[num] = ((2 * srd[num].step_delay)+rest[num])%(4 * srd[num].accel_count + 1);// �����������´μ��㲹���������������
          
					if(step_count[num] >= srd[num].decel_start)// ����ǹ�Ӧ�ÿ�ʼ����
          {
            srd[num].accel_count = srd[num].decel_val; // ���ټ���ֵΪ���ٽ׶μ���ֵ�ĳ�ʼֵ
            srd[num].run_state = DECEL;           // �¸����������ٽ׶�
          }
          else if(new_step_delay <= srd[num].min_delay) // ����Ƿ񵽴�����������ٶ�
          {
						srd[num].accel_count = srd[num].decel_val; 	// ���ټ���ֵΪ���ٽ׶μ���ֵ�ĳ�ʼֵ
          last_accel_delay[num] = new_step_delay; 	// ������ٹ��������һ����ʱ���������ڣ�
          new_step_delay = srd[num].min_delay;    	// ʹ��min_delay����Ӧ����ٶ�speed��
          rest[num] = 0;                          	// ������ֵ
          srd[num].run_state = RUN;               	// ����Ϊ��������״̬
          }
					last_accel_delay[num] = new_step_delay; 	  // ������ٹ��������һ����ʱ���������ڣ�
          break;

        case RUN:
          step_count[num]++;  // ������1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // ����λ�ü�1
          }
          else
          {
            step_position[num]--; // ����λ�ü�1
          }
          new_step_delay = srd[num].min_delay;     // ʹ��min_delay����Ӧ����ٶ�speed��
          if(step_count[num] >= srd[num].decel_start)   // ��Ҫ��ʼ����
          {
            srd[num].accel_count = srd[num].decel_val;  // ���ٲ�����Ϊ���ټ���ֵ
            new_step_delay = last_accel_delay[num];// �ӽ׶�������ʱ��Ϊ���ٽ׶ε���ʼ��ʱ(��������)
            srd[num].run_state = DECEL;            // ״̬�ı�Ϊ����
          }
          break;

        case DECEL:
          step_count[num]++;  // ������1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // ����λ�ü�1
          }
          else
          {
            step_position[num]--; // ����λ�ü�1
          }
          srd[num].accel_count++;
          new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num])/(4 * srd[num].accel_count + 1)); //������(��)һ����������(ʱ����)
          rest[num] = ((2 * srd[num].step_delay)+rest[num])%(4 * srd[num].accel_count + 1);// �����������´μ��㲹���������������
          
          //����Ƿ�Ϊ���һ��
          if(srd[num].accel_count >= 0)
          {
            srd[num].run_state = STOP;
          }
          break;
      }     
			if( (new_step_delay>>1) >0xFFFF)
			{
				new_step_delay = 0x1FFFF;
			}
      srd[num].step_delay = new_step_delay; // Ϊ�¸�(�µ�)��ʱ(��������)��ֵ
    
  }
	
}

