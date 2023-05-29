#include "can.h"
#include "delay.h"
#include "usart.h"
#include "moto.h"
#include "string.h"
//////////////////////////////////////////////////////////////////////////////////	 
//±¾³ÌĞòÖ»¹©Ñ§Ï°Ê¹ÓÃ£¬Î´¾­×÷ÕßĞí¿É£¬²»µÃÓÃÓÚÆäËüÈÎºÎÓÃÍ¾
//ALIENTEK STM32F407¿ª·¢°å
//CANÇı¶¯ ´úÂë	   
//ÕıµãÔ­×Ó@ALIENTEK
//¼¼ÊõÂÛÌ³:www.openedv.com
//´´½¨ÈÕÆÚ:2014/5/7
//°æ±¾£ºV1.0 
//°æÈ¨ËùÓĞ£¬µÁ°æ±Ø¾¿¡£
//Copyright(C) ¹ãÖİÊĞĞÇÒíµç×Ó¿Æ¼¼ÓĞÏŞ¹«Ë¾ 2014-2024
//All rights reserved									  
////////////////////////////////////////////////////////////////////////////////// 	 

//CAN³õÊ¼»¯
//tsjw:ÖØĞÂÍ¬²½ÌøÔ¾Ê±¼äµ¥Ôª. @ref CAN_synchronisation_jump_width   ·¶Î§: ; CAN_SJW_1tq~ CAN_SJW_4tq
//tbs2:Ê±¼ä¶Î2µÄÊ±¼äµ¥Ôª.   @ref CAN_time_quantum_in_bit_segment_2 ·¶Î§:CAN_BS2_1tq~CAN_BS2_8tq;
//tbs1:Ê±¼ä¶Î1µÄÊ±¼äµ¥Ôª.   @refCAN_time_quantum_in_bit_segment_1  ·¶Î§: ;	  CAN_BS1_1tq ~CAN_BS1_16tq
//brp :²¨ÌØÂÊ·ÖÆµÆ÷.·¶Î§:1~1024;(Êµ¼ÊÒª¼Ó1,Ò²¾ÍÊÇ1~1024) tq=(brp)*tpclk1
//²¨ÌØÂÊ=Fpclk1/((tsjw+tbs1+tbs2+3)*brp);
//mode: @ref CAN_operating_mode ·¶Î§£ºCAN_Mode_Normal,ÆÕÍ¨Ä£Ê½;CAN_Mode_LoopBack,»Ø»·Ä£Ê½;
//Fpclk1µÄÊ±ÖÓÔÚ³õÊ¼»¯µÄÊ±ºòÉèÖÃÎª36M,Èç¹ûÉèÖÃCAN_Normal_Init(CAN_SJW_1tq,CAN_BS2_6tq,CAN_BS1_7tq,6,CAN_Mode_LoopBack);
//Ôò²¨ÌØÂÊÎª:42M/((1+6+7)*6)=500Kbps
//·µ»ØÖµ:0,³õÊ¼»¯OK;
//    ÆäËû,³õÊ¼»¯Ê§°Ü;
//u8 Frame_Store[Max_Frame_Count][REC_LEN]; //Ö¡»º´æÊı×é£¬×î¶à¿É´æ·Å10ÌõÖ¡Ö¸Áî

u8 SeqNum_Frame_REC=0;  //Ö¡»º´æÊı×éÖĞÄ¿Ç°Ö¡´æ´¢Î»ÖÃ
u8 SeqNum_Frame_Deal=0; //Ö¡»º´æÊı×éÖĞÄ¿Ç°Ö¡Ö´ĞĞÎ»Ö

u8 slave_state_1 =0;
u8 slave_state_2 =0;
u8 slave_state_3 =0;
u8 slave_state_4 =0;

u8 CAN1_Mode_Init(u8 tsjw,u8 tbs2,u8 tbs1,u16 brp,u8 mode)
{

  	GPIO_InitTypeDef GPIO_InitStructure; 
	  CAN_InitTypeDef        CAN_InitStructure;
  	CAN_FilterInitTypeDef  CAN_FilterInitStructure;
#if CAN1_RX0_INT_ENABLE 
   	NVIC_InitTypeDef  NVIC_InitStructure;
#endif
    //Ê¹ÄÜÏà¹ØÊ±ÖÓ
//    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);//Ê¹ÄÜPORTAÊ±ÖÓ	                   											 
//   // RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);//Ê¹ÄÜPORTAÊ±ÖÓ	
//  	RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);//Ê¹ÄÜCAN1Ê±ÖÓ	
//	
//    //³õÊ¼»¯GPIO
//	  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11| GPIO_Pin_12;
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//¸´ÓÃ¹¦ÄÜ
//    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//ÍÆÍìÊä³ö
//    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
//    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//ÉÏÀ­
//    GPIO_Init(GPIOA, &GPIO_InitStructure);//³õÊ¼»¯PA11,PA12
////	
////	  //Òı½Å¸´ÓÃÓ³ÉäÅäÖÃ
//	  GPIO_PinAFConfig(GPIOA,GPIO_PinSource11,GPIO_AF_CAN1); //GPIOA11¸´ÓÃÎªCAN1
//	  GPIO_PinAFConfig(GPIOA,GPIO_PinSource12,GPIO_AF_CAN1); //GPIOA12¸´ÓÃÎªCAN1
		
		
		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);//Ê¹ÄÜPORTAÊ±ÖÓ	                   											 
   // RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);//Ê¹ÄÜPORTAÊ±ÖÓ	
  	RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);//Ê¹ÄÜCAN1Ê±ÖÓ	
	
    //³õÊ¼»¯GPIO
	  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0| GPIO_Pin_1;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//¸´ÓÃ¹¦ÄÜ
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//ÍÆÍìÊä³ö
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//ÉÏÀ­
    GPIO_Init(GPIOD, &GPIO_InitStructure);//³õÊ¼»¯PA11,PA12
//	
//	  //Òı½Å¸´ÓÃÓ³ÉäÅäÖÃ
	  GPIO_PinAFConfig(GPIOD,GPIO_PinSource0,GPIO_AF_CAN1); //GPIOA11¸´ÓÃÎªCAN1
	  GPIO_PinAFConfig(GPIOD,GPIO_PinSource1,GPIO_AF_CAN1); //GPIOA12¸´ÓÃÎªCAN1
		
		
		
////		
//	  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8| GPIO_Pin_9;
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//¸´ÓÃ¹¦ÄÜ
//    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//ÍÆÍìÊä³ö
//    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
//    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//ÉÏÀ­
//    GPIO_Init(GPIOB, &GPIO_InitStructure);//³õÊ¼»¯PA11,PA12
//	
//	  //Òı½Å¸´ÓÃÓ³ÉäÅäÖÃ
//	  GPIO_PinAFConfig(GPIOB,GPIO_PinSource8,GPIO_AF_CAN1); //GPIOA11¸´ÓÃÎªCAN1
//	  GPIO_PinAFConfig(GPIOB,GPIO_PinSource9,GPIO_AF_CAN1); //GPIOA12¸´ÓÃÎªCAN1
		
		
		
//		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB|RCC_AHB1Periph_GPIOI, ENABLE);//Ê¹ÄÜPORTAÊ±ÖÓ	
//  	RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);//Ê¹ÄÜCAN1Ê±ÖÓ	
//	
//	  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_9;
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//¸´ÓÃ¹¦ÄÜ
//    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//ÍÆÍìÊä³ö
//    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
//    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//ÉÏÀ­
//    GPIO_Init(GPIOB, &GPIO_InitStructure);//³õÊ¼»¯PA11,PA12
//		
//		
//		GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_9;
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//¸´ÓÃ¹¦ÄÜ
//    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;//ÍÆÍìÊä³ö
//    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;//100MHz
//    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;//ÉÏÀ­
//    GPIO_Init(GPIOI, &GPIO_InitStructure);//³õÊ¼»¯PA11,PA12
//	
//	  //Òı½Å¸´ÓÃÓ³ÉäÅäÖÃ
//	  GPIO_PinAFConfig(GPIOB,GPIO_PinSource9,GPIO_AF_CAN1); //GPIOA11¸´ÓÃÎªCAN1
//	  GPIO_PinAFConfig(GPIOI,GPIO_PinSource9,GPIO_AF_CAN1); //GPIOA12¸´ÓÃÎªCAN1
		
		
		
	  
  	//CANµ¥ÔªÉèÖÃ
   	CAN_InitStructure.CAN_TTCM=DISABLE;	//·ÇÊ±¼ä´¥·¢Í¨ĞÅÄ£Ê½   
  	CAN_InitStructure.CAN_ABOM=DISABLE;	//Èí¼ş×Ô¶¯ÀëÏß¹ÜÀí	  
  	CAN_InitStructure.CAN_AWUM=DISABLE;//Ë¯ÃßÄ£Ê½Í¨¹ıÈí¼ş»½ĞÑ(Çå³ıCAN->MCRµÄSLEEPÎ»)
  	CAN_InitStructure.CAN_NART=DISABLE;	//½ûÖ¹±¨ÎÄ×Ô¶¯´«ËÍ 
  	CAN_InitStructure.CAN_RFLM=DISABLE;	//±¨ÎÄ²»Ëø¶¨,ĞÂµÄ¸²¸Ç¾ÉµÄ  
  	CAN_InitStructure.CAN_TXFP=DISABLE;	//ÓÅÏÈ¼¶ÓÉ±¨ÎÄ±êÊ¶·û¾ö¶¨ 
  	CAN_InitStructure.CAN_Mode= mode;	 //Ä£Ê½ÉèÖÃ 
  	CAN_InitStructure.CAN_SJW=tsjw;	//ÖØĞÂÍ¬²½ÌøÔ¾¿í¶È(Tsjw)Îªtsjw+1¸öÊ±¼äµ¥Î» CAN_SJW_1tq~CAN_SJW_4tq
  	CAN_InitStructure.CAN_BS1=tbs1; //Tbs1·¶Î§CAN_BS1_1tq ~CAN_BS1_16tq
  	CAN_InitStructure.CAN_BS2=tbs2;//Tbs2·¶Î§CAN_BS2_1tq ~	CAN_BS2_8tq
  	CAN_InitStructure.CAN_Prescaler=brp;  //·ÖÆµÏµÊı(Fdiv)Îªbrp+1	
  	CAN_Init(CAN1, &CAN_InitStructure);   // ³õÊ¼»¯CAN1 
    
		//ÅäÖÃ¹ıÂËÆ÷ //id 0x01 Ö÷»úID ÆÁ±Î   
 	  CAN_FilterInitStructure.CAN_FilterNumber=0;	  //¹ıÂËÆ÷0
  	CAN_FilterInitStructure.CAN_FilterMode=CAN_FilterMode_IdMask; 
  	CAN_FilterInitStructure.CAN_FilterScale=CAN_FilterScale_32bit; //32Î» 
  	CAN_FilterInitStructure.CAN_FilterIdHigh=0x0000;////32Î»ID
  	CAN_FilterInitStructure.CAN_FilterIdLow=0x0000;
  	CAN_FilterInitStructure.CAN_FilterMaskIdHigh=0x0000;//32Î»MASK
  	CAN_FilterInitStructure.CAN_FilterMaskIdLow=0x0000;
   	CAN_FilterInitStructure.CAN_FilterFIFOAssignment=CAN_Filter_FIFO0;//¹ıÂËÆ÷0¹ØÁªµ½FIFO0
  	CAN_FilterInitStructure.CAN_FilterActivation=ENABLE; //¼¤»î¹ıÂËÆ÷0
  	CAN_FilterInit(&CAN_FilterInitStructure);//ÂË²¨Æ÷³õÊ¼»¯
		
#if CAN1_RX0_INT_ENABLE
	
	  CAN_ITConfig(CAN1,CAN_IT_FMP0,ENABLE);//FIFO0ÏûÏ¢¹ÒºÅÖĞ¶ÏÔÊĞí.		    
  
  	NVIC_InitStructure.NVIC_IRQChannel = CAN1_RX0_IRQn;
  	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;     // Ö÷ÓÅÏÈ¼¶Îª1
  	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;            // ´ÎÓÅÏÈ¼¶Îª0
  	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  	NVIC_Init(&NVIC_InitStructure);
#endif
	return 0;
}   
 
#if CAN1_RX0_INT_ENABLE	//Ê¹ÄÜRX0ÖĞ¶Ï  //
//ÖĞ¶Ï·şÎñº¯Êı			    
void CAN1_RX0_IRQHandler(void)
{
  CanRxMsg RxMessage;
	//int i=0;
  CAN_Receive(CAN1, 0, &RxMessage);
//	for(i=0;i<8;i++)
//	//printf("rxbuf[%d]:%d\r\n",i,RxMessage.Data[i]);
//	Frame_Store[SeqNum_Frame_REC][i]=RxMessage.Data[i];
//	
//	if((++SeqNum_Frame_REC) == Max_Frame_Count)
//	{
//		SeqNum_Frame_REC = 0;//Ö¸Áî»º³åÇøÊÕÂúÊ±£¬Ñ­»·ÖÁµÚ0¸öµ¥Ôª
//	}
if(RxMessage.StdId==0x01)
	{
		if((RxMessage.Data[0]==0x81)&&(RxMessage.Data[1]==0x01))
		{
			slave_state_1 = 1;

			
		}
	}
	if(RxMessage.StdId==0x02)
	{
			
		if((RxMessage.Data[0]==0x81)&&(RxMessage.Data[1]==0x01))
		{
			slave_state_2 = 1;
	
		}
	}
	if(RxMessage.StdId==0x03)
	{
			
		if((RxMessage.Data[0]==0x81)&&(RxMessage.Data[1]==0x01))
		{	
		slave_state_3 = 1;

slave_state_4 = 1;
		}
	}

	
}
#endif

//can·¢ËÍÒ»×éÊı¾İ(¹Ì¶¨¸ñÊ½:IDÎª0X12,±ê×¼Ö¡,Êı¾İÖ¡)	
//len:Êı¾İ³¤¶È(×î´óÎª8)				     
//msg:Êı¾İÖ¸Õë,×î´óÎª8¸ö×Ö½Ú.
//·µ»ØÖµ:0,³É¹¦;
//		 ÆäËû,Ê§°Ü;
u8 CAN1_Send_Msg(u8* msg,u8 len)
{	
  u8 mbox;
  u16 i=0;
  CanTxMsg TxMessage;
  TxMessage.StdId=0x1;	 // ±ê×¼±êÊ¶·ûÎª0
  TxMessage.ExtId=0x12;	 // ÉèÖÃÀ©Õ¹±êÊ¾·û£¨29Î»£©
  TxMessage.IDE=0;		  // Ê¹ÓÃÀ©Õ¹±êÊ¶·û
  TxMessage.RTR=0;		  // ÏûÏ¢ÀàĞÍÎªÊı¾İÖ¡£¬Ò»Ö¡8Î»
  TxMessage.DLC=len;							 // ·¢ËÍÁ½Ö¡ĞÅÏ¢
  for(i=0;i<len;i++)
  TxMessage.Data[i]=msg[i];				 // µÚÒ»Ö¡ĞÅÏ¢          
  mbox= CAN_Transmit(CAN1, &TxMessage);   
  i=0;
  while((CAN_TransmitStatus(CAN1, mbox)==CAN_TxStatus_Failed)&&(i<0XFFF))i++;	//µÈ´ı·¢ËÍ½áÊø
  if(i>=0XFFF)return 1;
  return 0;		
}


//u8 CAN1_Send_Msg_id(u8* msg,u8 len,u16 id)
u8 CAN1_Send_Msg_id(u8* msg,u16 id,u8 len)
{	
  u8 mbox;
  u16 i=0;
  CanTxMsg TxMessage;
  TxMessage.StdId=id;	 // ±ê×¼±êÊ¶·ûÎª0
  TxMessage.ExtId=0x12;	 // ÉèÖÃÀ©Õ¹±êÊ¾·û£¨29Î»£©
  TxMessage.IDE=0;		  // Ê¹ÓÃÀ©Õ¹±êÊ¶·û
  TxMessage.RTR=0;		  // ÏûÏ¢ÀàĞÍÎªÊı¾İÖ¡£¬Ò»Ö¡8Î»
  TxMessage.DLC=len;							 // ·¢ËÍÁ½Ö¡ĞÅÏ¢
  for(i=0;i<len;i++)
  TxMessage.Data[i]=msg[i];				 // µÚÒ»Ö¡ĞÅÏ¢          
  mbox= CAN_Transmit(CAN1, &TxMessage);   
  i=0;
  while((CAN_TransmitStatus(CAN1, mbox)==CAN_TxStatus_Failed)&&(i<0XFFF))i++;	//µÈ´ı·¢ËÍ½áÊø
  if(i>=0XFFF)return 1;
  return 0;		
}


//can¿Ú½ÓÊÕÊı¾İ²éÑ¯
//buf:Êı¾İ»º´æÇø;	 
//·µ»ØÖµ:0,ÎŞÊı¾İ±»ÊÕµ½;
//		 ÆäËû,½ÓÊÕµÄÊı¾İ³¤¶È;
u8 CAN1_Receive_Msg(u8 *buf)
{		   		   
 	u32 i;
	CanRxMsg RxMessage;
    if( CAN_MessagePending(CAN1,CAN_FIFO0)==0)return 0;		//Ã»ÓĞ½ÓÊÕµ½Êı¾İ,Ö±½ÓÍË³ö 
    CAN_Receive(CAN1, CAN_FIFO0, &RxMessage);//¶ÁÈ¡Êı¾İ	
    for(i=0;i<RxMessage.DLC;i++)
    buf[i]=RxMessage.Data[i];  
	return RxMessage.DLC;	
}





void Command_DealTest(u8 *s)
{
//	u8 i;	
//	u16 sppeeds;
//	float *sp;
////	for( i=0;i<8;i++)
////	printf("rxbuf[%d]:%d\r\n",i,s[i]);

//	switch(s[0])
//	{
//		case 0x01: MotoInit();
//			break;
//		case 0x02: 
//		{
//		  sppeeds =s[2]<<8;
//		  sppeeds+=s[3];
//		//	MotorStep(MotoCurrentStruct.MotoPosition,2);
//			
//		  MotorMov(1,sppeeds,20);
//		//	AxisMove(MotoCurrentStruct.MotoPosition, MotoCurrentStruct.Maccel, MotoCurrentStruct.Mdecel, MotoCurrentStruct.MotoSpeed);
//			break;
//		}
//		case 0x03: 
//		{
//			
//			  
////			MotoCurrentStruct.MotoSpeed=s[2]<<8;
////			MotoCurrentStruct.MotoSpeed+=s[3];
////		
////			MotoCurrentStruct.Maccel =s[4]<<8;
////			MotoCurrentStruct.Maccel +=s[5];
////			
////			MotoCurrentStruct.Mdecel =s[6]<<8;
////			MotoCurrentStruct.Mdecel +=s[7];
////			memcpy(sp,s,4);
////			sppeeds =*sp;
//			break;
//		}
//			
//		
//		case 0x04: MotoF();
//			break;
//		case 0x05: MotoZ();
//		break;
//		case 0x06: MotoStop();
//	  break;
//	}	
}
//  ack    ok  81 ¹ã²¥  82 ¶Á²ÎÊı 83 Ğ´²ÎÊı  84 ·¢Í¬²½ÃüÁî 85 Òì²½ÃüÁî 86±¾»ú×´Ì¬
//  errack     c1  c2  c3 c4  c5 c6
//
//
//
//

//void Command_Deal(u8 *s)
//{
//	if(s[0]==0x01)
//	{
////		switch(s[1]):		
////		{
////			case 0x01: 1;
////				break;
////			//case 0x02
////			
////		}		
//		
//	}
//	if(s[0]==0x02)
//	{
////		switch(s[1]):
////		{
////			//case 0x01:
////			
////		}
//	}
//	if(s[0]==0x03)
//	{
//		
//	}
//	if(s[0]==0x04)
//	{
//		
//	}
//	if(s[0]==0x05)
//	{
//		
//	}
//	if(s[0]==0x06)
//	{
//		
//	}
//	if(s[0]==0x07)
//	{
//		
//	}
//	
//}





