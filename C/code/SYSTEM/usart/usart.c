#include "sys.h"
#include "usart.h"	
#include "exti.h"
#include "cmd_queue.h"
#include "stm32f4xx.h" 
#include "delay.h"
#include "cmd_process.h"
////////////////////////////////////////////////////////////////////////////////// 	 
//���ʹ��ucos,����������ͷ�ļ�����.
#if SYSTEM_SUPPORT_UCOS
#include "includes.h"					
#endif
//////////////////////////////////////////////////////////////////////////////////	 
//V1.3�޸�˵�� 
//֧����Ӧ��ͬƵ���µĴ��ڲ���������.
//�����˶�printf��֧��
//�����˴��ڽ��������.
//������printf��һ���ַ���ʧ��bug
//V1.4�޸�˵��
//1,�޸Ĵ��ڳ�ʼ��IO��bug
//2,�޸���USART_RX_STA,ʹ�ô����������ֽ���Ϊ2��14�η�
//3,������USART_REC_LEN,���ڶ��崮�����������յ��ֽ���(������2��14�η�)
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
void _sys_exit(int x) 
{ 
	x = x; 
} 
//�ض���fputc���� 
int fputc(int ch, FILE *f)
{ 	
	while((USART1->SR&0X40)==0);//ѭ������,ֱ���������   
	USART1->DR = (u8) ch;    
//  while((USART1->SR&0X40)==0);  
	return ch;
}
#endif
 
#if EN_USART1_RX   //���ʹ���˽���
uint16 revflag = 0;
uint32 cmdstate = 0;

uint8 buffer[CMD_MAX_SIZE];
extern uint8 cmd_buffer[CMD_MAX_SIZE];//ָ���
extern uint16 Cmd_Cnt;
//extern uint8 _PACKLENGTH;


//��ʼ��IO ����
//bound:������
void uart_init(u32 bound){
   //GPIO�˿�����
	GPIO_InitTypeDef GPIO_InitStructure;
	USART_InitTypeDef USART_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;

	//////USART1 ��ʼ��////////////////////////
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA,ENABLE); //ʹ��GPIOaʱ��
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1,ENABLE);//ʹ��USARTʱ��
 
	//���ڶ�Ӧ���Ÿ���ӳ��
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource9,GPIO_AF_USART1); //GPIOD����ΪUSART
	GPIO_PinAFConfig(GPIOA,GPIO_PinSource10,GPIO_AF_USART1); //


	//USART�˿�����
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9 | GPIO_Pin_10; // GPIOA9��GPIOA10
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;			// ���ù���
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		// �ٶ�50MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;			// ���츴�����
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;			// ����
	GPIO_Init(GPIOA, &GPIO_InitStructure);					// ��ʼ��PA9��PA10

	// USART ��ʼ������
	USART_InitStructure.USART_BaudRate = bound;										// ����������
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;						// �ֳ�Ϊ8λ���ݸ�ʽ
	USART_InitStructure.USART_StopBits = USART_StopBits_1;							// һ��ֹͣλ
	USART_InitStructure.USART_Parity = USART_Parity_No;								// ����żУ��λ
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None; // ��Ӳ������������
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;					// �շ�ģʽ
	USART_Init(USART1, &USART_InitStructure);										// ��ʼ������

	USART_Cmd(USART1, ENABLE); // ʹ�ܴ���1

	USART_ClearFlag(USART1, USART_FLAG_TC);

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////USART3 ��ʼ��////////////////////////
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);  // ʹ��GPIODʱ��
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE); // ʹ��USARTʱ��

	// ���ڶ�Ӧ���Ÿ���ӳ��
	GPIO_PinAFConfig(GPIOD, GPIO_PinSource9, GPIO_AF_USART3); // GPIOD����ΪUSART
	GPIO_PinAFConfig(GPIOD, GPIO_PinSource8, GPIO_AF_USART3); //

	// USART�˿�����
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8 | GPIO_Pin_9; // GPIO
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;		   // ���ù���
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;	   // �ٶ�50MHz
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;		   // ���츴�����
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;		   // ����
	GPIO_Init(GPIOD, &GPIO_InitStructure);				   // ��ʼ��
	

	// USART ��ʼ������
	USART_InitStructure.USART_BaudRate = bound;										// ����������
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;						// �ֳ�Ϊ8λ���ݸ�ʽ
	USART_InitStructure.USART_StopBits = USART_StopBits_1;							// һ��ֹͣλ
	USART_InitStructure.USART_Parity = USART_Parity_No;								// ����żУ��λ
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None; // ��Ӳ������������
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;					// �շ�ģʽ
	USART_Init(USART3, &USART_InitStructure);										// ��ʼ������
	

	//USART_ClearFlag(USART3, USART_FLAG_TC);

	USART_Cmd(USART3, ENABLE); // ʹ�ܴ���3

	

#if EN_USART1_RX	
//////////////////////////usart1 �ж�����//////////////////////////////////////
	USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);//��������ж�

	//Usart NVIC ����
  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;//�����ж�ͨ��
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=0;//��ռ���ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelSubPriority =1;		//�����ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQͨ��ʹ��
	NVIC_Init(&NVIC_InitStructure);	//����ָ���Ĳ�����ʼ��VIC�Ĵ���

	//////////////////////////usart3 �ж�����//////////////////////////////////////
	USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);//��������ж�

	//Usart NVIC ����
  NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;//�����ж�ͨ��
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=0;//��ռ���ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelSubPriority =1;		//�����ȼ�3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQͨ��ʹ��
	NVIC_Init(&NVIC_InitStructure);	//����ָ���Ĳ�����ʼ��VIC�Ĵ���
	
	

#endif


}



void USART1_IRQHandler(void)                	//����1�жϷ������
{
	//u8 Res;
	if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)  //�����ж�
	{
//		 Res =USART_ReceiveData(USART1);//(USART1->DR);	//��ȡ���յ������ݣ�ͬʱӲ���Զ�����ж�
//     //queue_push(Res);
//			cmd_buffer[Cmd_Cnt++] = Res;
  } 
	SendChar(USART_ReceiveData(USART1));
} 

void USART3_IRQHandler(void)                	//����3�жϷ������
{
	u8 Res;
	if(USART_GetITStatus(USART3, USART_IT_RXNE) != RESET)  //�����ж�()
	{
			
			//buffer[Cmd_Cnt] = USART_ReceiveData(USART3); //(USART3->DR);	//��ȡ���յ������ݣ�ͬʱӲ���Զ�����ж�
			Res = USART_ReceiveData(USART3); //(USART3->DR);


			  queue_push(Res);
			  
			
  } 
	if(USART_GetFlagStatus(USART3,USART_FLAG_ORE) != RESET) //��� 
     { 
            USART_ClearFlag(USART3,USART_FLAG_ORE); //��SR 
            USART_ReceiveData(USART3); //��DR 
     }

	//SendChar_USART3(Res);
}  
		

void SendChar(unsigned char c)
 {

	    while(USART_GetFlagStatus(USART1,USART_FLAG_TXE)==RESET); //�ȴ���һ�η��ͽ���
	 
      USART_SendData(USART1,c); //��������	   
   	  //while(USART_GetFlagStatus(USART1,USART_FLAG_TC)==RESET); //�ȴ����ͽ���	
	
	 
 }

 void SendChar_USART3(unsigned char c)
 {

	    while(USART_GetFlagStatus(USART3,USART_FLAG_TXE)==RESET); //�ȴ���һ�η��ͽ���
	 
      USART_SendData(USART3,c); //��������	   
   	  //while(USART_GetFlagStatus(USART3,USART_FLAG_TC)==RESET); //�ȴ����ͽ���	
	
	 
 }

void Send_Str(unsigned char* str)
 {
	 while((*str) != '\0')
	{
		SendChar(*str);
		str++;
	}
 } 
 
 
void SendData(char *s,u8 len)
{
	u8 t;
	for(t=0;t<len;t++)
  {
		 //SendChar(s[t]);
		 SendChar_USART3(s[t]);

  }	
	
}

 #endif	


