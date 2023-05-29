 
#include "nmos.h"

/*
 * GPIO set
 NUM vlaue: 1 ~16
 value ：   0 low level
      :  1 high level
 */
void NMOS_Value_Set(u8 num,u8 val)
{
  switch (num)
  {
  case 1:
    if (val == 1)
    { 
      GPIO_WriteBit(GPIOA, GPIO_Pin_13, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOA, GPIO_Pin_13, Bit_RESET);
    }
    break;
  case 2:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_2, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_2, Bit_RESET);
    }
    break;
  case 3:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_10, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_10, Bit_RESET);
    }
    break;
  case 4:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_5, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_5, Bit_RESET);
    }
    break;
  case 5:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_11, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_11, Bit_RESET);
    }
    break;
  case 6:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_4, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_4, Bit_RESET);
    }
    break;
  case 7:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_12, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOB, GPIO_Pin_12, Bit_RESET);
    }
    break;
  case 8:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOA, GPIO_Pin_7, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOA, GPIO_Pin_7, Bit_RESET);
    }
    break;
  case 9:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOA, GPIO_Pin_15, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOA, GPIO_Pin_15, Bit_RESET);
    }
    break;
  case 10:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_14, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_14, Bit_RESET);
    }
    break;
  case 11:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_10, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_10, Bit_RESET);
    }
    break;
  case 12:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_13, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_13, Bit_RESET);
    }
    break;
  case 13:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_11, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_11, Bit_RESET);
    }
    break;
  case 14:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_12, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_12, Bit_RESET);
    }
    break;
  case 15:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_12, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOC, GPIO_Pin_12, Bit_RESET);
    }
    break;
  case 16:
    if (val == 1)
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_11, Bit_SET);
    }
    else
    {
      GPIO_WriteBit(GPIOD, GPIO_Pin_11, Bit_RESET);
    }
    break;
  }
}

void NMOS_Init(void)
{

	//NVIC_InitTypeDef   NVIC_InitStructure;
	//EXTI_InitTypeDef   EXTI_InitStructure;
	  
	GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA|RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOC|RCC_AHB1Periph_GPIOD|RCC_AHB1Periph_GPIOE, ENABLE);//使能GPIOA,B C D E 时钟
 
	/*GPIOA*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_7|GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOA, &GPIO_InitStructure);//初始化GPIO

	/*GPIOB*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2|GPIO_Pin_10|GPIO_Pin_11|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOB, &GPIO_InitStructure);//初始化GPIO
	
	
		/*GPIOC*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4|GPIO_Pin_5|GPIO_Pin_10|GPIO_Pin_11|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOB, &GPIO_InitStructure);//初始化GPIO
	
	/*GPIOD*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11|GPIO_Pin_12|GPIO_Pin_13|GPIO_Pin_14; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOD, &GPIO_InitStructure);//初始化GPIO
	
	/*GPIOE*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOE, &GPIO_InitStructure);//初始化GPIO
}
