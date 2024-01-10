#ifndef _USER_UART__
#define _USER_UART__

#include "stm32f10x_it.h" //�����û�MCU�����޸�

#define uchar unsigned char
#define uint8 unsigned char
#define uint16 unsigned short int
#define uint32 unsigned long
#define int16 short int
#define int32 long

/****************************************************************************
 * ��    �ƣ� UartInit()
 * ��    �ܣ� ���ڳ�ʼ��
 * ��ڲ����� ��
 * ���ڲ����� ��
 ****************************************************************************/
void UartInit(uint32 Baudrate);

/*****************************************************************
 * ��    �ƣ� SendChar()
 * ��    �ܣ� ����1���ֽ�
 * ��ڲ����� t  ���͵��ֽ�
 * ���ڲ����� ��
 *****************************************************************/
void SendChar(uchar t);

#endif
