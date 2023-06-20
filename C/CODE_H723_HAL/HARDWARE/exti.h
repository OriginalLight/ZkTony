#ifndef _EXIT_H
#define _EXIT_H

#include "sys.h"


/* 轮询方式
IO口读取*/
#define MSensor7 HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_10)
#define MSensor10 HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_15)
#define MSensor12 HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_8)
#define MSensor13 HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_7)
#define MSensor15 HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_3)
#define MSensor16 HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_12)

// 中断处理
#define MSensor1 HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_13)
#define MSensor2 HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_7)
#define MSensor3 HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_14)
#define MSensor4 HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_8)
#define MSensor5 HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_8)
#define MSensor6 HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_10)
#define MSensor8 HAL_GPIO_ReadPin(GPIOE, GPIO_PIN_12)
#define MSensor9 HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_2)
#define MSensor11 HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_3)
#define MSensor14 HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_11)

void EXTIX_Init(void); // 外部中断初始化


void GPIO_EXTI_Callback(uint16_t GPIO_Pin);
void EXTI_Check(void);
uint8_t GPIO_CHECK(uint8_t num);

#endif
