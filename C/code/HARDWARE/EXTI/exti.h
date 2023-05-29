#ifndef __EXTI_H
#define __EXIT_H	 
#include "sys.h"  	
//////////////////////////////////////////////////////////////////////////////////	 							  
////////////////////////////////////////////////////////////////////////////////// 	 

//sbit X_EN  = P7^2;
//sbit YB_EN  = P7^3;
//sbit YT_EN  = P7^4;
//sbit X_DIR  = P7^5;
//sbit YB_DIR  = P7^6;
//sbit YT_DIR  = P7^7;

//#define X_EN PAout(12)	// DS1	  X ,Y moto  enable io and  direction io 
//#define YB_EN PCout(9)	// DS0
//#define YT_EN PDout(14)	// DS1	 


//#define X_DIR PFout(11)	// DS0


//#define YB_DIR PCout(8)	// DS1	 
//#define YT_DIR PDout(15)	// DS0


//sbit CEX0=P0^2;   //MOTO PWM口 
//sbit CEX1=P0^3;
//sbit CEX2=P0^4;


//sbit BELL  = P3^0;
//sbit  FUN = P3^4;
//sbit  UV = P3^5;
//sbit  BAK = P3^6;

//#define XR 		GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_2) //PE2
//#define YR   GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_3)	//PE3 
//#define ZR   GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_4) //PE4   //一开始是低电平；


/* 轮询方式
IO口读取*/
#define MSensor7 		  GPIO_ReadInputDataBit(GPIOD,GPIO_Pin_10) 
#define MSensor10 		GPIO_ReadInputDataBit(GPIOD,GPIO_Pin_15)
#define MSensor12 		GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_8)
#define MSensor13 		GPIO_ReadInputDataBit(GPIOD,GPIO_Pin_7)
#define MSensor15 		GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_3)
#define MSensor16 		GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_12)

//中断处理
#define MSensor1 		  GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_13) 
#define MSensor2 			GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_7)
#define MSensor3 			GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_14)
#define MSensor4 			GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_8)
#define MSensor5 			GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_15)
#define MSensor6 			GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_10)
#define MSensor8 		  GPIO_ReadInputDataBit(GPIOE,GPIO_Pin_12) 
#define MSensor9 			GPIO_ReadInputDataBit(GPIOD,GPIO_Pin_2)
#define MSensor11 		GPIO_ReadInputDataBit(GPIOD,GPIO_Pin_3)
#define MSensor14 		GPIO_ReadInputDataBit(GPIOA,GPIO_Pin_11)





void EXTIX_Init(void);	//外部中断初始化		 		


void InitSwitch(void);
void EXTIX_Init_AJ(void);  

//#define Switch0 PDout(0)	 //
//#define Switch1 PDout(1)	 //
//#define Switch2 PDout(2)	 //
//#define Switch3 PDout(3)	 //
//#define Switch4 PDout(4)	 //
//#define Switch5 PDout(5)	 //
//#define Switch6 PDout(6)	 //
//#define Switch7 PDout(7)	 //



#define Switch0 PDout(7)	 //
#define Switch1 PDout(6)	 //
#define Switch2 PDout(4)	 //
#define Switch3 PDout(5)	 //
#define Switch4 PDout(3)	 //
#define Switch5 PDout(2)	 //
#define Switch6 PDout(0)	 //
#define Switch7 PDout(1)	 //




void GPIO_EXTI_Callback(uint16_t GPIO_Pin);
void EXTI_Check(void);



#endif

























