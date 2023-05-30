#ifndef __MOTO_H
#define __MOTO_H
#include "sys.h"

typedef struct
{
	//! What part of the speed ramp we are in.
	u8 run_state;
	//! Direction stepper motor should move.
	u8 dir;
	//! Peroid of next timer delay. At start this value set the accelration rate.
	u32 step_delay;
	//! What step_pos to start decelaration
	u32 decel_start;

	u32 run_start;
	//! Sets deceleration rate.
	s32 decel_val;
	//! Minimum time delay (max speed)
	s32 min_delay;

	s32 max_delay;
	//! Counter used when accelerateing/decelerateing to calculate step_delay.
	s32 accel_count;
	////////////////////////////////////////////////////////
	u8 lock;
} SpeedRampData;

/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/
// #define T1_FREQ 2000000
#define T1_FREQ_2 1000000
// #define SPR 1000

// ���嶨ʱ��Ԥ��Ƶ����ʱ��ʵ��ʱ��Ƶ��Ϊ��168MHz/��STEPMOTOR_TIMx_PRESCALER+1��
// #define STEPMOTOR_TIM_PRESCALER               5  // �������������ϸ������Ϊ��   32  ϸ��
#define STEPMOTOR_TIM_PRESCALER 9 // �������������ϸ������Ϊ��   16  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               19  // �������������ϸ������Ϊ��   8  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               39  // �������������ϸ������Ϊ��   4  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               79  // �������������ϸ������Ϊ��   2  ϸ��
// #define STEPMOTOR_TIM_PRESCALER               159 // �������������ϸ������Ϊ��   1  ϸ��

#define STOP 0													  // �Ӽ�������״̬��ֹͣ
#define ACCEL 1													  // �Ӽ�������״̬�����ٽ׶�
#define DECEL 2													  // �Ӽ�������״̬�����ٽ׶�
#define RUN 3													  // �Ӽ�������״̬�����ٽ׶�
#define T1_FREQ (SystemCoreClock / (STEPMOTOR_TIM_PRESCALER + 1)) // Ƶ��ftֵ
#define FSPR 200												  // ���������Ȧ����
#define MICRO_STEP 32											  // �������������ϸ����
#define SPR (FSPR * MICRO_STEP)									  // ��תһȦ��Ҫ��������

// ��ѧ����
#define ALPHA ((float)(2 * 3.14159 / SPR)) // ��= 2*pi/spr
#define A_T_x10 ((float)(10 * ALPHA * T1_FREQ))
#define T1_FREQ_148 ((float)((T1_FREQ * 0.676) / 10)) // 0.676Ϊ�������ֵ
#define A_SQ ((float)(2 * 100000 * ALPHA))
#define A_x200 ((float)(2000 * ALPHA))

//// Maths constants. To simplify maths when calculating in AxisMoveRel().
// #define ALPHA (2*3.14159/SPR)                    // 2*pi/spr
// #define A_T_x100 ((long)(ALPHA*T1_FREQ*100))     // (ALPHA / T1_FREQ)*100
// #define T1_FREQ_148 ((int)((T1_FREQ*0.676)/100)) // divided by 100 and scaled by 0.676
// #define A_SQ (long)(ALPHA*2*100000*100000)         //
// #define A_x20000 (int)(ALPHA*20000)              // ALPHA*20000
//  Speed ramp states
#define STOP 0
#define ACCEL 1
#define DECEL 2
#define RUN 3

#define MOTONUM 16

#define Moto_For 1	// �����ת
#define Moto_Back 0 // �����ת

extern u8 M1ResFlag;
extern u8 M2ResFlag;
extern u8 M3ResFlag;
extern u8 M4ResFlag;
extern u8 M5ResFlag;
extern u8 M6ResFlag;
extern u8 M7ResFlag;
extern u8 M8ResFlag;
extern u8 M9ResFlag;
extern u8 M10ResFlag;
extern u8 M11ResFlag;
extern u8 M12ResFlag;
extern u8 M13ResFlag;
extern u8 M14ResFlag;
extern u8 M15ResFlag;
extern u8 M16ResFlag;

extern u8 Move_flag;
extern u8 Reset_flag;
extern u8 Pause_flag;
extern u8 Stop_flag;
extern u8 DataReady_flag;

extern u16 curr_control_id;
extern u16 curr_screen_id;

extern u8 curr_hole_totalcnt;
extern u8 curr_hole_cnt;

extern u16 BY_Dis[3];

extern u8 Moto_State; // 0 λ����  1 ��λ  2 �˶���  3 ��ͣ  4 �˶�ֹͣ

extern u8 Orgin_State;

extern u16 KwCnt;

#define FREQUENCY_ADD 100	// �Ӽ��ٵ�ÿ����Ҫ��Ƶ����
#define FREQUENCY_START 200 // ������Ƶ

#define DIVIDVALUE 2000;

extern u8 Para_Save[32];

typedef struct
{
	u8 MotoDir; //
	u8 Mxf;		// ϸ��
	u8 MAccfactor;
	u8 MDelfactor;
	u8 Mmode;	// ����˶�ģʽ
	u8 Maction; // �������
	u8 Mlevel;
	u8 Mstate; // ���״̬
	u8 MID;
	u8 Mflag;

	u16 MotoRpmSpeed;
	u16 MdelayTime;

	u16 MotoSpeed; //
	u16 Maccel;
	u16 Mdecel;

	u32 MotoPosCurr; // ��ǰλ��
	u32 MotoPosLast; // ��һ��λ��
	u32 MotoDis;	 // �������

	s32 Mstep; // ����

} Moto_Struct;

typedef struct
{
	u8 addr;   //
	u8 lamode; //
	u8 lbmode;
	u8 lcmode;
	u8 ldmode;
	u32 baud; // ϸ��
} Device_Struct;

extern Device_Struct DeviceA;
extern u8 cmd_DA[32];

// extern SpeedRampData srd1,srd2,srd3,srd4,srd5,srd6,srd7,srd8,srd9,srd10,srd11,srd12,srd13,srd14,srd15,srd16;

// extern Moto_Struct Moto1,Moto2,Moto3,Moto4,Moto5,Moto6,Moto7,Moto8,Moto9,Moto10,Moto[11],Moto12,Moto13,Moto14,Moto15,Moto16;

#define M1_EN PBout(6)
#define M1_DIR PCout(15) // DS0 ת��

#define M2_EN PBout(6)	// DS1
#define M2_DIR PCout(0) // DS0    Y1

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

#define XminSpeed 50
#define YminSpeed 50
#define ZminSpeed 50
#define AminSpeed 50
#define BminSpeed 50
#define CminSpeed 50
#define DminSpeed 50
#define EminSpeed 50

#define ACCNUM 200 // 160

#define ACC4NUM 800

#define ACC8NUM 1600

extern u32 QSACC[ACCNUM];
extern u32 WSACC[ACCNUM];
extern u32 ESACC[ACCNUM];
extern u32 RSACC[ACCNUM];
extern u32 TSACC[ACCNUM];
extern u32 YSACC[ACCNUM];
extern u32 USACC[ACCNUM];
extern u32 ISACC[ACCNUM];
extern u32 OSACC[ACCNUM];
extern u32 PSACC[ACCNUM];
extern u32 ASACC[ACCNUM];
extern u32 SSACC[ACCNUM];
extern u32 DSACC[ACCNUM];
extern u32 FSACC[ACCNUM];
extern u32 GSACC[ACCNUM];
extern u32 HSACC[ACCNUM];

void AxisMove(u32 num, s32 step, u32 accel, u32 decel, u32 speed);
void AxisMove1(u32 step, u32 accel, u32 decel, u32 speed);
void STEPMOTOR_AxisMoveRel(uint32_t num, int32_t step, uint32_t accel, uint32_t decel, uint32_t speed);
void CompareValue(u32 num, int32_t step);
void MotoInitConfig(void);
void AxisSpeed(u32 num, u32 accel, u32 speed);
void TIMControl(u8 num, u8 operation);
#endif
