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

/* ��ѯ���*/
void EXTI_Check(void)
{

    if (MSensor7 == 1)
    {
        if (Moto[6].MotoDir == Moto_Back)
        {
					StopMotor(6);
        }
    }
    if (MSensor10 == 1)
    {
        if (Moto[9].MotoDir == Moto_Back)
        {
            StopMotor(9);
        }
    }
    if (MSensor12 == 1)
    {
        if (Moto[11].MotoDir == Moto_Back)
        {
            StopMotor(11);
        }
    }
    if (MSensor13 == 1)
    {
        if (Moto[12].MotoDir == Moto_Back)
        {
            StopMotor(12);
        }
    }
    if (MSensor15 == 1)
    {
        if (Moto[14].MotoDir == Moto_Back)
        {
            StopMotor(14);
        }
    }
    if (MSensor16 == 1)
    {
        if (Moto[15].MotoDir == Moto_Back)
        {
            StopMotor(15);
        }
    }
}

/*�жϻص�����   �жϵĹ�����Ӧ���� */
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
                StopMotor(8);
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
                StopMotor(10);
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
                StopMotor(1);
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
                StopMotor(3);
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
                StopMotor(5);
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
                StopMotor(13);
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
                StopMotor(7);
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
               StopMotor(0);
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
                StopMotor(2);						
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
                StopMotor(4);
            }
        }
        break;
    }
    }
}

void EXTI2_IRQHandler(void)
{

    __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_2); // ���LINE �ϵ��жϱ�־λ
    GPIO_EXTI_Callback(GPIO_PIN_2);
}
void EXTI3_IRQHandler(void)
{
    __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_3); // ���LINE �ϵ��жϱ�־λ
    GPIO_EXTI_Callback(GPIO_PIN_3);
}

void EXTI9_5_IRQHandler(void)
{

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_7) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_7); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_7);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_8) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_8); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_8);
    }
}
void EXTI15_10_IRQHandler(void)
{

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_10) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_10); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_10);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_11) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_11); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_11);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_12) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_12); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_12);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_13) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_13); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_13);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_14) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_14); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_14);
    }

    if (__HAL_GPIO_EXTI_GET_IT(GPIO_PIN_15) != RESET)
    {
        __HAL_GPIO_EXTI_CLEAR_IT(GPIO_PIN_15); // ���LINE �ϵ��жϱ�־λ
        GPIO_EXTI_Callback(GPIO_PIN_15);
    }
}

// �ⲿ�жϳ�ʼ��

void EXTIX_Init(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;

    __HAL_RCC_GPIOA_CLK_ENABLE(); // ????GPIOA?��??
    __HAL_RCC_GPIOB_CLK_ENABLE(); // ????GPIO?��??
    __HAL_RCC_GPIOD_CLK_ENABLE(); // ????GPIO?��??
    __HAL_RCC_GPIOE_CLK_ENABLE(); // ????GPIO?��??
    /*GPIOA*/
    GPIO_InitStructure.Pin = GPIO_PIN_8 | GPIO_PIN_11 | GPIO_PIN_12;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // ??????????????
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // ????
    HAL_GPIO_Init(GPIOA, &GPIO_InitStructure);     // ??????GPIO

    /*GPIOB*/
    GPIO_InitStructure.Pin = GPIO_PIN_3 | GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // ??????????????
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // ????
    HAL_GPIO_Init(GPIOB, &GPIO_InitStructure);

    /*GPIOD*/
    GPIO_InitStructure.Pin = GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_7 | GPIO_PIN_10 | GPIO_PIN_15;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // ??????????????
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // ????
    HAL_GPIO_Init(GPIOD, &GPIO_InitStructure);

    /*GPIOE*/
    GPIO_InitStructure.Pin = GPIO_PIN_7 | GPIO_PIN_8 | GPIO_PIN_10 | GPIO_PIN_12;
    GPIO_InitStructure.Mode = GPIO_MODE_IT_RISING; // ??????????????
    GPIO_InitStructure.Pull = GPIO_PULLDOWN;       // ????
    HAL_GPIO_Init(GPIOE, &GPIO_InitStructure);     //

    /* ????EXTI_Line2 3 7 8 10 11 12 13 14 15 */

    HAL_NVIC_SetPriority(EXTI2_IRQn, 0, 1); /* ????0??��???????1 */
    HAL_NVIC_EnableIRQ(EXTI2_IRQn);         /* ?????????? */

    HAL_NVIC_SetPriority(EXTI3_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI3_IRQn);

    HAL_NVIC_SetPriority(EXTI9_5_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI9_5_IRQn);

    HAL_NVIC_SetPriority(EXTI15_10_IRQn, 0, 1);
    HAL_NVIC_EnableIRQ(EXTI15_10_IRQn);
}
