#ifndef __TIMER_H
#define __TIMER_H
#include "sys.h"
//////////////////////////////////////////////////////////////////////////////////	 

//********************************************************************************
//修改说明
//V1.1 20140504
//新增TIM14_PWM_Init函数,用于PWM输出
//V1.2 20140504
//新增TIM5_CH1_Cap_Init函数,用于输入捕获
////////////////////////////////////////////////////////////////////////////////// 	

/*变量名*/


/*函数*/
void TIM3_Int_Init(u16 arr,u16 psc);
void ENC_Init(void);
u32 TIM2_Encoder_Read(void);
void Tim2Config(void);
void TIM4_Int_Init(u16 arr, u16 psc);

#endif























