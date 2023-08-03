#include "sys.h"
#include "delay.h"
#include "usart.h"
#include "timer.h"
#include "cmd_process.h"
#include "moto.h"
#include "string.h"
#include "cmd_queue.h"
#include "exti.h"
#include "nmos.h"

/* �ⲿ����Ϊ8M
:plln=250,pllm=2,pllp=2,pllq=4.
//�õ�:Fvco=8*(250/2)=1000Mhz
//     Fsys=1000/2=500Mhz
//     Fq=1000/2=500Mhz
*/
extern COMM_EVENT DoComEvent;
extern uint16_t Cmd_Cnt;
extern uint8_t cmd_buffer[CMD_MAX_SIZE];

extern uint8_t Frame_flag;
extern AckPack pack[2];


int main(void)
{
	Cache_Enable();					// ��L1-Cache
	HAL_Init();						// ��ʼ��HAL��
	Stm32_Clock_Init(250, 2, 2, 4); // 250, 2, 2, 4 ����ʱ��,500Mhz
	delay_init(500);				// ��ʱ��ʼ��
	uart_init(115200);				// ���ڳ�ʼ��

	EXTIX_Init();
	STEPMOTOR_TIMx_Init();
	TIM4_Init(5000 - 1, 125 - 1);
	NMOS_Init();
	printf("test\n");

	while (1)
	{
		EXTI_Check();

		if (Frame_flag) // ���յ�һ����������֡
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

		if (1 == pack[0].flag)
		{
			USART3_Send(pack[0].data, pack[0].datalen);

			pack[0].flag = 0;
			memset(pack[0].data, 0, pack[0].datalen);
		}
		else if (1 == pack[1].flag)
		{
			USART3_Send(pack[1].data, pack[1].datalen);

			pack[1].flag = 0;
			memset(pack[1].data, 0, pack[1].datalen);
		}
	}
}
