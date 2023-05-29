#include "DataDeal.h"
	
	float Hex_to_Float(u8 *hex)
	{
		Hex_Float HexFloat;
		
		if(hex==(u8*)0)
     return 0.0;	
    HexFloat.CharData[0]=hex[6];
		HexFloat.CharData[1]=hex[5];
		HexFloat.CharData[2]=hex[4];
		HexFloat.CharData[3]=hex[3];
		return HexFloat.FloatData;
	}
	
	
	void Float_to_Hex(u8 *s,float data)
	{
		Hex_Float HexFloat;
		HexFloat.FloatData= data;
    s[3]=HexFloat.CharData[3];
		s[4]=HexFloat.CharData[2];
		s[5]=HexFloat.CharData[1];
		s[6]=HexFloat.CharData[0];
		
	}
	
	 
   	u32 Hex_to_Int32(u8 *hex)
	{
			HEX_Int32 HEXInt32;
				if(hex==(u8*)0)
     return 0.0;	
    HEXInt32.CharData[0]=hex[6];
		HEXInt32.CharData[1]=hex[5];
		HEXInt32.CharData[2]=hex[4];
		HEXInt32.CharData[3]=hex[3];
		return HEXInt32.Int32Data;
		
	}
  void Int32_to_Hex(u8 *s,u32 data)
	{
		
		HEX_Int32 HEXInt32;
				HEXInt32.Int32Data= data;
    s[3]=HEXInt32.CharData[3];
		s[4]=HEXInt32.CharData[2];
		s[5]=HEXInt32.CharData[1];
		s[6]=HEXInt32.CharData[0];
		
	}
	
	
	
	
	