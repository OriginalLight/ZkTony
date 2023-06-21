#include "timer.h"
#include "cmd_process.h"
#include "moto.h"
#include "sys.h"
#include <math.h>
#include <string.h>
#include <stdint.h>
//////////////////////////////////////////////////////////////////////////////////
// ������ֻ��ѧϰʹ�ã�δ���������ɣ��������������κ���;
// ALIENTEK STM32H7������
// ��ʱ���ж���������
// ����ԭ��@ALIENTEK
// ������̳:www.openedv.com
// ��������:2017/8/12
// �汾��V1.0
// ��Ȩ���У�����ؾ���
// Copyright(C) �������������ӿƼ����޹�˾ 2014-2024
// All rights reserved
//////////////////////////////////////////////////////////////////////////////////
/*
APB1 ��ʱ���� TIM2, TIM3 ,TIM4, TIM5, TIM6, TIM7, TIM12, TIM13, TIM14��LPTIM1
APB2 ��ʱ���� TIM1, TIM8 , TIM15, TIM16��TIM17
AHB1 = sysclk/2
APB1 = AHB1/2 MHZ
AHB2 = sysclk/2
APB2 = AHB2/2 MHZ
*/
////////////////
TIM_HandleTypeDef TIM4_Handler; // ��ʱ�����

TIM_HandleTypeDef htimx_STEPMOTOR;
TIM_HandleTypeDef *htim = &htimx_STEPMOTOR;

extern Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];

__IO int32_t step_position[MOTONUM] = {0};

typedef struct
{
	uint16_t Pulse_Pin; // ��ʱ�������������
	uint32_t Channel;	// ��ʱ��ͨ��
	uint32_t IT_CCx;	// ��ʱ��ͨ���ж�ʹ��λ
	uint32_t Flag_CCx;	// ��ʱ��SR�жϱ��λ
} Tim;

/* ��ʱ�������������*/
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

/* ������ --------------------------------------------------------------------*/
/**
 * ��������: STEPMOTOR���GPIO��ʼ������
 * �������: ��
 * �� �� ֵ: ��
 * ˵    ��: ��
 */
void STEPMOTOR_GPIO_Init()
{
	uint8_t i = 0;
	GPIO_InitTypeDef GPIO_InitStruct;

	/* �����ʱ��ʱ��ʹ��*/
	__HAL_RCC_GPIOA_CLK_ENABLE();
	__HAL_RCC_GPIOB_CLK_ENABLE();
	__HAL_RCC_GPIOC_CLK_ENABLE();
	__HAL_RCC_GPIOE_CLK_ENABLE();

	for (i = 0; i < 4; i++)
	{
		/* ���������������������� */
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

	/* ����������������������  Ĭ��Ϊ��ת Ϊ1*/
	// HAL_GPIO_WritePin(GPIOC,(GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15),GPIO_PIN_RESET);//Ĭ������Ϊ˳ʱ�뷽��
	GPIO_InitStruct.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
	GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
	GPIO_InitStruct.Pull = GPIO_PULLUP;
	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

	// HAL_GPIO_WritePin(GPIOA, (GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6), GPIO_PIN_RESET); // Ĭ������Ϊ˳ʱ�뷽��
	GPIO_InitStruct.Pin = GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
	GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
	GPIO_InitStruct.Pull = GPIO_PULLUP;
	HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

	// HAL_GPIO_WritePin(GPIOE,(GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6),GPIO_PIN_RESET);//Ĭ������Ϊ˳ʱ�뷽��
	GPIO_InitStruct.Pin = GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
	GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
	GPIO_InitStruct.Pull = GPIO_PULLUP;
	HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

	/* ���������������ʹ�ܿ��� */
	// HAL_GPIO_WritePin();
	GPIO_InitStruct.Pin = GPIO_PIN_0;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
	GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
	GPIO_InitStruct.Pull = GPIO_PULLDOWN;
	HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_6 | GPIO_PIN_7 | GPIO_PIN_8 | GPIO_PIN_9;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_HIGH;
	GPIO_InitStruct.Alternate = GPIO_AF0_TRACE;
	GPIO_InitStruct.Pull = GPIO_PULLDOWN;
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);
}

//-------------------------------------------------------------------------
/**
 * ��������: ���������������ʱ����ʼ��
 * �������: ��
 * �� �� ֵ: ��
 * ˵    ��: ��
 */
void STEPMOTOR_TIMx_Init()
{
	uint8_t i = 0;
	TIM_ClockConfigTypeDef sClockSourceConfig; // ��ʱ��ʱ��
	TIM_OC_InitTypeDef sConfigOC;			   // ��ʱ��ͨ���Ƚ����

	/* STEPMOTOR���GPIO��ʼ������ */
	STEPMOTOR_GPIO_Init();

	// TIME 8
	__HAL_RCC_TIM8_CLK_ENABLE();

	/* ��ʱ�������������� */
	htimx_STEPMOTOR.Instance = TIM8;							 // ��ʱ�����
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 // ��ʱ��Ԥ��Ƶ��
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 // �����������ϼ���
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 // ��ʱ������
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // ʱ�ӷ�Ƶ
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);

	/* ��ʱ��ʱ��Դ���� */
	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // ʹ���ڲ�ʱ��Դ
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);

	/* ��ʱ���Ƚ�������� */
	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // �Ƚ����ģʽ����ת���
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // ������
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // �������
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // ����ͨ���������
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // ����ģʽ
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // ���е�ƽ
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // ����ͨ�����е�ƽ

	/* ʹ�ܱȽ����ͨ�� */
	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer8[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer8[i].Channel);
		TIM_CCxChannelCmd(TIM8, Timer8[i].Channel, TIM_CCx_DISABLE);
	}

	/* ���ö�ʱ���ж����ȼ���ʹ�� */
	HAL_NVIC_SetPriority(TIM8_CC_IRQn, 1, 0);
	HAL_NVIC_EnableIRQ(TIM8_CC_IRQn);

	/* Enable the main output */
	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // ʹ�ܶ�ʱ��
	//----------------------------------------------------------------------
	// TIME 1
	__HAL_RCC_TIM1_CLK_ENABLE();

	/* ��ʱ�������������� */
	htimx_STEPMOTOR.Instance = TIM1;							 // ��ʱ�����
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 // ��ʱ��Ԥ��Ƶ��
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 // �����������ϼ���
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 // ��ʱ������
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // ʱ�ӷ�Ƶ
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);

	/* ��ʱ��ʱ��Դ���� */
	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // ʹ���ڲ�ʱ��Դ
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);

	/* ��ʱ���Ƚ�������� */
	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // �Ƚ����ģʽ����ת���
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // ������
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // �������
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // ����ͨ���������
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // ����ģʽ
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // ���е�ƽ
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // ����ͨ�����е�ƽ

	/* ʹ�ܱȽ����ͨ�� */
	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer1[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer1[i].Channel);
		TIM_CCxChannelCmd(TIM1, Timer1[i].Channel, TIM_CCx_DISABLE);
	}

	/* ���ö�ʱ���ж����ȼ���ʹ�� */
	HAL_NVIC_SetPriority(TIM1_CC_IRQn, 1, 0);
	HAL_NVIC_EnableIRQ(TIM1_CC_IRQn);

	/* Enable the main output */
	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // ʹ�ܶ�ʱ��

	//----------------------------------------------------------------------
	// TIME 2
	__HAL_RCC_TIM2_CLK_ENABLE();

	/* ��ʱ�������������� */
	htimx_STEPMOTOR.Instance = TIM2;							 // ��ʱ�����
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 // ��ʱ��Ԥ��Ƶ��
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 // �����������ϼ���
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 // ��ʱ������
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // ʱ�ӷ�Ƶ
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);

	/* ��ʱ��ʱ��Դ���� */
	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // ʹ���ڲ�ʱ��Դ
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);

	/* ��ʱ���Ƚ�������� */
	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // �Ƚ����ģʽ����ת���
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // ������
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // �������
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // ����ͨ���������
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // ����ģʽ
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // ���е�ƽ
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // ����ͨ�����е�ƽ

	/* ʹ�ܱȽ����ͨ�� */
	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer2[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer2[i].Channel);
		TIM_CCxChannelCmd(TIM2, Timer2[i].Channel, TIM_CCx_DISABLE);
	}

	/* ���ö�ʱ���ж����ȼ���ʹ�� */
	HAL_NVIC_SetPriority(TIM2_IRQn, 1, 1);
	HAL_NVIC_EnableIRQ(TIM2_IRQn);

	/* Enable the main output */
	//	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // ʹ�ܶ�ʱ��

	//----------------------------------------------------------------------
	// TIME 3
	__HAL_RCC_TIM3_CLK_ENABLE();

	/* ��ʱ�������������� */
	htimx_STEPMOTOR.Instance = TIM3;							 // ��ʱ�����
	htimx_STEPMOTOR.Init.Prescaler = STEPMOTOR_TIM_PRESCALER;	 // ��ʱ��Ԥ��Ƶ��
	htimx_STEPMOTOR.Init.CounterMode = TIM_COUNTERMODE_UP;		 // �����������ϼ���
	htimx_STEPMOTOR.Init.Period = STEPMOTOR_TIM_PERIOD;			 // ��ʱ������
	htimx_STEPMOTOR.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // ʱ�ӷ�Ƶ
	HAL_TIM_Base_Init(&htimx_STEPMOTOR);

	/* ��ʱ��ʱ��Դ���� */
	sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL; // ʹ���ڲ�ʱ��Դ
	HAL_TIM_ConfigClockSource(&htimx_STEPMOTOR, &sClockSourceConfig);

	/* ��ʱ���Ƚ�������� */
	sConfigOC.OCMode = TIM_OCMODE_TOGGLE;			 // �Ƚ����ģʽ����ת���
	sConfigOC.Pulse = STEPMOTOR_TIM_PERIOD;			 // ������
	sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;		 // �������
	sConfigOC.OCNPolarity = TIM_OCNPOLARITY_LOW;	 // ����ͨ���������
	sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;		 // ����ģʽ
	sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;	 // ���е�ƽ
	sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET; // ����ͨ�����е�ƽ

	/* ʹ�ܱȽ����ͨ�� */
	for (i = 0; i < 4; i++)
	{
		HAL_TIM_OC_Stop_IT(&htimx_STEPMOTOR, Timer3[i].Channel);
		HAL_TIM_OC_ConfigChannel(&htimx_STEPMOTOR, &sConfigOC, Timer3[i].Channel);
		TIM_CCxChannelCmd(TIM3, Timer3[i].Channel, TIM_CCx_DISABLE);
	}

	/* ���ö�ʱ���ж����ȼ���ʹ�� */
	HAL_NVIC_SetPriority(TIM3_IRQn, 1, 1);
	HAL_NVIC_EnableIRQ(TIM3_IRQn);

	/* Enable the main output */
	//	__HAL_TIM_MOE_ENABLE(&htimx_STEPMOTOR);
	HAL_TIM_Base_Start(&htimx_STEPMOTOR); // ʹ�ܶ�ʱ��
}
/*-------------------------------------------------------------------------*/
/**
 * ��������: ��ʱ���жϻص�����
 * �������: ��
 * �� �� ֵ: ��
 * ˵    ��: ʵ�ּӼ��ٹ���
 */

void TIM_Callback(uint8_t num)
{
	__IO static uint16_t tim_count = 0;
	__IO uint32_t new_step_delay = 0;
	__IO static uint8_t i[MOTONUM] = {0};
	__IO static uint16_t last_accel_delay[MOTONUM] = {0};
	// ���ƶ�����������
	__IO static uint32_t step_count[MOTONUM] = {0};
	// ��¼new_step_delay�е������������һ������ľ���
	__IO static int32_t rest[MOTONUM] = {0};
	// ��ʱ��ʹ�÷�תģʽ����Ҫ���������жϲ����һ����������

	// ���ñȽ�ֵ

	tim_count = TIM_GetCompare(num);
	tim_count += (srd[num].step_delay / 2);
	TIM_SetCompare(num, tim_count);

	i[num]++;		 // ��ʱ���жϴ�������ֵ
	if (i[num] == 2) // 2�Σ�˵���Ѿ����һ����������
	{
		i[num] = 0;					// ���㶨ʱ���жϴ�������ֵ
		switch (srd[num].run_state) // �Ӽ������߽׶�
		{
		case STOP:

			Moto[num].MotionStatus = STOP; //  ���Ϊֹͣ״̬
			step_count[num] = 0;		   // ���㲽��������
			rest[num] = 0;				   // ������ֵ
			last_accel_delay[num] = 0;
			srd[num].accel_count = 0;
			srd[num].step_delay = 0;
			srd[num].min_delay = 0;
			// �ر�ͨ��
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

			step_count[num]++; // ������1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // ����λ�ü�1
			}
			else
			{
				step_position[num]--; // ����λ�ü�1
			}
			srd[num].accel_count++; // ���ټ���ֵ��1

			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num]) / (4 * srd[num].accel_count + 1)); // ������(��)һ����������(ʱ����)
			rest[num] = ((2 * srd[num].step_delay) + rest[num]) % (4 * srd[num].accel_count + 1);							   // �����������´μ��㲹���������������

			if (step_count[num] >= srd[num].decel_start) // ����Ƿ�Ӧ�ÿ�ʼ����
			{
				srd[num].accel_count = srd[num].decel_val; // ���ټ���ֵΪ���ٽ׶μ���ֵ�ĳ�ʼֵ
				srd[num].run_state = DECEL;				   // �¸����������ٽ׶�
			}
			else if (new_step_delay <= srd[num].min_delay) // ����Ƿ񵽴�����������ٶ�
			{
				srd[num].accel_count = srd[num].decel_val; // ���ټ���ֵΪ���ٽ׶μ���ֵ�ĳ�ʼֵ
				last_accel_delay[num] = new_step_delay;	   // ������ٹ��������һ����ʱ���������ڣ�
				new_step_delay = srd[num].min_delay;	   // ʹ��min_delay����Ӧ����ٶ�speed��
				rest[num] = 0;							   // ������ֵ
				srd[num].run_state = RUN;				   // ����Ϊ��������״̬
			}
			last_accel_delay[num] = new_step_delay; // ������ٹ��������һ����ʱ���������ڣ�
			break;

		case RUN:
			step_count[num]++; // ������1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // ����λ�ü�1
			}
			else
			{
				step_position[num]--; // ����λ�ü�1
			}
			new_step_delay = srd[num].min_delay;		 // ʹ��min_delay����Ӧ����ٶ�
			if (step_count[num] >= srd[num].decel_start) // ��Ҫ��ʼ����
			{
				srd[num].accel_count = srd[num].decel_val; // ���ٲ�����Ϊ���ټ���ֵ
				new_step_delay = last_accel_delay[num];	   // �ӽ׶�������ʱ��Ϊ���ٽ׶ε���ʼ��ʱ(��������)
				srd[num].run_state = DECEL;				   // ״̬�ı�Ϊ����
			}
			break;

		case DECEL:
			step_count[num]++; // ������1
			if (srd[num].dir == Moto_For)
			{
				step_position[num]++; // ����λ�ü�1
			}
			else
			{
				step_position[num]--; // ����λ�ü�1
			}
			srd[num].accel_count++;
			new_step_delay = srd[num].step_delay - (((2 * srd[num].step_delay) + rest[num]) / (4 * srd[num].accel_count + 1)); ////������(��)һ����������(ʱ����)
			rest[num] = ((2 * srd[num].step_delay) + rest[num]) % (4 * srd[num].accel_count + 1);							   // �����������´μ��㲹���������������

			// ����Ƿ�Ϊ���һ��
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
		srd[num].step_delay = new_step_delay; // Ϊ�¸�(�µ�)��ʱ(��������)��ֵ
	}
}

/*-------------------------------------------------------------------------*/
void TIM8_CC_IRQHandler(void)
{
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
// ͨ�ö�ʱ���жϳ�ʼ��,��ʱ����APB1�ϣ�APB1�Ķ�ʱ��ʱ��Ϊ200MHz
// arr���Զ���װֵ��
// psc��ʱ��Ԥ��Ƶ��
// ��ʱ�����ʱ����㷽��:Tout=((arr+1)*(psc+1))/Ft us.
// Ft=��ʱ������Ƶ��,��λ:Mhz
// ����ʹ�õ��Ƕ�ʱ��3!(��ʱ��3����APB1�ϣ�ʱ��ΪHCLK/2)
void TIM4_Init(u16 arr, u16 psc)
{
	TIM4_Handler.Instance = TIM4;							  // ͨ�ö�ʱ��
	TIM4_Handler.Init.Prescaler = psc;						  // ��Ƶ
	TIM4_Handler.Init.CounterMode = TIM_COUNTERMODE_UP;		  // ���ϼ�����
	TIM4_Handler.Init.Period = arr;							  // �Զ�װ��ֵ
	TIM4_Handler.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1; // ʱ�ӷ�Ƶ����
	HAL_TIM_Base_Init(&TIM4_Handler);

	HAL_TIM_Base_Start_IT(&TIM4_Handler); // ʹ�ܶ�ʱ���Ͷ�ʱ�������жϣ�TIM_IT_UPDATE
}

// ��ʱ���ײ�����������ʱ�ӣ������ж����ȼ�
// �˺����ᱻHAL_TIM_Base_Init()��������
void HAL_TIM_Base_MspInit(TIM_HandleTypeDef *htim)
{
	if (htim->Instance == TIM4)
	{
		__HAL_RCC_TIM4_CLK_ENABLE();		   // ʹ��TIMʱ��
		HAL_NVIC_SetPriority(TIM4_IRQn, 1, 3); // �����ж����ȼ�����ռ���ȼ�1�������ȼ�3
		HAL_NVIC_EnableIRQ(TIM4_IRQn);		   // ����ITM�ж�
	}
}

// ��ʱ���жϷ�����
void TIM4_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&TIM4_Handler);
}

uint8_t state[16] = {0};
uint8_t tx_data[32] = {0};
// ��ʱ���жϷ���������
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
