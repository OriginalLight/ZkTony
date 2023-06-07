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

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14 | GPIO_Pin_13 | GPIO_Pin_11 | GPIO_Pin_9; // GPIO
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;										// ���ù���
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;									// �ٶ�100MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;										// ���츴�����??
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;										// ����
	GPIO_Init(GPIOE, &GPIO_InitStructure);

	// TIM3
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource0, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource1, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource4, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource5, GPIO_AF_TIM3);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_4 | GPIO_Pin_5; // GPIO
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
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;									 // ���츴�����??
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;									 //
	GPIO_Init(GPIOB, &GPIO_InitStructure);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0;		   // GPIO  //DIR AND ENABLE
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
/*
// 高级定时器 时钟频率为168MHz 通用定时器 时钟频率为84MHz  
为了使输出频率一致 高级定时器10分频，普通定时器5分频
*/ 

void TIM1_PWM_Init(void)
{
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM1, ENABLE); // TIM时钟使能

	TIM_TimeBaseStructure.TIM_Prescaler = 10-1;	// 定时器分频
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; // 向上计数模式

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; // 自动重装载 ARR值
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM1, &TIM_TimeBaseStructure); // 初始化定时器

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;

	// init TIM1 Channel 1 2 3 4
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // toggle mode
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; // 比较输出使能
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  // 输出极性:TIM输出比较极性低
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM1, &TIM_OCInitStructure); // 根据指定的参数初始化外设TIM OC1
	TIM_OC2Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC3Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC4Init(TIM1, &TIM_OCInitStructure); //

	TIM_OC1PreloadConfig(TIM1, TIM_OCPreload_Disable); // 失能TIM1在CCR1上的预装载寄存器
	TIM_OC2PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC3PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM1, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM1, DISABLE); // ARPE disable
	

	TIM_CCxCmd(TIM1, TIM_Channel_1, TIM_CCx_Disable); //TIM通道捕获比较失能
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

	TIM_CtrlPWMOutputs(TIM1, ENABLE);//高级定时器必须设置
	TIM_Cmd(TIM1, ENABLE); // enable TIM1
}
void TIM3_PWM_Init(void)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = 5-1;					//
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;
	//  Channel 1/2/3/4 toggle

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
void TIM2_PWM_Init(void)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = 5-1;					
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = STEPMOTOR_TIM_PERIOD; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = STEPMOTOR_TIM_PERIOD;
	//

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // toggle mode
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

	
	TIM_Cmd(TIM2, ENABLE);
}

void TIM8_PWM_Init(void)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM8, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = 10-1;					//
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

		if (Moto[3].MotoDir == Moto_Back)
		{
			M4_DIR = Moto_Back;
		}
		else
		{
			M4_DIR = Moto_For;
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

		if (Moto[2].MotoDir == Moto_Back)
		{
			M3_DIR = Moto_Back;
		}
		else
		{
			M3_DIR = Moto_For;
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

		if (Moto[0].MotoDir == Moto_Back)
		{
			M1_DIR = Moto_Back;
		}
		else
		{
			M1_DIR = Moto_For;
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

		if (Moto[1].MotoDir == Moto_Back)
		{
			M2_DIR = Moto_Back;
		}
		else
		{
			M2_DIR = Moto_For;
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

		if (Moto[12].MotoDir == Moto_Back)
		{
			M13_DIR = Moto_Back;
		}
		else
		{
			M13_DIR = Moto_For;
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

		if (Moto[13].MotoDir == Moto_Back)
		{
			M14_DIR = Moto_Back;
		}
		else
		{
			M14_DIR = Moto_For;
		}
		if (Moto[13].Mstate == 1)
		{
			TIM_Callback(13);	
		}
	}

	if (TIM_GetITStatus(TIM8, TIM_IT_CC3) != RESET)
	{

		TIM_ClearITPendingBit(TIM8, TIM_IT_CC3);

		if (Moto[14].MotoDir == Moto_Back)
		{
			M15_DIR = Moto_Back;
		}
		else
		{
			M15_DIR = Moto_For;
		}

		if (Moto[14].Mstate == 1)
		{
			TIM_Callback(14);
		}
		
	}
	if (TIM_GetITStatus(TIM8, TIM_IT_CC4) != RESET)
	{

		TIM_ClearITPendingBit(TIM8, TIM_IT_CC4);

		if (Moto[15].MotoDir == Moto_Back)
		{
			M16_DIR = Moto_Back;
		}
		else
		{
			M16_DIR = Moto_For;
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

		if (Moto[11].MotoDir == Moto_Back)
		{
			M12_DIR = Moto_Back;
		}
		else
		{
			M12_DIR = Moto_For;
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

		if (Moto[10].MotoDir == Moto_Back)
		{
			M11_DIR = Moto_Back;
		}
		else
		{
			M11_DIR = Moto_For;
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

		if (Moto[5].MotoDir == Moto_Back)
		{
			M6_DIR = Moto_Back;
		}
		else
		{
			M6_DIR = Moto_For;
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

		if (Moto[4].MotoDir == Moto_Back)
		{
			M5_DIR = Moto_Back;
		}
		else
		{
			M5_DIR = Moto_For;
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

		if (Moto[8].MotoDir == Moto_Back)
		{
			M9_DIR = Moto_Back;
		}
		else
		{
			M9_DIR = Moto_For;
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

		if (Moto[9].MotoDir == Moto_Back)
		{
			M10_DIR = Moto_Back;
		}
		else
		{
			M10_DIR = Moto_For;
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

		if (Moto[6].MotoDir == Moto_Back)
		{
			M7_DIR = Moto_Back;
		}
		else
		{
			M7_DIR = Moto_For;
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

		if (Moto[7].MotoDir == Moto_Back)
		{
			M8_DIR = Moto_Back;
		}
		else
		{
			M8_DIR = Moto_For;
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
function: TIM  control channel open or close

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


/**
  * 函数功能: 定时器中断回调函数
  * 输入参数: 无
  * 返 回 值: 无
  * 说    明: 实现加减速过程
  */

void TIM_Callback(uint8_t num)
{
	__IO static uint16_t tim_count = 0;
  __IO  uint32_t new_step_delay = 0;
  __IO static uint8_t  i[MOTONUM]={0};
	__IO static uint16_t last_accel_delay[MOTONUM] = {0};
	// 总移动步数计数器
	__IO static uint32_t step_count[MOTONUM] = {0};
	// 记录new_step_delay中的余数，提高下一步计算的精度
	__IO static int32_t rest[MOTONUM] = {0};
	//定时器使用翻转模式，需要进入两次中断才输出一个完整脉冲
  

    
    // 设置比较值

		tim_count = TIM_GetCompare(num);
		tim_count += (srd[num].step_delay/2);
		TIM_SetCompare(num,tim_count);

    i[num]++;     // 定时器中断次数计数值
    if(i[num] == 2) // 2次，说明已经输出一个完整脉冲
    {
      i[num] = 0;   // 清零定时器中断次数计数值
      switch(srd[num].run_state) // 加减速曲线阶段
      {
        case STOP:
	
		Moto[num].MotionStatus = STOP;  		//  电机为停止状态
        step_count[num] = 0;  // 清零步数计数器
        rest[num] = 0;        // 清零余值
        last_accel_delay[num] = 0;
        srd[num].accel_count = 0;
        srd[num].step_delay = 0;
        srd[num].min_delay = 0;
          // 关闭通道
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
				
          step_count[num]++;      // 步数加1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // 绝对位置加1
          }
          else
          {
            step_position[num]--; // 绝对位置减1
          }
          srd[num].accel_count++; // 加速计数值加1
          
			new_step_delay = srd[num].step_delay - (((2 *srd[num].step_delay) + rest[num])/(4 * srd[num].accel_count + 1));//计算新(下)一步脉冲周期(时间间隔)
        	rest[num] = ((2 * srd[num].step_delay)+rest[num])%(4 * srd[num].accel_count + 1);// 计算余数，下次计算补上余数，减少误差
          
			if(step_count[num] >= srd[num].decel_start)// 检查是否应该开始减速
          {
            srd[num].accel_count = srd[num].decel_val; // 加速计数值为减速阶段计数值的初始值
            srd[num].run_state = DECEL;           // 下个脉冲进入减速阶段
          }
          else if(new_step_delay <= srd[num].min_delay) // 检查是否到达期望的最大速度
          {
			srd[num].accel_count = srd[num].decel_val; // 加速计数值为减速阶段计数值的初始值
			last_accel_delay[num] = new_step_delay;	   // 保存加速过程中最后一次延时（脉冲周期）
			new_step_delay = srd[num].min_delay;	   // 使用min_delay（对应最大速度speed）
			rest[num] = 0;							   // 清零余值
			srd[num].run_state = RUN;				   //设置为匀速运行状态
		  }
		last_accel_delay[num] = new_step_delay; 	  // 保存加速过程中最后一次延时（脉冲周期）
          break;

        case RUN:
          step_count[num]++;  // 步数加1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // 绝对位置加1
          }
          else
          {
            step_position[num]--; //绝对位置减1
          }
          new_step_delay = srd[num].min_delay;     // 使用min_delay（对应最大速度
          if(step_count[num] >= srd[num].decel_start)   // 需要开始减速
          {
            srd[num].accel_count = srd[num].decel_val;  //减速步数做为加速计数值
            new_step_delay = last_accel_delay[num];// 加阶段最后的延时做为减速阶段的起始延时(脉冲周期)
            srd[num].run_state = DECEL;            // 状态改变为减速
          }
          break;

        case DECEL:
          step_count[num]++;  // 步数加1
          if(srd[num].dir==Moto_For)
          {	  	
            step_position[num]++; // 绝对位置加1
          }
          else
          {
            step_position[num]--; // 绝对位置减1
          }
          srd[num].accel_count++;
          new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num])/(4 * srd[num].accel_count + 1)); ////计算新(下)一步脉冲周期(时间间隔)
          rest[num] = ((2 * srd[num].step_delay)+rest[num])%(4 * srd[num].accel_count + 1);// 计算余数，下次计算补上余数，减少误差
          
          //检查是否为最后一步
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
      srd[num].step_delay = new_step_delay; // 为下个(新的)延时(脉冲周期)赋值
    
  }
	
}

