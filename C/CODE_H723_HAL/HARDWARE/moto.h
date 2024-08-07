#ifndef __MOTO_H
#define __MOTO_H
#include <stdint.h>
#include "sys.h"
typedef struct
{
	//! What part of the speed ramp we are in.
	uint8_t run_state;
	//! Direction stepper motor should move.
	uint8_t dir;
	//! Peroid of next timer delay. At start this value set the accelration rate.
	int32_t step_delay;
	//! What step_pos to start decelaration
	uint32_t decel_start;

	uint32_t run_start;
	//! Sets deceleration rate.
	int32_t decel_val;
	//! Minimum time delay (max speed)
	int32_t min_delay;

	int32_t max_delay;
	//! Counter used when accelerateing/decelerateing to calculate step_delay.
	int32_t accel_count;
	////////////////////////////////////////////////////////
	uint8_t lock;
} SpeedRampData;

typedef struct
{
	uint8_t MotoDir; //
	uint8_t Mxf;	 //
	uint8_t MAccfactor;
	uint8_t MDelfactor;
	uint8_t Mmode;	 //
	uint8_t Maction; //
	uint8_t Mlevel;
	uint8_t Mstate; //
	uint8_t MID;
	uint8_t Mflag;
	uint8_t MotionStatus;

	uint16_t MotoRpmSpeed;
	uint16_t MdelayTime;

	uint32_t MotoPosCurr; //
	uint32_t MotoPosLast; //
	uint32_t MotoDis;	  //

	int32_t Mstep; //
	uint32_t Maccel;
	uint32_t Mdecel;
	uint32_t MotoSpeed; //

} Moto_Struct;

// 定义定时器预分频，定时器实际时钟频率为：MHz/（STEPMOTOR_TIMx_PRESCALER+1）
// #define STEPMOTOR_TIM_PRESCALER               5  // 步进电机驱动器细分设置为：   32  细分
#define STEPMOTOR_TIM_PRESCALER 9 					// 步进电机驱动器细分设置为：   16  细分
// #define STEPMOTOR_TIM_PRESCALER               19  // 步进电机驱动器细分设置为：   8  细分
// #define STEPMOTOR_TIM_PRESCALER               39  // 步进电机驱动器细分设置为：   4  细分
// #define STEPMOTOR_TIM_PRESCALER               79  // 步进电机驱动器细分设置为：   2  细分
// #define STEPMOTOR_TIM_PRESCALER               159 // 步进电机驱动器细分设置为：   1  细分
// 定义定时器周期，输出比较模式周期设置为0xFFFF
#define STEPMOTOR_TIM_PERIOD 0xFFFF

#define STOP                                  0 // 加减速曲线状态：停止
#define ACCEL                                 1 // 加减速曲线状态：加速阶段
#define DECEL                                 2 // 加减速曲线状态：减速阶段
#define RUN                                   3 // 加减速曲线状态：匀速阶段
#define T1_FREQ                               (SystemCoreClock/(STEPMOTOR_TIM_PRESCALER+1)) // 频率ft值
#define FSPR                                  200         //步进电机单圈步数
#define MICRO_STEP                            16          // 步进电机驱动器细分数
#define SPR                                   (FSPR*MICRO_STEP)   // 旋转一圈需要的脉冲数

// Maths constants
#define ALPHA 					((float)(2 * 3.14159 / SPR)) // α= 2*pi/spr///0.00196
#define A_T_x10 				((float)(10 * ALPHA * T1_FREQ))//9800000
#define T1_FREQ_148 			((float)((T1_FREQ * 0.676) / 10)) // 0.676为误差修正值
#define A_SQ 					((float)(2 * 100000 * ALPHA))
#define A_x200 					((float)(200 * ALPHA))//0.392

#define MOTONUM 16

#define Moto_For 1	// 正转
#define Moto_Back 0 // 反转

void TIM_SetCompare(uint8_t num, uint16_t val);
uint16_t TIM_GetCompare(uint8_t num);
void STEPMOTOR_AxisMoveRel(uint8_t num, int32_t step, uint32_t accel, uint32_t decel, uint32_t speed);

void MotoInitConfig(void);

void TIMControl(uint8_t num, uint8_t operation);
void Moto_Dir_Set(uint8_t num, uint8_t set);
uint8_t Moto_Dir_Get(uint8_t num);
void StopMotor(uint8_t num);
void STEPMOTOR_EN(void);

#endif
