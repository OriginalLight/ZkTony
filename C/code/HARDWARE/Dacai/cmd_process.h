#ifndef _CMD_PROCESS_H
#define _CMD_PROCESS_H
#include "hmi_driver.h"

/** LEN */
#define _HEAD_LEN 1
#define _ADDRESS_LEN 1
#define _DICTATE_LEN 1
#define _LENGTH_LEN 2
#define _DATA_LEN 256
#define _CRC_LEN 2
#define _END_LEN 4
#define _PACK_HEAD_LEN (_HEAD_LEN + _ADDRESS_LEN + _DICTATE_LEN + _LENGTH_LEN)
#define _PACK_END_LEN (_CRC_LEN + _END_LEN)
#define _PACK_NO_DATA_LEN (_PACK_HEAD_LEN + _PACK_END_LEN)
#define _PACK_LEN (_PACK_HEAD_LEN + _DATA_LEN + _PACK_END_LEN)

/** INDEX */
#define _HEAD_INDEX 0
#define _ADDRESS_INDEX 1
#define _DICTATE_INDEX 2
#define _LENGTH_INDEX 3

/** DEFAULT */
#define PACK_HEAD 0xEE
#define PACK_CMD 0x01
#define PACK_ACK 0x02
#define PACK_END 0XFFFFFCFF

/** RX DICTATE */
#define CMD_RX_RUN 0x01
#define CMD_RX_STOP 0x02
#define CMD_RX_QUERY 0x03

/** TX DICTATE */
#define CMD_TX_STATUS 0x01
#define CMD_TX_ERROR 0xFF

/** REPLY */
#define CMD_RT_OK 0x0000

#define CMD_Error_Header 0x0001
#define CMD_Error_Addr 0x0002
#define CMD_Error_Crc 0x0003
#define CMD_NO_COM 0x0004

/** CMD DICTATE CMD_QUERY 03 */
#define CMD_03_Stop 0x00
#define CMD_03_Runing 0x01

typedef enum
{
	NetLH_RES_OK = 0,
	NetLH_HEAD_ERR,
	NetLH_DEVICE_ADD_ERR,
	NetLH_CRC_ERR,
	NetLH_SP_OVER,
	NetLH_RES_NONE,

} NetLH_Res;

typedef enum
{
	NO_COMEVENT = 0,
	USART_COMEVENT,
} COMM_EVENT;

void CmdProcess();
void CmdAnalysis();
void CmdRun(uint8 *RXbuffer);
void CmdStop(uint8 *RXbuffer);
void CmdQuery(uint8 *RXbuffer);
NetLH_Res CmdCheckPack(uint8 *RXbuffer);
void ComAckPack(uint8 ack, uint8 dictate, uint8 data[], uint16 length);
#endif
