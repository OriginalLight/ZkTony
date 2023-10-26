#ifndef _TIMER_H
#define _TIMER_H
#include "sys.h"
//////////////////////////////////////////////////////////////////////////////////
// ������ֻ��ѧϰʹ�ã�δ���������ɣ��������������κ���;
// ALIENTEK STM32H7������
// ��ʱ����������
// ����ԭ��@ALIENTEK
// ������̳:www.openedv.com
// ��������:2017/8/12
// �汾��V1.0
// ��Ȩ���У�����ؾ���
// Copyright(C) �������������ӿƼ����޹�˾ 2014-2024
// All rights reserved
//////////////////////////////////////////////////////////////////////////////////
extern TIM_HandleTypeDef TIM4_Handler; // ��ʱ��3PWM���

void STEPMOTOR_TIMx_Init(void);
void STEPMOTOR_GPIO_Init(void);
void TIM4_Init(uint16_t arr, uint16_t psc);
void TIM_Callback(uint8_t num);


#endif
