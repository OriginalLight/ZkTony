#ifndef _PWM_H
#define _PWM_H
#include "sys.h"
//////////////////////////////////////////////////////////////////////////////////

void TIM_GPIO_Config(void);
void TIM1_PWM_Init(void);
void TIM2_PWM_Init(void);
void TIM3_PWM_Init(void);
void TIM8_PWM_Init(void);

void TIMxCHxOutControl(TIM_TypeDef *TIMx, uint8_t channelx, uint8_t operation);
void TIMxCH1OutControl(TIM_TypeDef *TIMx, uint8_t operation);
void TIMxCH2OutControl(TIM_TypeDef *TIMx, uint8_t operation);
void TIMxCH3OutControl(TIM_TypeDef *TIMx, uint8_t operation);
void TIMxCH4OutControl(TIM_TypeDef *TIMx, uint8_t operation);

void TIM_Callback(uint8_t num);

void TIM2_IRQHandler(void);
void TIM1_CC_IRQHandler(void);
void TIM3_IRQHandler(void);
void TIM8_CC_IRQHandler(void);

#endif
