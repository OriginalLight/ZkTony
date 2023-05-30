#include "cmd_process.h"
#include "moto.h"
#include "pwm.h"
#include "string.h"
#include "string_deal.h"
#include "cmd_queue.h"
#include "can.h"
#include "timer.h"
#include "usart.h"
#include "bsp_i2c_gpio.h"
#include "crc16.h"
#include "SEGGER_RTT.h"

uint8 cmd_buffer[CMD_MAX_SIZE]; // ָCommand buffer
uint8 cmd_RXbuffer[_PACK_LEN];	// Rx buffer
COMM_EVENT DoComEvent = NO_COMEVENT;
uint16 Cmd_Cnt = 0;
extern Moto_Struct Moto[MOTONUM];
extern SpeedRampData srd[MOTONUM];

void ComAckPack(uint8 ack, uint8 dictate, uint8 data[], uint16 length)
{
	// create data buffer to send
	uint8 TXbuffer[_PACK_LEN + length];
	uint8 *p = TXbuffer;
	// head to buffer
	*p++ = PACK_HEAD;
	// address to buffer
	*p++ = ack;
	// dictate to buffer
	*p++ = dictate;
	// length to buffer
	*p++ = length & 0xff;
	*p++ = (length >> 8) & 0xff;
	// data to buffer
	memcpy(p, data, length);
	p += length;
	// crc to buffer
	uint16 crc = do_crc_table(TXbuffer, (_PACK_HEAD_LEN + length));
	*p++ = crc & 0xff;
	*p++ = (crc >> 8) & 0xff;
	// end to buffer
	*p++ = PACK_END & 0xff;
	*p++ = (PACK_END >> 8) & 0xff;
	*p++ = (PACK_END >> 16) & 0xff;
	*p++ = (PACK_END >> 24) & 0xff;
	// send data
	SendData(TXbuffer, (_PACK_HEAD_LEN + length + _PACK_END_LEN));
}

NetLH_Res CmdCheckPack(uint8 *RXbuffer)
{
	uint16 _crc16, crc16, data_len;
	uint8 *p = RXbuffer;

	if (*p++ != PACK_HEAD)
	{
		Cmd_Cnt = 0;
		uint8 tx_data[2];
		tx_data[0] = CMD_Error_Header & 0xff;
		tx_data[1] = CMD_Error_Header >> 8 & 0xff;
		ComAckPack(PACK_ACK, CMD_TX_ERROR, tx_data, 2);
		return NetLH_HEAD_ERR;
	}

	if (*p++ != PACK_CMD)
	{
		Cmd_Cnt = 0;
		uint8 tx_data[2];
		tx_data[0] = CMD_Error_Addr & 0xff;
		tx_data[1] = CMD_Error_Addr >> 8 & 0xff;
		ComAckPack(PACK_ACK, CMD_TX_ERROR, tx_data, 2);
		return NetLH_DEVICE_ADD_ERR;
	}

	p++; // dictate

	data_len = *p | (*(p + 1) << 8);
	p += 2;

	_crc16 = do_crc_table(RXbuffer, (_PACK_HEAD_LEN + data_len));
	crc16 = *(p + data_len) | (*(p + data_len + 1) << 8);

	if (_crc16 != crc16)
	{
		Cmd_Cnt = 0;
		uint8 tx_data[2];
		tx_data[0] = CMD_Error_Crc & 0xff;
		tx_data[1] = CMD_Error_Crc >> 8 & 0xff;
		ComAckPack(PACK_ACK, CMD_TX_ERROR, tx_data, 2);
		return NetLH_CRC_ERR;
	}

	return NetLH_RES_OK;
}

void CmdRun(uint8 *RXbuffer)
{
	uint8 *p = &RXbuffer[_LENGTH_INDEX];
	uint16 data_len = *p | (*(p + 1) << 8);
	p += 2;
	uint8 count = data_len / 11;

	for (int i = 0; i < count; i++, p += 11)
	{
		uint8 id = *p;

		Moto[id].MID = id;
		Moto[id].Mstep = *(p + 1) | (*(p + 2) << 8) | (*(p + 3) << 16) | (*(p + 4) << 24);
		Moto[id].Maccel = *(p + 5) | (*(p + 6) << 8);
		Moto[id].Mdecel = *(p + 7) | (*(p + 8) << 8);
		Moto[id].MotoSpeed = *(p + 9) | (*(p + 10) << 8);

		AxisMove(Moto[id].MID, Moto[id].Mstep, Moto[id].Maccel, Moto[id].Mdecel, Moto[id].MotoSpeed);
	}
}

void CmdStop(uint8 *RXbuffer)
{
	uint8 *p = &RXbuffer[_LENGTH_INDEX];
	uint16 data_len = *p | (*(p + 1) << 8);
	p += 2;

	for (int i = 0; i < data_len; i++, p++)
	{
		uint8 id = *p;
		srd[id].run_state = STOP;
		srd[id].lock = 0;
		Moto[id].Mflag = 0;
	}
}

void CmdQuery(uint8 *RXbuffer)
{
	uint8 *p = &RXbuffer[_LENGTH_INDEX];
	uint16 data_len = *p | (*(p + 1) << 8);
	p += 2;
	uint8 tx_data[data_len * 2];
	uint8 *tx_p = tx_data;

	for (int i = 0; i < data_len; i++, p++)
	{
		uint8 id = *p;
		uint8 run_state = srd[id].run_state == STOP ? CMD_03_Stop : CMD_03_Runing;
		*tx_p++ = id;
		*tx_p++ = run_state;
	}

	ComAckPack(PACK_ACK, CMD_TX_STATUS, tx_data, data_len * 2);
}

void CmdAnalysis()
{
	if (Cmd_Cnt >= 12)
	{
		uint8 *p = cmd_buffer;
		// head check
		if (*p++ != PACK_HEAD || *p++ != PACK_CMD)
		{
			Cmd_Cnt = 0;
		}
		p++; // dictate
		// get data len
		uint16 data_len = *p | (*(p + 1) << 8);
		// len check
		if (Cmd_Cnt == _PACK_HEAD_LEN + data_len + _PACK_END_LEN)
		{
			memcpy(cmd_RXbuffer, cmd_buffer, Cmd_Cnt);
			Cmd_Cnt = 0;
			if (NetLH_RES_OK == CmdCheckPack(cmd_RXbuffer))
			{
				DoComEvent = USART_COMEVENT;
			}
		}
		else
		{
			Cmd_Cnt = 0;
		}
	}
}

void CmdProcess()
{
	uint8 tx_data[2];
	switch (cmd_RXbuffer[_DICTATE_INDEX])
	{
	case CMD_RX_RUN:
		CmdRun(cmd_RXbuffer);
		break;
	case CMD_RX_STOP:
		CmdStop(cmd_RXbuffer);
		break;
	case CMD_RX_QUERY:
		CmdQuery(cmd_RXbuffer);
		break;
	default:
		tx_data[0] = CMD_NO_COM & 0xff;
		tx_data[1] = CMD_NO_COM >> 8 & 0xff;
		ComAckPack(PACK_ACK, CMD_TX_ERROR, tx_data, 2);
		break;
	}
}