#ifndef __MOTO_H
#define __MOTO_H
#include "sys.h"

typedef struct
{
	//! What part of the speed ramp we are in.
	uint8_t run_state;
	//! Direction stepper motor should move.
	uint8_t dir;
	//! Peroid of next timer delay. At start this value set the accelration rate.
	uint32_t step_delay;
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

/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/
// #define T1_FREQ 2000000
//#define T1_FREQ_2 1000000
// #define SPR 1000

// ���嶨ʱ��Ԥ��Ƶ����ʱ��ʵ��ʱ��Ƶ��Ϊ��168MHz/��STEPMOTOR_TIMx_PRESCALER+1��
//  #define STEPMOTOR_TIM_PRESCALER               5  // �������������ϸ������Ϊ��   32  ϸ��
 #define STEPMOTOR_TIM_PRESCALER 9 					// �������������ϸ������Ϊ��   16  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               19  // �������������ϸ������Ϊ��   8  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               39  // �������������ϸ������Ϊ��   4  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               79  // �������������ϸ������Ϊ��   2  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               159 // �������������ϸ������Ϊ��   1  ϸ��

// ���嶨ʱ�����ڣ�����Ƚ�ģʽ��������Ϊ0xFFFF
#define STEPMOTOR_TIM_PERIOD                  0xFFFF

#define STOP 0													  // �Ӽ�������״̬��ֹͣ
#define ACCEL 1													  // �Ӽ�������״̬�����ٽ׶�
#define DECEL 2													  // �Ӽ�������״̬�����ٽ׶�
#define RUN 3													  // �Ӽ�������״̬�����ٽ׶�
#define T1_FREQ (SystemCoreClock / (STEPMOTOR_TIM_PRESCALER + 1)) // Ƶ��ftֵ
#define FSPR 200												  // ���������Ȧ����
#define MICRO_STEP 16											  // �������������ϸ����
#define SPR (FSPR * MICRO_STEP)									  // ��תһȦ��Ҫ��������

// ��ѧ����
#define ALPHA ((float)(2 * 3.14159 / SPR)) // ��= 2*pi/spr
#define A_T_x10 ((float)(10 * ALPHA * T1_FREQ))
#define T1_FREQ_148 ((float)((T1_FREQ * 0.676) / 10)) // 0.676Ϊ�������ֵ
#define A_SQ ((float)(2 * 100000 * ALPHA))
#define A_x200 ((float)(200 * ALPHA))

//// Maths constants. To simplify maths when calculating in AxisMoveRel().
// #define ALPHA (2*3.14159/SPR)                    // 2*pi/spr
// #define A_T_x100 ((long)(ALPHA*T1_FREQ*100))     // (ALPHA / T1_FREQ)*100
// #define T1_FREQ_148 ((int)((T1_FREQ*0.676)/100)) // divided by 100 and scaled by 0.676
// #define A_SQ (long)(ALPHA*2*100000*100000)         //
// #define A_x20000 (int)(ALPHA*20000)              // ALPHA*20000
//  Speed ramp states

// #define STOP 0
// #define ACCEL 1
// #define DECEL 2
// #define RUN 3

#define MOTONUM 16

#define Moto_For 1	// �����ת
#define Moto_Back 0 // �����ת

typedef struct
{
	uint8_t MotoDir; //
	uint8_t Mxf;		// ϸ��
	uint8_t MAccfactor;
	uint8_t MDelfactor;
	uint8_t Mmode;	// ����˶�ģʽ
	uint8_t Maction; // �������
	uint8_t Mlevel;
	uint8_t Mstate; // ���״̬
	uint8_t MID;
	uint8_t Mflag;
	uint8_t MotionStatus;

	uint16_t MotoRpmSpeed;
	uint16_t MdelayTime;

	uint16_t MotoSpeed; //
	uint16_t Maccel;
	uint16_t Mdecel;

	uint32_t MotoPosCurr; // ��ǰλ��
	uint32_t MotoPosLast; // ��һ��λ��
	uint32_t MotoDis;	 // �������

	int32_t Mstep; // ����

} Moto_Struct;

typedef struct
{
	uint8_t addr;   //
	uint8_t lamode; //
	uint8_t lbmode;
	uint8_t lcmode;
	uint8_t ldmode;
	uint32_t baud; // ϸ��
} Device_Struct;




#define M1_EN PBout(6)
#define M1_DIR PCout(15) // DS0 

#define M2_EN PBout(6)	// DS1
#define M2_DIR PCout(0) // DS0    

#define M3_EN PBout(6)	// DS1
#define M3_DIR PCout(1) // DS0

#define M4_EN PBout(6)	// DS1
#define M4_DIR PCout(2) // DS0

#define M5_EN PBout(7)	// DS1
#define M5_DIR PCout(3) // DS0

#define M6_EN PBout(7)	// DS1
#define M6_DIR PAout(4) // DS0

#define M7_EN PBout(7)	// DS1
#define M7_DIR PAout(5) // DS0

#define M8_EN PBout(7)	// DS1
#define M8_DIR PAout(6) // DS0

#define M9_EN PBout(6)	// DS1
#define M9_DIR PEout(1) // DS0

#define M10_EN PBout(6)	 // DS1
#define M10_DIR PEout(2) // DS0

#define M11_EN PBout(6)	 // DS1
#define M11_DIR PEout(3) // DS0

#define M12_EN PBout(6)	 // DS1
#define M12_DIR PEout(4) // DS0

#define M13_EN PBout(7)	 // DS1
#define M13_DIR PEout(5) // DS0

#define M14_EN PBout(8)	 // DS1
#define M14_DIR PEout(6) // DS0

#define M15_EN PBout(9)	  // DS1
#define M15_DIR PCout(13) // DS0

#define M16_EN PEout(0)	  // DS1
#define M16_DIR PCout(14) // DS0

void TIM_SetCompare(uint8_t num,uint16_t val);
uint16_t TIM_GetCompare(uint8_t num);
void STEPMOTOR_AxisMoveRel(uint8_t num, int32_t step, uint32_t accel, uint32_t decel, uint32_t speed);

void MotoInitConfig(void);

void TIMControl(uint8_t num, uint8_t operation);
#endif
