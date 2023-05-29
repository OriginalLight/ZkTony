#include "exti.h"
#include "delay.h" 
#include "pwm.h"
#include "time.h"
#include "moto.h"
#include "usart.h"
#include "DeviceVibration.h"

//////////////////////////////////////////////////////////////////////////////////	 
//本程序只供学习使用，未经作者许可，不得用于其它任何用途
//ALIENTEK STM32F407开发板
//外部中断 驱动代码	   
//正点原子@ALIENTEK
//技术论坛:www.openedv.com
//创建日期:2014/5/4
//版本：V1.0
//版权所有，盗版必究。
//Copyright(C) 广州市星翼电子科技有限公司 2014-2024
//All rights reserved									  
////////////////////////////////////////////////////////////////////////////////// 


extern  Moto_Struct Moto[MOTONUM];
extern speedRampData srd[MOTONUM];
/* 轮询检测*/
void EXTI_Check(void)
{
	
			if(MSensor7 == 1){
								if(Moto[7].Mstate == 2)
								{
									srd[7].run_state = DECEL;
									
								}
								else if(Moto[7].Mstate == 1)
								{
									srd[7].run_state = STOP;
									
								}
								else
								{
									srd[7].run_state = STOP;
									TIMxCH3OutControl(TIM1,0);
									
								}
				
			}
			if(MSensor10 == 1){
								if (Moto[10].Mstate == 2)
								{
									srd[10].run_state = DECEL;
								}
								else if (Moto[10].Mstate == 1)
								{
									srd[10].run_state = STOP;
								}
								else
								{
									srd[10].run_state = STOP;
									TIMxCH2OutControl(TIM2, 0);
								}
			}
			if(MSensor12 == 1){
								if (Moto[12].Mstate == 2)
								{
									srd[12].run_state = DECEL;
								}
								else if (Moto[12].Mstate == 1)
								{
									srd[12].run_state = STOP;
								}
								else
								{
									srd[12].run_state = STOP;
									TIMxCH1OutControl(TIM1, 0);
								}
			}
			if(MSensor13 == 1){
								if (Moto[13].Mstate == 2)
								{
									srd[13].run_state = DECEL;
								}
								else if (Moto[13].Mstate == 1)
								{
									srd[13].run_state = STOP;
								}
								else
								{
									srd[13].run_state = STOP;
									TIMxCH1OutControl(TIM8, 0);
								}
			}
			if(MSensor15 == 1){
								if (Moto[15].Mstate == 2)
								{
									srd[15].run_state = DECEL;
								}
								else if (Moto[15].Mstate == 1)
								{
									srd[15].run_state = STOP;
								}
								else
								{
									srd[15].run_state = STOP;
									TIMxCH3OutControl(TIM8, 0);
								}
			}
			if(MSensor16 == 1){
								if (Moto[16].Mstate == 2)
								{
									srd[16].run_state = DECEL;
								}
								else if (Moto[16].Mstate == 1)
								{
									srd[16].run_state = STOP;
								}
								else
								{
									srd[16].run_state = STOP;
									TIMxCH4OutControl(TIM8, 0);
								}
			}	
	
}

/*中断回调函数   中断的功能相应处理 */
void GPIO_EXTI_Callback(uint16_t GPIO_Pin)
{
		switch(GPIO_Pin)
		{
				case GPIO_Pin_2:
				{
					if(MSensor9 == 1)
						{
								if(Moto[9].Mstate == 2)
								{
									srd[9].run_state = DECEL;
								}
								else if(Moto[9].Mstate == 1)
								{
									srd[9].run_state = STOP;
								}
								else
								{
									srd[9].run_state = STOP;
									TIMxCH1OutControl(TIM2,0);
								}
						}
				}
				case GPIO_Pin_3:
				{
					if(MSensor11 == 1)
						{
								if(Moto[11].Mstate == 2)
								{
									//srd[11].run_state = DECEL;
									srd[11].run_state = STOP;
									TIMxCH2OutControl(TIM3,0);
								}
								else if(Moto[11].Mstate == 1)
								{
									srd[11].run_state = STOP;
								}
								else
								{
									srd[11].run_state = STOP;
									TIMxCH2OutControl(TIM3,0);
								}
						}
				}
				case GPIO_Pin_7:
				{
					if(MSensor2 == 1)
						{
								if(Moto[2].Mstate == 2)
								{
									srd[2].run_state = DECEL;
								}
								else if(Moto[2].Mstate == 1)
								{
									srd[2].run_state = STOP;
								}
								else
								{
									srd[2].run_state = STOP;
									TIMxCH4OutControl(TIM1,0);
								}
						}
				}
				case GPIO_Pin_8:
				{
					if(MSensor4 == 1)
						{
								if(Moto[4].Mstate == 2)
								{
									srd[4].run_state = DECEL;
								}
								else if(Moto[4].Mstate == 1)
								{
									srd[4].run_state = STOP;
								}
								else
								{
									srd[4].run_state = STOP;
									TIMxCH1OutControl(TIM1,0);
								}
						}
				}	
				case GPIO_Pin_10:
				{
					if(MSensor6 == 1)
						{
								if(Moto[6].Mstate == 2)
								{
									srd[6].run_state = DECEL;
								}
								else if(Moto[6].Mstate == 1)
								{
									srd[6].run_state = STOP;
								}
								else
								{
									srd[6].run_state = STOP;
									TIMxCH3OutControl(TIM3,0);
								}
						}
				}
				case GPIO_Pin_11:
				{
					if(MSensor14 == 1)
						{
								if(Moto[14].Mstate == 2)
								{
									srd[14].run_state = DECEL;
								}
								else if(Moto[14].Mstate == 1)
								{
									srd[14].run_state = STOP;
								}
								else
								{
									srd[14].run_state = STOP;
									TIMxCH2OutControl(TIM8,0);
								}
						}
				}
				case GPIO_Pin_12:
				{
					if(MSensor8 == 1)
						{
								if(Moto[8].Mstate == 2)
								{
									srd[8].run_state = DECEL;
								}
								else if(Moto[8].Mstate == 1)
								{
									srd[8].run_state = STOP;
								}
								else
								{
									srd[8].run_state = STOP;
									TIMxCH3OutControl(TIM2,0);
								}
						}
				}
				case GPIO_Pin_13:
				{
						if(MSensor1 == 1)
						{
								if(Moto[1].Mstate == 2)
								{
									srd[1].run_state = DECEL;
								}
								else if(Moto[1].Mstate == 1)
								{
									srd[1].run_state = STOP;
								}
								else
								{
									srd[1].run_state = STOP;
									TIMxCH3OutControl(TIM1,0);
								}
						}
				}
				case GPIO_Pin_14:
				{
					if(MSensor3 == 1)
						{
								if(Moto[3].Mstate == 2)
								{
									srd[3].run_state = DECEL;
								}
								else if(Moto[3].Mstate == 1)
								{
									srd[3].run_state = STOP;
								}
								else
								{
									srd[3].run_state = STOP;
									TIMxCH2OutControl(TIM1,0);
								}
						}
				}
				case GPIO_Pin_15:
				{
					if(MSensor5 == 1)
						{
								if(Moto[5].Mstate == 2)
								{
									srd[5].run_state = DECEL;
								}
								else if(Moto[5].Mstate == 1)
								{
									srd[5].run_state = STOP;
								}
								else
								{
									srd[5].run_state = STOP;
									TIMxCH4OutControl(TIM3,0);
								}
						}
				}
						
		}
}


void EXTI1_IRQHandler(void)
{ 

	EXTI_ClearITPendingBit(EXTI_Line1);//清除LINE2上的中断标志位 
	
}


void EXTI2_IRQHandler(void)
{	

	
  EXTI_ClearITPendingBit(EXTI_Line2);  //清除LINE2上的中断标志位   	
	GPIO_EXTI_Callback(GPIO_Pin_2);
}
void EXTI3_IRQHandler(void)
{	

	
  EXTI_ClearITPendingBit(EXTI_Line3);  //清除LINE3上的中断标志位   
	GPIO_EXTI_Callback(GPIO_Pin_3);	
}


void EXTI0_IRQHandler(void)
{

	
	EXTI_ClearITPendingBit(EXTI_Line0);//清除LINE4上的中断标志位  
}



void EXTI9_5_IRQHandler(void)
{
	
	if(EXTI_GetITStatus(EXTI_Line7) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line7);//清除LINE7上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_7);
	}
		if(EXTI_GetITStatus(EXTI_Line8) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line8);//清除LINE8上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_8);
	}

	
}
void EXTI15_10_IRQHandler(void)
{
	
		if(EXTI_GetITStatus(EXTI_Line10) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line10);//清除LINE10上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_10);
	}
	
		if(EXTI_GetITStatus(EXTI_Line11) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line11);//清除LINE11上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_11);
	}
	
		if(EXTI_GetITStatus(EXTI_Line12) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line12);//清除LINE12上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_12);
	}
	
		if(EXTI_GetITStatus(EXTI_Line13) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line13);//清除LINE13上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_13);
	}
	
		if(EXTI_GetITStatus(EXTI_Line14) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line14);//清除LINE14上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_14);
	}
	
	if(EXTI_GetITStatus(EXTI_Line15) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line15);//清除LINE15上的中断标志位  
		GPIO_EXTI_Callback(GPIO_Pin_15);
	}

	
}

	   
//外部中断初始化程序



void EXTIX_Init(void)
{
	NVIC_InitTypeDef   NVIC_InitStructure;
	EXTI_InitTypeDef   EXTI_InitStructure;
	  
	GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA|RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOD|RCC_AHB1Periph_GPIOE, ENABLE);//使能GPIOA,B D E 时钟
 
	/*GPIOA*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8|GPIO_Pin_11|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOA, &GPIO_InitStructure);//初始化GPIO

	/*GPIOB*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3|GPIO_Pin_13|GPIO_Pin_14|GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOB, &GPIO_InitStructure);//初始化GPIO
	
	
	/*GPIOD*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_7|GPIO_Pin_10|GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOD, &GPIO_InitStructure);//初始化GPIO
	
	/*GPIOE*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_7|GPIO_Pin_8|GPIO_Pin_10|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOE, &GPIO_InitStructure);//初始化GPIO
	
	/* Connect EXTI Line to GPIO *///连接到中断线
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);//使能SYSCFG时钟
	
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
	EXTI_InitStructure.EXTI_Line =  EXTI_Line2 | EXTI_Line3|EXTI_Line7|EXTI_Line8 | EXTI_Line10|EXTI_Line11|EXTI_Line12 | EXTI_Line13|EXTI_Line14|EXTI_Line15;
  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//中断事件
  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //上升沿触发
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//中断线使能
  EXTI_Init(&EXTI_InitStructure);//配置
	
		
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI0_IRQn;//外部中断
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
//  NVIC_Init(&NVIC_InitStructure);//配置
//	
//	
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI1_IRQn;//外部中断
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
//  NVIC_Init(&NVIC_InitStructure);//配置
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI2_IRQn;//外部中断
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI3_IRQn;//外部中断
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
	
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI4_IRQn;//外部中断
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
//  NVIC_Init(&NVIC_InitStructure);//配置
	
  NVIC_InitStructure.NVIC_IRQChannel = EXTI9_5_IRQn;//外部中断5
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置

  NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;//外部中断5
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
}


void EXTIX_Init_AJ(void)
{
	NVIC_InitTypeDef   NVIC_InitStructure;
	EXTI_InitTypeDef   EXTI_InitStructure;
	
	//KEY_Init(); //按键对应的IO口初始化
  
	GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOE, ENABLE);//使能GPIOA,GPIOE时钟
 
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0|GPIO_Pin_1|GPIO_Pin_2; //KEY0 KEY1 KEY2对应引脚
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//上拉
  GPIO_Init(GPIOB, &GPIO_InitStructure);//初始化GPIOE2,3,4

//	
//	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5; //KEY0 KEY1 KEY2对应引脚
//  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//普通输入模式
//  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
//  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//上拉
//  GPIO_Init(GPIOE, &GPIO_InitStructure);//初始化GPIOE2,3,4
	
	
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);//使能SYSCFG时钟
	
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource0);//PE2 连接到中断线2
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource1);//PE3 连接到中断线3
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource2);//PE4 连接到中断线4
//	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource5);//PE5连接到中断线5
	
	/* 配置EXTI_Line2,3,4 */
	EXTI_InitStructure.EXTI_Line =  EXTI_Line0 | EXTI_Line1|EXTI_Line2;
  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//中断事件
  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //笛卮シ
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//中断线使能
  EXTI_Init(&EXTI_InitStructure);//配置
	
//  EXTI_InitStructure.EXTI_Line =  EXTI_Line2 | EXTI_Line3|EXTI_Line4|EXTI_Line5;
//  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//中断事件
//  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //笛卮シ
//  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//中断线使能
//  EXTI_Init(&EXTI_InitStructure);//配置
//	
		
	NVIC_InitStructure.NVIC_IRQChannel = EXTI0_IRQn;//外部中断2
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级3
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI1_IRQn;//外部中断3
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级2
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x02;//子优先级2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI2_IRQn;//外部中断4
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//抢占优先级1
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x03;//子优先级2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
  NVIC_Init(&NVIC_InitStructure);//配置
	
//  NVIC_InitStructure.NVIC_IRQChannel = EXTI9_5_IRQn;//外部中断5
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x02;//抢占优先级1
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//子优先级2
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//使能外部中断通道
//  NVIC_Init(&NVIC_InitStructure);//配置
//	   
}


void InitSwitch()
{
	
  GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD|RCC_AHB1Periph_GPIOE, ENABLE);//??GPIOF??

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0|GPIO_Pin_1|GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5|GPIO_Pin_6|GPIO_Pin_7|GPIO_Pin_8|GPIO_Pin_9|GPIO_Pin_10;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//??????
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//????
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//??
  GPIO_Init(GPIOD, &GPIO_InitStructure);//???
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//??????
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//????
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//??
  GPIO_Init(GPIOE, &GPIO_InitStructure);//???	

	
}


