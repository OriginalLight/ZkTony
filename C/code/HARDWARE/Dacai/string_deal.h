#ifndef __STRING_DEAL_H
#define __STRING_DEAL_H
#include "sys.h"


	typedef union 
	{
		u8 CharData[4];
		float FloatData;	
	}Hex_Float;

  typedef union 
  {
	 		u8 CharData[4];
		  u32 Int32Data;	
 }HEX_Int32;
	

 	float Hex_to_Float(u8 *hex);
  void Float_to_Hex(u8 *s,float data);
 
   	u32 Hex_to_Int32(u8 *hex);
  void Int32_to_Hex(u8 *s,u32 data);

u32 ASCII_U32(char *s);
u16 ASCII_U16(char *s);

 
 
uint8_t is_digit(u8 ch);   //判断是否是数字
uint8_t is_letter(u8 ch);  //判断是否是字母
uint8_t is_space(u8 ch);   //判断是否是空格
uint8_t is_inquiry(u8 *s); //判断是否是查询指令

//ASCII-TO-INT32/Float
uint8_t Convert_ASCII_to_INT32(u8 *s); //提取字符串中等号'='后的整数,ASCII--->INT32
uint8_t Convert_ASCII_to_INT1(u8 *s);  //提取字符串中等号'='后的整数1值,ASCII--->INT
uint8_t Convert_ASCII_to_INT2(u8 *s);  //提取字符串中等号'='后的整数2值,ASCII--->INT

float Convert_ASCII_to_FLOAT(u8 *s);   //ASCII--->Float
void Convert_ASCII_to_FLOAT_Array(u8 *s,float *p); //ASCII--->Float_array (8 values)
void Convert_ASCII_to_INT_Array(u8 *s,u8 *p); //ASCII--->INT_array (8 values)

float Convert_HEX_to_FLOAT(u8 *s);
	

//Float-TO-ASCII

char *Float_to_String2(float number,char *strnum,uint8_t precision);
char *Float_to_String(float number,char *strnum,uint8_t precision);  //Float--->ASCII
char *Int_to_String(int16_t number,char *strnum); //Int--->ASCII
char *Int_to_String_Time(u8 number1,u8 number2,char *strnum);//INT_Time--->ASCII

float Extract_Floatfrom_PM1200(u8 *s); //Extract the float value from the frame
void Extract_Floatfrom_HM29(u8 *s,float *p); //Extract the float value from the frame
u16 Extract_Floatfrom_DTSD1352(u8 *s);

u32 Extract_Floatfrom_DTSD1352P(u8 *s);

void Extract_Floatfrom_DTSD1352_V(u8 *s,float *p,float *p1,float *p2);
void Extract_Floatfrom_DTSD1352_I(u8 *s,float *p,float *p1,float *p2);
void Extract_Floatfrom_DTSD1352_P(u8 *s,float *p);
void  Extract_Floatfrom_DTSD1352_W(u8 *s,float *p);

void Extract_Floatfrom_Wind(u8 *s,float *p);
void Extract_Floatfrom_SM1911(u8 *s,float *p,float *p1);

extern u8 pMem[4];

#endif
