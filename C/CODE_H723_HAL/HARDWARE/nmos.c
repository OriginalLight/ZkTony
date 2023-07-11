#include "nmos.h"
#include "sys.h"
/*
 * GPIO set
 NUM vlaue: 0 ~15
 value :   0 low level
	  :  1 high level
 */
void NMOS_Value_Set(uint8_t num, uint8_t val)
{
	switch (num)
	{
	case 0:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_15, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_15, GPIO_PIN_RESET);
		}
		break;
	case 1:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_2, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_2, GPIO_PIN_RESET);
		}
		break;
	case 2:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_10, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_10, GPIO_PIN_RESET);
		}
		break;
	case 3:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_5, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_5, GPIO_PIN_RESET);
		}
		break;
	case 4:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_11, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_11, GPIO_PIN_RESET);
		}
		break;
	case 5:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_4, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_4, GPIO_PIN_RESET);
		}
		break;
	case 6:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_12, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOB, GPIO_PIN_12, GPIO_PIN_RESET);
		}
		break;
	case 7:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOA, GPIO_PIN_7, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOA, GPIO_PIN_7, GPIO_PIN_RESET);
		}
		break;
	case 8:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOA, GPIO_PIN_15, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOA, GPIO_PIN_15, GPIO_PIN_RESET);
		}
		break;
	case 9:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, GPIO_PIN_RESET);
		}
		break;
	case 10:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_10, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_10, GPIO_PIN_RESET);
		}
		break;
	case 11:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_RESET);
		}
		break;
	case 12:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_11, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_11, GPIO_PIN_RESET);
		}
		break;
	case 13:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_RESET);
		}
		break;
	case 14:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_12, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_12, GPIO_PIN_RESET);
		}
		break;
	case 15:
		if (val == 1)
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_11, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_11, GPIO_PIN_RESET);
		}
		break;
	}
}

void NMOS_Init(void)
{

	GPIO_InitTypeDef GPIO_InitStructure;

	__HAL_RCC_GPIOA_CLK_ENABLE(); // 开启GPIOA时钟
	__HAL_RCC_GPIOB_CLK_ENABLE(); // B
	__HAL_RCC_GPIOC_CLK_ENABLE(); // C
	__HAL_RCC_GPIOD_CLK_ENABLE(); // D
	__HAL_RCC_GPIOE_CLK_ENABLE(); // E

	/*GPIOA*/
	GPIO_InitStructure.Pin = GPIO_PIN_7 | GPIO_PIN_15;
	GPIO_InitStructure.Mode = GPIO_MODE_OUTPUT_PP;	 /* 推挽输出 */
	GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_HIGH; /* 高速 */
	GPIO_InitStructure.Pull = GPIO_PULLDOWN;		 /* 下拉 */
	HAL_GPIO_Init(GPIOA, &GPIO_InitStructure);		 // 初始化GPIO

	/*GPIOB*/
	GPIO_InitStructure.Pin = GPIO_PIN_2 | GPIO_PIN_10 | GPIO_PIN_11 | GPIO_PIN_12;
	GPIO_InitStructure.Mode = GPIO_MODE_OUTPUT_PP;	 
	GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_HIGH; 
	GPIO_InitStructure.Pull = GPIO_PULLDOWN;		 
	HAL_GPIO_Init(GPIOB, &GPIO_InitStructure);		 // 

	/*GPIOC*/
	GPIO_InitStructure.Pin = GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_10 | GPIO_PIN_11 | GPIO_PIN_12;
	GPIO_InitStructure.Mode = GPIO_MODE_OUTPUT_PP;	  
	GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_HIGH;   
	GPIO_InitStructure.Pull = GPIO_PULLDOWN;		  
	HAL_GPIO_Init(GPIOC, &GPIO_InitStructure);		 // 

	/*GPIOD*/
	GPIO_InitStructure.Pin = GPIO_PIN_11 | GPIO_PIN_12 | GPIO_PIN_13 | GPIO_PIN_14;
	GPIO_InitStructure.Mode = GPIO_MODE_OUTPUT_PP;	  
	GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_HIGH;  
	GPIO_InitStructure.Pull = GPIO_PULLDOWN;		  
	HAL_GPIO_Init(GPIOD, &GPIO_InitStructure);		 

	/*GPIOE*/
	GPIO_InitStructure.Pin = GPIO_PIN_15;
	GPIO_InitStructure.Mode = GPIO_MODE_OUTPUT_PP;	 
	GPIO_InitStructure.Speed = GPIO_SPEED_FREQ_HIGH; 
	GPIO_InitStructure.Pull = GPIO_PULLDOWN;		 
	HAL_GPIO_Init(GPIOE, &GPIO_InitStructure);		
}
