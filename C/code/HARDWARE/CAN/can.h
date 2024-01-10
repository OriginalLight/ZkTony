#ifndef __CAN_H
#define __CAN_H	 
#include "sys.h"	    
//////////////////////////////////////////////////////////////////////////////////	 
//本程序只供学习使用，未经作者许可，不得用于其它任何用途
//ALIENTEK STM32F407开发板
//CAN驱动 代码	   
//正点原子@ALIENTEK
//技术论坛:www.openedv.com
//创建日期:2014/5/7
//版本：V1.0 
//版权所有，盗版必究。
//Copyright(C) 广州市星翼电子科技有限公司 2014-2024
//All rights reserved									  
////////////////////////////////////////////////////////////////////////////////// 	 
	  	
#define REC_LEN 8   //接收缓冲,最大64个字节

#define Max_Frame_Count 20 //一次性接收并存放10条帧指令

#define MASTER_ID  0xff
#define SLAVE_ID_1 0X01
#define SLAVE_ID_2 0X02
#define SLAVE_ID_3 0X03
#define SLAVE_ID_4 0X04
/* 0:不动作 1：动作完成 2： 错误 */
extern u8 slave_state_1;
extern u8 slave_state_2;
extern u8 slave_state_3;
extern u8 slave_state_4;

extern u8 Frame_Store[Max_Frame_Count][REC_LEN]; //帧缓存数组，最多可存放10条帧指令
extern u8   SeqNum_Frame_REC;  //帧缓存数组中目前帧存储位置
extern u8   SeqNum_Frame_Deal; //帧缓存数组中目前帧执行位置
	
//CAN1接收RX0中断使能
#define CAN1_RX0_INT_ENABLE	1		//0,不使能;1,使能.								    
										 							 				    
u8 CAN1_Mode_Init(u8 tsjw,u8 tbs2,u8 tbs1,u16 brp,u8 mode);//CAN初始化
 
u8 CAN1_Send_Msg(u8* msg,u8 len);						//发送数据
//u8 CAN1_Send_Msg_id(u8* msg,u8 len,u16 id);
u8 CAN1_Send_Msg_id(u8* msg,u16 id,u8 len);
u8 CAN1_Receive_Msg(u8 *buf);							//接收数据
void Command_Deal(u8 *s);
void Command_DealTest(u8 *s);
#endif

















