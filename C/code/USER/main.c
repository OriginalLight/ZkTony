#include "sys.h"
#include "delay.h"
#include "usart.h"
#include "exti.h"
#include "timer.h"
#include "spi.h"
#include "pwm.h"
#include "moto.h"
#include "string.h"
#include "adc.h"
#include "can.h"
#include <math.h>
#include "hmi_driver.h"
#include "cmd_queue.h"
#include "cmd_process.h"
#include "string_deal.h"
#include "bsp_eeprom_24xx.h"
#include "bsp_i2c_gpio.h"
#include <stdlib.h>
#include "usart.h"
#include "SEGGER_RTT.h"

extern Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];
extern COMM_EVENT DoComEvent;
extern uint16 Cmd_Cnt;

uint8 cmd_DA[32] = {0x01, 0x01, 0xC2, 0x00, 0x01, 0x01, 0x01, 0x01, 0x10, 0x02, 0x58, 0x64, 0x64, 0x01, 0x00, 0x00, 0x10, 0x02, 0x58, 0x64, 0x64, 0x01, 0x00, 0x00, 0x10, 0x02, 0x58, 0x64, 0x64, 0x00, 0x00, 0x00};

// u8 canbuf[]={0x10,0x06};
extern uint8 cmd_buffer[CMD_MAX_SIZE]; // ָ���
extern uint16 revflag;
// extern uint16 USART_RX_STA;

int main(void)
{

	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); // ����ϵͳ�ж����ȼ�����2

	delay_init(168);
	uart_init(115200); // ��ʼ�����ڲ�����Ϊ115200

	// ��ʱ
	TIM_GPIO_Config(); // 168M ����

	MotoInitConfig(); // ����˶�������ʼ��

	//
	TIM8_PWM_Init(4000 - 1, 168 - 1);
	TIM2_PWM_Init(4000 - 1, 168 - 1);
	TIM3_PWM_Init(4000 - 1, 168 - 1);
	TIM1_PWM_Init(4000 - 1, 168 - 1);

	TIM4_Int_Init(999 - 1, 168 - 1);
	printf("test for usart1\n");
	while (1)
	{

		uint8 size = queue_find_cmd(cmd_buffer, CMD_MAX_SIZE);
		if (size > 0) // ���յ�ָ��
		{
			Cmd_Cnt = size;
			CmdAnalysis(); // ָ��� ����
		}

		if (DoComEvent != NO_COMEVENT)
		{
			CmdProcess();
			DoComEvent = NO_COMEVENT;
		}
	}
}
