#include "hmi_driver.h"

#define TX_8(P1) SEND_DATA((P1)&0xFF)	   // ���͵����ֽ�
#define TX_8N(P, N) SendNU8((uint8 *)P, N) // ����N���ֽ�
#define TX_16(P1)    \
	TX_8((P1) >> 8); \
	TX_8(P1)								  // ����16λ����
#define TX_16N(P, N) SendNU16((uint16 *)P, N) // ����N��16λ����
#define TX_32(P1)      \
	TX_16((P1) >> 16); \
	TX_16((P1)&0xFFFF) // ����32λ����

#if (CRC16_ENABLE)

static uint16 _crc16 = 0xffff;
static void AddCRC16(uint8 *buffer, uint16 n, uint16 *pcrc)
{
	uint16 i, j, carry_flag, a;

	for (i = 0; i < n; i++)
	{
		*pcrc = *pcrc ^ buffer[i];
		for (j = 0; j < 8; j++)
		{
			a = *pcrc;
			carry_flag = a & 0x0001;
			*pcrc = *pcrc >> 1;
			if (carry_flag == 1)
				*pcrc = *pcrc ^ 0xa001;
		}
	}
}

uint16 CheckCRC16(uint8 *buffer, uint16 n)
{
	uint16 crc0 = 0x0;
	uint16 crc1 = 0xffff;

	if (n >= 2)
	{
		crc0 = ((buffer[n - 2] << 8) | buffer[n - 1]);
		AddCRC16(buffer, n - 2, &crc1);
	}

	return (crc0 == crc1);
}

void SEND_DATA(uint8 c)
{
	AddCRC16(&c, 1, &_crc16);
	SendChar(c);
}

void BEGIN_CMD()
{
	TX_8(0XEE);
	_crc16 = 0XFFFF; // ��ʼ����CRC16
}

void END_CMD()
{
	uint16 crc16 = _crc16;
	TX_16(crc16); // ����CRC16
	TX_32(0XFFFCFFFF);
}

#else // NO CRC16

#define SEND_DATA(P) SendChar(P)
#define BEGIN_CMD() TX_8(0XEE)
#define END_CMD() TX_32(0XFFFCFFFF)

#endif

void DelayMS(unsigned int n)
{
	int i, j;
	for (i = n; i > 0; i--)
		for (j = 1000; j > 0; j--)
			;
}

void SendStrings(uchar *str)
{
	while (*str)
	{
		TX_8(*str);
		str++;
	}
}

void SendNU8(uint8 *pData, uint16 nDataLen)
{
	uint16 i = 0;
	for (; i < nDataLen; ++i)
	{
		TX_8(pData[i]);
	}
}

void SendNU16(uint16 *pData, uint16 nDataLen)
{
	uint16 i = 0;
	for (; i < nDataLen; ++i)
	{
		TX_16(pData[i]);
	}
}

void SetHandShake()
{
	BEGIN_CMD();
	TX_8(0x00);
	END_CMD();
}

void SetFcolor(uint16 color)
{
	BEGIN_CMD();
	TX_8(0x41);
	TX_16(color);
	END_CMD();
}

void SetBcolor(uint16 color)
{
	BEGIN_CMD();
	TX_8(0x42);
	TX_16(color);
	END_CMD();
}

void ColorPicker(uint8 mode, uint16 x, uint16 y, uint16 z)
{
	BEGIN_CMD();
	TX_8(0xA3);
	TX_8(mode);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	END_CMD();
}

void GUI_CleanScreen()
{
	BEGIN_CMD();
	TX_8(0x01);
	END_CMD();
}

void SetTextSpace(uint8 x_w, uint8 y_w, uint8 z_w)
{
	BEGIN_CMD();
	TX_8(0x43);
	TX_8(x_w);
	TX_8(y_w);
	TX_8(z_w);
	END_CMD();
}

void SetFont_Region(uint8 enable, uint16 width, uint16 height)
{
	BEGIN_CMD();
	TX_8(0x45);
	TX_8(enable);
	TX_16(width);
	TX_16(height);
	END_CMD();
}

void SetFilterColor(uint16 fillcolor_dwon, uint16 fillcolor_up)
{
	BEGIN_CMD();
	TX_8(0x44);
	TX_16(fillcolor_dwon);
	TX_16(fillcolor_up);
	END_CMD();
}

void DisText(uint16 x, uint16 y, uint16 z, uint8 back, uint8 font, uchar *strings)
{
	BEGIN_CMD();
	TX_8(0x20);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_8(back);
	TX_8(font);
	SendStrings(strings);
	END_CMD();
}

void DisCursor(uint8 enable, uint16 x, uint16 y, uint16 z, uint8 width, uint8 height)
{
	BEGIN_CMD();
	TX_8(0x21);
	TX_8(enable);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_8(width);
	TX_8(height);
	END_CMD();
}

void DisFull_Image(uint16 image_id, uint8 masken)
{
	BEGIN_CMD();
	TX_8(0x31);
	TX_16(image_id);
	TX_8(masken);
	END_CMD();
}

void DisArea_Image(uint16 x, uint16 y, uint16 z, uint16 image_id, uint8 masken)
{
	BEGIN_CMD();
	TX_8(0x32);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16(image_id);
	TX_8(masken);
	END_CMD();
}

void DisCut_Image(uint16 x, uint16 y, uint16 z, uint16 image_id, uint16 image_x, uint16 image_y, uint16 image_z, uint16 image_l, uint16 image_w, uint8 masken)
{
	BEGIN_CMD();
	TX_8(0x33);
	TX_16(x);
	TX_16(y);
	TX_16(image_id);
	TX_16(image_x);
	TX_16(image_y);
	TX_16(image_z);
	TX_16(image_l);
	TX_16(image_w);
	TX_8(masken);
	END_CMD();
}

void DisFlashImage(uint16 x, uint16 y, uint16 z, uint16 flashimage_id, uint8 enable, uint8 playnum)
{
	BEGIN_CMD();
	TX_8(0x80);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16(flashimage_id);
	TX_8(enable);
	TX_8(playnum);
	END_CMD();
}

void GUI_Dot(uint16 x, uint16 y, uint16 z)
{
	BEGIN_CMD();
	TX_8(0x50);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	END_CMD();
}

void GUI_Line(uint16 x0, uint16 y0, uint16 z0, uint16 x1, uint16 y1, uint16 z1)
{
	BEGIN_CMD();
	TX_8(0x51);
	TX_16(x0);
	TX_16(y0);
	TX_16(x1);
	TX_16(y1);
	TX_16(z0);
	TX_16(z1);
	END_CMD();
}

void GUI_ConDots(uint8 mode, uint16 *dot, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x63);
	TX_8(mode);
	TX_16N(dot, dot_cnt * 2);
	END_CMD();
}

void GUI_ConSpaceDots(uint16 x, uint16 x_space, uint16 *dot_y, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x59);
	TX_16(x);
	TX_16(x_space);
	TX_16N(dot_y, dot_cnt);
	END_CMD();
}

void GUI_FcolorConOffsetDots(uint16 x, uint16 y, uint16 z, uint16 *dot_offset, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x75);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16N(dot_offset, dot_cnt);
	END_CMD();
}

void GUI_BcolorConOffsetDots(uint16 x, uint16 y, uint16 z, uint8 *dot_offset, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x76);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16N(dot_offset, dot_cnt);
	END_CMD();
}

void SetPowerSaving(uint8 enable, uint8 bl_off_level, uint8 bl_on_level, uint8 bl_on_time)
{
	BEGIN_CMD();
	TX_8(0x77);
	TX_8(enable);
	TX_8(bl_off_level);
	TX_8(bl_on_level);
	TX_8(bl_on_time);
	END_CMD();
}

void GUI_FcolorConDots(uint16 *dot, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x68);
	TX_16N(dot, dot_cnt * 2);
	END_CMD();
}

void GUI_BcolorConDots(uint16 *dot, uint16 dot_cnt)
{
	BEGIN_CMD();
	TX_8(0x69);
	TX_16N(dot, dot_cnt * 2);
	END_CMD();
}

void GUI_Circle(uint16 x, uint16 y, uint16 z, uint16 r)
{
	BEGIN_CMD();
	TX_8(0x52);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16(r);
	END_CMD();
}

void GUI_CircleFill(uint16 x, uint16 y, uint16 z, uint16 r)
{
	BEGIN_CMD();
	TX_8(0x53);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16(r);
	END_CMD();
}

void GUI_Arc(uint16 x, uint16 y, uint16 z, uint16 r, uint16 sa, uint16 ea)
{
	BEGIN_CMD();
	TX_8(0x67);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_16(r);
	TX_16(sa);
	TX_16(ea);
	END_CMD();
}

void GUI_Rectangle(uint16 x0, uint16 y0, uint16 x1, uint16 y1)
{
	BEGIN_CMD();
	TX_8(0x54);
	TX_16(x0);
	TX_16(y0);
	TX_16(x1);
	TX_16(y1);
	END_CMD();
}

void GUI_RectangleFill(uint16 x0, uint16 y0, uint16 x1, uint16 y1)
{
	BEGIN_CMD();
	TX_8(0x55);
	TX_16(x0);
	TX_16(y0);
	TX_16(x1);
	TX_16(y1);
	END_CMD();
}

void GUI_Ellipse(uint16 x0, uint16 y0, uint16 x1, uint16 y1)
{
	BEGIN_CMD();
	TX_8(0x56);
	TX_16(x0);
	TX_16(y0);
	TX_16(x1);
	TX_16(y1);
	END_CMD();
}

void GUI_EllipseFill(uint16 x0, uint16 y0, uint16 x1, uint16 y1, uint16 z0, uint16 z1)
{
	BEGIN_CMD();
	TX_8(0x57);
	TX_16(x0);
	TX_16(y0);
	TX_16(z0);
	TX_16(x1);
	TX_16(y1);
	TX_16(z1);
	END_CMD();
}

void SetBackLight(uint8 light_level)
{
	BEGIN_CMD();
	TX_8(0x60);
	TX_8(light_level);
	END_CMD();
}

void SetBuzzer(uint8 time)
{
	BEGIN_CMD();
	TX_8(0x61);
	TX_8(time);
	END_CMD();
}

void GUI_AreaInycolor(uint16 x0, uint16 y0, uint16 x1, uint16 y1, uint16 z0, uint16 z1)
{
	BEGIN_CMD();
	TX_8(0x65);
	TX_16(x0);
	TX_16(y0);
	TX_16(z0);
	TX_16(x1);
	TX_16(y1);
	TX_16(z0);
	END_CMD();
}

void SetTouchPaneOption(uint8 enbale, uint8 beep_on, uint8 work_mode, uint8 press_calibration)
{
	uint8 options = 0;

	if (enbale)
		options |= 0x01;
	if (beep_on)
		options |= 0x02;
	if (work_mode)
		options |= (work_mode << 2);
	if (press_calibration)
		options |= (press_calibration << 5);

	BEGIN_CMD();
	TX_8(0x70);
	TX_8(options);
	END_CMD();
}

void CalibrateTouchPane()
{
	BEGIN_CMD();
	TX_8(0x72);
	END_CMD();
}

void TestTouchPane()
{
	BEGIN_CMD();
	TX_8(0x73);
	END_CMD();
}

void LockDeviceConfig(void)
{
	BEGIN_CMD();
	TX_8(0x09);
	TX_8(0xDE);
	TX_8(0xED);
	TX_8(0x13);
	TX_8(0x31);
	END_CMD();
}

void UnlockDeviceConfig(void)
{
	BEGIN_CMD();
	TX_8(0x08);
	TX_8(0xA5);
	TX_8(0x5A);
	TX_8(0x5F);
	TX_8(0xF5);
	END_CMD();
}

void SetCommBps(uint8 option)
{
	BEGIN_CMD();
	TX_8(0xA0);
	TX_8(option);
	END_CMD();
}

void WriteLayer(uint8 layer)
{
	BEGIN_CMD();
	TX_8(0xA1);
	TX_8(layer);
	END_CMD();
}

void DisplyLayer(uint8 layer)
{
	BEGIN_CMD();
	TX_8(0xA2);
	TX_8(layer);
	END_CMD();
}

void CopyLayer(uint8 src_layer, uint8 dest_layer)
{
	BEGIN_CMD();
	TX_8(0xA4);
	TX_8(src_layer);
	TX_8(dest_layer);
	END_CMD();
}

void ClearLayer(uint8 layer)
{
	BEGIN_CMD();
	TX_8(0x05);
	TX_8(layer);
	END_CMD();
}

void GUI_DispRTC(uint8 enable, uint8 mode, uint8 font, uint16 color, uint16 x, uint16 y, uint16 z)
{
	BEGIN_CMD();
	TX_8(0x85);
	TX_8(enable);
	TX_8(mode);
	TX_8(font);
	TX_16(color);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	END_CMD();
}

void WriteUserFlash(uint32 startAddress, uint16 length, uint8 *_data)
{
	BEGIN_CMD();
	TX_8(0x87);
	TX_32(startAddress);
	TX_8N(_data, length);
	END_CMD();
}

void ReadUserFlash(uint32 startAddress, uint16 length)
{
	BEGIN_CMD();
	TX_8(0x88);
	TX_32(startAddress);
	TX_16(length);
	END_CMD();
}

void GetScreen()
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x01);
	END_CMD();
}

void SetScreen(uint16 screen_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x00);
	TX_16(screen_id);
	END_CMD();
}

void SetScreenUpdateEnable(uint8 enable)
{
	BEGIN_CMD();
	TX_8(0xB3);
	TX_8(enable);
	END_CMD();
}

void SetControlFocus(uint16 screen_id, uint16 control_id, uint8 focus)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x02);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(focus);
	END_CMD();
}

void SetControlVisiable(uint16 screen_id, uint16 control_id, uint8 visible)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x03);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(visible);
	END_CMD();
}

void SetControlEnable(uint16 screen_id, uint16 control_id, uint8 enable)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x04);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(enable);
	END_CMD();
}

void SetButtonValue(uint16 screen_id, uint16 control_id, uchar state)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(state);
	END_CMD();
}

void SetTextValue(uint16 screen_id, uint16 control_id, uchar *str)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	SendStrings(str);
	END_CMD();
}

void SetProgressValue(uint16 screen_id, uint16 control_id, uint32 value)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	TX_32(value);
	END_CMD();
}

void SetMeterValue(uint16 screen_id, uint16 control_id, uint32 value)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	TX_32(value);
	END_CMD();
}

void SetSliderValue(uint16 screen_id, uint16 control_id, uint32 value)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	TX_32(value);
	END_CMD();
}

void SetSelectorValue(uint16 screen_id, uint16 control_id, uint8 item)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x10);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(item);
	END_CMD();
}

void GetControlValue(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x11);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void AnimationStart(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x20);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void AnimationStop(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x21);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void AnimationPause(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x22);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void AnimationPlayFrame(uint16 screen_id, uint16 control_id, uint8 frame_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x23);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(frame_id);
	END_CMD();
}

void AnimationPlayPrev(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x24);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void AnimationPlayNext(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x25);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void GraphChannelAdd(uint16 screen_id, uint16 control_id, uint8 channel, uint16 color)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x30);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(channel);
	TX_16(color);
	END_CMD();
}

void GraphChannelDel(uint16 screen_id, uint16 control_id, uint8 channel)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x31);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(channel);
	END_CMD();
}

void GraphChannelDataAdd(uint16 screen_id, uint16 control_id, uint8 channel, uint8 *pData, uint16 nDataLen)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x32);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(channel);
	TX_16(nDataLen);
	TX_8N(pData, nDataLen);
	END_CMD();
}

void GraphChannelDataClear(uint16 screen_id, uint16 control_id, uint8 channel)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x33);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(channel);
	END_CMD();
}

void GraphSetViewport(uint16 screen_id, uint16 control_id, int16 x_offset, uint16 x_mul, int16 y_offset, uint16 y_mul, int16 z_offset, uint16 z_mul)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x34);
	TX_16(screen_id);
	TX_16(control_id);
	TX_16(x_offset);
	TX_16(x_mul);
	TX_16(y_offset);
	TX_16(y_mul);
	TX_16(z_offset);
	TX_16(z_mul);
	END_CMD();
}

void BatchBegin(uint16 screen_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x12);
	TX_16(screen_id);
}

void BatchSetButtonValue(uint16 control_id, uint8 state)
{
	TX_16(control_id);
	TX_16(1);
	TX_8(state);
}

void BatchSetProgressValue(uint16 control_id, uint32 value)
{
	TX_16(control_id);
	TX_16(4);
	TX_32(value);
}

void BatchSetSliderValue(uint16 control_id, uint32 value)
{
	TX_16(control_id);
	TX_16(4);
	TX_32(value);
}

void BatchSetMeterValue(uint16 control_id, uint32 value)
{
	TX_16(control_id);
	TX_16(4);
	TX_32(value);
}

uint32 GetStringLen(uchar *str)
{
	uchar *p = str;
	while (*str)
	{
		str++;
	}

	return (str - p);
}

void BatchSetText(uint16 control_id, uchar *strings)
{
	TX_16(control_id);
	TX_16(GetStringLen(strings));
	SendStrings(strings);
}

void BatchSetFrame(uint16 control_id, uint16 frame_id)
{
	TX_16(control_id);
	TX_16(2);
	TX_16(frame_id);
}

void BatchEnd()
{
	END_CMD();
}

void SeTimer(uint16 screen_id, uint16 control_id, uint32 timeout)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x40);
	TX_16(screen_id);
	TX_16(control_id);
	TX_32(timeout);
	END_CMD();
}

void StartTimer(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x41);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void StopTimer(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x42);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void PauseTimer(uint16 screen_id, uint16 control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x44);
	TX_16(screen_id);
	TX_16(control_id);
	END_CMD();
}

void SetControlBackColor(uint16 screen_id, uint16 control_id, uint16 color)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x18);
	TX_16(screen_id);
	TX_16(control_id);
	TX_16(color);
	END_CMD();
}

void SetControlForeColor(uint16 screen_id, uint16 control_id, uint16 color)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x19);
	TX_16(screen_id);
	TX_16(control_id);
	TX_16(color);
	END_CMD();
}

void ShowPopupMenu(uint16 screen_id, uint16 control_id, uint8 show, uint16 focus_control_id)
{
	BEGIN_CMD();
	TX_8(0xB1);
	TX_8(0x13);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(show);
	TX_16(focus_control_id);
	END_CMD();
}

void ShowKeyboard(uint8 show, uint16 x, uint16 y, uint16 z, uint8 type, uint8 option, uint8 max_len)
{
	BEGIN_CMD();
	TX_8(0x86);
	TX_8(show);
	TX_16(x);
	TX_16(y);
	TX_16(z);
	TX_8(type);
	TX_8(option);
	TX_8(max_len);
	END_CMD();
}

void CMD_ACK_R(uint8 control_id, uint8 state)
{
	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x03);
	TX_8(control_id);
	TX_8(state);
	END_CMD();
}

void CMD_ACK_W(uint8 control_id, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x06);
	TX_8(control_id);
	TX_8(state);
	END_CMD();
}

void CMD_ACK_Move_W(uint8 control_id, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x86);
	TX_8(control_id);
	TX_8(state);
	END_CMD();
}

void SET_Para_ACK(uint8 screen_id, uint8 control_id, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x06);
	TX_8(screen_id);
	TX_8(control_id);
	TX_8(state);
	END_CMD();
}

void SET_KW_ACK(uint8 screen_id, uint8 control_id, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x05);
	TX_8(0x01);
	TX_8(screen_id);
	TX_8(control_id);
	TX_8(state);
	END_CMD();
}

void SET_KW_Move_ACK(uint16 screen_id, uint16 control_id, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);

	TX_8(0x85);
	TX_8(0x01);
	TX_16(screen_id);
	TX_16(control_id);
	TX_8(state);
	END_CMD();
}

void Read_MotoPara_ACK(uint8 screen_id, uint8 control_id, uint16 moto_speed, uint8 acc, uint8 dcc, uint8 state)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x03);
	TX_8(0x04);
	TX_8(screen_id);
	TX_8(control_id);
	TX_16(moto_speed);
	TX_8(acc);
	TX_8(dcc);
	TX_8(state);
	END_CMD();
}

void Read_MotoParas__ACK(uint8 screen_id, uint8 control_id, uint16 moto_speed, uint8 acc, uint8 dcc, uint8 state, uint16 moto_delay)
{

	BEGIN_CMD();
	TX_8(0x01);
	TX_8(0x03);
	TX_8(0x04);
	TX_8(screen_id);
	TX_8(control_id);
	TX_16(moto_speed);
	TX_8(acc);
	TX_8(dcc);
	TX_8(state);
	TX_16(moto_delay);
	END_CMD();
}
