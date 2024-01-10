#include "timer.h"
#include "cmd_process.h"
#include "moto.h"
#include "sys.h"
#include <math.h>
#include <string.h>
#include <stdint.h>
//////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////////
// /*
// // APB1 定时器有 TIM2, TIM3 ,TIM4, TIM5, TIM6, TIM7, TIM12, TIM13, TIM14，LPTIM1
// // APB2 定时器有 TIM1, TIM8 , TIM15, TIM16，TIM17
// // AHB1 = sysclk/2   
// // APB1 = AHB1/2 MHZ
// // AHB2 = sysclk/2    
// // APB2 = AHB2/2 MHZ
// */
////////////////
TIM_HandleTypeDef TIM4_Handler; // 定时器句柄

TIM_HandleTypeDef htimx_STEPMOTOR;
TIM_HandleTypeDef *htim = &htimx_STEPMOTOR;

extern Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];

__IO int32_t step_position[MOTONUM] = {0};

typedef struct
{
    uint16_t Pulse_Pin; // 定时器脉冲输出引脚
    uint32_t Channel;   // 定时器通道
    uint32_t IT_CCx;    // 定时器通道中断使能位
    uint32_t Flag_CCx;  // 定时器SR中断标记位
} Tim;

/* 定时器脉冲输出引脚*/
const Tim Timer8[4] = {
    {GPIO_PIN_6, TIM_CHANNEL_1, TIM_IT_CC1, TIM_FLAG_CC1},
    {GPIO_PIN_7, TIM_CHANNEL_2, TIM_IT_CC2, TIM_FLAG_CC2}, // GPIOC
    {GPIO_PIN_8, TIM_CHANNEL_3, TIM_IT_CC3, TIM_FLAG_CC3}, //
    {GPIO_PIN_9, TIM_CHANNEL_4, TIM_IT_CC4, TIM_FLAG_CC4}, //
};

const Tim Timer1[4] = {
    {GPIO_PIN_9, TIM_CHANNEL_1, TIM_IT_CC1, TIM_FLAG_CC1},
    {GPIO_PIN_11, TIM_CHANNEL_2, TIM_IT_CC2, TIM_FLAG_CC2}, // GPIOE
    {GPIO_PIN_13, TIM_CHANNEL_3, TIM_IT_CC3, TIM_FLAG_CC3}, //
    {GPIO_PIN_14, TIM_CHANNEL_4, TIM_IT_CC4, TIM_FLAG_CC4}, //
};

const Tim Timer2[4] = {
    {GPIO_PIN_0, TIM_CHANNEL_1, TIM_IT_CC1, TIM_FLAG_CC1},
    {GPIO_PIN_1, TIM_CHANNEL_2, TIM_IT_CC2, TIM_FLAG_CC2}, // GPIOA
    {GPIO_PIN_2, TIM_CHANNEL_3, TIM_IT_CC3, TIM_FLAG_CC3}, //
    {GPIO_PIN_3, TIM_CHANNEL_4, TIM_IT_CC4, TIM_FLAG_CC4}, //
};

const Tim Timer3[4] = {
		{GPIO_PIN_4, TIM_CHANNEL_1, TIM_IT_CC1, TIM_FLAG_CC1},
		{GPIO_PIN_5, TIM_CHANNEL_2, TIM_IT_CC2, TIM_FLAG_CC2}, // GPIOB
		{GPIO_PIN_0, TIM_CHANNEL_3, TIM_IT_CC3, TIM_FLAG_CC3}, //
		{GPIO_PIN_1, TIM_CHANNEL_4, TIM_IT_CC4, TIM_FLAG_CC4}, //
};

/* 函数体 --------------------------------------------------------------------*/
/**
 * 函数功能: STEPMOTOR相关GPIO初始化配置
 * 输入参数: 无
 * 返 回 值: 无
 * 说    明: 无
 */
void STEPMOTOR_GPIO_Init()
{
    uint8_t i = 0;
    GPIO_InitTypeDef GPIO_InitStruct;

    /* 电机定时器时钟使能*/
    __HAL_RCC_GPIOA_CLK_ENABLE();
    __HAL_RCC_GPIOB_CLK_ENABLE();
    __HAL_RCC_GPIOC_CLK_ENABLE();
    __HAL_RCC_GPIOE_CLK_ENABLE();

    for (i = 0; i < 4; i++)
    {
        /* 步进电机驱动器：脉冲输出 */
        GPIO_InitStruct.Pin = Timer8[i].Pulse_Pin;
        GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
        GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
        GPIO_InitStruct.Alternate = GPIO_AF3_TIM8;
        GPIO_InitStruct.Pull = GPIO_PULLUP;
        HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

        GPIO_InitStruct.Pin = Timer1[i].Pulse_Pin;
        GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
        GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
        GPIO_InitStruct.Alternate = GPIO_AF1_TIM1;
        GPIO_InitStruct.Pull = GPIO_PULLUP;
        HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

        GPIO_InitStruct.Pin = Timer2[i].Pulse_Pin;
        GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
        GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
        GPIO_InitStruct.Alternate = GPIO_AF1_TIM2;
        GPIO_InitStruct.Pull = GPIO_PULLUP;
        HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

        GPIO_InitStruct.Pin = Timer3[i].Pulse_Pin;
        GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
        GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
        GPIO_InitStruct.Alternate = GPIO_AF2_TIM3;
        GPIO_InitStruct.Pull = GPIO_PULLUP;
        HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);
    }

    ///* 步进电机驱动器：方向控制  默认为正转 为1*/
    GPIO_InitStruct.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

    GPIO_InitStruct.Pin = GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

    GPIO_InitStruct.Pin = GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

    ///* 步进电机驱动器：使能控制 默认失能状态 关闭， 电机运动后为使能态 开启 */
    GPIO_InitStruct.Pin = GPIO_PIN_0;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

    GPIO_InitStruct.Pin = GPIO_PIN_6 | GPIO_PIN_7 | GPIO_PIN_8 | GPIO_PIN_9;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
    GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
    GPIO_InitStruct.Pull = GPIO_PULLUP;
    HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);
}

//-------------------------------------------------------------------------
/**
 * 函数功能: 步进电机驱动器定时器初始化
 * 输入参数: 无
 * 返 回 值: 无
 * 说    明: 无
 */
void STEPMOTOR_TIMx_Init()
{
	uint8_t i = 0;
    TIM_ClockConfigTypeDef sClockSourceConfig; // 定时器时钟
    TIM_OC_InitTypeDef sConfigOC;              // 定时器通道比较输出

    /* STEPMOTOR相关GPIO初始化配置 */
	STEPMOTOR_GPIO_Init();

	// TIME 8
	__HAL_RCC_TIM8_CLK_ENABLE();

    /* 定时器基本环境配置 */
    htimx_STEPMOTOR.Instance = TIM8;                             // 定时器编号
    htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;    // 定时器预分频器
    htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;       // 计数方向：向上计数
    htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;          // 定时器周期
    htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // 时钟分频
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);

    /* 定时器时钟源配置 */
    sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // 使用内部时钟源
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);

    /* 定时器比较输出配置 */
    sConfigOC.OCMode = TIM_OCMODE_TOGGLE;            // 比较输出模式：反转输出
    sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;          // 脉冲数
    sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;      // 输出极性
    sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;     // 互补通道输出极性
    sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;       // 快速模式
    sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;   // 空闲电平
    sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // 互补通道空闲电平

    /* 使能比较输出通道 */
	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer8[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer8[i].Channel);
		TIM_CCxChannelCmd(TIM8, Timer8[i].Channel, TIM_CCx_DISABLE);
	}

    /* 配置定时器中断优先级并使能 */
	HAL_NVIC_SetPriority(TIM8_CC_IRQn, 1, 0);
	HAL_NVIC_EnableIRQ(TIM8_CC_IRQn);

	/* Enable the main output */
	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // 使能定时器
	//----------------------------------------------------------------------
	// TIME 1
	__HAL_RCC_TIM1_CLK_ENABLE();

	
	htimx_STEPMOTOR.Instance = TIM1;							
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; 
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);


	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // 
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);


	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // 
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // 
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // 
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // 
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // 
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // 
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // 


	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer1[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer1[i].Channel);
		TIM_CCxChannelCmd(TIM1, Timer1[i].Channel, TIM_CCx_DISABLE);
	}


	HAL_NVIC_SetPriority(TIM1_CC_IRQn, 1, 0);
	HAL_NVIC_EnableIRQ(TIM1_CC_IRQn);

	/* Enable the main output */
	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // 

	//----------------------------------------------------------------------
	// TIME 2
	__HAL_RCC_TIM2_CLK_ENABLE();


	htimx_STEPMOTOR.Instance = TIM2;							 
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; 
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);


	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; 
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);


	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; 


	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer2[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer2[i].Channel);
		TIM_CCxChannelCmd(TIM2, Timer2[i].Channel, TIM_CCx_DISABLE);
	}


	HAL_NVIC_SetPriority(TIM2_IRQn, 1, 1);
	HAL_NVIC_EnableIRQ(TIM2_IRQn);

	/* Enable the main output */
	//	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // 

	//----------------------------------------------------------------------
	// TIME 3
	__HAL_RCC_TIM3_CLK_ENABLE();


	htimx_STEPMOTOR.Instance = TIM3;							 // 
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 // 
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 // 
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 // 
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // 
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);


	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // 
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);


	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // 
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // 
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // 
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // 
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // 
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // 
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // 


	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer3[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer3[i].Channel);
		TIM_CCxChannelCmd(TIM3, Timer3[i].Channel, TIM_CCx_DISABLE);
	}


	HAL_NVIC_SetPriority(TIM3_IRQn, 1, 1);
	HAL_NVIC_EnableIRQ(TIM3_IRQn);

	/* Enable the main output */
	//	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // 
}
/*-------------------------------------------------------------------------*/
/**
 * 函数功能: 定时器中断回调函数
 * 输入参数: 无
 * 返 回 值: 无
 * 说    明: 实现加减速过程
 */

void TIM_Callback(uint8_t num)
{
	__IO  uint16_t tim_count = 0;
	__IO uint32_t new_step_delay = 0;
	__IO static uint8_t i[MOTONUM] = {0};
	__IO static uint16_t last_accel_delay[MOTONUM] = {0};
    // 总移动步数计数器
	__IO static uint32_t step_count[MOTONUM] = {0};
    // 记录new_step_delay中的余数，提高下一步计算的精度
	__IO static int32_t rest[MOTONUM] = {0};
    // 定时器使用翻转模式，需要进入两次中断才输出一个完整脉冲

    // 设置比较值

	tim_count = TIM_GetCompare(num);
	tim_count += (srd[num].step_delay / 2);
	TIM_SetCompare(num, tim_count);

	i[num]++;		 //  定时器中断次数计数值
	if (i[num] == 2) // 2次，说明已经输出一个完整脉冲
	{
		i[num] = 0;					// 清零定时器中断次数计数值
		switch (srd[num].run_state) // 加减速曲线阶段
		{
		case STOP:

			Moto[num].MotionStatus = STOP; //  电机为停止状态
			step_count[num] = 0;		   // 清零步数计数器
			rest[num] = 0;				   // 清零余值
			last_accel_delay[num] = 0;
			srd[num].accel_count = 0;
			srd[num].step_delay = 0;
			srd[num].min_delay = 0;
			
			// close channle
			TIMControl(num, 0);	


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

			step_count[num]++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // 绝对位置加1
			}
			else
			{
				step_position[num]--; // 绝对位置减1
			}
			srd[num].accel_count++; // 加速计数值加1

			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num]) / (4 * srd[num].accel_count + 1)); // 计算新(下)一步脉冲周期(时间间隔)
			rest[num] = ((2 * srd[num].step_delay) + rest[num]) % (4 * srd[num].accel_count + 1);							   // 计算余数，下次计算补上余数，减少误差

			if (step_count[num] >= srd[num].decel_start) // 检查是否应该开始减速
			{
				srd[num].accel_count = srd[num].decel_val; // 加速计数值为减速阶段计数值的初始值
				srd[num].run_state = DECEL;				   // 下个脉冲进入减速阶段
			}
			else if (new_step_delay <= srd[num].min_delay) // 检查是否到达期望的最大速度
			{
				srd[num].accel_count = srd[num].decel_val; // 加速计数值为减速阶段计数值的初始值
				last_accel_delay[num] = new_step_delay;	   // 保存加速过程中最后一次延时（脉冲周期）
				new_step_delay = srd[num].min_delay;	   // 使用min_delay（对应最大速度speed）
				rest[num] = 0;							   // 清零余值
				srd[num].run_state = RUN;				   // 设置为匀速运行状态
			}
			last_accel_delay[num] = new_step_delay; // 保存加速过程中最后一次延时（脉冲周期）
			break;

		case RUN:
			step_count[num]++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // 绝对位置加1
			}
			else
			{
				step_position[num]--; // 绝对位置减1
			}
			new_step_delay = srd[num].min_delay;		 // 使用min_delay（对应最大速度
			if (step_count[num] >= srd[num].decel_start) // 需要开始减速
			{
				srd[num].accel_count = srd[num].decel_val; // 减速步数做为加速计数值
				new_step_delay = last_accel_delay[num];	   // 加阶段最后的延时做为减速阶段的起始延时(脉冲周期)
				srd[num].run_state = DECEL;				   // 状态改变为减速
			}
			break;

		case DECEL:
			step_count[num]++; // 步数加1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // 绝对位置加1
			}
			else
			{
				step_position[num]--; // 绝对位置减1
			}
			srd[num].accel_count++;
			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num]) / (4 * srd[num].accel_count + 1)); //计算新(下)一步脉冲周期(时间间隔)
			rest[num] = ((2 * srd[num].step_delay) + rest[num]) % (4 * srd[num].accel_count + 1);							   // 计算余数，下次计算补上余数，减少误差

			// 检查是否为最后一步
			if (srd[num].accel_count >= 0)
			{
				srd[num].run_state = STOP;
			}
			break;
		}
		if ((new_step_delay >> 1) > 0xFFFF)
		{
			new_step_delay = 0x1FFFF;
		}
		srd[num].step_delay = new_step_delay; // 为下个(新的)延时(脉冲周期)赋值
	}
}

/*-------------------------------------------------------------------------*/
void TIM8_CC_IRQHandler(void)
{
	TIM_HandleTypeDef htimx_STEPMOTOR;
	TIM_HandleTypeDef* htim = &htimx_STEPMOTOR;
	htimx_STEPMOTOR.Instance = TIM8;
	/* Capture compare 1 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC1) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC1) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC1);

			srd[12].dir = Moto_Dir_Get(12);
			if (srd[12].dir != Moto[12].MotoDir)
			{
				Moto_Dir_Set(12, Moto[12].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(12);
		}
	}
	/* Capture compare 2 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC2) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC2) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC2);

			srd[13].dir = Moto_Dir_Get(13);
			if (srd[13].dir != Moto[13].MotoDir)
			{
				Moto_Dir_Set(13, Moto[13].MotoDir);
			}

			/* Output compare event */
			TIM_Callback(13);
		}
	}
	/* Capture compare 3 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC3) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC3) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC3);

			srd[14].dir = Moto_Dir_Get(14);
			if (srd[14].dir != Moto[14].MotoDir)
			{
				Moto_Dir_Set(14, Moto[14].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(14);
		}
	}
	/* Capture compare 4 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC4) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC4) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC4);

			srd[15].dir = Moto_Dir_Get(15);
			if (srd[15].dir != Moto[15].MotoDir)
			{
				Moto_Dir_Set(15, Moto[15].MotoDir);
			}

			/* Output compare event */
			TIM_Callback(15);
		}
	}
}
/*-------------------------------------------------------------------------*/
void TIM1_CC_IRQHandler(void)
{

		TIM_HandleTypeDef htimx_STEPMOTOR;
	TIM_HandleTypeDef* htim = &htimx_STEPMOTOR;
	htimx_STEPMOTOR.Instance = TIM1;
	/* Capture compare 1 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC1) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC1) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC1);

			srd[3].dir = Moto_Dir_Get(3);
			if (srd[3].dir != Moto[3].MotoDir)
			{
				Moto_Dir_Set(3, Moto[3].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(3);
		}
	}
	/* Capture compare 2 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC2) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC2) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC2);
			srd[2].dir = Moto_Dir_Get(2);
			if (srd[2].dir != Moto[2].MotoDir)
			{
				Moto_Dir_Set(2, Moto[2].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(2);
		}
	}
	/* Capture compare 3 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC3) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC3) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC3);

			srd[0].dir = Moto_Dir_Get(0);
			if (srd[0].dir != Moto[0].MotoDir)
			{
				Moto_Dir_Set(0, Moto[0].MotoDir);
			}

			/* Output compare event */
			TIM_Callback(0);
		}
	}
	/* Capture compare 4 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC4) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC4) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC4);

			srd[1].dir = Moto_Dir_Get(1);
			if (srd[1].dir != Moto[1].MotoDir)
			{
				Moto_Dir_Set(1, Moto[1].MotoDir);
			}

			/* Output compare event */
			TIM_Callback(1);
		}
	}
}

/*-------------------------------------------------------------------------*/
void TIM2_IRQHandler(void)
{
		TIM_HandleTypeDef htimx_STEPMOTOR;
	TIM_HandleTypeDef* htim = &htimx_STEPMOTOR;
	htimx_STEPMOTOR.Instance = TIM2;
	/* Capture compare 1 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC1) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC1) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC1);

			srd[8].dir = Moto_Dir_Get(8);
			if (srd[8].dir != Moto[8].MotoDir)
			{
				Moto_Dir_Set(8, Moto[8].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(8);
		}
	}
	/* Capture compare 2 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC2) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC2) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC2);

			srd[9].dir = Moto_Dir_Get(9);
			if (srd[9].dir != Moto[9].MotoDir)
			{
				Moto_Dir_Set(9, Moto[9].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(9);
		}
	}
	/* Capture compare 3 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC3) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC3) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC3);

			srd[7].dir = Moto_Dir_Get(7);
			if (srd[7].dir != Moto[7].MotoDir)
			{
				Moto_Dir_Set(7, Moto[7].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(7);
		}
	}
	/* Capture compare 4 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC4) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC4) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC4);

			srd[6].dir = Moto_Dir_Get(6);
			if (srd[6].dir != Moto[6].MotoDir)
			{
				Moto_Dir_Set(6, Moto[6].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(6);
		}
	}
}

/*-------------------------------------------------------------------------*/
void TIM3_IRQHandler(void)
{

		TIM_HandleTypeDef htimx_STEPMOTOR;
	TIM_HandleTypeDef* htim = &htimx_STEPMOTOR;
	htimx_STEPMOTOR.Instance = TIM3;
	/* Capture compare 1 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC1) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC1) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC1);

			srd[11].dir = Moto_Dir_Get(11);
			if (srd[11].dir != Moto[11].MotoDir)
			{
				Moto_Dir_Set(11, Moto[11].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(11);
		}
	}
	/* Capture compare 2 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC2) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC2) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC2);

			srd[10].dir = Moto_Dir_Get(10);
			if (srd[10].dir != Moto[10].MotoDir)
			{
				Moto_Dir_Set(10, Moto[10].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(10);
		}
	}
	/* Capture compare 3 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC3) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC3) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC3);

			srd[5].dir = Moto_Dir_Get(5);
			if (srd[5].dir != Moto[5].MotoDir)
			{
				Moto_Dir_Set(5, Moto[5].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(5);
		}
	}
	/* Capture compare 4 event */
	if (__HAL_TIM_GET_FLAG(htim, TIM_FLAG_CC4) != RESET)
	{
		if (__HAL_TIM_GET_IT_SOURCE(htim, TIM_IT_CC4) != RESET)
		{
			__HAL_TIM_CLEAR_IT(htim, TIM_IT_CC4);

			srd[4].dir = Moto_Dir_Get(4);
			if (srd[4].dir != Moto[4].MotoDir)
			{
				Moto_Dir_Set(4, Moto[4].MotoDir);
			}
			/* Output compare event */
			TIM_Callback(4);
		}
	}
}

/*-------------------------------------------------------------------------*/

//-------------------------------------------------------------------------

void TIM4_Init(u16 arr, u16 psc)
{
    TIM4_Handler.Instance = TIM4;                             // 通用定时器
    TIM4_Handler.Init.Prescaler = psc;                        // 分频
    TIM4_Handler.Init.CounterMode = TIM_COUNTERMODE_UP;       // 向上计数器
    TIM4_Handler.Init.Period = arr;                           // 自动装载值
    TIM4_Handler.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // 时钟分频因子
	HAL_TIM_Base_Init(&TIM4_Handler);

	HAL_TIM_Base_Start_IT(&TIM4_Handler); // 使能定时器和定时器更新中断：TIM_IT_UPDATE
}

// 定时器底册驱动，开启时钟，设置中断优先级
// 此函数会被HAL_TIM_Base_Init()函数调用
void HAL_TIM_Base_MspInit(TIM_HandleTypeDef *htim)
{
	if (htim->Instance == TIM4)
	{
        __HAL_RCC_TIM4_CLK_ENABLE();           // 使能TIM时钟
        HAL_NVIC_SetPriority(TIM4_IRQn, 2, 3); // 设置中断优先级，抢占优先级1，子优先级3
        HAL_NVIC_EnableIRQ(TIM4_IRQn);         // 开启ITM中断
	}
}

// 定时器中断服务函数
void TIM4_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&TIM4_Handler);
}

uint8_t state[16] = {0};
uint8_t tx_data[32] = {0};
// 定时器中断服务函数调用
void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef *htim)
{
	if (htim == (&TIM4_Handler))
	{
		uint8_t len = 0;
		for (uint8_t i = 0; i < 16; i++)
		{
			if (state[i] != Moto[i].Mflag)
			{
				state[i] = Moto[i].Mflag;
				if (Moto[i].Mflag == 0)
				{
					tx_data[len * 2] = i;
					tx_data[len * 2 + 1] = 0;
					len++;
				}
			}
		}
		if (len > 0)
		{
			uint8_t tx[len * 2];
			memcpy(tx, tx_data, len * 2);
			ComAckPack(PACK_ACK, CMD_TX_MOTOR_STATUS, tx, (len * 2));
		}
	}
}
