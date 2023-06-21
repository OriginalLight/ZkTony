#include "exti.h"
#include "moto.h"

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
            TIMControl(6, 0);
            srd[6].accel_count = 0;
            srd[6].step_delay = 0;
            srd[6].min_delay = 0;
            Moto[6].MotionStatus = STOP;
            srd[6].run_state = STOP;
        }
    }
    if (MSensor10 == 1)
    {
        if (Moto[9].MotoDir == Moto_Back)
        {
            TIMControl(9, 0);
            srd[9].accel_count = 0;
            srd[9].step_delay = 0;
            srd[9].min_delay = 0;
            Moto[9].MotionStatus = STOP;
            srd[9].run_state = STOP;
        }
    }
    if (MSensor12 == 1)
    {
        if (Moto[11].MotoDir == Moto_Back)
        {
            TIMControl(11, 0);
            srd[11].accel_count = 0;
            srd[11].step_delay = 0;
            srd[11].min_delay = 0;
            Moto[11].MotionStatus = STOP;
            srd[11].run_state = STOP;
        }
    }
    if (MSensor13 == 1)
    {
        if (Moto[12].MotoDir == Moto_Back)
        {
            TIMControl(12, 0);
            srd[12].accel_count = 0;
            srd[12].step_delay = 0;
            srd[12].min_delay = 0;
            Moto[12].MotionStatus = STOP;
            srd[12].run_state = STOP;
        }
    }
    if (MSensor15 == 1)
    {
        if (Moto[14].MotoDir == Moto_Back)
        {
            TIMControl(14, 0);
            srd[14].accel_count = 0;
            srd[14].step_delay = 0;
            srd[14].min_delay = 0;
            Moto[14].MotionStatus = STOP;
            srd[14].run_state = STOP;
        }
    }
    if (MSensor16 == 1)
    {
        if (Moto[15].MotoDir == Moto_Back)
        {
            TIMControl(15, 0);
            srd[15].accel_count = 0;
            srd[15].step_delay = 0;
            srd[15].min_delay = 0;
            Moto[15].MotionStatus = STOP;
            srd[15].run_state = STOP;
        }
    }
}

/*中断回调函数   中断的功能相应处理 */
void GPIO_EXTI_Callback(uint16_t GPIO_Pin)
{
    switch (GPIO_Pin)
    {
    case GPIO_PIN_2:
    {
        if (MSensor9 == 1)
        {
            if (Moto[8].MotoDir == Moto_Back)
            {
                TIMControl(8, 0);
                srd[8].accel_count = 0;
                srd[8].step_delay = 0;
                srd[8].min_delay = 0;
                Moto[8].MotionStatus = STOP;
                srd[8].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_3:
    {
        if (MSensor11 == 1)
        {

            if (Moto[10].MotoDir == Moto_Back)
            {
                TIMControl(10, 0);
                srd[10].accel_count = 0;
                srd[10].step_delay = 0;
                srd[10].min_delay = 0;
                Moto[10].MotionStatus = STOP;
                srd[10].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_7:
    {
        if (MSensor2 == 1)
        {
            if (Moto[1].MotoDir == Moto_Back)
            {
                TIMControl(1, 0);
                srd[1].accel_count = 0;
                srd[1].step_delay = 0;
                srd[1].min_delay = 0;
                Moto[1].MotionStatus = STOP;
                srd[1].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_8:
    {
        if (MSensor4 == 1)
        {
            if (Moto[3].MotoDir == Moto_Back)
            {
                TIMControl(3, 0);
                srd[3].accel_count = 0;
                srd[3].step_delay = 0;
                srd[3].min_delay = 0;
                Moto[3].MotionStatus = STOP;
                srd[3].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_10:
    {
        if (MSensor6 == 1)
        {
            if (Moto[5].MotoDir == Moto_Back)
            {
                TIMControl(5, 0);
                srd[5].accel_count = 0;
                srd[5].step_delay = 0;
                srd[5].min_delay = 0;
                Moto[5].MotionStatus = STOP;
                srd[5].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_11:
    {
        if (MSensor14 == 1)
        {
            if (Moto[13].MotoDir == Moto_Back)
            {
                TIMControl(13, 0);
                srd[13].accel_count = 0;
                srd[13].step_delay = 0;
                srd[13].min_delay = 0;
                Moto[13].MotionStatus = STOP;
                srd[13].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_12:
    {
        if (MSensor8 == 1)
        {
            if (Moto[7].MotoDir == Moto_Back)
            {
                TIMControl(7, 0);
                srd[7].accel_count = 0;
                srd[7].step_delay = 0;
                srd[7].min_delay = 0;
                Moto[7].MotionStatus = STOP;
                srd[7].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_13:
    {
        if (MSensor1 == 1)
        {
            if (Moto[0].MotoDir == Moto_Back)
            {
                TIMControl(0, 0);
                srd[0].accel_count = 0;
                srd[0].step_delay = 0;
                srd[0].min_delay = 0;
                Moto[0].MotionStatus = STOP;
                srd[0].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_14:
    {
        if (MSensor3 == 1)
        {
            if (Moto[2].MotoDir == Moto_Back)
            {
                TIMControl(2, 0);
                srd[2].accel_count = 0;
                srd[2].step_delay = 0;
                srd[2].min_delay = 0;
                Moto[2].MotionStatus = STOP;
                srd[2].run_state = STOP;
            }
        }
        break;
    }
    case GPIO_PIN_15:
    {
        if (MSensor5 == 1)
        {
            if (Moto[4].MotoDir == Moto_Back)
            {
                TIMControl(4, 0);
                srd[4].accel_count = 0;
                srd[4].step_delay = 0;
                srd[4].min_delay = 0;
                Moto[4].MotionStatus = STOP;
                srd[4].run_state = STOP;
            }
        }
        break;
    }
    }
}

void EXTI2_IRQHandler(void)
{

    __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_2); // 清除LINE 上的中断标志位
    GPIO_EXTI_Callback(GPIO_PIN_2);
}
void EXTI3_IRQHandler(void)
{
    __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_3); // 清除LINE 上的中断标志位
    GPIO_EXTI_Callback(GPIO_PIN_3);
}

void EXTI9_5_IRQHandler(void)
{

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_7) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_7); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_7);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_8) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_8); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_8);
    }
}
void EXTI15_10_IRQHandler(void)
{

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_10) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_10); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_10);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_11) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_11); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_11);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_12) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_12); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_12);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_13) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_13); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_13);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_14) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_14); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_14);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_15) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_15); // 清除LINE 上的中断标志位
        GPIO_EXTI_Callback(GPIO_PIN_15);
    }
}

// 外部中断初始化

void EXTIX_Init(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;

    __HAL_RCC_GPIOA_CLK_ENABLE(); // 开启GPIOA时钟
    __HAL_RCC_GPIOB_CLK_ENABLE(); // 开启GPIO时钟
    __HAL_RCC_GPIOD_CLK_ENABLE(); // 开启GPIO时钟
    __HAL_RCC_GPIOE_CLK_ENABLE(); // 开启GPIO时钟
    /*GPIOA*/
    GPIO_InitStructure.Pin = GPIO_PIN_8 | GPIO_PIN_11 | GPIO_PIN_12;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // 上升沿触发模式
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // 下拉
    HAL_GPIO_Init(GPIOA, &GPIO_InitStructure);     // 初始化GPIO

    /*GPIOB*/
    GPIO_InitStructure.Pin = GPIO_PIN_3 | GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // 上升沿触发模式
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // 下拉
    HAL_GPIO_Init(GPIOB, &GPIO_InitStructure);

    /*GPIOD*/
    GPIO_InitStructure.Pin = GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_7 | GPIO_PIN_10 | GPIO_PIN_15;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // 上升沿触发模式
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // 下拉
    HAL_GPIO_Init(GPIOD, &GPIO_InitStructure);

    /*GPIOE*/
    GPIO_InitStructure.Pin = GPIO_PIN_7 | GPIO_PIN_8 | GPIO_PIN_10 | GPIO_PIN_12;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // 上升沿触发模式
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // 下拉
    HAL_GPIO_Init(GPIOE, &GPIO_InitStructure);     //

    /* 配置EXTI_Line2 3 7 8 10 11 12 13 14 15 */

    HAL_NVIC_SetPriority(EXTI2_IRQn, 0, 1); /* 抢占0，子优先级1 */
    HAL_NVIC_EnableIRQ(EXTI2_IRQn);         /* 使能中断线 */

    HAL_NVIC_SetPriority(EXTI3_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI3_IRQn);

    HAL_NVIC_SetPriority(EXTI9_5_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI9_5_IRQn);

    HAL_NVIC_SetPriority(EXTI15_10_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI15_10_IRQn);
}
