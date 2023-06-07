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
extern uint8 cmd_buffer[CMD_MAX_SIZE]; //



int main(void)
{
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); // 设置系统中断优先级分组2

	delay_init(168);
	uart_init(115200); // init usart 


	TIM_GPIO_Config(); // 

	MotoInitConfig(); // 

	TIM8_PWM_Init();
	TIM2_PWM_Init();
	TIM3_PWM_Init();
	TIM1_PWM_Init();

	TIM4_Int_Init(999 - 1, 168 - 1);
	printf("test for usart1\n");
	while (1)
	{

		uint8 size = queue_find_cmd(cmd_buffer, CMD_MAX_SIZE);
		if (size > 0) // 
		{
			Cmd_Cnt = size;
			CmdAnalysis(); //command Analysis
		}

		if (DoComEvent != NO_COMEVENT)
		{
			CmdProcess();// DO command
			DoComEvent = NO_COMEVENT;
		}
	}
}
