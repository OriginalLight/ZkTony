#include "sys.h"
#include "usart.h"	
#include "exti.h"
#include "cmd_queue.h"
#include "stm32f4xx.h" 
#include "delay.h"
#include "cmd_process.h"
////////////////////////////////////////////////////////////////////////////////// 	 
//如果使用ucos,则包括下面的头文件即可.
#if SYSTEM_SUPPORT_UCOS
#include "includes.h"					
#endif
//////////////////////////////////////////////////////////////////////////////////	 
//V1.3修改说明 
//支持适应不同频率下的串口波特率设置.
//加入了对printf的支持
//增加了串口接收命令功能.
//修正了printf第一个字符丢失的bug
//V1.4修改说明
//1,修改串口初始化IO的bug
//2,修改了USART_RX_STA,使得串口最大接收字节数为2的14次方
//3,增加了USART_REC_LEN,用于定义串口最大允许接收的字节数(不大于2的14次方)
//4,修改了EN_USART1_RX的使能方式
//V1.5修改说明
//1,增加了对UCOSII的支持
////////////////////////////////////////////////////////////////////////////////// 	  
 

//////////////////////////////////////////////////////////////////
//加入以下代码,支持printf函数,而不需要选择use MicroLIB	  
#if 1
#pragma import(__use_no_semihosting)             
//标准库需要的支持函数                 
struct __FILE 
{ 
	int handle; 
}; 

FILE __stdout;       
//定义_sys_exit()以避免使用半主机模式    
void _sys_exit(int x) 
{ 
	x = x; 
} 
//重定义fputc函数 
int fputc(int ch, FILE *f)
{ 	
	while((USART1->SR&0X40)==0);//循环发送,直到发送完毕   
	USART1->DR = (u8) ch;    
//  while((USART1->SR&0X40)==0);  
	return ch;
}
#endif
 
#if EN_USART1_RX   //如果使能了接收
uint16 revflag = 0;
uint32 cmdstate = 0;

uint8 buffer[CMD_MAX_SIZE];
extern uint8 cmd_buffer[CMD_MAX_SIZE];//指令缓存
extern uint16 Cmd_Cnt;
//extern uint8 _PACKLENGTH;


//初始化IO 串口
//bound:波特率
void uart_init(u32 bound){
   //GPIO端口设置
	GPIO_InitTypeDef GPIO_InitStructure;
	USART_InitTypeDef USART_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;

	//////USART1 初始化////////////////////////
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA,ENABLE); //使能GPIOa时钟
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1,ENABLE);//使能USART时钟
 
	//串口对应引脚复用映射
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource9,GPIO_AF_USART1); //GPIOD复用为USART
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource10,GPIO_AF_USART1); //


	//USART端口配置
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9 | GPIO_Pin_10; // GPIOA9与GPIOA10
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;			// 复用功能
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		// 速度50MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;			// 推挽复用输出
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;			// 上拉
	GPIO_Init(GPIOA, &GPIO_InitStructure);					// 初始化PA9，PA10

	// USART 初始化设置
	USART_InitStructure.USART_BaudRate = bound;										// 波特率设置
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;						// 字长为8位数据格式
	USART_InitStructure.USART_StopBits = USART_StopBits_1;							// 一个停止位
	USART_InitStructure.USART_Parity = USART_Parity_No;								// 无奇偶校验位
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None; // 无硬件数据流控制
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;					// 收发模式
	USART_Init(USART1, &USART_InitStructure);										// 初始化串口

	USART_Cmd(USART1, ENABLE); // 使能串口1

	USART_ClearFlag(USART1, USART_FLAG_TC);

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////USART3 初始化////////////////////////
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);  // 使能GPIOD时钟
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE); // 使能USART时钟

	// 串口对应引脚复用映射
	GPIO_PinAFConfig(GPIOD, GPIO_PinSource9, GPIO_AF_USART3); // GPIOD复用为USART
	GPIO_PinAFConfig(GPIOD, GPIO_PinSource8, GPIO_AF_USART3); //

	// USART端口配置
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8 | GPIO_Pin_9; // GPIO
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;		   // 复用功能
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;	   // 速度50MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;		   // 推挽复用输出
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;		   // 上拉
	GPIO_Init(GPIOD, &GPIO_InitStructure);				   // 初始化
	

	// USART 初始化设置
	USART_InitStructure.USART_BaudRate = bound;										// 波特率设置
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;						// 字长为8位数据格式
	USART_InitStructure.USART_StopBits = USART_StopBits_1;							// 一个停止位
	USART_InitStructure.USART_Parity = USART_Parity_No;								// 无奇偶校验位
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None; // 无硬件数据流控制
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;					// 收发模式
	USART_Init(USART3, &USART_InitStructure);										// 初始化串口
	

	//USART_ClearFlag(USART3, USART_FLAG_TC);

	USART_Cmd(USART3, ENABLE); // 使能串口3

	

#if EN_USART1_RX	
//////////////////////////usart1 中断配置//////////////////////////////////////
	USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);//开启相关中断

	//Usart NVIC 配置
  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;//串口中断通道
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=0;//抢占优先级3
	NVIC_InitStructure.NVIC_IRQChannelSubPriority =1;		//子优先级3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQ通道使能
	NVIC_Init(&NVIC_InitStructure);	//根据指定的参数初始化VIC寄存器

	//////////////////////////usart3 中断配置//////////////////////////////////////
	USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);//开启相关中断

	//Usart NVIC 配置
  NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;//串口中断通道
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=0;//抢占优先级3
	NVIC_InitStructure.NVIC_IRQChannelSubPriority =1;		//子优先级3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQ通道使能
	NVIC_Init(&NVIC_InitStructure);	//根据指定的参数初始化VIC寄存器
	
	

#endif


}



void USART1_IRQHandler(void)                	//串口1中断服务程序
{
	//u8 Res;
	if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)  //接收中断
	{
//		 Res =USART_ReceiveData(USART1);//(USART1->DR);	//读取接收到的数据，同时硬件自动清除中断
//     //queue_push(Res);
//			cmd_buffer[Cmd_Cnt++] = Res;
  } 
	SendChar(USART_ReceiveData(USART1));
} 

void USART3_IRQHandler(void)                	//串口3中断服务程序
{
	u8 Res;
	if(USART_GetITStatus(USART3, USART_IT_RXNE) != RESET)  //接收中断()
	{
			
			//buffer[Cmd_Cnt] = USART_ReceiveData(USART3); //(USART3->DR);	//读取接收到的数据，同时硬件自动清除中断
			Res = USART_ReceiveData(USART3); //(USART3->DR);


			  queue_push(Res);
			  
			
  } 
	if(USART_GetFlagStatus(USART3,USART_FLAG_ORE) != RESET) //溢出 
     { 
            USART_ClearFlag(USART3,USART_FLAG_ORE); //读SR 
            USART_ReceiveData(USART3); //读DR 
     }

	//SendChar_USART3(Res);
}  
		

void SendChar(unsigned char c)
 {

	    while(USART_GetFlagStatus(USART1,USART_FLAG_TXE)==RESET); //等待上一次发送结束
	 
      USART_SendData(USART1,c); //发送数据	   
   	  //while(USART_GetFlagStatus(USART1,USART_FLAG_TC)==RESET); //等待发送结束	
	
	 
 }

 void SendChar_USART3(unsigned char c)
 {

	    while(USART_GetFlagStatus(USART3,USART_FLAG_TXE)==RESET); //等待上一次发送结束
	 
      USART_SendData(USART3,c); //发送数据	   
   	  //while(USART_GetFlagStatus(USART3,USART_FLAG_TC)==RESET); //等待发送结束	
	
	 
 }

void Send_Str(unsigned char* str)
 {
	 while((*str) != '\0')
	{
		SendChar(*str);
		str++;
	}
 } 
 
 
void SendData(char *s,u8 len)
{
	u8 t;
	for(t=0;t<len;t++)
  {
		 //SendChar(s[t]);
		 SendChar_USART3(s[t]);

  }	
	
}

 #endif	


