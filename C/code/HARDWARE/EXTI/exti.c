#include "exti.h"
#include "delay.h" 
#include "pwm.h"
#include "time.h"
#include "moto.h"
#include "usart.h"
#include "DeviceVibration.h"

//////////////////////////////////////////////////////////////////////////////////	 
//������ֻ��ѧϰʹ�ã�δ���������ɣ��������������κ���;
//ALIENTEK STM32F407������
//�ⲿ�ж� ��������	   
//����ԭ��@ALIENTEK
//������̳:www.openedv.com
//��������:2014/5/4
//�汾��V1.0
//��Ȩ���У�����ؾ���
//Copyright(C) �������������ӿƼ����޹�˾ 2014-2024
//All rights reserved									  
////////////////////////////////////////////////////////////////////////////////// 


extern  Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];
/* ��ѯ���*/
void EXTI_Check(void)
{
	
			if(MSensor7 == 1){
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
									TIMxCH3OutControl(TIM1,0);
									
								}
				
			}
			if(MSensor10 == 1){
								if (Moto[9].Mstate == 2)
								{
									srd[9].run_state = DECEL;
								}
								else if (Moto[9].Mstate == 1)
								{
									srd[9].run_state = STOP;
								}
								else
								{
									srd[9].run_state = STOP;
									TIMxCH2OutControl(TIM2, 0);
								}
			}
			if(MSensor12 == 1){
								if (Moto[11].Mstate == 2)
								{
									srd[11].run_state = DECEL;
								}
								else if (Moto[11].Mstate == 1)
								{
									srd[11].run_state = STOP;
								}
								else
								{
									srd[11].run_state = STOP;
									TIMxCH1OutControl(TIM1, 0);
								}
			}
			if(MSensor13 == 1){
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
									TIMxCH1OutControl(TIM8, 0);
								}
			}
			if(MSensor15 == 1){
								if (Moto[14].Mstate == 2)
								{
									srd[14].run_state = DECEL;
								}
								else if (Moto[14].Mstate == 1)
								{
									srd[14].run_state = STOP;
								}
								else
								{
									srd[14].run_state = STOP;
									TIMxCH3OutControl(TIM8, 0);
								}
			}
			if(MSensor16 == 1){
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
									TIMxCH4OutControl(TIM8, 0);
								}
			}	
	
}

/*�жϻص�����   �жϵĹ�����Ӧ���� */
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

	EXTI_ClearITPendingBit(EXTI_Line1);//���LINE2�ϵ��жϱ�־λ 
	
}


void EXTI2_IRQHandler(void)
{	

	
  EXTI_ClearITPendingBit(EXTI_Line2);  //���LINE2�ϵ��жϱ�־λ   	
	GPIO_EXTI_Callback(GPIO_Pin_2);
}
void EXTI3_IRQHandler(void)
{	

	
  EXTI_ClearITPendingBit(EXTI_Line3);  //���LINE3�ϵ��жϱ�־λ   
	GPIO_EXTI_Callback(GPIO_Pin_3);	
}


void EXTI0_IRQHandler(void)
{

	
	EXTI_ClearITPendingBit(EXTI_Line0);//���LINE4�ϵ��жϱ�־λ  
}



void EXTI9_5_IRQHandler(void)
{
	
	if(EXTI_GetITStatus(EXTI_Line7) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line7);//���LINE7�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_7);
	}
		if(EXTI_GetITStatus(EXTI_Line8) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line8);//���LINE8�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_8);
	}

	
}
void EXTI15_10_IRQHandler(void)
{
	
		if(EXTI_GetITStatus(EXTI_Line10) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line10);//���LINE10�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_10);
	}
	
		if(EXTI_GetITStatus(EXTI_Line11) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line11);//���LINE11�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_11);
	}
	
		if(EXTI_GetITStatus(EXTI_Line12) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line12);//���LINE12�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_12);
	}
	
		if(EXTI_GetITStatus(EXTI_Line13) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line13);//���LINE13�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_13);
	}
	
		if(EXTI_GetITStatus(EXTI_Line14) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line14);//���LINE14�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_14);
	}
	
	if(EXTI_GetITStatus(EXTI_Line15) != RESET)
	{
		
		EXTI_ClearITPendingBit(EXTI_Line15);//���LINE15�ϵ��жϱ�־λ  
		GPIO_EXTI_Callback(GPIO_Pin_15);
	}

	
}

	   
//�ⲿ�жϳ�ʼ������



void EXTIX_Init(void)
{
	NVIC_InitTypeDef   NVIC_InitStructure;
	EXTI_InitTypeDef   EXTI_InitStructure;
	  
	GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA|RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOD|RCC_AHB1Periph_GPIOE, ENABLE);//ʹ��GPIOA,B D E ʱ��
 
	/*GPIOA*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8|GPIO_Pin_11|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
  GPIO_Init(GPIOA, &GPIO_InitStructure);//��ʼ��GPIO

	/*GPIOB*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3|GPIO_Pin_13|GPIO_Pin_14|GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
  GPIO_Init(GPIOB, &GPIO_InitStructure);//��ʼ��GPIO
	
	
	/*GPIOD*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_7|GPIO_Pin_10|GPIO_Pin_15; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
  GPIO_Init(GPIOD, &GPIO_InitStructure);//��ʼ��GPIO
	
	/*GPIOE*/
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_7|GPIO_Pin_8|GPIO_Pin_10|GPIO_Pin_12; 
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
  GPIO_Init(GPIOE, &GPIO_InitStructure);//��ʼ��GPIO
	
	/* Connect EXTI Line to GPIO *///���ӵ��ж���
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);//ʹ��SYSCFGʱ��
	
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
	
	
	/* ����EXTI_Line2 3 7 8 10 11 12 13 14 15 */
	EXTI_InitStructure.EXTI_Line =  EXTI_Line2 | EXTI_Line3|EXTI_Line7|EXTI_Line8 | EXTI_Line10|EXTI_Line11|EXTI_Line12 | EXTI_Line13|EXTI_Line14|EXTI_Line15;
  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//�ж��¼�
  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //�����ش���
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//�ж���ʹ��
  EXTI_Init(&EXTI_InitStructure);//����
	
		
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI0_IRQn;//�ⲿ�ж�
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
//  NVIC_Init(&NVIC_InitStructure);//����
//	
//	
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI1_IRQn;//�ⲿ�ж�
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
//  NVIC_Init(&NVIC_InitStructure);//����
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI2_IRQn;//�ⲿ�ж�
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI3_IRQn;//�ⲿ�ж�
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
	
//	NVIC_InitStructure.NVIC_IRQChannel = EXTI4_IRQn;//�ⲿ�ж�
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
//  NVIC_Init(&NVIC_InitStructure);//����
	
  NVIC_InitStructure.NVIC_IRQChannel = EXTI9_5_IRQn;//�ⲿ�ж�5
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����

  NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;//�ⲿ�ж�5
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
}


void EXTIX_Init_AJ(void)
{
	NVIC_InitTypeDef   NVIC_InitStructure;
	EXTI_InitTypeDef   EXTI_InitStructure;
	
	//KEY_Init(); //������Ӧ��IO�ڳ�ʼ��
  
	GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOE, ENABLE);//ʹ��GPIOA,GPIOEʱ��
 
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0|GPIO_Pin_1|GPIO_Pin_2; //KEY0 KEY1 KEY2��Ӧ����
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
  GPIO_Init(GPIOB, &GPIO_InitStructure);//��ʼ��GPIOE2,3,4

//	
//	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2|GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5; //KEY0 KEY1 KEY2��Ӧ����
//  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;//��ͨ����ģʽ
//  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100M
//  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//����
//  GPIO_Init(GPIOE, &GPIO_InitStructure);//��ʼ��GPIOE2,3,4
	
	
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);//ʹ��SYSCFGʱ��
	
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource0);//PE2 ���ӵ��ж���2
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource1);//PE3 ���ӵ��ж���3
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource2);//PE4 ���ӵ��ж���4
//	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOE, EXTI_PinSource5);//PE5���ӵ��ж���5
	
	/* ����EXTI_Line2,3,4 */
	EXTI_InitStructure.EXTI_Line =  EXTI_Line0 | EXTI_Line1|EXTI_Line2;
  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//�ж��¼�
  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //��ش��
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//�ж���ʹ��
  EXTI_Init(&EXTI_InitStructure);//����
	
//  EXTI_InitStructure.EXTI_Line =  EXTI_Line2 | EXTI_Line3|EXTI_Line4|EXTI_Line5;
//  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;//�ж��¼�
//  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising; //��ش��
//  EXTI_InitStructure.EXTI_LineCmd = ENABLE;//�ж���ʹ��
//  EXTI_Init(&EXTI_InitStructure);//����
//	
		
	NVIC_InitStructure.NVIC_IRQChannel = EXTI0_IRQn;//�ⲿ�ж�2
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�3
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI1_IRQn;//�ⲿ�ж�3
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�2
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x02;//�����ȼ�2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
	
	
	NVIC_InitStructure.NVIC_IRQChannel = EXTI2_IRQn;//�ⲿ�ж�4
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x00;//��ռ���ȼ�1
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x03;//�����ȼ�2
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
  NVIC_Init(&NVIC_InitStructure);//����
	
//  NVIC_InitStructure.NVIC_IRQChannel = EXTI9_5_IRQn;//�ⲿ�ж�5
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x02;//��ռ���ȼ�1
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x01;//�����ȼ�2
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;//ʹ���ⲿ�ж�ͨ��
//  NVIC_Init(&NVIC_InitStructure);//����
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


