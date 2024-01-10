#include "timer.h"
#include "usart.h"
#include "delay.h"
#include "moto.h"
#include "exti.h"
#include "cmd_process.h"
#include "cmd_queue.h"

//////////////////////////////////////////////////////////////////////////////////
extern uint8 cmd_RXbuffer[];

extern SpeedRampData srd[MOTONUM];
extern Moto_Struct Moto[MOTONUM];



void TIM4_Int_Init(u16 arr, u16 psc)
{
	TIM_TimeBaseInitTypeDef TIM_TimeBaseInitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM4, ENABLE); ///

	TIM_TimeBaseInitStructure.TIM_Period = arr;						//
	TIM_TimeBaseInitStructure.TIM_Prescaler = psc;					//
	TIM_TimeBaseInitStructure.TIM_CounterMode = TIM_CounterMode_Up; //
	TIM_TimeBaseInitStructure.TIM_ClockDivision = TIM_CKD_DIV1;

	TIM_TimeBaseInit(TIM4, &TIM_TimeBaseInitStructure); //

	TIM_ClearITPendingBit(TIM4, TIM_IT_Update); // 

	TIM_ITConfig(TIM4, TIM_IT_Update, ENABLE); //

	NVIC_InitStructure.NVIC_IRQChannel = TIM4_IRQn;				 //
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x01; //
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x1;		 //
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	TIM_Cmd(TIM4, ENABLE);
}

uint8 state[16] = {0};
uint8 tx_data[32] = {0};

void TIM4_IRQHandler(void)
{
	if (TIM_GetITStatus(TIM4, TIM_IT_Update) == SET)
	{
		TIM_ClearITPendingBit(TIM4, TIM_IT_Update);
		uint8 len = 0;
		for (uint8 i = 0; i < 16; i++)
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
			uint8 tx[len * 2];
			memcpy(tx, tx_data, len * 2);
			ComAckPack(PACK_ACK, CMD_TX_MOTOR_STATUS, tx,(len * 2));
		}
	}
}
