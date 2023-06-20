#include "usart.h"
#include "cmd_queue.h"
//////////////////////////////////////////////////////////////////////////////////
// 如果使用os,则包括下面的头文件即可.
#if SYSTEM_SUPPORT_OS
#include "includes.h" //os 使用
#endif
//////////////////////////////////////////////////////////////////////////////////
// 本程序只供学习使用，未经作者许可，不得用于其它任何用途
// ALIENTEK STM32H7开发板
// 串口1初始化
// 正点原子@ALIENTEK
// 技术论坛:www.openedv.com
// 修改日期:2017/6/8
// 版本：V1.0
// 版权所有，盗版必究。
// Copyright(C) 广州市星翼电子科技有限公司 2009-2019
// All rights reserved
//********************************************************************************
// V1.0修改说明
//////////////////////////////////////////////////////////////////////////////////
// 加入以下代码,支持printf函数,而不需要选择use MicroLIB
// #define PUTCHAR_PROTOTYPE int fputc(int ch, FILE *f)
#if 1
// #pragma import(__use_no_semihosting)
// 标准库需要的支持函数
struct __FILE
{
	int handle;
};

FILE __stdout;
// 定义_sys_exit()以避免使用半主机模式
void _sys_exit(int x)
{
	x = x;
}
// 重定义fputc函数
int fputc(int ch, FILE *f)
{
	while ((USART1->ISR & 0X40) == 0)
		; // 循环发送,直到发送完毕
	USART1->TDR = (u8)ch;
	return ch;
}
#endif

// 串口1中断服务程序
// 注意,读取USARTx->SR能避免莫名其妙的错误
u8 USART_RX_BUF[USART_REC_LEN]; // 接收缓冲,最大USART_REC_LEN个字节.
// 接收状态
// bit15，	接收完成标志
// bit14，	接收到0x0d
// bit13~0，	接收到的有效字节数目
u16 USART_RX_STA = 0; // 接收状态标记

// u8 aRxBuffer[RXBUFFERSIZE];//HAL库使用的串口接收缓冲
UART_HandleTypeDef UART1_Handler; // UART句柄
UART_HandleTypeDef UART3_Handler;
//---------------------------------------------------------------------//
#define RX_MAX_COUNT 3000			   // 串口接收最大字节数
__IO u8 aRxBuffer[RX_MAX_COUNT] = {0}; // 接收缓冲区
__IO uint16_t RxCount = 0;			   // 已接收到的字节数
__IO uint8_t Frame_flag = 0;		   // 帧标志：1：一个新的数据帧  0：无数据帧

// 初始化IO 串口1
// bound:波特率
void uart_init(u32 bound)
{
	// UART 初始化设置
	UART1_Handler.Instance = USART1;					// USART1
	UART1_Handler.Init.BaudRate = bound;				// 波特率
	UART1_Handler.Init.WordLength = UART_WORDLENGTH_8B; // 字长为8位数据格式
	UART1_Handler.Init.StopBits = UART_STOPBITS_1;		// 一个停止位
	UART1_Handler.Init.Parity = UART_PARITY_NONE;		// 无奇偶校验位
	UART1_Handler.Init.HwFlowCtl = UART_HWCONTROL_NONE; // 无硬件流控
	UART1_Handler.Init.Mode = UART_MODE_TX_RX;			// 收发模式
	HAL_UART_Init(&UART1_Handler);						// HAL_UART_Init()会使能UART1

	// HAL_UART_Receive_IT(&UART1_Handler, (u8 *)aRxBuffer, RXBUFFERSIZE);//该函数会开启接收中断：标志位UART_IT_RXNE，并且设置接收缓冲以及接收缓冲接收最大数据量

	// 使用直接在中断函数方式
	__HAL_UART_ENABLE_IT(&UART1_Handler, USART_IT_RXNE);
	//-------------------------------------------------------------
	UART3_Handler.Instance = USART3;					// USART3
	UART3_Handler.Init.BaudRate = bound;				// 波特率
	UART3_Handler.Init.WordLength = UART_WORDLENGTH_8B; // 字长为8位数据格式
	UART3_Handler.Init.StopBits = UART_STOPBITS_1;		// 一个停止位
	UART3_Handler.Init.Parity = UART_PARITY_NONE;		// 无奇偶校验位
	UART3_Handler.Init.HwFlowCtl = UART_HWCONTROL_NONE; // 无硬件流控
	UART3_Handler.Init.Mode = UART_MODE_TX_RX;			// 收发模式
	HAL_UART_Init(&UART3_Handler);						// HAL_UART_Init()会使能UART1

	// HAL_UART_Receive_IT(&UART3_Handler, (u8 *)aRxBuffer, RXBUFFERSIZE);//该函数会开启接收中断：标志位UART_IT_RXNE，并且设置接收缓冲以及接收缓冲接收最大数据量

	// 使用直接在中断函数方式
	__HAL_UART_ENABLE_IT(&UART3_Handler, USART_IT_RXNE);
}

// UART底层初始化，时钟使能，引脚配置，中断配置
// 此函数会被HAL_UART_Init()调用
// huart:串口句柄

void HAL_UART_MspInit(UART_HandleTypeDef *huart)
{
	// GPIO端口设置
	GPIO_InitTypeDef GPIO_Initure;

	if (huart->Instance == USART1) // 如果是串口1，进行串口1 MSP初始化
	{
		__HAL_RCC_GPIOA_CLK_ENABLE();  // 使能GPIOA时钟
		__HAL_RCC_USART1_CLK_ENABLE(); // 使能USART1时钟

		GPIO_Initure.Pin = GPIO_PIN_9 | GPIO_PIN_10; // PA9 PA10
		GPIO_Initure.Mode = GPIO_MODE_AF_PP;		 // 复用推挽输出
		GPIO_Initure.Pull = GPIO_PULLUP;			 // 上拉
		GPIO_Initure.Speed = GPIO_SPEED_FREQ_HIGH;	 // 高速
		GPIO_Initure.Alternate = GPIO_AF7_USART1;	 // 复用为USART1
		HAL_GPIO_Init(GPIOA, &GPIO_Initure);		 // 初始化PA9

		HAL_NVIC_EnableIRQ(USART1_IRQn);		 // 使能USART1中断通道
		HAL_NVIC_SetPriority(USART1_IRQn, 0, 0); // 抢占优先级3，子优先级3
	}
	if (huart->Instance == USART3) /* 如果是串口3，进行串口3 MSP初始化 */
	{
		__HAL_RCC_USART3_CLK_ENABLE(); /* USART3 时钟使能 */
		__HAL_RCC_GPIOD_CLK_ENABLE();  // 使能GPIOD时钟

		GPIO_Initure.Pin = GPIO_PIN_8 | GPIO_PIN_9; /* TX引脚 */ /* RX引脚 */
		GPIO_Initure.Mode = GPIO_MODE_AF_PP;					 /* 复用推挽输出 */
		GPIO_Initure.Pull = GPIO_PULLUP;						 /* 上拉 */
		GPIO_Initure.Speed = GPIO_SPEED_FREQ_HIGH;				 /* 高速 */
		GPIO_Initure.Alternate = GPIO_AF7_USART3;				 /* 复用为USART */
		HAL_GPIO_Init(GPIOD, &GPIO_Initure);					 /* 初始化发送引脚 */

		HAL_NVIC_EnableIRQ(USART3_IRQn);		 /* 使能USART中断通道 */
		HAL_NVIC_SetPriority(USART3_IRQn, 0, 0); /* 抢占优先级3，子优先级3 */
	}
}

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
	//	if(huart->Instance==USART1)//如果是串口1
	//	{
	//		if((USART_RX_STA&0x8000)==0)//接收未完成
	//		{
	//			if(USART_RX_STA&0x4000)//接收到了0x0d
	//			{
	//				if(aRxBuffer[0]!=0x0a)USART_RX_STA=0;//接收错误,重新开始
	//				else USART_RX_STA|=0x8000;	//接收完成了
	//			}
	//			else //还没收到0X0D
	//			{
	//				if(aRxBuffer[0]==0x0d)USART_RX_STA|=0x4000;
	//				else
	//				{
	//					USART_RX_BUF[USART_RX_STA&0X3FFF]=aRxBuffer[0] ;
	//					USART_RX_STA++;
	//					if(USART_RX_STA>(USART_REC_LEN-1))USART_RX_STA=0;//接收数据错误,重新开始接收
	//				}
	//			}
	//		}

	//	}
}

////串口1中断服务程序
// void USART1_IRQHandler(void)
//{
//	u32 timeout=0;
//     u32 maxDelay=0x1FFFF;
// #if SYSTEM_SUPPORT_OS	 	//使用OS
//	OSIntEnter();
// #endif
//
//	HAL_UART_IRQHandler(&UART1_Handler);	//调用HAL库中断处理公用函数
//
//	timeout=0;
//     while (HAL_UART_GetState(&UART1_Handler)!=HAL_UART_STATE_READY)//等待就绪
//	{
//         timeout++;////超时处理
//         if(timeout>maxDelay) break;
//	}
//
//	timeout=0;
//	while(HAL_UART_Receive_IT(&UART1_Handler,(u8 *)aRxBuffer, RXBUFFERSIZE)!=HAL_OK)//一次处理完成之后，重新开启中断并设置RxXferCount为1
//	{
//         timeout++; //超时处理
//         if(timeout>maxDelay) break;
//	}

//}

/*下面代码我们直接把中断控制逻辑写在中断服务函数内部。*/
/*

//串口1中断服务程序
void USART1_IRQHandler(void)
{
	u8 Res;
#if SYSTEM_SUPPORT_OS	 	//使用OS
	OSIntEnter();
#endif
	if((__HAL_UART_GET_FLAG(&UART1_Handler,UART_FLAG_RXNE)!=RESET))  //接收中断(接收到的数据必须是0x0d 0x0a结尾)
	{
		HAL_UART_Receive(&UART1_Handler,&Res,1,1000);
		if((USART_RX_STA&0x8000)==0)//接收未完成
		{
			if(USART_RX_STA&0x4000)//接收到了0x0d
			{
				if(Res!=0x0a)USART_RX_STA=0;//接收错误,重新开始
				else USART_RX_STA|=0x8000;	//接收完成了
			}
			else //还没收到0X0D
			{
				if(Res==0x0d)USART_RX_STA|=0x4000;
				else
				{
					USART_RX_BUF[USART_RX_STA&0X3FFF]=Res ;
					USART_RX_STA++;
					if(USART_RX_STA>(USART_REC_LEN-1))USART_RX_STA=0;//接收数据错误,重新开始接收
				}
			}
		}
	}
	HAL_UART_IRQHandler(&UART1_Handler);

}
#endif
*/
//------------------------------------------------------------------------------

void USART1_IRQHandler(void)
{
	if (__HAL_USART_GET_FLAG(&UART1_Handler, USART_FLAG_RXNE) != RESET) // 接收中断：接收到数据
	{

		uint8_t data;
		data = READ_REG(UART1_Handler.Instance->RDR); // 读取数据
		if (RxCount == 0)							  // 如果是重新接收到数据帧，开启串口空闲中断
		{
			__HAL_UART_CLEAR_FLAG(&UART1_Handler, USART_FLAG_IDLE); // 清除空闲中断标志
			__HAL_UART_ENABLE_IT(&UART1_Handler, UART_IT_IDLE);		// 使能空闲中断
		}
		if (RxCount < RX_MAX_COUNT) // 判断接收缓冲区未满
		{
			aRxBuffer[RxCount] = data; // 保存数据
			RxCount++;				   // 增加接收字节数计数
		}
	}
	else if (__HAL_USART_GET_FLAG(&UART1_Handler, USART_FLAG_IDLE) != RESET) // 串口空闲中断
	{
		__HAL_UART_CLEAR_FLAG(&UART1_Handler, USART_FLAG_IDLE); // 清除空闲中断标志
		__HAL_UART_DISABLE_IT(&UART1_Handler, UART_IT_IDLE);	// 关闭空闲中断
		Frame_flag = 1;											// 数据帧置位，标识接收到一个完整数据帧
	}
}

void USART3_IRQHandler(void)
{
	if (__HAL_USART_GET_FLAG(&UART3_Handler, USART_FLAG_RXNE) != RESET) // 接收中断：接收到数据
	{

		uint8_t data;
		data = READ_REG(UART3_Handler.Instance->RDR); // 读取数据
		if (RxCount == 0)							  // 如果是重新接收到数据帧，开启串口空闲中断
		{
			__HAL_UART_CLEAR_FLAG(&UART3_Handler, USART_FLAG_IDLE); // 清除空闲中断标志
			__HAL_UART_ENABLE_IT(&UART3_Handler, UART_IT_IDLE);		// 使能空闲中断
		}
		if (RxCount < RX_MAX_COUNT) // 判断接收缓冲区未满
		{
			// aRxBuffer[RxCount]=data;  // 保存数据
			queue_push(data);
			RxCount++; // 增加接收字节数计数
		}
	}
	else if (__HAL_USART_GET_FLAG(&UART3_Handler, USART_FLAG_IDLE) != RESET) // 串口空闲中断
	{
		__HAL_UART_CLEAR_FLAG(&UART3_Handler, USART_FLAG_IDLE); // 清除空闲中断标志
		__HAL_UART_DISABLE_IT(&UART3_Handler, UART_IT_IDLE);	// 关闭空闲中断
		Frame_flag = 1;											// 数据帧置位，标识接收到一个完整数据帧
		RxCount = 0;
	}
}

//------------------------------------------------------------------------------

void USART1_Send(uint8_t *p, uint8_t len)
{
	HAL_UART_Transmit(&UART1_Handler, p, len, 1000);
	while (__HAL_UART_GET_FLAG(&UART1_Handler, UART_FLAG_TC) != SET)
		; // 等待发送结束
}

void USART3_Send(uint8_t *p, uint8_t len)
{
	HAL_UART_Transmit(&UART3_Handler, p, len, 1000);
	while (__HAL_UART_GET_FLAG(&UART3_Handler, UART_FLAG_TC) != SET)
		; // 等待发送结束
}
