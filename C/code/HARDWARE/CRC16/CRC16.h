#ifndef __CRC16_H
#define __CRC16_H
#include<stdint.h>

uint16_t crc_cal_by_byte(uint8_t *ptr, uint32_t len);
unsigned short do_crc_table(unsigned char *ptr, int len);
#endif
















