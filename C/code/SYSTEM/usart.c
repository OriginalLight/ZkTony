#include "sys.h"
#include "usart.h"	
////////////////////////////////////////////////////////////////////////////////// 	 
//���ʹ��ucos,����������ͷ�ļ�����.
#if SYSTEM_SUPPORT_UCOS
#include "includes.h"					//ucos ʹ��	  
#endif
//////////////////////////////////////////////////////////////////////////////////	 
//������ֻ��ѧϰʹ�ã�δ���������ɣ��������������κ���;
//ALIENTEK STM32F4̽���߿�����
//����1��ʼ��		   
//����ԭ��@ALIENTEK
//������̳:www.openedv.com
//�޸�����:2014/6/10
//�汾��V1.5
//��Ȩ���У�����ؾ���
//Copyright(C) �������������ӿƼ����޹�˾ 2009-2019
//All rights reserved
//********************************************************************************
//V1.3�޸�˵�� 
//֧����Ӧ��ͬƵ���µĴ��ڲ���������.
//�����˶�printf��֧��
//�����˴��ڽ��������.
//������printf��һ���ַ���ʧ��bug
//V1.4�޸�˵��
//1,�޸Ĵ��ڳ�ʼ��IO��bug
//2,�޸���USART_RX_STA,ʹ�ô����������ֽ���Ϊ2��14�η�
//3,������USART_REC_LEN,���ڶ��崮������������յ��ֽ���(������2��14�η�)
//4,�޸���EN_USART1_RX��ʹ�ܷ�ʽ
//V1.5�޸�˵��
//1,�����˶�UCOSII��֧��
////////////////////////////////////////////////////////////////////////////////// 	  
 

//////////////////////////////////////////////////////////////////
//�������´���,֧��printf����,������Ҫѡ��use MicroLIB	  
#if 1
#pragma import(__use_no_semihosting)             
//��׼����Ҫ��֧�ֺ���                 
struct __FILE 
{ 
	int handle; 
}; 

FILE __stdout;       
//����_sys_exit()�Ա���ʹ�ð�����ģʽ    
_sys_exit(int x) 
{ 
	x = x; 
} 
//�ض���fputc���� 
int fputc(int ch, FILE *f)
{ 	
	while((USART1->SR&0X40)==0);//ѭ������,ֱ���������   
	USART1->DR = (u8) ch;      
	return ch;
}
#endif
 
#if EN_USART1_RX   //���ʹ���˽���
//����1�жϷ������
//ע��,��ȡUSARTx->SR�ܱ���Ī������Ĵ���   	
u8 USART_RX_BUF[USART_REC_LEN];     //���ջ���,���USART_REC_LEN���ֽ�.
//����״̬
//bit15��	������ɱ�־
//bit14��	���յ�0x0d
//bit13~0��	���յ�����Ч�ֽ���Ŀ
u16 USART_RX_STA=0;       //����״̬���	

u8 Sending=0;
u8 dat_ok=0;
//uchar revbuf[600] = {0};
u16 EEprom[50] = {0};

u8 HeadOk = 0;
u16 HeadPosition = 0;
u8 HeaderOk = 0;
u8 reving = 0;

unsigned char FrameDeal[16] = {0};
u16 WriteP = 0;
u16 ReadP = 0;


u8 t1k =0;
u8 t2k =0;
u8 t3k =0;
u8 t4k =0;
u8 t5k =0;
u8 t6k =0;
u8 t7k =0;
u8 t0k =0;



u16 FrameLen=0;

u8 sss=0;    //����/ֹͣ
u8 ppp=0;

u16 lll=0;
u16 uuu=0;
u16 tzw=0;
u16 s0p=0;
//u16 usk=0;
//u16 usr=0;
//u16 uss=0;
//u16 us0=0;
//u16 us9=0;
 u16 s1p=0;
 u16 s2p=0;
 u16 s3p=0;
 u16 s4p=0;
 u16 s5p=0;

u16 usy=0;

u16 xdelt=0;
u16 y1firstmov=0;
u16 y2firstmov=0;
	   	   	   	   
unsigned char BufId[5];
unsigned char BufValue[5];

u16 NumValue=0;

unsigned char WorkStatusBuf1[15]={0x3A,0x73,0x3D,0x74,0x72,0x75,0x65,0x38,0x65,0x0D,0x0A,'\0'};	   //s
unsigned char WorkStatusBuf2[15]={0x3A,0x68,0x3D,0x74,0x72,0x75,0x65,0x39,0x39,0x0D,0x0A,'\0'};	   //h
unsigned char WorkStatusBuf3[15]={0x3A,0x6A,0x3D,0x74,0x72,0x75,0x65,0x39,0x37,0x0D,0x0A,'\0'};	   //j
unsigned char WorkStatusBuf4[15]={0x3A,0x77,0x31,0x3D,0x74,0x72,0x75,0x65,0x35,0x39,0x0D,0x0A,'\0'}; //w1
unsigned char WorkStatusBuf5[15]={0x3A,0x77,0x32,0x3D,0x74,0x72,0x75,0x65,0x35,0x38,0x0D,0x0A,'\0'}; //w2	
unsigned char WorkStatusBuf6[15]={0x3A,0x77,0x33,0x3D,0x74,0x72,0x75,0x65,0x35,0x37,0x0D,0x0A,'\0'}; //w3
unsigned char WorkStatusBuf7[15]={0x3A,0x74,0x3D,0x74,0x72,0x75,0x65,0x38,0x64,0x0D,0x0A,'\0'};	   //t
unsigned char WorkStatusBuf8[15]={0x3A,0x66,0x3D,0x74,0x72,0x75,0x65,0x39,0x62,0x0D,0x0A,'\0'};	   //f
unsigned char WorkStatusBuf9[15]={0x3A,0x7A,0x3D,0x66,0x61,0x6C,0x73,0x65,0x33,0x63,0x0D,0x0A,'\0'};       //z
unsigned char WorkStatusBuf10[15]={0x3A,0x6F,0x3D,0x74,0x72,0x75,0x65,0x39,0x32,0x0D,0x0A,'\0'};		   //o
unsigned char WorkStatusBuf11[18]={0x3A,0x73,0x61,0x76,0x65,0x6F,0x6B,0x3D,0x74,0x72,0x75,0x65,0x37,0x36,0x0D,0x0A,'\0'};  //saveok
unsigned char WorkStatusBuf12[15]={0x3A,0x6F,0x76,0x65,0x72,0x3D,0x31,0x64,0x34,0x0D,0x0A,'\0'};


unsigned char SendBackBuf[20]={0};




u8 Frame_Store[Max_Frame_Count][RS232_REC_LEN]; //֡�������飬���ɴ��10��ָ֡��
u8 RS232_RX_BUF[RS232_REC_LEN];

//���յ������ݳ���
uint16_t RS232_RX_CNT=0;
uint16_t Flag_Deal = 0;
uint16_t  Circle_Frame_REC = 0; //ָ�����������ָ���ѭ����
uint16_t  Circle_Frame_Deal = 0;//ָ�������ִ��ָ���ѭ����

u8   SeqNum_Frame_REC = 0; //֡����������Ŀǰ֡�洢λ��
u8   SeqNum_Frame_Deal =0; //֡����������Ŀǰִ֡��λ��



//��ʼ��IO ����1 
//bound:������
void uart_init(u32 bound){
   //GPIO�˿�����
  GPIO_InitTypeDef GPIO_InitStructure;
	USART_InitTypeDef USART_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;
	
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA,ENABLE); //ʹ��GPIOAʱ��
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1,ENABLE);//ʹ��USART1ʱ��
 
	//����1��Ӧ���Ÿ���ӳ��
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource9,GPIO_AF_USART1); //GPIOA9����ΪUSART1
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource10,GPIO_AF_USART1); //GPIOA10����ΪUSART1
	
	//USART1�˿�����
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9 | GPIO_Pin_10; //GPIOA9��GPIOA10
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;//���ù���
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;	//�ٶ�50MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP; //���츴�����
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP; //����
	GPIO_Init(GPIOA,&GPIO_InitStructure); //��ʼ��PA9��PA10

   //USART1 ��ʼ������
	USART_InitStructure.USART_BaudRate = bound;//����������
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;//�ֳ�Ϊ8λ���ݸ�ʽ
	USART_InitStructure.USART_StopBits = USART_StopBits_1;//һ��ֹͣλ
	USART_InitStructure.USART_Parity = USART_Parity_No;//����żУ��λ
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;//��Ӳ������������
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;	//�շ�ģʽ
  USART_Init(USART1, &USART_InitStructure); //��ʼ������1
	
  USART_Cmd(USART1, ENABLE);  //ʹ�ܴ���1 
	
	USART_ClearFlag(USART1, USART_FLAG_TC);
	
#if EN_USART1_RX	
	USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);//��������ж�

	//Usart1 NVIC ����
  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;//����1�ж�ͨ��
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=3;//��ռ���ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelSubPriority =2;		//�����ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQͨ��ʹ��
	NVIC_Init(&NVIC_InitStructure);	//����ָ���Ĳ�����ʼ��VIC�Ĵ�����

#endif
	
}

void USART1_IRQHandler(void)                	//����1�жϷ������
{
	u8 Res;
  u16 VarPosition = 0;
	u16 ValuePosition = 0;
	
	if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)  //�����ж�(���յ������ݱ�����0x0d 0x0a��β)
	{
		 USART_RX_BUF[WriteP]=USART_ReceiveData(USART1);//(USART1->DR);	//��ȡ���յ�������
		
//		USART_RX_BUF[WriteP]=Res ;
		if(USART_RX_BUF[WriteP]==':')
		{
			HeadOk = 1;
			HeadPosition = WriteP;			
		}
		if(HeadOk)
		{
			if((USART_RX_BUF[WriteP]=='\n') && (USART_RX_BUF[WriteP-1]=='\r'))
			{
				if(HeadPosition+3>=300)
				{
					VarPosition = HeadPosition+3 - 300;
					ValuePosition = HeadPosition+5 - 300;
				}
				else
				{
					VarPosition = HeadPosition+3;
					ValuePosition = HeadPosition+5;					
				}
				if((USART_RX_BUF[VarPosition] == 's')&&(USART_RX_BUF[ValuePosition] == 0x31))
				{
						sss= 1;
				}
				if((USART_RX_BUF[VarPosition] == 's')&&(USART_RX_BUF[ValuePosition] == 0x30))
				{
					sss=0;
//	
//					RSTSRC |= (1<<4);			////////////////////////////////				
				}
				if((USART_RX_BUF[VarPosition] == 'w')&&(USART_RX_BUF[ValuePosition] == 0x30))
				{
					tzw = 0;													
				}
				
				HeadOk = 0;		

			}

		}

		WriteP++;
		if(WriteP >= 300)
		{
			WriteP = 0;
		}
		
//		if((USART_RX_STA&0x8000)==0)//����δ���
//		{
//			if(USART_RX_STA&0x4000)//���յ���0x0d
//			{
//				if(Res!=0x0a)USART_RX_STA=0;//���մ���,���¿�ʼ
//				else USART_RX_STA|=0x8000;	//��������� 
//			}
//			else //��û�յ�0X0D
//			{	
//				if(Res==0x0d)USART_RX_STA|=0x4000;
//				else
//				{
//					USART_RX_BUF[USART_RX_STA&0X3FFF]=Res ;
//					USART_RX_STA++;
//					if(USART_RX_STA>(USART_REC_LEN-1))USART_RX_STA=0;//�������ݴ���,���¿�ʼ����	  
//				}		 
//			}
//		}   		 
  } 
} 





		

//void USART1_IRQHandler(void)                	//����1�жϷ������
//{
//	u8 Res;
//	if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)  //�����ж�(���յ������ݱ�����0x0d 0x0a��β)
//	{
//		Res =USART_ReceiveData(USART1);//(USART1->DR);	//��ȡ���յ�������
//		
////		if((USART_RX_STA&0x8000)==0)//����δ���
////		{
////			if(USART_RX_STA&0x4000)//���յ���0x0d
////			{
////				if(Res!=0x0a)USART_RX_STA=0;//���մ���,���¿�ʼ
////				else USART_RX_STA|=0x8000;	//��������� 
////			}
////			else //��û�յ�0X0D
////			{	
////				if(Res==0x0d)USART_RX_STA|=0x4000;
////				else
////				{
////					USART_RX_BUF[USART_RX_STA&0X3FFF]=Res ;
////					USART_RX_STA++;
////					if(USART_RX_STA>(USART_REC_LEN-1))USART_RX_STA=0;//�������ݴ���,���¿�ʼ����	  
////				}		 
////			}
////		} 
//		if((SeqNum_Frame_REC == SeqNum_Frame_Deal)&&(Circle_Frame_REC != Circle_Frame_Deal)) //����������ݸ��ǣ����ش������
//		{
//			//���ش���
//			 //RS232_Send_Data(":ERR=8\r\n",8);
//			 Circle_Frame_REC = 0; //ָ�����������ָ���ѭ����
//       Circle_Frame_Deal = 0;//ָ�������ִ��ָ���ѭ����

//       SeqNum_Frame_REC = 0; //֡����������Ŀǰ֡�洢λ��
//       SeqNum_Frame_Deal =0; //֡����������Ŀǰִ֡��λ��
//			
//		}
//		else if((RS232_RX_CNT == 0)&&(res == 0x3A))
//		{
//			Frame_Store[SeqNum_Frame_REC][RS232_RX_CNT] = res;
//			RS232_RX_CNT++;
//		}
//		else if(RS232_RX_CNT > 0)
//		{
//				if(RS232_RX_CNT&0x4000) //���յ���0x0d
//					{
//							if(res != 0x0a)
//								RS232_RX_CNT=0; //���մ���,���¿�ʼ
//							else
//							{
//								//Flag_Deal = 1;
//								Frame_Store[SeqNum_Frame_REC][RS232_RX_CNT&0X3FFF] = res;
//								if((++SeqNum_Frame_REC) == Max_Frame_Count)
//								{
//									SeqNum_Frame_REC = 0;//ָ���������ʱ��ѭ������0����Ԫ
//									Circle_Frame_REC++; //����ָ���ѭ������һ
//								}
//								RS232_RX_CNT = 0;	//
//							}								
//					}
//				else //��û�յ�0X0d
//					{	
//						if(res == 0x0d)
//						{
//							Frame_Store[SeqNum_Frame_REC][RS232_RX_CNT&0X3FFF] = res;
//							RS232_RX_CNT++;
//							RS232_RX_CNT |= 0x4000;								
//						}
//					 else
//						{
//							Frame_Store[SeqNum_Frame_REC][RS232_RX_CNT&0X3FFF] = res;
//							RS232_RX_CNT++;
//							if(RS232_RX_CNT > (RS232_REC_LEN-1))
//							RS232_RX_CNT = 0;//�������ݴ���,���¿�ʼ����	  
//						}	
//					}
//		}
//	}
//  } 
//}		
		

//void USART1_IRQHandler(void)                	//����1�жϷ������
//{
//	u8 Res;
//		u16 VarPosition = 0;
//	u16 ValuePosition = 0;
//#ifdef OS_TICKS_PER_SEC	 	//���ʱ�ӽ�����������,˵��Ҫʹ��ucosII��.
//	OSIntEnter();    
//#endif
//	if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)  //�����ж�(���յ������ݱ�����0x0d 0x0a��β)
//	{
//		Res =USART_ReceiveData(USART1);//(USART1->DR);	//��ȡ���յ�������
//		
//		if((USART_RX_STA&0x8000)==0)//����δ���
//		{
//			if(USART_RX_STA&0x4000)//���յ���0x0d
//			{
//				if(Res!=0x0a)USART_RX_STA=0;//���մ���,���¿�ʼ
//				else 
//				{
//					
//					USART_RX_STA|=0x8000;	//��������� 
//					if(HeadOk)
//					{
////						if((USART_RX_BUF[WriteP]=='\n') && (USART_RX_BUF[WriteP-1]=='\r'))
////						{
////							if(HeadPosition+3>=600)
////							{
////								VarPosition = HeadPosition+3 - 600;
////								ValuePosition = HeadPosition+5 - 600;
////							}
////							else
////							{
//								VarPosition = HeadPosition+3;
//								ValuePosition = HeadPosition+5;					
////							}
//							if((USART_RX_BUF[VarPosition] == 's')&&(USART_RX_BUF[ValuePosition] == 0x31))
//							{
//								sss = 1;
//							}
//							if((USART_RX_BUF[VarPosition] == 's')&&(USART_RX_BUF[ValuePosition] == 0x30))
//							{
//								sss=0;
//				//					SFRPAGE = UART0_PAGE;	
//				//					RSTSRC |= (1<<4);							
//							}
//							if((USART_RX_BUF[VarPosition] == 'w')&&(USART_RX_BUF[ValuePosition] == 0x30))
//							{
//								tzw = 0;													
//							}
//							
//							HeadOk = 0;		

//						}

//					}
//					
//					
//					
//		   }
//			else //��û�յ�0X0D
//			{	
//				if(Res==0x0d)USART_RX_STA|=0x4000;
//				else
//				{
//					USART_RX_BUF[USART_RX_STA&0X3FFF]=Res ;
//					if(USART_RX_BUF[WriteP]==':')
//					{
//						HeadOk = 1;
//						HeadPosition = WriteP;			
//					}
//					
//					WriteP++;
//					if(WriteP >= USART_REC_LEN-1)
//					{
//						WriteP = 0;
//					}
//					
//					USART_RX_STA++;
//					if(USART_RX_STA>(USART_REC_LEN-1))USART_RX_STA=0;//�������ݴ���,���¿�ʼ����	  
//				}		 
//			}
//		}   		 
//  } 
//#ifdef OS_TICKS_PER_SEC	 	//���ʱ�ӽ�����������,˵��Ҫʹ��ucosII��.
//	OSIntExit();  											 
//#endif
//} 


 void SendOneByte(unsigned char c)
 {
	 
      USART_SendData(USART1,c); //��������
   	  while(USART_GetFlagStatus(USART1,USART_FLAG_TC)==RESET); //�ȴ����ͽ���		
 }
 
 
 void Send_Str(unsigned char* str)
 {
	 while((*str) != '\0')
	{
		SendOneByte(*str);
		str++;
	}
 }

 

 void FrameCopyDeal(void)
 {
	 while(ReadP != WriteP)
	{

		reving = USART_RX_BUF[ReadP++];
		if(ReadP>299) ReadP=0;

		if(dat_ok == 0)
		{
			if(HeaderOk)
			{
				FrameDeal[FrameLen] = reving;
				if((FrameDeal[FrameLen]=='\n') && (FrameDeal[FrameLen-1]=='\r'))
				{
				  dat_ok = 1;
				  HeaderOk=0;				
				}
				FrameLen++;			
			}

		}

		if(!HeaderOk && (reving==':'))  
 		{ 
			HeaderOk=1;   
			FrameLen=0;
		}
//unsigned char BufId[5];
//unsigned char BufValue[5];
    // 	if(0 == DataCheck(BufId,BufValue))
		if(dat_ok)
  	{
		
				unsigned char tempbuf[4] = {0};
				u16 tm = 0;
				u16 ts = 0; 
				int num = 0;

				unsigned char i=0,cmd_len=0,temp_len=0;
				unsigned char * cmd_name;
				unsigned char * cmd_value;
				unsigned char * cmd_index ;

				dat_ok = 0;

				//			num = 0;
				cmd_len = strlen(FrameDeal);
				//			for(i=0;i<cmd_len-4;i++)
				//			{			
				//				num += FrameDeal[i];//num=num+sendbuf[i];				
				//			}
				//			num %= 255;
				//			num = (~num)+1;
				//			num &= 255;			
				//			//memset( temp , 0x00 , 4);
				//		    sprintf(temp, "%02x", num);

				//			memcpy(Lrc_tmp,&FrameDeal[i],2);

				//			if(strcmp(temp,Lrc_tmp) == 0)
				//			{
				cmd_name = FrameDeal;
				//				if(!(strchr(FrameDeal,'=') && strchr(FrameDeal,'\n')))
				//				{
				//					//Send_Str("CMD need '=' and '\n'!");
				//					//Send_Str("\n");
				//					return 2; //return for error
				//				}

				cmd_index = &FrameDeal[0];
				while ('=' != *cmd_index)
				{
					cmd_index++;
					temp_len++;	

					if(temp_len >= 4)
					{
						Send_Str("Invalid CMD!");
				//					//Send_Str("\n");
				//					return (2);//return for error
						break;
					 }
				}

				*cmd_index='\0';
				cmd_value = cmd_index+1;
				while ('\n' != *cmd_index)
				{
					cmd_index++;
				}
				*(cmd_index-1)='\0';
				strcpy(BufId,cmd_name);
				strcpy(BufValue,cmd_value);
				memset(FrameDeal,0,30);

				NumValue = my_atoi(BufValue);

			switch(BufId[2])
			{
				case 'l':
					lll = NumValue;
					break;	
				case 'u':			
					uuu = NumValue;
					break;
				case 'w':			
					tzw = NumValue;
					break;
//				case 'p':			
//					stp = NumValue;
//					break;
//				case 'k':			
//					usk = NumValue;
//					break;
//				case 'r':			
//					usr = NumValue;
//					break;
//				case 's':			
//					uss = NumValue;
//					break;
//				case '0':			
//					us0 = NumValue;
//					break;
//				case '9':			
//					us9 = NumValue;
//					break;
				case 'y':			
					usy = NumValue;
					break;
			   //L
				case 'a':
					EEprom[0] = NumValue;	
					break;				
				case 'b':			
					EEprom[1] = NumValue;						
					break;				
				case 'c':			
					EEprom[2] = NumValue;
					break;				
				case 'd':			
					EEprom[3] = NumValue;
					SendBack_L();
					break;
				//H					
				case 'e':			
//					switch(BufId[1])
//						{
//							case 'h':
					EEprom[4] = NumValue;	
					SendBack_L();
					break;

//						}				
//					break;				
				case 'f':			
					switch(BufId[1])
						{
							case 'h':
								EEprom[5] = NumValue;	
								SendBack_L();
								break;
							case '1':
								y1firstmov = NumValue;
								break;
							case '2':
								y2firstmov = NumValue;
								break;
							
							case '3':
								xdelt = NumValue;
								break;
						}		
					break;				
				case 'g':			
					EEprom[6] = NumValue;
					break;				
				case 'h':			
					EEprom[7] = NumValue;
					break;				
				case 'i':			
					EEprom[8] = NumValue;
					break;				
				case 'j':			
					EEprom[9] = NumValue;
					SendBack_H();
					break;				
				case 'k'
				{
						switch(BufId[1])
						{
							case '0':
								t0k = NumValue;	
								break;
							case '1':
								t1k = NumValue;	
								break;
							case '2':
								t2k = NumValue;	
								break;
							case '3':
								t3k = NumValue;	
								break;
							case '4':
								t4k = NumValue;	
								break;
							case '5':
								t5k = NumValue;	
								break;
							case '6':
								t6k = NumValue;	
								break;
							case '7':
								t7k = NumValue;	
								break;
						}
					
				}
				case 'v':			
					EEprom[10] = NumValue;
					break;
				//J
				case 'm':			
					EEprom[11] = NumValue;	
					break;				
				case 'n':			
					EEprom[12] = NumValue;	
					break;				
				case 'o':			
					EEprom[13] = NumValue;
					break;				
				case 'p':			
						switch(BufId[1])
						{
							case '0':
									s0p = NumValue;
								break;
							case '1':
							  	s1p = NumValue;
								break;
							case '2':
								  s2p = NumValue;
								break;
							
							case '3':
							  	s3p = NumValue;
								break;
							case '4':
							  	s4p = NumValue;
								break;
							case '5':
							  	s5p = NumValue;
								break;
						}		
					break;
				
				case 'q':			
					EEprom[14] = NumValue;
					break;							
				case 't':			
					EEprom[15] = NumValue;
					break;				
				case 'x':			
					EEprom[16] = NumValue;
					SendBack_J();
					break;
				//1
				case 'A':			
					EEprom[17] = NumValue;	
					break;				
				case 'B':			
					EEprom[18] = NumValue;	
					break;				
				case 'C':			
					EEprom[19] = NumValue;
					break;				
				case 'D':			
					EEprom[20] = NumValue;
					break;							
				case 'E':			
					EEprom[21] = NumValue;
					break;				
				case 'F':			
					EEprom[22] = NumValue;
					SendBack_1();
					break;
				//2
				case 'G':			
					EEprom[23] = NumValue;	
					break;				
				case 'H':			
					EEprom[24] = NumValue;	
					break;				
				case 'I':			
						EEprom[25] = NumValue;
					break;				
				case 'J':			
					EEprom[26] = NumValue;
					break;							
				case 'K':			
					EEprom[27] = NumValue;
					break;				
				case 'L':			
					EEprom[28] = NumValue;
					SendBack_2();
					break;
				//3
				case 'M':			
					EEprom[29] = NumValue;	
					break;				
				case 'N':			
					EEprom[30] = NumValue;	
					break;				
				case 'O':			
					EEprom[31] = NumValue;
					break;				
				case 'P':			
					EEprom[32] = NumValue;
					break;							
				case 'Q':			
					EEprom[33] = NumValue;
					break;				
				case 'R':			
					EEprom[34] = NumValue;
					SendBack_3();
					break;
				//T
				case 'S':			
					EEprom[35] = NumValue;	
					break;				
				case 'T':			
					EEprom[36] = NumValue;										
					break;				
				case 'U':			
					EEprom[37] = NumValue;
					break;				
				case 'V':			
					EEprom[38] = NumValue;
					break;							
				case 'W':			
					EEprom[39] = NumValue;
					break;				
				case 'X':			
					EEprom[40] = NumValue;
					SendBack_T();
					break;
				//S				
				case 'Y':			
					EEprom[41] = NumValue;	
					break;							
				case 'Z':			
					EEprom[42] = NumValue;	
					break;				
				case 'z':			
					EEprom[43] = NumValue;
					SendBack_S();
					break;			

				default:
					break;
			}
							
		}   

	}

}

 //USART1����len���ֽ�.
//buf:�������׵�ַ
//len:���͵��ֽ���(Ϊ�˺ͱ�����Ľ���ƥ��,���ｨ�鲻Ҫ����64���ֽ�)
void USART1_Send_Data(u8 *buf,u8 len)
{
	u8 t;
	for(t=0;t<len;t++)		//ѭ����������
	{
	  while(USART_GetFlagStatus(USART1,USART_FLAG_TC)==RESET); //�ȴ����ͽ���		
    USART_SendData(USART1,buf[t]); //��������
	}	 
	while(USART_GetFlagStatus(USART1,USART_FLAG_TC)==RESET); //�ȴ����ͽ���		
}
 
 
 
/*************************************************************************************
��������DataCheck( )
�������ܣ��������յ���ASCII����ָ��
����������id (�洢�������ID����),value (�洢�������VALUE����)
��������ֵ�����ؽ���״̬��Ϣ 0��������ȷ1����������
*************************************************************************************/

unsigned char  DataCheck(unsigned char* id,unsigned char* value)
{
	int num = 0;
	unsigned char i=0,cmd_len=0,temp_len=0;
	unsigned char * cmd_name;
	unsigned char * cmd_value;
	unsigned char * cmd_index ;
	unsigned char temp[3] = {0};
	unsigned char Lrc_tmp[3] = {0};
	if(dat_ok)
	{
		dat_ok = 0;

			num = 0;
			cmd_len = strlen(FrameDeal);
			for(i=0;i<cmd_len-4;i++)
			{			
				num += FrameDeal[i];//num=num+sendbuf[i];				
			}
			num %= 255;
			num = (~num)+1;
			num &= 255;			
			//memset( temp , 0x00 , 4);
		    sprintf(temp, "%02x", num);

			memcpy(Lrc_tmp,&FrameDeal[i],2);

			if(strcmp(temp,Lrc_tmp) == 0)
			{
			 	cmd_name = FrameDeal;
				if(!(strchr(FrameDeal,'=') && strchr(FrameDeal,'\n')))
				{
					//Send_Str("CMD need '=' and '\n'!");
					//Send_Str("\n");
					return 2; //return for error
				}

				cmd_index = &FrameDeal[0];
				while ('=' != *cmd_index)
				{
					cmd_index++;
					temp_len++;
				}
				if(temp_len >= 20)
				{
					//Send_Str("Invalid CMD!");
					//Send_Str("\n");
					return (2);//return for error
				}
				*cmd_index='\0';
				cmd_value = cmd_index+1;
				while ('\n' != *cmd_index)
				{
					cmd_index++;
				}
				*(cmd_index-3)='\0';
				strcpy(id,cmd_name);
				strcpy(value,cmd_value);
				memset(FrameDeal,0,30);
				return 0;
			}
			else
			{
//				Send_Str("LRC Failed!\n");
				memset(FrameDeal,0,30);
				return 2;
			}
	}				 	
	return 1;
}

/*************************************************************************************
��������Package
�������ܣ����Ҫ���͵�һ������ָ�ָ���ʽ��":ID=VALUE(LRCУ��)\r\n"
����������id (����ָ��Ҫ������ID���� [�ַ����׵�ַ]),
Value (����ָ��Ҫ������VALUE���� [�ַ����׵�ַ])
Tempbuf (�����ɺ�洢��������)
��������ֵ����
*************************************************************************************/
void Package(unsigned char* id,unsigned char* value,unsigned char* tempbuf)
{
	int num;
	unsigned char i = 0;
	unsigned char temp[2];
	tempbuf[i++] = ':';
	tempbuf[i] = '\0';
	
	//strcat(tempbuf,':');
	strcat(tempbuf,id);
	strcat(tempbuf,"=");
	strcat(tempbuf,value);
	strcat(tempbuf,"\r\n");
	
//	strcat(tempbuf,"\0");

//		num = 0;		              //LRC   
////		i++; //Remove ":" to add
//		while(tempbuf[i] != '\0')
//		{
//			num += tempbuf[i];//num=num+sendbuf[i];
//			i++;
//		}
//		num %= 255;
//		num = (~num)+1;
//		num &= 255;			
//		//memset( temp , 0x00 , 4);
//	    sprintf(temp, "%02x", num);
//		memcpy(&tempbuf[i],&temp[0] ,2);
//		                                   //add LRC Verify

//	i += 2;
//	tempbuf[i++] = '\r';
//	tempbuf[i++] = '\n';
//	tempbuf[i] = '\0';
} 

/*************************************************************************************
��������my_atoi()
�������ܣ��ַ���ת��Ϊint������
����������str (Ҫת�����ַ����׵�ַ)
��������ֵ��ת�����int������
*************************************************************************************/
int my_atoi(const char * str)
{
	u16 res=0,begin=0;
 	unsigned char minus=0;
 	while(*str != '\0')
 	{
  		if(begin==0&&(('0'<=*str&&*str<='9')|| *str=='-') )   //�ӵ�һ�����ֻ���'-'�ſ�ʼ
  		{
   			begin=1;
   			if(*str == '-')
   			{
    			minus=1;
    			str++;
   			}
  		}
  		else if( begin==1&&(*str<'0'||*str>'9') )     //������һ�������֣��˳�
   		break;
  		if(begin==1)
   			res=res*10+(*str-'0');                       //����
  			str++;
 	}
 	return minus? -res : res;
} 


/*************************************************************************************
��������SendBack()
�������ܣ��ش�EEprom[50]��Ԫ��
������������
��������ֵ����
*************************************************************************************/

void SendBack_L(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[0]);							
	Package("fla",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[1]);							
	Package("alb",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
						
	tm = floor(EEprom[2]/60);
	ts = EEprom[2]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("lzm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
					
	sprintf(tempbuf,"%d",ts);							
	Package("lzs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[3]/60);
	ts = EEprom[3]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("ldm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
					
	sprintf(tempbuf,"%d",ts);							
	Package("lds",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);


}

void SendBack_H(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[4]);							
	Package("fhe",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[5]);							
	Package("ahf",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[6]/60);
	ts = EEprom[6]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("hzm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("hzs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[7]/60);
	ts = EEprom[7]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("hd1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("hd1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[8]/60);
	ts = EEprom[8]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("hxm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("hxs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[9]/60);
	ts = EEprom[9]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("hd2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("hd2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	sprintf(tempbuf,"%d",EEprom[10]);							
	Package("vjv",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
}

void SendBack_J(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[11]);							
	Package("fjm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[12]);							
	Package("ajn",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[13]/60);
	ts = EEprom[13]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("jzm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("jzs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[14]/60);
	ts = EEprom[14]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("jd1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("jd1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[15]/60);
	ts = EEprom[15]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("jxm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("jxs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[16]/60);
	ts = EEprom[16]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("jd2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("jd2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
}

void SendBack_1(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[17]);							
	Package("f1A",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[18]);							
	Package("a1B",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[19]/60);
	ts = EEprom[19]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w1zm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("w1zs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[20]/60);
	ts = EEprom[20]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w1d1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w1d1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[21]/60);
	ts = EEprom[21]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w1xm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w1xs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[22]/60);
	ts = EEprom[22]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w1d2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w1d2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
}

void SendBack_2(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[23]);							
	Package("f2G",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[24]);							
	Package("a2H",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[25]/60);
	ts = EEprom[25]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w2zm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("w2zs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[26]/60);
	ts = EEprom[26]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w2d1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w2d1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[27]/60);
	ts = EEprom[27]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w2xm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w2xs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[28]/60);
	ts = EEprom[28]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w2d2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w2d2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
}

void SendBack_3(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[29]);							
	Package("f3M",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[30]);							
	Package("a3N",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[31]/60);
	ts = EEprom[31]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w3zm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("w3zs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[32]/60);
	ts = EEprom[32]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w3d1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w3d1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[33]/60);
	ts = EEprom[33]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w3xm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w3xs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[34]/60);
	ts = EEprom[34]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("w3d2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("w3d2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

}

void SendBack_T(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[35]);							
	Package("ftS",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[36]);							
	Package("atT",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[37]/60);
	ts = EEprom[37]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("tzm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("tzs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[38]/60);
	ts = EEprom[38]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("td1m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("td1s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[39]/60);
	ts = EEprom[39]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("txm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("txs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);

	tm = floor(EEprom[40]/60);
	ts = EEprom[40]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("td2m",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	sprintf(tempbuf,"%d",ts);							
	Package("td2s",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
}

void SendBack_S(void)
{
	unsigned char tempbuf[4] = {0};
	u16 tm = 0;
	u16 ts = 0;

	sprintf(tempbuf,"%d",EEprom[41]);							
	Package("fsY",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	sprintf(tempbuf,"%d",EEprom[42]);							
	Package("asZ",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);
	
	tm = floor(EEprom[43]/60);
	ts = EEprom[43]%60;
	sprintf(tempbuf,"%d",tm);							
	Package("szm",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);					
	sprintf(tempbuf,"%d",ts);							
	Package("szs",tempbuf,SendBackBuf);
	Send_Str(SendBackBuf);	
}

void SendBack(void)
{
	SendBack_L();
	SendBack_H();
	SendBack_J();
	SendBack_1();
	SendBack_2();
	SendBack_3();
	SendBack_T();
	SendBack_S();
} 


#endif	

 


