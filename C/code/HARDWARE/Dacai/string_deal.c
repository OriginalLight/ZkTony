#include "sys.h"
#include "math.h"
#include "string_deal.h" 


//////////////////////////////////////////////////////////////////////////////////	 
						  
////////////////////////////////////////////////////////////////////////////////// 

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


//Int/Float-TO-ASCII
char *Float_to_String(float number,char *strnum,uint8_t precision);  //Float--->ASCII
char *Int_to_String(int16_t number,char *strnum); //Int--->ASCII
char *Int_to_String_Time(u8 number1,u8 number2,char *strnum);//INT_Time--->ASCII

float Extract_Floatfrom_PM1200(u8 *s); //Extract the float value from the frame
void Extract_Floatfrom_HM29(u8 *s,float *p); //Extract the float value from the frame

u8 pMem[4];
/** 
  * @brief 判断是否是数字
  * @param  
  * @retval 
  */ 
uint8_t is_digit(u8 ch)  
{  
    if(ch>='0'&&ch<='9')  
        return 1;  
    else  
        return 0;  
}
/** 
  * @brief 判断是否是字母
  * @param  
  * @retval 
  */ 
uint8_t is_letter(u8 ch)  
{  
    if(ch>='A'&&ch<='Z')  
        return 1;  
    else  
        return 0;  
}
/** 
  * @brief 判断是否是空格
  * @param  
  * @retval 
  */ 
uint8_t is_space(u8 ch)  
{  
    if(ch==' ')  
        return 1;  
    else  
        return 0;  
}
/**
  * @brief  判断是否是查询指令
  * @param  
  * @retval 
  */
uint8_t is_inquiry(u8 *s)
{
	uint8_t i=0;
	//assert(s != NULL); //判断字符串是否为空 
	while(s[i] != '=') i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	if(s[i] == '?')
			return 1;
	else
		return 0;
}

/** 
  * @brief    提取字符串中等号'='后的整数,ASCII--->INT32
  * @param  
  * @retval 
  */ 
uint8_t Convert_ASCII_to_INT32(u8 *s)
{
	uint32_t value;
	uint8_t i=0;
	//assert(s != NULL); //判断字符串是否为空 
	while(s[i] != '=') i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	
	for(value = 0;is_digit(s[i]);i++)//计算小数点前的数值 
		value = value*10 + (s[i]-'0');  
	return value;	
}

/** 
  * @brief    提取字符串中等号'='后的整数1值,ASCII--->INT
  * @param  
  * @retval 
  */ 
uint8_t Convert_ASCII_to_INT1(u8 *s)
{
	uint8_t value;
	uint8_t i=0;
	//assert(s != NULL); //判断字符串是否为空 
	while(s[i] != '=') i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	
	for(value = 0;is_digit(s[i]);i++)//计算小数点前的数值 
		value = value*10 + (s[i]-'0');  
	return value;	
}
/** 
  * @brief    提取字符串中等号'='后的整数2值,ASCII--->INT
  * @param  
  * @retval 
  */ 
uint8_t Convert_ASCII_to_INT2(u8 *s)
{
	uint8_t value;
	uint8_t i=0;
	//assert(s != NULL); //判断字符串是否为空 
	while(s[i] != '=') i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	
	while(s[i] != '-') i++;  //除去第二个冒号前的变量符号的内容
	i++;
	
	for(value = 0;is_digit(s[i]);i++)//计算小数点前的数值 
		value = value*10 + (s[i]-'0');  
	return value;	
}


float Convert_HEX_to_FLOAT(u8 *s)
{
	
	
	
	
}



/** 
  * @brief   ASCII--->Float
  * @param  
  * @retval 
  */ 
float Convert_ASCII_to_FLOAT(u8 *s)
{
	float power,value;
	uint8_t i=0,flag=0;
	//assert(s != NULL); //判断字符串是否为空 
	while((s[i] != '=')&&(s[i] != '>')) i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	
	if(s[i] == '-')   //判断是否是负数
	{
		i++;
		flag = 1;
	}
	else if(s[i] == '+')  //判断是带有正号
	{
		i++;
		flag = 0;
	}
	
	for(value = 0.0;is_digit(s[i]);i++)//计算小数点前的数值 
	value = value*10.0 + (s[i]-'0');
	if(s[i]=='.')
		i++;
	for(power = 1.0;is_digit(s[i]);i++)//计算小数点后的数值
	{
		value = value*10.0+(s[i]-'0');
		power *= 10.0;  
  }
	
	if(flag == 1)   //添加符号
	{
		return (float)(-1*(value/power));
	}
	else
	{
		 return (float)(value/power);
	}   	
}
/** 
  * @brief   ASCII--->Float_array (8 values)
  * @param  
  * @retval 
  */ 
void Convert_ASCII_to_FLOAT_Array(u8 *s,float *p)
{
	float power,value;
	u8 i=0,flag=0,j=0;
	//assert(s != NULL); //判断字符串是否为空 
	while((s[i] != '=')&&(s[i] != '>')) i++; //除去等号前的变量符号的内容
	i++;               //指向等号后第一个量	
	
	while(s[i] != '\r')
	{
		if(s[i] == '-')   //判断是否是负数
		{
			i++;
			flag = 1;
		}
		else if(s[i] == '+')  //判断是带有正号
		{
			i++;
			flag = 0;
		}
	
		for(value = 0.0;is_digit(s[i]);i++)//计算小数点前的数值 
		value = value*10.0 + (s[i]-'0');
		if(s[i]=='.')
			i++;
		for(power = 1.0;is_digit(s[i]);i++)//计算小数点后的数值
		{
			value = value*10.0+(s[i]-'0');
			power *= 10.0;  
		}
	
		if(flag == 1)   //添加符号
		{
			p[j++] = (float)(-1*(value/power));
		}
		else
		{
			p[j++] = (float)(value/power);
		}
	}
}
/** 
  * @brief   ASCII--->INT(8 value)
  * @param  
  * @retval 
  */
void Convert_ASCII_to_INT_Array(u8 *s,u8 *p)
{
	u8 value=0;
	if(s[0] == '!')
	{
		if((s[1] == '0')&&(s[2] == '1'))
		{
			value = (s[3]- 0x30)*0x10 + (s[4]- 0x30);
			p[0] = value & 0x01;
			p[1] = value & 0x02;
			p[2] = value & 0x04;
			p[3] = value & 0x08;
			p[4] = value & 0x10;
			p[5] = value & 0x20;
			p[6] = value & 0x40;
			p[7] = value & 0x80;
		}
	}
}
/** 
  * @brief   INT--->ASCII
  * @param  
  * @retval 
  */
char *Int_to_String(int16_t number,char *strnum)
 {
	 uint8_t i=0,n=0,flag=0;
	 uint8_t temp;
	 if(number < 0)
	 {
		 flag = 1;
		 number = -number;
	 }
	 
	 do{
		 *(strnum+n) = number%10+48;
		 number = number/10;
			n++;
	 }while(number > 0);
	 
	 if(flag)
		 {
			 *(strnum+n) = '-';
			 n++;
		 }
		 
		 for(i=0;i<n/2;i++)
		 {
			 temp = *(strnum+i);
			 *(strnum+i) = *(strnum+n-i-1);
			 *(strnum+n-i-1) = temp;
		 }
		 
		 strnum[n] = '\r';
		 strnum[n+1] = '\n';
		 strnum[n+2]= '\0';
		 
		 return strnum;
 }
 
/** 
  * @brief   Float--->ASCII
  * @param  
  * @retval 
  */
char *Float_to_String(float number,char *strnum,uint8_t precision)
 {
	 uint8_t j=0,i=0,n=0;
	 uint8_t temp,flag=0;
	 int32_t integer;
	 float   decimal;
		 
	 if(number < 0)		//整数部分的转换
	 {
		 flag = 1;//0.0245   0.24  8.24 
		 number = -number;
	 }
	 
	 integer = (int32_t)(number/1);
	 decimal = (float)(number-integer);
	 	 
	 do
	  {
			*(strnum+n) = integer%10+48;
			integer = integer/10;
			n++;
		}while(integer>0);
		
		if(flag)
		 {
			 *(strnum+n) = '-';
			 n++;
		 }
		 
		 for(i=0;i<n/2;i++)
		 {
			 temp = *(strnum+i);
			 *(strnum+i) = *(strnum+n-i-1);
			 *(strnum+n-i-1) = temp;
		 }
		 strnum[n]='.';
		 n++;
		 integer = (int32_t)(decimal*pow(10,precision));		//对小数部分转化  24
		 		 
		 
		// do  0.6   6 
		// {
		 while(precision){
			 precision--;
			 *(strnum+n+j) = integer/(int32_t)pow(10,precision)+48;
			 integer = integer%(int32_t)(pow(10,precision));
			 j++;
			 
			 
		 }
		// }while(precision);
		 
//		 do
//		 {
//			 *(strnum+n+j) = integer%10+48;
//			 integer = integer/10;
//			 j++;
//		 }while(integer>0);
//		 
//		 for(i=0;i<j/2;i++)
//		 {
//			 temp = *(strnum+n+i);
//			 *(strnum+n+i) = *(strnum+n+j-i-1);
//			 *(strnum+n+j-i-1) = temp;
//		 }
		 strnum[n+j] = '\r';
		 strnum[n+j+1] = '\n';
		 strnum[n+j+2]= '\0';
		 
		 return strnum;
 }
 
 char *Float_to_String2(float number,char *strnum,uint8_t precision)
 {
	 	 uint8_t j=0,i=0,n=0;
	 uint8_t temp,flag=0;
	 int32_t integer;
	 float   decimal;
		 
	 if(number < 0)		//整数部分的转换
	 {
		 flag = 1;
		 number = -number;
	 }
	 
	 integer = (int32_t)(number/1);
	 decimal = (float)(number-integer);
	 	 
	 do
	  {
			*(strnum+n) = integer%10+48;
			integer = integer/10;
			n++;
		}while(integer>0);
		
		if(flag)
		 {
			 *(strnum+n) = '-';
			 n++;
		 }
		 
		 for(i=0;i<n/2;i++)
		 {
			 temp = *(strnum+i);
			 *(strnum+i) = *(strnum+n-i-1);
			 *(strnum+n-i-1) = temp;
		 }
		 strnum[n]='.';
		 n++;
		 integer = (int32_t)(decimal*pow(10,precision));		//对小数部分转化
		 do
		 {
			 *(strnum+n+j) = integer%10+48;
			 integer = integer/10;
			 j++;
		 }while(integer>0);
		 
		 for(i=0;i<j/2;i++)
		 {
			 temp = *(strnum+n+i);
			 *(strnum+n+i) = *(strnum+n+j-i-1);
			 *(strnum+n+j-i-1) = temp;
		 }
		 strnum[n+j] = '\0';

		 return strnum;
	 
	 
	 
	 
 }
 
/** 
  * @brief   INT_Time--->ASCII
  * @param  
  * @retval 
  */

char *Int_to_String_Time(u8 number1,u8 number2,char *strnum)
 {
	 uint8_t i=0;
	 
	 strnum[i++] = number1/10+0x30;
	 strnum[i++] = number1%10+0x30;
	 strnum[i++] = '-';
	 strnum[i++] = number2/10+0x30;
	 strnum[i++] = number2%10+0x30;
	 
	 strnum[i++] = '\r';
	 strnum[i++] = '\n';
	 strnum[i]= '\0';
	 
	 return strnum;
 }
/** 
  * @brief   Extract the float value from the frame
  * @param  
  * @retval 
  */
float Extract_Floatfrom_PM1200(u8 *s)
{
	u8 i,j,value1 = 0;
	u8 value2 = 0;
	float fvalue = 0.0;
	if(s[2] == 0x04)
	{
		value1 = (s[3]<<1)|((s[4]&0x80)>>7);
		value2 = (u8)(value1 -127);
		if(value2 >= 0)
		{
			fvalue +=1;
		}
		for(i=1;i<8;i++)
		{
			if(s[4]&(0x80>>i))
			{
				fvalue += pow(0.5,i);
			}
		}
		for(j=0;j<8;j++)
		{
			if(s[5]&(0x80>>j))
			{
				fvalue += pow(0.5,i+j);
			}
		}
		fvalue = fvalue*pow(2,value2);
		if(s[3]&0x80)
		{
			//the value is negative
			fvalue = fvalue*(-1);
		}
		return fvalue;
	}
	return 0; //err
}

u16 Extract_Floatfrom_DTSD1352(u8 *s)
{
	//u8 i,j,value1 = 0;
	//u8 value2 = 0;
	u16 value = 0;
	//u32 value1 = 0;
	//u16 fvalue = 0.0;
	if(s[2] == 0x02)
	{

	value = s[3];
	value = (value<<8)+(u8)s[4];
		
	//fvalue=	value;
		
	return value;
	}
	

	
	
//		if(s[2] == 0x04)
//	{
//		value1 = (s[3]<<1)|((s[4]&0x80)>>7);
//		value2 = (u8)(value1 -127);
//		if(value2 >= 0)
//		{
//			fvalue +=1;
//		}
//		for(i=1;i<8;i++)
//		{
//			if(s[4]&(0x80>>i))
//			{
//				fvalue += pow(0.5,i);
//			}
//		}
//		for(j=0;j<8;j++)
//		{
//			if(s[5]&(0x80>>j))
//			{
//				fvalue += pow(0.5,i+j);
//			}
//		}
//		fvalue = fvalue*pow(2,value2);
//		if(s[3]&0x80)
//		{
//			//the value is negative
//			fvalue = fvalue*(-1);
//		}
//		return fvalue;
//	}
	
	
	return 0; //err
}




u32 Extract_Floatfrom_DTSD1352P(u8 *s)

{
	u32 value = 0;
	
	if(s[2] == 0x04)
	{
	value=s[3];
	value = (value<<8)+(u8)s[4];
	value = (value<<8)+(u8)s[5];
	value = (value<<8)+(u8)s[6];
		
	return value;
	}
	return 0;

}
//100
void Extract_Floatfrom_DTSD1352_V(u8 *s,float *p,float *p1,float *p2)

{
	u16 value = 0;
	u16 value1=0;
	u16 value2=0;
	
	
	if(s[2] == 0x06)
	{
	 value=s[3];
	 value = (value<<8)+(u8)s[4];
	 value1 = s[5];
	 value1 = (value1<<8)+(u8)s[6];
   value2 =s[7];
	 value2 = (value2<<8)+(u8)s[8];		
	*p= value/10.0;
	*p1= value1/10.0;
	*p2= value2/10.0;
	}

	

}


//10
void Extract_Floatfrom_DTSD1352_I(u8 *s,float *p,float *p1,float *p2)

{
	u16 value = 0;
	u16 value1=0;
	u16 value2=0;
	
	if(s[2] == 0x06)
	{
	 value=s[3];
	 value = (value<<8)+(u8)s[4];
	 value1 = s[5];
	 value1 = (value1<<8)+(u8)s[6];
   value2 =s[7];
	 value2 = (value2<<8)+(u8)s[8];		
	
	*p= value/100.0;
	*p1= value1/100.0;
	*p2= value2/100.0;
	}
	


}
void Extract_Floatfrom_DTSD1352_P(u8 *s,float *p)

{
  u16 value=0;
	
	if(s[2] == 0x02)
	{
	  value = s[3];
	  value = (value<<8)+(u8)s[4];
		*p =value/1000.0;
	}
	
	

}


void  Extract_Floatfrom_DTSD1352_W(u8 *s,float *p)

{
	u32 value = 0;
	
	if(s[2] == 0x04)
	{
		value=s[3];
		value = (value<<8)+(u8)s[4];
		value = (value<<8)+(u8)s[5];
		value = (value<<8)+(u8)s[6];
		
		*p =value/100.0;

	}
	

}




 /** 
  * @brief   Extract the float value from the frame
  * @param  
  * @retval 
  */
void Extract_Floatfrom_HM29(u8 *s,float *p)
{
	float value1 = 0,value2 = 0;
	if(s[1] == '2')
	{
		value1 = (float)((s[2]-0x30)*10000+(s[3]-0x30)*1000+(s[4]-0x30)*100+(s[5]-0x30)*10+(s[6]-0x30));
		//value2 = (float)(pow(10,(6-(s[7]-0x30))));
		//value2 = (float)(pow(10,((s[7]-0x30))));
	//	if((s[7]=='3')||(s[7]=='4'))
	//	{
			 *p = value1/10000.00;
	//	}
		//*p = (float)(s[2]*10000+s[3]*1000+s[4]*100+s[5]*10+s[6])/(float)(pow(10,s[6]-1));
	}
}	
 
//void Extract_Floatfrom_Wind(u8 *s,float *p)
//{
//	u16 value = 0;
//	//float fvalue = 0.0;
//	value = s[3];
//	value = (value<<8)+(u8)s[4];
//	*p = value/100.0;
//}



void Extract_Floatfrom_Wind(u8 *s,float *p)
{
//	u32 value = 0;
//	//float fvalue = 0.0;
//	value = s[3];
//	value = (value<<8)+(u8)s[4];
////	value = (value<<8)+(u8)s[5];
////	value = (value<<8)+(u8)s[6];
//	*p = value/100.0;
	union 
	{
		u8 CharData[4];
		float FloatData;	
	}DataStruct;
DataStruct.CharData[0]= s[6];
DataStruct.CharData[1]= s[5];
DataStruct.CharData[2]= s[4];
DataStruct.CharData[3]= s[3];
	//DataStruct.CharData[0]= s[3];
//DataStruct.CharData[0]= 0x00;
//DataStruct.CharData[1]= 0x00;
//DataStruct.CharData[2]= 0x28;
//DataStruct.CharData[3]= 0x41;
	if(DataStruct.FloatData<1) DataStruct.FloatData=0;
	*p=DataStruct.FloatData;
	
	
//	*p=*(float*)&CharData;
	
}



void Extract_Floatfrom_SM1911(u8 *s,float *p,float *p1)
{
	u16 value = 0;
	u16 value1 = 0;
	//float fvalue = 0.0;
	value = s[3];
	value = (value<<8)+(u8)s[4];
	*p = value/100.0;

  value1 = s[5];
	value1 = (value1<<8)+(u8)s[6];
	
	*p1 = value1/100.0;
		
}



	float Hex_to_Float(u8 *hex)
	{
		Hex_Float HexFloat;
		
		if(hex==(u8*)0)
     return 0.0;	
    HexFloat.CharData[0]=hex[3];
		HexFloat.CharData[1]=hex[4];
		HexFloat.CharData[2]=hex[5];
		HexFloat.CharData[3]=hex[6];
		return HexFloat.FloatData;
	}
	
	
	void Float_to_Hex(u8 *s,float data)
	{
		Hex_Float HexFloat;
		HexFloat.FloatData= data;
    s[3]=HexFloat.CharData[0];
		s[4]=HexFloat.CharData[1];
		s[5]=HexFloat.CharData[2];
		s[6]=HexFloat.CharData[3];
		
	}
	
	 
   	u32 Hex_to_Int32(u8 *hex)
	{
			HEX_Int32 HEXInt32;
				if(hex==(u8*)0)
     return 0.0;	
    HEXInt32.CharData[0]=hex[3];
		HEXInt32.CharData[1]=hex[4];
		HEXInt32.CharData[2]=hex[5];
		HEXInt32.CharData[3]=hex[6];
		return HEXInt32.Int32Data;
		
	}
  void Int32_to_Hex(u8 *s,u32 data)
	{
		
		HEX_Int32 HEXInt32;
				HEXInt32.Int32Data= data;
    s[3]=HEXInt32.CharData[0];
		s[4]=HEXInt32.CharData[1];
		s[5]=HEXInt32.CharData[2];
		s[6]=HEXInt32.CharData[3];
		
	}
	
	
	
u32 ASCII_U32(char *s)
{
	u32 cov=0;
	while(*s != '\0')
	{
		cov= cov*10+(*s-0x30);
		s++;
	}
	return cov;
}
		

s32 ASCII_INT32(char *s)
{
	s32 cov=0;
	while(*s != '\0')
	{
		cov= cov*10+(*s-0x30);
		s++;
	}
	return cov;
}


u16 ASCII_U16(char *s)
{
	u16 covs =0;
	while(*s != '\0')
	{
		covs= covs*10+(*s-0x30);
		s++;
	}
	return covs;
}
	
//u8 ASCII_U8(char *s)
//{
//	u16 covs =0;
//	while(*s != '\0')
//	{
//		covs= covs*10+(*s-0x30);
//		s++;
//	}
//	return covs;
//}
	

///*************************************************************************************
//函数名：CheckCSFun( )
//函数功能：检测校验码CS是否正确
//函数参数：*pData 指向帧头的指针，Len CS前的长度
//输出参数：无
//函数返回值：1:CS检测正确，0：CS检测错误
//*************************************************************************************/
//char8 CheckCSFun(char8 *pData, int16 Len)
//{	
//	INT8U CS = 0;
//	INT16U i;
//	
//	for(i=0; i<Len; i++)
//	{	//
//		CS += pData[i];
//	}
//	
//	if(pData[Len] == CS)
//	{
//		return(0);
//	}
//	return(-1);
//}


///*************************************************************************************
//函数名：计算CS函数
//函数功能：计算CS
//函数参数：*pData 指向帧头的指针，Len CS前的长度
//函数返回值：CS值
//*************************************************************************************/
//char8 CalculateCS(char8 *pData, int16 Len)
//{	
//	INT8U CS = 0;
//	INT16U i;
//	
//	for(i=0; i<Len; i++)
//	{	
//		CS += pData[i];
//	}
//		
//	return(CS);
//}


///*************************************************************************************
//函数名：BIN2BCD()
//函数功能：二进制转BCD
//输入参数：bin 二进制码
//输出参数：无
//函数返回值：BCD码
//*************************************************************************************/
//uchar BIN2BCD(uchar bin)
//{
//    uchar bcd = 0;

//    bcd =(uchar) ((((bin) / 10) << 4) + (bin) % 10);

//    return bcd;
//}
///*************************************************************************************
//函数名：BCD2BIN()
//函数功能：BCD转二进制
//输入参数：bcd BCD码
//输出参数：无
//函数返回值：二进制码
//*************************************************************************************/
//uchar BCD2BIN(uchar bcd)
//{
//	uchar bin = 0;

//    bin = (uchar)((bcd & 0x0f) +(bcd >> 4) * 10);

//    return bin;
//}

//					ReadReply[9] = BIN2BCD(temp[0]%100); temp[0] = temp[0]/100;
//					ReadReply[10] = BIN2BCD(temp[0]%100); temp[0] = temp[0]/100;
//					ReadReply[11] = BIN2BCD(temp[0]%100); temp[0] = temp[0]/100;
