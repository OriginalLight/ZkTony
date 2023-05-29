#include "pwm.h"
#include "usart.h"
#include "moto.h"
#include "SEGGER_RTT.h"
#include "cmd_process.h"
#include "timer.h"

extern speedRampData srd[MOTONUM];
extern Moto_Struct Moto[MOTONUM];
extern uint8 laststate[];
extern uint8 state[];

u32 PWMX_Num = 0; // 3200  4mm;
u32 PWMY1_Num = 0;
u32 PWMY2_Num = 0;				// 1600  40.6MM;   40 1mm;
__IO int32_t step_position = 0; // 当前位置

uint16 err = CMD_RT_OK;
uint8 ID;
/*
TIM GOPIO初始化
EN GOPIO初始化
DIR GOPIO初始化
*/
void TIM_GPIO_Config()
{

	GPIO_InitTypeDef GPIO_InitStructure;

	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA | RCC_AHB1Periph_GPIOC | RCC_AHB1Periph_GPIOD | RCC_AHB1Periph_GPIOB | RCC_AHB1Periph_GPIOE, ENABLE); // 使锟斤拷PORTF时锟斤拷
	// TIM1

	GPIO_PinAFConfig(GPIOE, GPIO_PinSource14, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource13, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource11, GPIO_AF_TIM1); //
	GPIO_PinAFConfig(GPIOE, GPIO_PinSource9, GPIO_AF_TIM1);	 //

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14 | GPIO_Pin_13 | GPIO_Pin_11 | GPIO_Pin_9; // GPIOF9
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;										// 复用功能
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;									// 速度100MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;										// 推挽复用输出
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;										// 上拉
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
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;									 // 输出模式
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;								 // 速度100MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;									 // 推挽复用输出
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

void TIM1_PWM_Init(u32 arr, u32 psc)
{

	// 此部分需手动修改IO口设置
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM1, ENABLE); // TIM1时钟使能

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					// 定时器分频
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; // 向上计数模式

	TIM_TimeBaseStructure.TIM_Period = arr; // 自动重装载
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM1, &TIM_TimeBaseStructure); // 初始化定时器

	TIM_OCInitStructure.TIM_Pulse = arr / 3;

	// 初始化TIM1 Channel 1 2 3 4
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // 选择定时器模式:toggle模式
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; // 比较输出使能
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  // 输出极性:TIM输出比较极性低
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;

	TIM_OC1Init(TIM1, &TIM_OCInitStructure); // 根据T指定的参数初始化外设TIM3 4OC1
	TIM_OC2Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC3Init(TIM1, &TIM_OCInitStructure); //
	TIM_OC4Init(TIM1, &TIM_OCInitStructure); //

	TIM_OC1PreloadConfig(TIM1, TIM_OCPreload_Disable); // 失能TIM1在CCR1上的预装载寄存器
	TIM_OC2PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC3PreloadConfig(TIM1, TIM_OCPreload_Disable);
	TIM_OC4PreloadConfig(TIM1, TIM_OCPreload_Disable);

	TIM_ARRPreloadConfig(TIM1, DISABLE); // ARPE失能

	TIM_CCxCmd(TIM1, TIM_Channel_1, TIM_CCx_Disable); // TIM通道捕获比较失能
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
	TIM_Cmd(TIM1, ENABLE); // 使能TIM1
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

	TIM_TimeBaseStructure.TIM_Period = arr; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = arr / 3;
	//  Channel 1/2/3/4 toggle模式

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

	TIM_TimeBaseStructure.TIM_Period = arr; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = arr / 3;
	//

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // :toggle模式
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

void TIM9_PWM_Init(u32 arr, u32 psc)
{
	//
	NVIC_InitTypeDef NVIC_InitStructure;

	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_TIM9, ENABLE); //

	TIM_TimeBaseStructure.TIM_Prescaler = psc;					//
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //

	TIM_TimeBaseStructure.TIM_Period = arr; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM9, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = arr / 3;
	//  Channel

	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_Toggle;			  // :toggle模式
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable; //
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_Low;	  //
	TIM_OCInitStructure.TIM_OCIdleState = TIM_OCIdleState_Set;
	TIM_OCInitStructure.TIM_OCNIdleState = TIM_OCNIdleState_Reset;
	TIM_OC2Init(TIM9, &TIM_OCInitStructure); //
	TIM_OC1Init(TIM9, &TIM_OCInitStructure); //

	TIM_OC2PreloadConfig(TIM9, TIM_OCPreload_Disable); //
	TIM_OC1PreloadConfig(TIM9, TIM_OCPreload_Disable); //

	TIM_ARRPreloadConfig(TIM9, DISABLE); //

	TIM_CCxCmd(TIM9, TIM_Channel_1, TIM_CCx_Disable);
	TIM_CCxCmd(TIM9, TIM_Channel_2, TIM_CCx_Disable);

	TIM_ITConfig(TIM9, TIM_IT_CC1, DISABLE); //
	TIM_ITConfig(TIM9, TIM_IT_CC2, DISABLE); //

	NVIC_InitStructure.NVIC_IRQChannel = TIM1_BRK_TIM9_IRQn;	 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;		 //
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_Cmd(TIM9, ENABLE);
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

	TIM_TimeBaseStructure.TIM_Period = arr; //
	TIM_TimeBaseStructure.TIM_RepetitionCounter = 0;

	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM8, &TIM_TimeBaseStructure); //

	TIM_OCInitStructure.TIM_Pulse = arr / 3;
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

	//	SEGGER_RTT_printf(0,"%p,%p,%p\n",last_accel_delay,step_count,rest);
	//	SEGGER_RTT_printf(0,"%x,%x,%x\n",*Plast_accel_delay,*Pstep_count,*Prest);

	// SEGGER_RTT_printf(0,"1-- %d %d %d %d %d\n",new_step_delay,delay_count_temp,last_accel_delay,step_count,rest);

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
			TIM1->CCR1 = srd[3].step_delay / 2;
			TIM1->ARR = srd[3].step_delay;
			Moto_Run_Control1(3, TIM1, 1);
			//			TIM_Callback(4, TIM1, 1);
		}
		else if (Moto[3].Mstate == 2)
		{
			Moto_Run_Control2(3, TIM1, 1);
		}
		else if (Moto[3].Mstate == 3)
		{
			TIM1->CCR1 = srd[3].step_delay / 2;
			TIM1->ARR = srd[3].step_delay;
			Moto_Run_Control3(3, TIM1, 1);
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
			TIM1->CCR2 = srd[2].step_delay / 2;
			TIM1->ARR = srd[2].step_delay;
			Moto_Run_Control1(2, TIM1, 2);
			//		TIM_Callback(3, TIM1, 2);
		}
		else if (Moto[2].Mstate == 2)
		{
			Moto_Run_Control2(2, TIM1, 2);
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
			TIM1->CCR3 = srd[0].step_delay / 2;
			TIM1->ARR = srd[0].step_delay;
			;
			Moto_Run_Control1(0, TIM1, 3);
			///////////////////////////////////////
			//			TIM_Callback(1, TIM1, 3);

			///////////////////////////////////
		}
		else if (Moto[0].Mstate == 2)
		{
			Moto_Run_Control2(0, TIM1, 3);
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
			TIM1->CCR4 = srd[1].step_delay / 2;
			TIM1->ARR = srd[1].step_delay;
			Moto_Run_Control1(1, TIM1, 4);
			//			TIM_Callback(2, TIM1, 4);
		}
		else if (Moto[1].Mstate == 2)
		{
			Moto_Run_Control2(1, TIM1, 4);
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

			TIM8->CCR1 = srd[12].step_delay / 2;
			TIM8->ARR = srd[12].step_delay;
			Moto_Run_Control1(12, TIM8, 1);
		}
		else if (Moto[12].Mstate == 2)
		{
			Moto_Run_Control2(12, TIM8, 1);
		}
		else if (Moto[12].Mstate == 3)
		{

			TIM8->CCR1 = srd[12].step_delay / 2;
			TIM8->ARR = srd[12].step_delay;
			Moto_Run_Control3(12, TIM8, 1);
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

			TIM8->CCR2 = srd[13].step_delay / 2;
			TIM8->ARR = srd[13].step_delay;
			Moto_Run_Control1(13, TIM8, 2);
		}
		else if (Moto[13].Mstate == 2)
		{
			Moto_Run_Control2(13, TIM8, 2);
		}
		else if (Moto[13].Mstate == 3)
		{

			TIM8->CCR2 = srd[13].step_delay / 2;
			TIM8->ARR = srd[13].step_delay;
			Moto_Run_Control3(13, TIM8, 2);
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

			TIM8->CCR3 = srd[14].step_delay / 2;
			TIM8->ARR = srd[14].step_delay;
			Moto_Run_Control1(14, TIM8, 3);
		}
		else if (Moto[14].Mstate == 2)
		{
			Moto_Run_Control2(14, TIM8, 3);
		}
		else if (Moto[14].Mstate == 3)
		{

			TIM8->CCR3 = srd[14].step_delay / 2;
			TIM8->ARR = srd[14].step_delay;
			Moto_Run_Control3(14, TIM8, 3);
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
			TIM8->CCR4 = srd[15].step_delay / 2;
			TIM8->ARR = srd[15].step_delay;
			Moto_Run_Control1(15, TIM8, 4);
		}
		else if (Moto[15].Mstate == 2)
		{
			Moto_Run_Control2(15, TIM8, 4);
		}
		else if (Moto[15].Mstate == 3)
		{
			TIM8->CCR4 = srd[15].step_delay / 2;
			TIM8->ARR = srd[15].step_delay;
			Moto_Run_Control3(15, TIM8, 4);
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

			TIM3->CCR1 = srd[11].step_delay / 2;
			TIM3->ARR = srd[11].step_delay;
			Moto_Run_Control1(11, TIM3, 1);
		}
		else if (Moto[11].Mstate == 2)
		{
			Moto_Run_Control2(11, TIM3, 1);
		}
		else if (Moto[11].Mstate == 3)
		{
			TIM3->CCR1 = srd[11].step_delay / 2;
			TIM3->ARR = srd[11].step_delay;
			Moto_Run_Control3(11, TIM3, 1);
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

			TIM3->CCR2 = srd[10].step_delay / 2;
			TIM3->ARR = srd[10].step_delay;
			Moto_Run_Control1(10, TIM3, 2);
		}
		else if (Moto[10].Mstate == 2)
		{
			Moto_Run_Control2(10, TIM3, 2);
		}
		else if (Moto[10].Mstate == 3)
		{
			TIM3->CCR2 = srd[10].step_delay / 2;
			TIM3->ARR = srd[10].step_delay;
			Moto_Run_Control3(10, TIM3, 2);
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

			TIM3->CCR3 = srd[5].step_delay / 2;
			TIM3->ARR = srd[5].step_delay;
			Moto_Run_Control1(5, TIM3, 3);
		}
		else if (Moto[5].Mstate == 2)
		{
			Moto_Run_Control2(5, TIM3, 3);
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
			TIM3->CCR4 = srd[4].step_delay / 2;
			TIM3->ARR = srd[4].step_delay;
			Moto_Run_Control1(4, TIM3, 4);
			// TIM_Callback(5, TIM3, 4);
		}
		else if (Moto[4].Mstate == 2)
		{
			Moto_Run_Control2(4, TIM3, 4);
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

			TIM2->CCR1 = srd[8].step_delay / 2;
			TIM2->ARR = srd[8].step_delay;
			Moto_Run_Control1(8, TIM2, 1);
		}
		else if (Moto[8].Mstate == 2)
		{
			Moto_Run_Control2(8, TIM2, 1);
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
			TIM2->CCR2 = srd[9].step_delay / 2;
			TIM2->ARR = srd[9].step_delay;
			Moto_Run_Control1(9, TIM2, 2);
		}
		else if (Moto[9].Mstate == 2)
		{
			Moto_Run_Control2(9, TIM2, 2);
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
			TIM2->CCR4 = srd[6].step_delay / 2;
			TIM2->ARR = srd[6].step_delay;
			Moto_Run_Control1(6, TIM2, 4);
		}
		else if (Moto[6].Mstate == 2)
		{
			Moto_Run_Control2(6, TIM2, 4);
		}
		else if (Moto[6].Mstate == 3)
		{
			TIM2->CCR4 = srd[6].step_delay / 2;
			TIM2->ARR = srd[6].step_delay;
			Moto_Run_Control3(6, TIM2, 4);
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
			TIM2->CCR3 = srd[7].step_delay / 2;
			TIM2->ARR = srd[7].step_delay;
			Moto_Run_Control1(7, TIM2, 3);
			// TIM_Callback(8, TIM2, 3);
		}
		else if (Moto[7].Mstate == 2)
		{
			Moto_Run_Control2(7, TIM2, 3);
		}
	}
}

/*
function: TIM通道控制 control channel open or close

TIMx: where x can be 1 to 14 to select the TIMx peripheral.
operation :1 enable
					:0 disable
channelx: where x can be 1 to 4 to select

*/
void TIMxCHxOutControl(TIM_TypeDef *TIMx, u8 channelx, u8 operation)
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
function: TIM通道控制 control channel open or close

TIMx: where x can be 1 to 14 to select the TIMx peripheral.
operation :1 enable
			:0 disable

*/
void TIMxCH1OutControl(TIM_TypeDef *TIMx, u8 operation)
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
void TIMxCH2OutControl(TIM_TypeDef *TIMx, u8 operation)
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
void TIMxCH3OutControl(TIM_TypeDef *TIMx, u8 operation)
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
void TIMxCH4OutControl(TIM_TypeDef *TIMx, u8 operation)
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

/*电机运动算法*/
void Moto_Run_Control1(u32 num, TIM_TypeDef *TIMx, u8 channelx)
{
	// Holds next delay period.
	u32 new_step_delay = 0;
	u32 delay_count_temp = 0;
	// Remember the last step delay used when accelrating.
	static u32 last_accel_delay;
	// Counting steps when moving.
	static u32 step_count = 0;
	// Keep track of remainder from new_step-delay calculation to incrase accurancy
	static s32 rest = 0;
	// static u8 lock[16] = {0};

	switch (srd[num].run_state)
	{
	case STOP:

		step_count = 0;
		rest = 0;
		new_step_delay = 0;

		srd[num].step_delay = 0;

		last_accel_delay = 0;

		TIMxCHxOutControl(TIMx, channelx, 0);

		break;

	case ACCEL:
		//////////////////////////////////////////////////
		if (srd[num].lock == 0)
		{
			Moto[num].Mflag = 1;
			srd[num].lock = 1;
		}
		///////////////////////////////////////////////
		(step_count)++;
		srd[num].accel_count++;
		new_step_delay = srd[num].step_delay - (((2 * (long)srd[num].step_delay) + rest) / (4 * srd[num].accel_count + 1));
		(rest) = ((2 * (long)srd[num].step_delay) + (rest)) % (4 * srd[num].accel_count + 1);
		if (step_count >= srd[num].decel_start)
		{
			srd[num].accel_count = srd[num].decel_val;
			srd[num].run_state = DECEL;
		}
		else if (new_step_delay <= srd[num].min_delay)
		{
			last_accel_delay = new_step_delay;
			new_step_delay = srd[num].min_delay;
			rest = 0;
			srd[num].run_state = RUN;
		}
		break;

	case RUN:

		(step_count)++;
		new_step_delay = srd[num].min_delay;

		if (step_count >= srd[num].decel_start)
		{
			// Start decelration with same delay as accel ended with.
			srd[num].accel_count = srd[num].decel_val;
			new_step_delay = last_accel_delay;
			srd[num].run_state = DECEL;
		}

		break;

	case DECEL:
		(step_count)++;

		srd[num].accel_count++;
		delay_count_temp = 0xffffffff - srd[num].accel_count;
		new_step_delay = srd[num].step_delay + (((2 * srd[num].step_delay) + rest) / (4 * delay_count_temp + 1));
		rest = ((2 * (long)srd[num].step_delay) + rest) % (4 * delay_count_temp + 1);

		if (srd[num].accel_count >= 0)
		{
			step_count = 0;
			rest = 0;
			new_step_delay = 0;

			srd[num].step_delay = 0;

			last_accel_delay = 0;

			TIMxCHxOutControl(TIMx, channelx, 0);
			srd[num].run_state = STOP;

			//////////////////////////////////////////////////
			if (srd[num].lock == 1)
			{
				Moto[num].Mflag = 0;
				srd[num].lock = 0;
			}
			///////////////////////////////////////////////////
		}

		break;
	}
	srd[num].step_delay = new_step_delay;
}
void Moto_Run_Control2(u32 num, TIM_TypeDef *TIMx, u8 channelx)
{
	// Holds next delay period.
	u32 new_step_delay = 0;
	u32 delay_count_temp = 0;
	// Remember the last step delay used when accelrating.
	static u32 last_accel_delay;
	// Counting steps when moving.
	static u32 step_count = 0;
	// Keep track of remainder from new_step-delay calculation to incrase accurancy
	static s32 rest = 0;

	switch (srd[num].run_state)
	{
	case STOP:
		step_count = 0;
		rest = 0;
		new_step_delay = 0;

		last_accel_delay = 0;

		srd[num].step_delay = 0;

		TIMxCHxOutControl(TIMx, channelx, 0);
		break;

	case ACCEL:

		(step_count)++;

		if (step_count < ACC8NUM)
		{
			srd[num].step_delay = RSACC[(step_count) / 8];
		}
		else
		{
			srd[num].run_state = RUN;
			step_count = ACC8NUM / 4;

			srd[num].step_delay = RSACC[ACCNUM - 1];
		}

		if (channelx == 1)
		{
			TIMx->CCR1 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 2)
		{
			TIMx->CCR2 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 3)
		{
			TIMx->CCR3 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 4)
		{
			TIMx->CCR4 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}

		break;

	case RUN:
		break;

	case DECEL:
		step_count--;
		if (step_count < 2) //   if(PWMX_Num >srd1616.accel_count-1)
		{
			srd[num].run_state = STOP;
		}

		srd[num].step_delay = RSACC[step_count / 2];

		if (channelx == 1)
		{
			TIMx->CCR1 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 2)
		{
			TIMx->CCR2 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 3)
		{
			TIMx->CCR3 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}
		if (channelx == 4)
		{
			TIMx->CCR4 = srd[num].step_delay / 2;
			TIMx->ARR = srd[num].step_delay;
		}

		break;
	default:
		break;
	}
}
void Moto_Run_Control3(u32 num, TIM_TypeDef *TIMx, u8 channelx)
{
	// Holds next delay period.
	u32 new_step_delay = 0;
	u32 delay_count_temp = 0;
	// Remember the last step delay used when accelrating.
	static u32 last_accel_delay;
	// Counting steps when moving.
	static u32 step_count = 0;
	// Keep track of remainder from new_step-delay calculation to incrase accurancy
	static s32 rest = 0;

	switch (srd[num].run_state)
	{
	case STOP:
		step_count = 0;
		rest = 0;
		new_step_delay = 0;
		srd[num].step_delay = 0;
		last_accel_delay = 0;
		TIMxCHxOutControl(TIMx, channelx, 0);
		break;
	case ACCEL:

		(step_count)++;
		srd[num].accel_count++;
		new_step_delay = srd[num].step_delay - (((2 * (long)srd[num].step_delay) + rest) / (4 * srd[num].accel_count + 1));
		rest = ((2 * (long)srd[num].step_delay) + rest) % (4 * srd[num].accel_count + 1);
		if (step_count >= srd[num].decel_start)
		{
			srd[num].accel_count = srd[num].decel_val;
			srd[num].run_state = DECEL;
		}
		else if (new_step_delay <= srd[num].min_delay)
		{
			last_accel_delay = new_step_delay;
			new_step_delay = srd[num].min_delay;
			rest = 0;
			srd[num].run_state = RUN;
		}
		srd[num].step_delay = new_step_delay;
		break;

	case RUN:

		(step_count)++;
		new_step_delay = srd[num].min_delay;

		if (step_count >= srd[num].decel_start)
		{
			// Start decelration with same delay as accel ended with.
			srd[num].accel_count = srd[num].decel_val;
			new_step_delay = last_accel_delay;
			srd[num].run_state = DECEL;
		}

		srd[num].step_delay = new_step_delay;
		break;
	case DECEL:
		(step_count)++;
		srd[num].accel_count++;

		if (srd[num].accel_count >= 0)
		{
			step_count = 0;
			rest = 0;
			new_step_delay = 0;
			srd[num].step_delay = 0;
			last_accel_delay = 0;
			TIMxCHxOutControl(TIMx, channelx, 0);
			srd[num].run_state = STOP;
		}
		break;
	}
}

//************************************************************************
/**
  * 函数功能: 定时器中断回调函数
  * 输入参数: 无
  * 返 回 值: 无
  * 说    明: 实现加减速过程

硬石版本算法
  */
void TIM_Callback(uint8_t num, TIM_TypeDef *TIMx, uint8_t channelx)
{
	__IO uint32_t tim_count = 0;
	__IO uint32_t tmp = 0;
	// 保存新（下）一个延时周期
	uint16_t new_step_delay = 0;
	// 加速过程中最后一次延时（脉冲周期）.
	__IO static uint16_t last_accel_delay = 0;
	// 总移动步数计数器
	__IO static uint32_t step_count = 0;
	// 记录new_step_delay中的余数，提高下一步计算的精度
	__IO static int32_t rest = 0;
	// 定时器使用翻转模式，需要进入两次中断才输出一个完整脉冲
	__IO static uint8_t i = 0;

	// 设置比较值
	//    tim_count=__HAL_TIM_GET_COUNTER(&htimx_STEPMOTOR);
	//    tmp = tim_count+srd.step_delay;
	//    __HAL_TIM_SET_COMPARE(&htimx_STEPMOTOR,STEPMOTOR_TIM_CHANNEL_x,tmp);
	// CompareValue( num, step);

	i++;		// 定时器中断次数计数值
	if (i == 2) // 2次，说明已经输出一个完整脉冲
	{
		i = 0;						// 清零定时器中断次数计数值
		switch (srd[num].run_state) // 加减速曲线阶段
		{
		case STOP:
			step_count = 0; // 清零步数计数器
			rest = 0;		// 清零余值
					  // 关闭通道

			//          MotionStatus = 0;  //  电机为停止状态
			TIMxCHxOutControl(TIMx, channelx, 0);
			break;

		case ACCEL:
			step_count++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position++; // 绝对位置加1
			}
			else
			{
				step_position--; // 绝对位置减1
			}
			srd[num].accel_count++;																						  // 加速计数值加1
			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest) / (4 * srd[num].accel_count + 1)); // 计算新(下)一步脉冲周期(时间间隔)
			rest = ((2 * srd[num].step_delay) + rest) % (4 * srd[num].accel_count + 1);									  // 计算余数，下次计算补上余数，减少误差
			if (step_count >= srd[num].decel_start)																		  // 检查是够应该开始减速
			{
				srd[num].accel_count = srd[num].decel_val; // 加速计数值为减速阶段计数值的初始值
				srd[num].run_state = DECEL;				   // 下个脉冲进入减速阶段
			}
			else if (new_step_delay <= srd[num].min_delay) // 检查是否到达期望的最大速度
			{
				last_accel_delay = new_step_delay;	 // 保存加速过程中最后一次延时（脉冲周期）
				new_step_delay = srd[num].min_delay; // 使用min_delay（对应最大速度speed）
				rest = 0;							 // 清零余值
				srd[num].run_state = RUN;			 // 设置为匀速运行状态
			}
			break;

		case RUN:
			step_count++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position++; // 绝对位置加1
			}
			else
			{
				step_position--; // 绝对位置减1
			}
			new_step_delay = srd[num].min_delay;	// 使用min_delay（对应最大速度speed）
			if (step_count >= srd[num].decel_start) // 需要开始减速
			{
				srd[num].accel_count = srd[num].decel_val; // 减速步数做为加速计数值
				new_step_delay = last_accel_delay;		   // 加阶段最后的延时做为减速阶段的起始延时(脉冲周期)
				srd[num].run_state = DECEL;				   // 状态改变为减速
			}
			break;

		case DECEL:
			step_count++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position++; // 绝对位置加1
			}
			else
			{
				step_position--; // 绝对位置减1
			}
			srd[num].accel_count++;
			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest) / (4 * srd[num].accel_count + 1)); // 计算新(下)一步脉冲周期(时间间隔)
			rest = ((2 * srd[num].step_delay) + rest) % (4 * srd[num].accel_count + 1);									  // 计算余数，下次计算补上余数，减少误差

			// 检查是否为最后一步
			if (srd[num].accel_count >= 0)
			{
				srd[num].run_state = STOP;
			}
			break;
		}
		srd[num].step_delay = new_step_delay; // 为下个(新的)延时(脉冲周期)赋值
	}
}
