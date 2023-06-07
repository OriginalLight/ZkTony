#include "exti.h"
#include "delay.h"
#include "pwm.h"
#include "time.h"
#include "moto.h"
#include "usart.h"
#include "DeviceVibration.h"

extern Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];

uint8_t GPIO_CHECK(uint8_t num)
{
    uint8_t val;
    switch (num)
    {
    case 0:
        val = MSensor1;
        break;
    case 1:
        val = MSensor2;
        break;
    case 2:
        val = MSensor3;
        break;
    case 3:
        val = MSensor4;
        break;
    case 4:
        val = MSensor5;
        break;
    case 5:
        val = MSensor6;
        break;
    case 6:
        val = MSensor7;
        break;
    case 7:
        val = MSensor8;
        break;
    case 8:
        val = MSensor9;
        break;
    case 9:
        val = MSensor10;
        break;
    case 10:
        val = MSensor11;
        break;
    case 11:
        val = MSensor12;
        break;
    case 12:
        val = MSensor13;
        break;
    case 13:
        val = MSensor14;
        break;
    case 14:
        val = MSensor15;
        break;
    case 15:
        val = MSensor16;
        break;
    }
    return val;
}

/* 轮询检测*/
void EXTI_Check(void)
{

    if (MSensor7 == 1)
    {
        if (Moto[6].MotoDir == Moto_Back)
        {
            srd[6].run_state = STOP;
        }
    }
    if (MSensor10 == 1)
    {
        if (Moto[9].MotoDir == Moto_Back)
        {
            srd[9].run_state = STOP;
        }
    }
    if (MSensor12 == 1)
    {
        if (Moto[11].MotoDir == Moto_Back)
        {
            srd[11].run_state = STOP;
        }
    }
    if (MSensor13 == 1)
    {
        if (Moto[12].MotoDir == Moto_Back)
        {
            srd[12].run_state = STOP;
        }
    }
    if (MSensor15 == 1)
    {
        if (Moto[14].MotoDir == Moto_Back)
        {
            srd[14].run_state = STOP;
        }
    }
    if (MSensor16 == 1)
    {
        if (Moto[15].MotoDir == Moto_Back)
        {
            srd[15].run_state = STOP;
        }
    }
}

/*中断回调函数   中断的功能相应处理 */
void GPIO_EXTI_Callback(uint16_t GPIO_Pin)
{
    switch (GPIO_Pin)
    {
    case GPIO_Pin_2:
    {
        if (MSensor9 == 1)
        {
            if (Moto[8].MotoDir == Moto_Back)
            {
                srd[8].run_state = STOP;
                
            }
        }
    }
    case GPIO_Pin_3:
    {
        if (MSensor11 == 1)
        {

            if (Moto[10].MotoDir == Moto_Back)
            {
                srd[10].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_7:
    {
        if (MSensor2 == 1)
        {
            if (Moto[1].MotoDir == Moto_Back)
            {
                srd[1].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_8:
    {
        if (MSensor4 == 1)
        {
            if (Moto[3].MotoDir == Moto_Back)
            {
                srd[3].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_10:
    {
        if (MSensor6 == 1)
        {
            if (Moto[5].MotoDir == Moto_Back)
            {
                srd[5].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_11:
    {
        if (MSensor14 == 1)
        {
            if (Moto[13].MotoDir == Moto_Back)
            {
                srd[13].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_12:
    {
        if (MSensor8 == 1)
        {
            if (Moto[7].MotoDir == Moto_Back)
            {
                srd[7].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_13:
    {
        if (MSensor1 == 1)
        {
            if (Moto[0].MotoDir == Moto_Back)
            {
                srd[0].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_14:
    {
        if (MSensor3 == 1)
        {
            if (Moto[2].MotoDir == Moto_Back)
            {
                srd[2].run_state = STOP;
            }
        }
    }
    case GPIO_Pin_15:
    {
        if (MSensor5 == 1)
        {
            if (Moto[4].MotoDir == Moto_Back)
            {
                srd[4].run_state = STOP;
            }
        }
    }
    }
}



void EXTI2_IRQHandler(void)
{

    EXTI_ClearITPendingBit(EXTI_Line2); // 清除LINE 上的中断标志位
    GPIO_EXTI_Callback(GPIO_Pin_2);
}
void EXTI3_IRQHandler(void)
{

    EXTI_ClearITPendingBit(EXTI_Line3); // 清除LINE 上的中断标志位
    GPIO_EXTI_Callback(GPIO_Pin_3);
}



void EXTI9_5_IRQHandler(void)
{

    if (EXTI_GetITStatus(EXTI_Line7) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line7); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_7);
    }
    if (EXTI_GetITStatus(EXTI_Line8) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line8); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_8);
    }
}
void EXTI15_10_IRQHandler(void)
{

    if (EXTI_GetITStatus(EXTI_Line10) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line10); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_10);
    }

    if (EXTI_GetITStatus(EXTI_Line11) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line11); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_11);
    }

    if (EXTI_GetITStatus(EXTI_Line12) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line12); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_12);
    }

    if (EXTI_GetITStatus(EXTI_Line13) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line13); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_13);
    }

    if (EXTI_GetITStatus(EXTI_Line14) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line14); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_14);
    }

    if (EXTI_GetITStatus(EXTI_Line15) != RESET)
    {

        EXTI_ClearITPendingBit(EXTI_Line15); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_Pin_15);
    }
}

// 外部中断初始化

void EXTIX_Init(void)
{
    NVIC_InitTypeDef NVIC_InitStructure;
    EXTI_InitTypeDef EXTI_InitStructure;

    GPIO_InitTypeDef GPIO_InitStructure;

    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA | RCC_AHB1Periph_GPIOB | RCC_AHB1Periph_GPIOD | RCC_AHB1Periph_GPIOE, ENABLE); // ʹ��GPIOA,B D E ʱ��

    /*GPIOA*/
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8 | GPIO_Pin_11 | GPIO_Pin_12;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;       // 普通输入模式
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; // 100M
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;     // 下拉
    GPIO_Init(GPIOA, &GPIO_InitStructure);             // 初始化GPIO

    /*GPIOB*/
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3 | GPIO_Pin_13 | GPIO_Pin_14 | GPIO_Pin_15;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; // 100M
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    /*GPIOD*/
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2 | GPIO_Pin_3 | GPIO_Pin_7 | GPIO_Pin_10 | GPIO_Pin_15;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; // 100M
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;     //
    GPIO_Init(GPIOD, &GPIO_InitStructure);             //

    /*GPIOE*/
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_7 | GPIO_Pin_8 | GPIO_Pin_10 | GPIO_Pin_12;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;       //
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz; // 100M
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;     //
    GPIO_Init(GPIOE, &GPIO_InitStructure);             //

    /* Connect EXTI Line to GPIO */                        // 连接到中断线
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE); // 使能SYSCFG时钟

    /*GPIOA*/

    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOA, EXTI_PinSource11);

    /*GPIOB*/

    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource13);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource14);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource15);

    /*GPIOD*/
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOD, EXTI_PinSource2);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOD, EXTI_PinSource3);

    /*GPIOE*/
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource7);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource8);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource10);
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource12);

    /* 配置EXTI_Line2 3 7 8 10 11 12 13 14 15 */
    EXTI_InitStructure.EXTI_Line = EXTI_Line2 | EXTI_Line3 | EXTI_Line7 | EXTI_Line8 | EXTI_Line10 | EXTI_Line11 | EXTI_Line12 | EXTI_Line13 | EXTI_Line14 | EXTI_Line15;
    EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;    // 中断事件
    EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; // 上升沿触发
    EXTI_InitStructure.EXTI_LineCmd = ENABLE;              // 中断线使能
    EXTI_Init(&EXTI_InitStructure);                        // 配置

    NVIC_InitStructure.NVIC_IRQChannel = EXTI2_IRQn;             // 外部中断
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00; // 抢占优先级
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;        // 子优先级
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;              // 使能外部中断通道
    NVIC_Init(&NVIC_InitStructure);                              // 配置

    NVIC_InitStructure.NVIC_IRQChannel = EXTI3_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    NVIC_InitStructure.NVIC_IRQChannel = EXTI9_5_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
}
