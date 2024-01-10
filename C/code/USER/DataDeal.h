#ifndef __DataDeal_H
#define __DataDeal_H	 
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
 

#endif
