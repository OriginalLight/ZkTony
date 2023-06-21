#include "sys.h"
#include "delay.h"
#include "usart.h"
#include "timer.h"
#include "cmd_process.h"
#include "moto.h"
#include "string.h"
#include "cmd_queue.h"
#include "exti.h"

/* 外部晶振为8M
:plln=250,pllm=2,pllp=2,pllq=4.
//得到:Fvco=8*(250/2)=1000Mhz
//     Fsys=1000/2=500Mhz
//     Fq=1000/2=500Mhz
*/
extern COMM_EVENT DoComEvent;
extern uint16_t Cmd_Cnt;
extern uint8_t cmd_buffer[CMD_MAX_SIZE];

extern uint8_t Frame_flag;

int main(void)
{
	Cache_Enable();					// 打开L1-Cache
	HAL_Init();						// 初始化HAL库
	Stm32_Clock_Init(250, 2, 2, 4); // 250, 2, 2, 4 设置时钟,500Mhz  
	delay_init(500);				// 延时初始化
	uart_init(115200);				// 串口初始化

	EXTIX_Init();
	STEPMOTOR_TIMx_Init();
	TIM4_Init(5000 - 1, 125 - 1);

	while (1)
	{
		EXTI_Check();

		if (Frame_flag) // 接收到一个完整数据帧
		{
			uint8_t size = queue_find_cmd(cmd_buffer, CMD_MAX_SIZE);

			if (size > 0) //
			{
				Cmd_Cnt = size;
				CmdAnalysis(); // command Analysis
			}
		}

		if (DoComEvent != NO_COMEVENT)
		{
			CmdProcess(); // DO command
			DoComEvent = NO_COMEVENT;
		}
	}


}
