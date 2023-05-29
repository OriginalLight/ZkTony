#include "128S085.h"
//#include "adc.h"
#include "spi.h"
#include "delay.h"

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
 

//DAC通道1输出初始化
//void Dac1_Init(void)
//{  
//GPIO_InitTypeDef  GPIO_InitStructure;
//DAC_InitTypeDef DAC_InitType;

//RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);//使能GPIOA时钟
//RCC_APB1PeriphClockCmd(RCC_APB1Periph_DAC, ENABLE);//使能DAC时钟
//	 
//GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4;
//GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AN;//模拟输入
//GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_DOWN;//下拉
//GPIO_Init(GPIOA, &GPIO_InitStructure);//初始化

//DAC_InitType.DAC_Trigger=DAC_Trigger_None;	//不使用触发功能 TEN1=0
//DAC_InitType.DAC_WaveGeneration=DAC_WaveGeneration_None;//不使用波形发生
//DAC_InitType.DAC_LFSRUnmask_TriangleAmplitude=DAC_LFSRUnmask_Bit0;//屏蔽、幅值设置
//DAC_InitType.DAC_OutputBuffer=DAC_OutputBuffer_Enable ;	//DAC1输出缓存关闭 BOFF1=1
//DAC_Init(DAC_Channel_1,&DAC_InitType);	 //初始化DAC通道1

//DAC_Cmd(DAC_Channel_1, ENABLE);  //使能DAC通道1

//DAC_SetChannel1Data(DAC_Align_12b_R, 0);  //12位右对齐数据格式设置DAC值
//}
//设置通道1输出电压
//vol:0~3300,代表0~3.3V
//void Dac1_Set_Vol(u16 vol)
//{
//	double temp=vol;
//	temp/=1000;
//	temp=temp*4096/3.3;
//	DAC_SetChannel1Data(DAC_Align_12b_R,temp);//12位右对齐数据格式设置DAC值
//}

void Dac128S085_Init(void)//DAC通道1初始化	 
{
	GPIO_InitTypeDef  GPIO_InitStructure;
  SPI_InitTypeDef  SPI_InitStructure;
	
  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);//使能GPIOA时钟
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_SPI1, ENABLE);//使能SPI1时钟
 
  //GPIOFB3,4,5初始化设置
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5;//PB3~5复用功能输出	
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//复用功能
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//推挽输出
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//上拉
  GPIO_Init(GPIOB, &GPIO_InitStructure);//初始化
//  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6|GPIO_Pin_7|GPIO_Pin_5;//PB3~5复用功能输出	
//  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//复用功能
//  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//推挽输出
//  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
//  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//上拉
//  GPIO_Init(GPIOA, &GPIO_InitStructure);//初始化
	
	GPIO_PinAFConfig(GPIOB,GPIO_PinSource3,GPIO_AF_SPI1); //PB3复用为 SPI1
	GPIO_PinAFConfig(GPIOB,GPIO_PinSource4,GPIO_AF_SPI1); //PB4复用为 SPI1
	GPIO_PinAFConfig(GPIOB,GPIO_PinSource5,GPIO_AF_SPI1); //PB5复用为 SPI1
 
	//这里只针对SPI口初始化
	RCC_APB2PeriphResetCmd(RCC_APB2Periph_SPI1,ENABLE);//复位SPI1
	RCC_APB2PeriphResetCmd(RCC_APB2Periph_SPI1,DISABLE);//停止复位SPI1

	SPI_InitStructure.SPI_Direction = SPI_Direction_1Line_Tx;  //设置SPI单向或者双向的数据模式:SPI设置为双线双向全双工
	SPI_InitStructure.SPI_Mode = SPI_Mode_Master;		//设置SPI工作模式:设置为主SPI
	SPI_InitStructure.SPI_DataSize = SPI_DataSize_16b;		//设置SPI的数据大小:SPI发送接收8位帧结构
	SPI_InitStructure.SPI_CPOL = SPI_CPOL_High;		//串行同步时钟的空闲状态为高电平
	SPI_InitStructure.SPI_CPHA = SPI_CPHA_2Edge;	//串行同步时钟的第二个跳变沿（上升或下降）数据被采样
	SPI_InitStructure.SPI_NSS = SPI_NSS_Soft;		//NSS信号由硬件（NSS管脚）还是软件（使用SSI位）管理:内部NSS信号有SSI位控制
	SPI_InitStructure.SPI_BaudRatePrescaler = SPI_BaudRatePrescaler_256;		//定义波特率预分频的值:波特率预分频值为256
	SPI_InitStructure.SPI_FirstBit = SPI_FirstBit_MSB;	//指定数据传输从MSB位还是LSB位开始:数据传输从MSB位开始
	SPI_InitStructure.SPI_CRCPolynomial = 7;	//CRC值计算的多项式
	SPI_Init(SPI1, &SPI_InitStructure);  //根据SPI_InitStruct中指定的参数初始化外设SPIx寄存器
 
	SPI_Cmd(SPI1, ENABLE); //使能SPI外设

//	SPI1_ReadWriteByte(0xff);//启动传输		 
	
	
  Dac128S085_CS=1;			//SPI FLASH不选中
//	SPI1_Init();		   			//初始化SPI
	//SPI1_SetSpeed(SPI_BaudRatePrescaler_8);		//设置为42/4=10.5 M时钟,高速模式 
	
}




void Dac128S_Init(void)
{
	  
  GPIO_InitTypeDef  GPIO_InitStructure;

  RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);//??GPIOF??

  //GPIOF9,F10?????
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3|GPIO_Pin_5|GPIO_Pin_7;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;//??????
  GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//????
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
  GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//??
  GPIO_Init(GPIOB, &GPIO_InitStructure);//???
	
	GPIO_SetBits(GPIOB,GPIO_Pin_7);//GPIOF9,F10???,??
	GPIO_SetBits(GPIOB,GPIO_Pin_5);//GPIOF9,F10???,??
	GPIO_ResetBits(GPIOB,GPIO_Pin_3);//GPIOF9,F10???,??
	
	Dac128S085_Vol(0xfff,9);       //设置adc128s085 为WTM模式
  //Dac128S085_Vol(0xfff,12);      //设置adc128s085的8个通道输出最高电压
	Dac128S085_Vol(3700,0);
	Dac128S085_Vol(3700,1);
	Dac128S085_Vol(3700,2);
	Dac128S085_Vol(3700,3);
	Dac128S085_Vol(3700,4);
	Dac128S085_Vol(3700,5);
	Dac128S085_Vol(3700,6);
	Dac128S085_Vol(3700,7);
	
	
	
}//DAC通道1初始化	 

//void Dac128S085_Vol(u16 vol,u8 chanle1)
//{
//	
// int i = 16;
// u16 data =0;

// data= vol+(chanle1<<12);
//	
// Dac128S085_CS=1;
// delay_us(1);
// Dac128S085_CS=0;
// 
// while(i--)        {
//	      SCK=1;
//	      if(data&0x8000)
//					SDO=1;
//				else
//					SDO=0;
//				   
//				delay_us(1);
//				SCK=0;
//				data <<= 1; 
//				delay_us(1);
//				
//}
//SCK=1; 
//Dac128S085_CS=1;
//delay_us(10);
//}

//中值滤波；
//u16 GetMedianNum(u16 * bArray, u8 iFilterLen)  
//{  
//    u8 i,j;// ?????  
//    u16 bTemp;  
//      
//    // ????
//    for (j = 0; j < iFilterLen - 1; j ++)  
//    {  
//        for (i = 0; i < iFilterLen - j - 1; i ++)  
//        {  
//            if (bArray > bArray[i + 1])  
//            {  
//                // ??
//                bTemp = bArray;  
//                bArray = bArray[i + 1];  
//                bArray[i + 1] = bTemp;  
//            }  
//        }  
//    }   
//    bTemp = bArray[(iFilterLen + 1) / 2];  


//    return bTemp;  
//}  
void Dac128S085_Vol(u16 vol,u16 chanle1)
{
	
 int i = 16;
 u16 data =0;
 data= vol+(chanle1<<12); 
 Dac128S085_CS=1;
 delay_us(2);
 Dac128S085_CS=0;
 
 while(i--)        
	 {
	      SCK=1;
	      if(data&0x8000)
					SDO=1;
				else
					SDO=0;
				   
				delay_us(2);
				SCK=0;
				data <<= 1; 
				delay_us(2);
				
}
SCK=1; 
Dac128S085_CS=1;
delay_us(20);
}



//0-7 左移12位 到达 
void Dac128S085_Set_Vol(u16 vol,u8 chanle1)
{
	
	  
   
  Dac128S085_CS=0;			//SPI FLASH不选中
	
	SPI_I2S_SendData(SPI1, vol); //通过外设SPIx发送一个byte  数据
	while (SPI_I2S_GetFlagStatus(SPI1, SPI_I2S_FLAG_TXE) == RESET){}//等待发送区空  
	
		
	Dac128S085_CS=1;			//SPI FLASH不选中
	
	
	
}


void DacUpdate()
{
	
	Dac128S085_CS=0;			//SPI FLASH不选中
	
	SPI_I2S_SendData(SPI1, 0xA0FF); //通过外设SPIx发送一个byte  数据
	while (SPI_I2S_GetFlagStatus(SPI1, SPI_I2S_FLAG_TXE) == RESET){}//等待发送区空  
	
		
	Dac128S085_CS=1;			//SPI FLASH不选中
	
	
	
}
	//设置通道1输出电压




//u16 GetVoltageDAC0(u16 temperature)
//{
//  u16 DacV=0;
//	u8 i;
//  //加热区间 40-99度；//31-102
//	i=temperature-31;
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
//  else if(i>71)
//	{
//		i=71;
//		//err ;over temp
//		//DacV=2500; 
//		
//		//DacV=2500; 
//		
//    return TableDac[71];		
//	}
//	
//	
//	else return TableDac[i];		
//	
//	
//}



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




void TemSet()
{
	
	
}












































