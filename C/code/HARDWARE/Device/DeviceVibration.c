#include "DeviceVibration.h"
#include "string_deal.h" 
#include "moto.h"
#include "math.h"
#include "usart.h"
#include "stdio.h"
#include "delay.h" 
#include "exti.h"
#include "adc.h"
#include "128S085.h"

u8 Flag_run = 0;               //记录仪器运行状态，0：停止；1：暂停；2：复位结束；3,9：准备就绪（接收到启动指令，且复位结束）；4：运行状态
//u8 RunFlag =0;
u16  CX_POS = 0;
 
void MovProcess(void)
{
  
	//增加Y轴是否复位检测；
	

	Send_Str(":s=1!\r\n");
	
//	RunFlag=1;
	
	// 风扇；
	// 风开；
	FUN  =1;
//	BAK  =1;
  MotorX0Mov(UP,800,xdelt);  //1mm？？？？？？？？？？？？？？？？？？？？？？？？
	MotorY1Mov(DOWN,V_Y1_DOWN,y1firstmov); 
	MotorY2Mov(DOWN,800,y2firstmov);	

//裂解   **********只保留第一步裂解的Delay(EEprom[3]); 其余步骤的Delay(EEprom[3]);全部去掉

//	Send_Str(WorkStatusBuf1);

	
	MotorY1Mov(DOWN,V_Y1_DOWN,FB_POS); 
	
	
	MotorY1Vib(EEprom[0],EEprom[1],EEprom[2]);       
	//磁棒振动0
	Delay(EEprom[3]);
	MotorX0Mov(UP,V_X0,DISTANCE_X0);
//	Send_Str_uart4(" liejie_end\r\n");

	//磁珠混匀
//	Send_Str_uart4(" cizhuhunyun_begining\r\n");
//	Send_Str(WorkStatusBuf2);
	Send_Str(":h=1!\r\n");
	MotorY1Mov(DOWN,V_Y1_DOWN,FB_POS);    //磁吸距离分为2部分-----1500  -----1000  5--6 深孔板的距离 与500 液体距离	
//	MotoY1Mov(Y1_up,800,(vibbottom1/(2.034*20))*1600);
	MotorY1Vib(EEprom[4],EEprom[5],EEprom[6]);                   //磁棒振动1
//	Delay(EEprom[3]);//
	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);
	//MotorY1Y2Mov(Y2Y1_up,EEprom[10],CX_POSDISTANCE_Y1Y2-DISTANCE_CX);
	Delay(EEprom[9]);          //???????????????????????
	MotorX0Mov(DOWN,V_X0,DISTANCE_X0);  ///????????????????
//  Send_Str_uart4(" cizhuhunyun_end\r\n");
	//结合
//	Send_Str_uart4(" jiehe_beging\r\n");
	//Send_Str(WorkStatusBuf3);
	Send_Str(":j=1!\r\n");
	/*----------------------------------------*/
   //MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS); 
   MotorY1Y2Mov(DOWN,7,FB_POS);
	 delay_ms(100);
	// MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
   //Delay(5);   ////////////////////////////
	 
	 MotorY2Mov(UP,V_Y2_UP,FB_POS);
	 
	 //	MotoY1Mov(Y1_up,800,(vibbottom2/(2.034*20))*1600);  //39.33136676499508
		
		
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
/*----------------------------------------*/	


  
	MotorY1Vib(EEprom[11],EEprom[12],EEprom[13]);                 //磁棒振动2
	
//	Delay(EEprom[3]);//
	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);
//	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(DOWN,V_Y2_DOWN,FB_POS-CX_POS);  

//	//开始磁吸
//	MotorY1Y2Mov(DOWN,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(UP,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(DOWN,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(UP,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(DOWN,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(UP,EEprom[10],CX_POS);
//	delay_ms(100);
//	MotorY1Y2Mov(UP,7,FB_POS-CX_POS);
	Delay(EEprom[16]);
	MotorX0Mov(UP,V_X0,DISTANCE_X0*2);   //x zhou budong 
//	Send_Str_uart4(" jiehe_end\r\n");
	
////	TC1 = 0;  增加关闭4路继电器
////	TC3 = 0;

//关闭裂解温度加热

//	Dac128S085_Vol(2000,7);
//	Dac128S085_Vol(2000,5);
//	Dac128S085_Vol(2000,3);
//	Dac128S085_Vol(2000,1);

//	Switch0 =1;

//	Switch2 =1;

//	Switch4 =1;

//	Switch6 =1;	  //关闭继电器加热托盘	




	// 洗涤1
//	Send_Str_uart4(" xidi1_begining\r\n");
//	Send_Str(WorkStatusBuf4);
   Send_Str(":w1=1!\r\n");
/*----------------------------------------*/
   MotorY1Y2Mov(DOWN,7,FB_POS);
   //Delay(5); 
	 
	 MotorY2Mov(UP,V_Y2_UP,FB_POS);
	// MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
/*----------------------------------------*/	
//	MotoY1Mov(Y1_up,800,(vibbottom3/(2.034*20))*1600);
	MotorY1Vib(EEprom[17],EEprom[18],EEprom[19]);	                //磁棒振动3
//	Delay(EEprom[3]);//	

	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);
//	Delay(EEprom[16]);


//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom3);
//	Delay(EEprom[20]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[21]);
	
	
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[22]);
	MotorX0Mov(UP,V_X0,DISTANCE_X0);
//	Send_Str_uart4(" xidi1_end\r\n");
	// 洗涤2
//	Send_Str_uart4(" xidi2_begining\r\n");
//	Send_Str(WorkStatusBuf5);
  Send_Str(":w2=1!\r\n");
/*----------------------------------------*/
   MotorY1Y2Mov(DOWN,7,FB_POS);
   //Delay(5); 
	 MotorY2Mov(UP,V_Y2_UP,FB_POS);
	// MotorY1Mov(DOWN,V_Y1_DOWN,SEPARATE_POS);
/*----------------------------------------*/	
//	MotoY1Mov(Y1_up,800,(vibbottom4/(2.034*20))*1600);
	MotorY1Vib(EEprom[23],EEprom[24],EEprom[25]);	               //磁棒振动4
	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);

//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom4);
//	Delay(EEprom[26]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[27]);
	
	
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[28]);
	MotorX0Mov(UP,V_X0,DISTANCE_X0);
//  Send_Str_uart4(" xidi2_end\r\n");
	//洗涤3
//	Send_Str_uart4(" xidi3_beginging\r\n");
//	Send_Str(WorkStatusBuf6);
   Send_Str(":w3=1!\r\n");
/*----------------------------------------*/
   MotorY1Y2Mov(DOWN,11,FB_POS);
   //Delay(5); 
	 MotorY2Mov(UP,V_Y2_UP,FB_POS);
	// MotorY1Mov(DOWN,V_Y1_DOWN,SEPARATE_POS);
/*----------------------------------------*/	
//	MotoY1Mov(Y1_up,800,(vibbottom5/(2.034*20))*1600);
	MotorY1Vib(EEprom[29],EEprom[30],EEprom[31]);                //磁棒振动5
	
//	Delay(EEprom[3]);//
//	Delay(EEprom[14]);  //磁珠吸附动作
	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);
	
	
	
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom5);
//	Delay(EEprom[32]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[33]);
	
//	FUN  =0;
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
	Delay(EEprom[34]);
	MotorX0Mov(UP,V_X0,DISTANCE_X0);
//  Send_Str_uart4(" xidi3_end\r\n");
	//洗脱
//	Send_Str_uart4(" xituo_begining\r\n");
//	Send_Str(WorkStatusBuf7);
	Send_Str(":t=1!\r\n");
/*----------------------------------------*/
   MotorY1Y2Mov(DOWN,11,FB_POS);
   //Delay(5); 
	 MotorY2Mov(UP,V_Y2_UP,FB_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
	 

	 
/*----------------------------------------*/	
//	MotoY1Mov(Y1_up,800,(vibbottom6/(2.034*20))*1600);
	MotorY1Vib(EEprom[35],EEprom[36],EEprom[37]);               //磁棒振动6
	
//	Delay(EEprom[3]);//

//	Delay(EEprom[14]);  //磁珠吸附动作
	MotorY2Mov(DOWN,V_Y2_DOWN,FB_POS);  

	//开始磁吸
	MotorY1Mov(UP,V_Y1_UP,FB_POS);    //更改 Y上去速度 
  MotorY1Y2Mov(DOWN,EEprom[10],FB_POS);
	MotorY1Y2Mov(UP,7,FB_POS);
	
	
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom6);
//	Delay(EEprom[38]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[39]);
	
	//FUN  =0;
	//MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
	Delay(EEprom[40]);
	MotorX0Mov(DOWN,V_X0,DISTANCE_X0<<2); //放到第二个工位，距离X4
//  Send_Str_uart4(" xituo_end\r\n");

////	TC2 = 0;  继续关闭其他几路
////	TC4 = 0;


	//磁珠释放
//	Send_Str_uart4(" cizhushifang_begining\r\n");
//	Send_Str(WorkStatusBuf8);
	Send_Str(":f=1!\r\n");
	MotorY1Y2Mov(DOWN,7,FB_POS);
	
	MotorY2Mov(DOWN,V_Y2_UP,FB_POS);

//	MotoY1Mov(Y1_up,800,(vibbottom7/(2.034*20))*1600);
	MotorY1Vib(EEprom[41],EEprom[42],EEprom[43]);              //磁棒振动7
	 
//	Delay(EEprom[3]);//
	
	MotorY1Mov(UP,V_Y1_UP,FB_POS);
//  Send_Str_uart4(" cizhushifang_end\r\n");
//	//结束

//	Y1Y2Reset();	
//	X0Reset();	

  delay_ms(10000);	//风扇关闭需要延时一定的时间
	FUN  =0;	

	OverAlarm();
	Send_Str(":o=1!\r\n");
	Flag_run=0;
	
//  RunFlag =0;
	NVIC_SystemReset();
//	MotorX0Mov(X_forward,800,xdelt);  //1mm？？？？？？？？？？？？？？？？？？？？？？？？
//	MotorY1Mov(Y1_down,V_Y1_DOWN,y1firstmov); 
//	MotorY2Mov(Y2_down,800,y2firstmov);	

////裂解   **********只保留第一步裂解的Delay(EEprom[3]); 其余步骤的Delay(EEprom[3]);全部去掉

////	Send_Str(WorkStatusBuf1);

//	
//	MotorY1Mov(Y1_down,V_Y1_DOWN,DISTANCE_Y1_DOWN); 
//	
//  MotorY1Mov(Y1_up,V_Y1_UP,Vibbottom0);
//	
//	
//	


//	
//	MotorY1Vib(EEprom[0],EEprom[1],EEprom[2]);       
//	//磁棒振动0
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom0);
//	
//	Delay(EEprom[3]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
////	Send_Str_uart4(" liejie_end\r\n");

//	//磁珠混匀
////	Send_Str_uart4(" cizhuhunyun_begining\r\n");
////	Send_Str(WorkStatusBuf2);
//	Send_Str(":h=1!\r\n");
//	MotorY1Mov(Y1_down,V_Y1_DOWN,DISTANCE_Y1_DOWN);    //磁吸距离分为2部分-----1500  -----1000  5--6 深孔板的距离 与500 液体距离
//	MotorY1Mov(Y1_up,800,Vibbottom1);			
////	MotoY1Mov(Y1_up,800,(vibbottom1/(2.034*20))*1600);
//	MotorY1Vib(EEprom[4],EEprom[5],EEprom[6]);                   //磁棒振动1
////	Delay(EEprom[3]);//
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom1);    //更改 Y上去速度 
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  
////	Delay(EEprom[7]);
//	
//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	//磁吸时间
//	//Delay(EEprom[8]);  //磁吸时间??????????????????????????????
//  FUN  =0;
//	
//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);
//	//MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2-DISTANCE_CX);
//	Delay(EEprom[9]);          //???????????????????????
//	MotorX0Mov(X_backward,V_X0,DISTANCE_X0);  ///????????????????
////  Send_Str_uart4(" cizhuhunyun_end\r\n");
//	//结合
////	Send_Str_uart4(" jiehe_beging\r\n");
//	//Send_Str(WorkStatusBuf3);
//	Send_Str(":j=1!\r\n");
//	/*----------------------------------------*/
//   //MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS); 
//   MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//	 delay_ms(100);
//	// MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//   //Delay(5); 
//	 FUN  =1;      ////////////////////////////
//	 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom2);
//	 
//	 //	MotoY1Mov(Y1_up,800,(vibbottom2/(2.034*20))*1600);  //39.33136676499508
//		
//		
////	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
///*----------------------------------------*/	


//  
//	MotorY1Vib(EEprom[11],EEprom[12],EEprom[13]);                 //磁棒振动2
//	
////	Delay(EEprom[3]);//
//		FUN  =0;
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom2);
////	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  

//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[15]);
//	

//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);
//	Delay(EEprom[16]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0*2);   //x zhou budong 
////	Send_Str_uart4(" jiehe_end\r\n");
//	
//////	TC1 = 0;  增加关闭4路继电器
//////	TC3 = 0;

////关闭裂解温度加热

////	Dac128S085_Vol(2000,7);
////	Dac128S085_Vol(2000,5);
////	Dac128S085_Vol(2000,3);
////	Dac128S085_Vol(2000,1);

////	Switch0 =1;

////	Switch2 =1;

////	Switch4 =1;

////	Switch6 =1;	  //关闭继电器加热托盘	




//	// 洗涤1
////	Send_Str_uart4(" xidi1_begining\r\n");
////	Send_Str(WorkStatusBuf4);
//   Send_Str(":w1=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//   //Delay(5); 
//	 
//	 FUN  =1;
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP);
//	// MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	  MotorY1Mov(Y1_up,800,Vibbottom3);
///*----------------------------------------*/	
////	MotoY1Mov(Y1_up,800,(vibbottom3/(2.034*20))*1600);
//	MotorY1Vib(EEprom[17],EEprom[18],EEprom[19]);	                //磁棒振动3
////	Delay(EEprom[3]);//	
//FUN  =0;
//  	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom3);
////	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  

//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[15]);
//	

//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);
////	Delay(EEprom[16]);


////	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom3);
////	Delay(EEprom[20]);
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[21]);
//	
//	
////	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[22]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
////	Send_Str_uart4(" xidi1_end\r\n");
//	// 洗涤2
////	Send_Str_uart4(" xidi2_begining\r\n");
////	Send_Str(WorkStatusBuf5);
//  Send_Str(":w2=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//   //Delay(5); 
//	 
//	 FUN  =	1;
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom4);
///*----------------------------------------*/	
////	MotoY1Mov(Y1_up,800,(vibbottom4/(2.034*20))*1600);
//	MotorY1Vib(EEprom[23],EEprom[24],EEprom[25]);	               //磁棒振动4
////	Delay(EEprom[3]);//	
//  FUN  =0;
//    	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom4);
////	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  

//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[15]);
//	

//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);

////	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom4);
////	Delay(EEprom[26]);
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[27]);
//	
//	
////	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[28]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
////  Send_Str_uart4(" xidi2_end\r\n");
//	//洗涤3
////	Send_Str_uart4(" xidi3_beginging\r\n");
////	Send_Str(WorkStatusBuf6);
//Send_Str(":w3=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//   //Delay(5); 
//	 FUN  =1;
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 MotorY1Mov(Y1_up,800,Vibbottom5);
///*----------------------------------------*/	
////	MotoY1Mov(Y1_up,800,(vibbottom5/(2.034*20))*1600);
//	MotorY1Vib(EEprom[29],EEprom[30],EEprom[31]);                //磁棒振动5
//	
////	Delay(EEprom[3]);//
//	  FUN  =0;
//  MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom5);
////	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  

//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[15]);
//	

//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);
//	
//	
//	
////	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom5);
////	Delay(EEprom[32]);
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[33]);
//	
////	FUN  =0;
////	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[34]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
////  Send_Str_uart4(" xidi3_end\r\n");
//	//洗脱
////	Send_Str_uart4(" xituo_begining\r\n");
////	Send_Str(WorkStatusBuf7);
//	Send_Str(":t=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//   //Delay(5); 
//	 FUN  =1;
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom6);
//	 
///*----------------------------------------*/	
////	MotoY1Mov(Y1_up,800,(vibbottom6/(2.034*20))*1600);
//	MotorY1Vib(EEprom[35],EEprom[36],EEprom[37]);               //磁棒振动6
//	
////	Delay(EEprom[3]);//
//	
//	  FUN  =0;
//    	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_CX-Vibbottom6);
////	Delay(EEprom[14]);  //磁珠吸附动作
//	MotorY2Mov(Y2_down,V_Y2_D,DISTANCE_Y2_UP-DISTANCE_CX);  

//	//开始磁吸
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_CX);
//	delay_ms(100);
//	
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[15]);
//	

//	MotorY1Y2Mov(Y2Y1_up,7,DISTANCE_Y1Y2-DISTANCE_CX);
//	
//	
////	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom6);
////	Delay(EEprom[38]);
////	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
////	Delay(EEprom[39]);
//	
//	//FUN  =0;
//	//MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[40]);
//	MotorX0Mov(X_backward,V_X0,DISTANCE_X0<<2); //放到第二个工位，距离X4
////  Send_Str_uart4(" xituo_end\r\n");

//////	TC2 = 0;  继续关闭其他几路
//////	TC4 = 0;







//	//磁珠释放
////	Send_Str_uart4(" cizhushifang_begining\r\n");
////	Send_Str(WorkStatusBuf8);
//	Send_Str(":f=1!\r\n");
//	MotorY1Y2Mov(Y2Y1_down,7,DISTANCE_Y1Y2);
//	
//	FUN  =1;
//	MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP);
//	 MotorY1Mov(Y1_up,800,Vibbottom7);
////	MotoY1Mov(Y1_up,800,(vibbottom7/(2.034*20))*1600);
//	MotorY1Vib(EEprom[41],EEprom[42],EEprom[43]);              //磁棒振动7
//	 
////	Delay(EEprom[3]);//
//	
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom7);
////  Send_Str_uart4(" cizhushifang_end\r\n");
////	//结束


////	Y1Y2Reset();	
////	X0Reset();	
//	

//	//Send_Str_uart4(" end 1\r\n");  1 3 5 7
//	Switch0 =1;
//	Switch1 =1;
//	Switch2 =1;
//	Switch3 =1;
//	Switch4 =1;
//	Switch5 =1;
//	Switch6 =1;
//	Switch7 =1;	  //关闭继电器加热托盘	
//	//Send_Str_uart4(" end 2\r\n");
//	
////	for(i=30000;i>0;i--);      //延时	

//  delay_ms(10000);	//风扇关闭需要延时一定的时间
//	FUN  =0;	
//	
//	
//	
//	OverAlarm();
//	Send_Str(":o=1!\r\n");
//	Flag_run=0;
//	
//  RunFlag =0;
//	NVIC_SystemReset();
	 
	 
	 
//运动偏移量
//    
//		MotorX0Mov(X_forward,800,xdelt);  //1mm？？？？？？？？？？？？？？？？？？？？？？？？
//		MotorY1Mov(Y1_down,V_Y1_DOWN,y1firstmov); 
//		MotorY2Mov(Y2_down,800,y2firstmov);	

////裂解

////	Send_Str(WorkStatusBuf1);

//	
//	MotorY1Mov(Y1_down,V_Y1_DOWN,DISTANCE_Y1_DOWN); 
//	
//  MotorY1Mov(Y1_up,V_Y1_UP,Vibbottom0);
//	
//	
//	


//	
//	MotorY1Vib(EEprom[0],EEprom[1],EEprom[2]);       
//	//磁棒振动0
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom0);
//	
//	Delay(EEprom[3]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
////	Send_Str_uart4(" liejie_end\r\n");

//	//磁珠混匀
////	Send_Str_uart4(" cizhuhunyun_begining\r\n");
////	Send_Str(WorkStatusBuf2);
//	Send_Str(":h=1!\r\n");
//	MotorY1Mov(Y1_down,V_Y1_DOWN,DISTANCE_Y1_DOWN);
//	
//	  MotorY1Mov(Y1_up,800,Vibbottom1);
//			
////	MotoY1Mov(Y1_up,800,(vibbottom1/(2.034*20))*1600);
//	MotorY1Vib(EEprom[4],EEprom[5],EEprom[6]);                   //磁棒振动1
////	Delay(EEprom[3]);//
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom1);
//	Delay(EEprom[7]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[8]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[9]);
//	MotorX0Mov(X_backward,V_X0,DISTANCE_X0);  ///????????????????
//	
//	
//	//加入判断是否打开裂解关闭状态按钮，如果关闭；
//	//关闭裂解温度加热
//  if(w8r)
//	{
//			Dac128S085_Vol(3700,7);
//			Dac128S085_Vol(3700,4);
//			Dac128S085_Vol(3700,3);
//			Dac128S085_Vol(3700,0);
//			Switch0 =1;
//			Switch2 =1;
//			Switch4 =1;
//			Switch6 =1;	  //关闭继电器加热托盘	
//  }
// 
//  //结合开始；
//	//Send_Str(WorkStatusBuf3);
//	Send_Str(":j=1!\r\n");
//	/*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS);
//   //Delay(5); 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom2);
//	
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
///*----------------------------------------*/	



//	MotorY1Vib(EEprom[11],EEprom[12],EEprom[13]);                 //磁棒振动2
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom2);
//	Delay(EEprom[14]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[15]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[16]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0*2);   //x zhou budong 
//	
//	// 洗涤1
//   Send_Str(":w1=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS);
//   //Delay(5); 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom3);
///*----------------------------------------*/	
//	MotorY1Vib(EEprom[17],EEprom[18],EEprom[19]);	                //磁棒振动3
////	Delay(EEprom[3]);//	
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom3);
//	Delay(EEprom[20]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[21]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[22]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
//	// 洗涤2
////	Send_Str(WorkStatusBuf5);
//  Send_Str(":w2=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS);
//   //Delay(5); 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom4);
///*----------------------------------------*/	
//	MotorY1Vib(EEprom[23],EEprom[24],EEprom[25]);	               //磁棒振动4
////	Delay(EEprom[3]);//	
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom4);
//	Delay(EEprom[26]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[27]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[28]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
//	//洗涤3
////	Send_Str(WorkStatusBuf6);
//  Send_Str(":w3=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS);
//   //Delay(5); 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 MotorY1Mov(Y1_up,800,Vibbottom5);
///*----------------------------------------*/	
//	MotorY1Vib(EEprom[29],EEprom[30],EEprom[31]);                //磁棒振动5
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom5);
//	Delay(EEprom[32]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[33]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[34]);
//	MotorX0Mov(X_forward,V_X0,DISTANCE_X0);
//	//洗脱
////	Send_Str(WorkStatusBuf7);
//	Send_Str(":t=1!\r\n");
///*----------------------------------------*/
//   MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2-SEPARATE_POS);
//   //Delay(5); 
//	 MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP-SEPARATE_POS);
//	 MotorY1Mov(Y1_down,V_Y1_DOWN,SEPARATE_POS);
//	 
//	 MotorY1Mov(Y1_up,800,Vibbottom6);
//	 
///*----------------------------------------*/	
//	MotorY1Vib(EEprom[35],EEprom[36],EEprom[37]);               //磁棒振动6
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom6);
//	Delay(EEprom[38]);
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[39]);
//	MotorY1Y2Mov(Y2Y1_up,EEprom[10],DISTANCE_Y1Y2);
//	Delay(EEprom[40]);
//	MotorX0Mov(X_backward,V_X0,DISTANCE_X0<<2); //放到第二个工位，距离X4
////  Send_Str_uart4(" xituo_end\r\n");

//////	TC2 = 0;  继续关闭其他几路
//////	TC4 = 0;







//	//磁珠释放
////	Send_Str_uart4(" cizhushifang_begining\r\n");
////	Send_Str(WorkStatusBuf8);
//	Send_Str(":f=1!\r\n");
//	MotorY1Y2Mov(Y2Y1_down,EEprom[10],DISTANCE_Y1Y2);
//	MotorY2Mov(Y2_up,V_Y2_UP,DISTANCE_Y2_UP);
//	MotorY1Mov(Y1_up,800,Vibbottom7);
////	MotoY1Mov(Y1_up,800,(vibbottom7/(2.034*20))*1600);
//	MotorY1Vib(EEprom[41],EEprom[42],EEprom[43]);              //磁棒振动7
//	 
////	Delay(EEprom[3]);//
//	MotorY1Mov(Y1_up,V_Y1_UP,DISTANCE_Y1_UP-Vibbottom7);
////  Send_Str_uart4(" cizhushifang_end\r\n");
////	//结束


////	Y1Y2Reset();	
////	X0Reset();	
//	

//	//Send_Str_uart4(" end 1\r\n");  1 3 5 7
//	Switch0 =1;
//	Switch1 =1;
//	Switch2 =1;
//	Switch3 =1;
//	Switch4 =1;
//	Switch5 =1;
//	Switch6 =1;
//	Switch7 =1;	  //关闭继电器加热托盘	
//	//Send_Str_uart4(" end 2\r\n");
//	


//  Delay(30);	//风扇关闭需要延时一定的时间
//	FUN  =0;	

//	OverAlarm();
//	Send_Str(":o=1!\r\n");
//	Flag_run=0;
//	
//  RunFlag =0;
//	NVIC_SystemReset();
	
	
	
	//实验结束 切换 到磁套上升  
	//风扇延时 60秒 复位；/8
	
}



void Delay(u16 t)
{
	u16 i;
	for(i=0;i<t;i++)
	{
	  delay_ms(1000);//ssss
	}
	
}


