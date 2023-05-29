#ifndef _PWM_H
#define _PWM_H
#include "sys.h"
//////////////////////////////////////////////////////////////////////////////////	 
					  

extern u32 PWMX_Num;
extern u32 PWMY1_Num;
extern u32 PWMY2_Num;


void TIM_GPIO_Config(void);
void TIM1_PWM_Init(u32 arr,u32 psc);
void TIM2_PWM_Init(u32 arr,u32 psc);	
void TIM3_PWM_Init(u32 arr,u32 psc);
void TIM8_PWM_Init(u32 arr,u32 psc);	
	
void TIM5_Int_Init(u16 arr, u16 psc);




void TIMxCHxOutControl(TIM_TypeDef* TIMx,u8 channelx,u8 operation);
void TIMxCH1OutControl(TIM_TypeDef* TIMx,u8 operation);	
void TIMxCH2OutControl(TIM_TypeDef* TIMx,u8 operation);	
void TIMxCH3OutControl(TIM_TypeDef* TIMx,u8 operation);
void TIMxCH4OutControl(TIM_TypeDef* TIMx,u8 operation);

void Moto_Run_Control1(u32 num,TIM_TypeDef* TIMx,u8 channelx);
void Moto_Run_Control2(u32 num,TIM_TypeDef* TIMx,u8 channelx);
void Moto_Run_Control3(u32 num,TIM_TypeDef* TIMx,u8 channelx);

void TIM_Callback(uint8_t num,TIM_TypeDef *TIMx, uint8_t channelx);


void TIM2_IRQHandler(void);

void TIM1_CC_IRQHandler(void);
void TIM3_IRQHandler(void);
void TIM8_CC_IRQHandler(void);
void TIM5_IRQHandler(void);


#endif
