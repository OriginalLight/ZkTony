#ifndef __DeviceVibration_H
#define __DeviceVibration_H	 
#include "sys.h"  	
//////////////////////////////////////////////////////////////////////////////////	 
//All rights reserved									  
////////////////////////////////////////////////////////////////////////////////// 	 
//#define J1  PDout(0)
//#define J2  PDout(1)
//#define J3  PDout(2)
//#define J4  PDout(3)
//#define J5  PDout(4)
//#define J6  PDout(5)
//#define J7  PDout(6)
//#define J8  PDout(7)



//2.0.1 修改了CX_OVER 从160---80


typedef struct {
	
	//void 
	u8 VSoftVerM;  //02
	u8 VSoftVerS;
	u8 VSoftVer;
	u32 VSoftTimer;   //03
	u8 VHardwareVerM;  //04
	u8 VHardwareVerS;  
	u8 VHardwareVer;
	u32 VHardwareTimer;  //05
	
	u16 VAdressID; //11
	u8 VMainID;
	u8 VSubID;
	u16 VAdressMask;
	
	u16 VBoudValue; //12
	
	u32 VPositionDe; //21
	u32 VPositionMem; //22
	
	u32 VCoderDe; //31
	u32 VCoderMem; //32
	u32 VSpeedDe; //33
	u32 VSpeedMem; //34

	u32 VibrationSpeed; //51
	u32 VibrationTime; //52

} DeviceVibration;



extern u8 Flag_run;

extern u8 Moto_State;  //0 位动作  1 复位  2 运动中  3 暂停  4 运动停止

//#define	DISTANCE_Y1_UP 2000  //3250
#define	V_Y1_UP 800

//#define	DISTANCE_Y2_UP 2000
#define	V_Y2_UP 5200


#define V_Y2_DOWN 800

//#define	DISTANCE_Y1_DOWN 2000
#define	V_Y1_DOWN 800
//#define	DISTANCE_Y1Y2 2000

#define UP 1
#define DOWN 0

#define V_Y1Y2_UP   11
#define V_Y1Y2_DOWN  11

extern u16  CX_POS ;  //18mm  //  720
#define FB_POS 2000  //18mm
#define CX_OVER 80 //24mm; ---800--20mm  //

//#define	DISTANCE_X0 3040

#define	DISTANCE_X0 7200    //9mm  
#define	V_X0 2400
//extern u8 Flag_run;


void Delay(u16 t);
//void MovProcess();

#endif
