#ifndef __ADC_H
#define __ADC_H	
#include "sys.h" 
//////////////////////////////////////////////////////////////////////////////////	 
						  
////////////////////////////////////////////////////////////////////////////////// 	 
 
#define TIMES_AVER      32   

void Adc_Init(void); 				//ADC通道初始化
u16  Get_Adc(u8 ch); 				//获得某个通道值 
u16 Get_Adc_Average(u8 ch,u8 times);//得到某个通道给定次数采样的平均值  

//u16 TableSearchTemperature123( int a[][77], u16 voltage, u16 n );
//extern u16 Table[3][77];
extern volatile u16 aADCDualConvertedValue[96][8];
extern volatile float TempNow[8];
extern volatile u8 TempLast[8];
extern volatile u16 ConvertedValue[96][8];

extern u16 TableAdc[8][79];

extern u16 TableDac[8][79];

void filter(void);

float  TableSearchTemperatures(u16 adcv,u8 idex);


u16 GetVoltageDACS(u16 temperature,u8 index);

//float  TableSearchTemperaturef2(u16 adcv);
//float  TableSearchTemperaturef3(u16 adcv);
//float  TableSearchTemperaturef4(u16 adcv);

extern u8 FlagDMA;

extern u16 BDDac[8];

extern u16 BDAdc[8];

extern float ad[8];


//extern u16 JRTime;
//extern u8 JRFlag;

void bsp_InitADC(void);


//u8 TableSearchTemperature(void);
float TableSearchTemperature(u8 jj,u16 adcv);

u16  filters(u8 k);


#endif 















