#ifndef __128S085_H
#define __128S085_H	 
#include "sys.h"	     			    
//////////////////////////////////////////////////////////////////////////////////	 
//本程序只供学习使用，未经作者许可，不得用于其它任何用途
//ALIENTEK STM32F407开发板
//DAC 驱动代码	   
//正点原子@ALIENTEK
//技术论坛:www.openedv.com
//创建日期:2014/5/6
//版本：V1.0
//版权所有，盗版必究。
//Copyright(C) 广州市星翼电子科技有限公司 2014-2024
//All rights reserved 
////////////////////////////////////////////////////////////////////////////////// 	

#define	Dac128S085_CS 		PBout(7)  		//W25QXX的片选信号
#define	SCK 		PBout(3)  		//W25QXX的片选信号
#define	SDO 		PBout(5)  		//W25QXX的片选信号
//#define DAC_A_FULL   0x0FFC
//#define DAC_B_three_quarters   0x1C00
//#define	DAC_C_half 0x2800
//#define	DAC_D_one_quarter 0x3400
//#define DAC_E_FULL 	0x4FFC
//#define DAC_F_half 	0x5800
#define DAC_UPDATE 0xA0FF //8个通道




void Dac128S085_Init(void);		//DAC通道1初始化	 	 
void Dac128S085_Set_Vol(u16 vol,u8 chanle);	//设置通道1输出电压

void DacUpdate();

void TemSet();

void Dac128S_Init(void);
void Dac128S085_Vol(u16 vol,u16 chanle1);



//void TemValueSet();

//u16 TableSearchVoltageDAC0( u16 a[][60], u16 temperature, u16 n );


//u16 GetVoltageDAC0(u16 temperature);


#endif

















