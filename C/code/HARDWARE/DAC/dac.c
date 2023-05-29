#include "dac.h"
#include "adc.h"

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
 
//pb7 /synk
//pb6 din;




//DAC通道1输出初始化
void Dac1_Init(void)
{  
  GPIO_InitTypeDef  GPIO_InitStructure;
	DAC_InitTypeDef DAC_InitType;
	
  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);//使能GPIOA时钟
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_DAC, ENABLE);//使能DAC时钟
	   
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AN;//模拟输入
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
  GPIO_Init(GPIOA, &GPIO_InitStructure);//初始化

	DAC_InitType.DAC_Trigger=DAC_Trigger_None;	//不使用触发功能 TEN1=0
	DAC_InitType.DAC_WaveGeneration=DAC_WaveGeneration_None;//不使用波形发生
	DAC_InitType.DAC_LFSRUnmask_TriangleAmplitude=DAC_LFSRUnmask_Bit0;//屏蔽、幅值设置
	DAC_InitType.DAC_OutputBuffer=DAC_OutputBuffer_Enable ;	//DAC1输出缓存关闭 BOFF1=1
  DAC_Init(DAC_Channel_1,&DAC_InitType);	 //初始化DAC通道1

	DAC_Cmd(DAC_Channel_1, ENABLE);  //使能DAC通道1
  
  DAC_SetChannel1Data(DAC_Align_12b_R, 0);  //12位右对齐数据格式设置DAC值
}







//设置通道1输出电压
//vol:0~3300,代表0~3.3V
void Dac1_Set_Vol(u16 vol)
{
	double temp=vol;
	temp/=1000;
	temp=temp*4096/3.3;
	DAC_SetChannel1Data(DAC_Align_12b_R,temp);//12位右对齐数据格式设置DAC值
}


u16 GetVoltageDAC0(u16 temperature)
{
  u16 DacV=0;
	u8 i;
  //加热区间 40-99度；//31-102
	i=temperature-31;
	if(i<0)
	{
		
		
		DacV=2500;
		
		//err; 设置温度保护；
		
		return DacV;
		
		
	}
  else if(i>71)
	{
		i=71;
		//err ;over temp
		//DacV=2500; 
		
		//DacV=2500; 
		
    return TableDac[71];		
	}
	
	
	else return TableDac[i];		
	
	
}



//voltage = TableSearchVoltageDAC0( Table,EEpromJR[0],77 );			  //

//u16 TableSearchVoltageDAC0( u16 a[][60], u16 temperature, u16 n )
//{
//	int16 left = 0, right = n - 1, mid = 0;
//	mid = ( left + right ) / 2;
//	
//	while( left < right && a[0][mid] != temperature )
//	{
//		if( a[0][mid] < temperature )
//			left = mid + 1;
//		else if( a[0][mid] > temperature )
//			right = mid - 1;
//		mid = ( left + right ) / 2;
//	}
//	if( a[0][mid] == temperature )
//	{
//		return a[3][mid];
//	}
//	else
//	{
//		return -1;
//	}
	
//	u16 mid=0;
//	mid=temperature-29;
//	if(mid<0)
//	{
//		return -1;
//	}
//	else
//		return Table[2][mid];
//}




//u16 GetVoltageDAC0(u16 temperature)
//{
//  u16 DacV=0;
//	u8 i;
//  //加热区间 40-99度；//31-102
//	i=temperature-40;
//	if(i<0)
//	{
//		
//		
//		DacV=2500;
//		
//		//err; 设置温度保护；
//		
//		return DacV;
//		
//		
//	}
//  if(i>59)
//	{
//		i=60;
//		//err ;over temp
//		DacV=2500; 
//		
//		//DacV=2500; 
//		
//    return TableDac[59];		
//	}
//	
//	
//	return TableDac[i];		
//	
//	
//}




void TemValueSet()
{
	
	
}












































