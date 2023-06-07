/*!
 *  \file hmi_driver.h
 *  \brief �����������ļ�
 *  \version 1.0
 *  \date 2012-2015
 *  \copyright ���ݴ�ʹ��Ƽ����޹�˾
 */

#ifndef _HMI_DRIVER_
#define _HMI_DRIVER_

#define CRC16_ENABLE 0 /*!< �����ҪCRC16У�鹦�ܣ��޸Ĵ˺�Ϊ1(��ʱ��Ҫ��VisualTFT��������CRCУ��)*/

#define QUEUE_MAX_SIZE 8202 /*!< ָ����ջ�������С��������Ҫ�������������ô�һЩ*/

// extern uint8 cmd_buffer[CMD_MAX_SIZE];

#include "usart.h"
/*!
 *  \brief  ��������Ƿ����CRC16У��
 *  \param buffer ��У������ݣ�ĩβ�洢CRC16
 *  \param n ���ݳ��ȣ�����CRC16
 *  \return У��ͨ������1�����򷵻�0
 */
uint16 CheckCRC16(uint8 *buffer, uint16 n);

/*!
 *  \brief  ��ʱ
 *  \param  n ��ʱʱ��(���뵥λ)
 */
void DelayMS(unsigned int n);

/*!
 *  \brief  �����豸���ã�����֮����Ҫ�����������޸Ĳ����ʡ���������������������ʽ
 */
void LockDeviceConfig(void);

/*!
 *  \brief  �����豸����
 */
void UnlockDeviceConfig(void);

/*!
 *  \brief     �޸Ĵ������Ĳ�����
 *  \details  ������ѡ�Χ[0~14]����Ӧʵ�ʲ�����
                   {1200,2400,4800,9600,19200,38400,57600,115200,1000000,2000000,218750,437500,875000,921800,2500000}
 *  \param  option ������ѡ��
 */
void SetCommBps(uint8 option);

/*!
 *  \brief  ������������
 */
void SetHandShake(void);
void DisText(uint16 x, uint16 y, uint16 z, uint8 back, uint8 font, uchar *strings);
void DisCursor(uint8 enable, uint16 x, uint16 y, uint16 z, uint8 width, uint8 height);
void DisArea_Image(uint16 x, uint16 y, uint16 z, uint16 image_id, uint8 masken);
void DisCut_Image(uint16 x, uint16 y, uint16 z, uint16 image_id, uint16 image_x, uint16 image_y, uint16 image_z, uint16 image_l, uint16 image_w, uint8 masken);
void DisFlashImage(uint16 x, uint16 y, uint16 z, uint16 flashimage_id, uint8 enable, uint8 playnum);
void GUI_Dot(uint16 x, uint16 y, uint16 z);
void GUI_Line(uint16 x0, uint16 y0, uint16 z0, uint16 x1, uint16 y1, uint16 z1);
void GUI_Circle(uint16 x, uint16 y, uint16 z, uint16 r);
void GUI_CircleFill(uint16 x, uint16 y, uint16 z, uint16 r);
void GUI_Arc(uint16 x, uint16 y, uint16 z, uint16 r, uint16 sa, uint16 ea);
void GUI_EllipseFill(uint16 x0, uint16 y0, uint16 x1, uint16 y1, uint16 z0, uint16 z1);
void GraphSetViewport(uint16 screen_id, uint16 control_id, int16 x_offset, uint16 x_mul, int16 y_offset, uint16 y_mul, int16 z_offset, uint16 z_mul);
void ShowKeyboard(uint8 show, uint16 x, uint16 y, uint16 z, uint8 type, uint8 option, uint8 max_len);
/*!
 *  \brief  ����ǰ��ɫ
 *  \param  color ǰ��ɫ
 */
void SetFcolor(uint16 color);

/*!
 *  \brief  ���ñ���ɫ
 *  \param  color ����ɫ
 */
void SetBcolor(uint16 color);

/*!
 *  \brief  �������
 */
void GUI_CleanScreen(void);

/*!
 *  \brief  �������ּ��
 *  \param  x_w ������
 *  \param  y_w ������
 */
void SetTextSpace(uint8 x_w, uint8 y_w, uint8 z_w);

/*!
 *  \brief  ����������ʾ����
 *  \param  enable �Ƿ���������
 *  \param  width ����
 *  \param  height �߶�
 */
void SetFont_Region(uint8 enable, uint16 width, uint16 height);

/*!
 *  \brief  ���ù���ɫ
 *  \param  fillcolor_dwon ��ɫ�½�
 *  \param  fillcolor_up ��ɫ�Ͻ�
 */
void SetFilterColor(uint16 fillcolor_dwon, uint16 fillcolor_up);

/*!
 *  \brief  ���ù���ɫ
 *  \param  x λ��X����
 *  \param  y λ��Y����
 *  \param  back ��ɫ�Ͻ�
 *  \param  font ����
 *  \param  strings �ַ�������
 */
// void DisText(uint16 x, uint16 y,uint8 back,uint8 font,uchar *strings );

/*!
 *  \brief    ��ʾ���
 *  \param  enable �Ƿ���ʾ
 *  \param  x λ��X����
 *  \param  y λ��Y����
 *  \param  width ����
 *  \param  height �߶�
 */
// void DisCursor(uint8 enable,uint16 x, uint16 y,uint8 width,uint8 height );

/*!
 *  \brief      ��ʾȫ��ͼƬ
 *  \param  image_id ͼƬ����
 *  \param  masken �Ƿ�����͸������
 */
void DisFull_Image(uint16 image_id, uint8 masken);

/*!
 *  \brief      ָ��λ����ʾͼƬ
 *  \param  x λ��X����
 *  \param  y λ��Y����
 *  \param  image_id ͼƬ����
 *  \param  masken �Ƿ�����͸������
 */
// void DisArea_Image(uint16 x,uint16 y,uint16 image_id,uint8 masken);

/*!
 *  \brief      ��ʾ�ü�ͼƬ
 *  \param  x λ��X����
 *  \param  y λ��Y����
 *  \param  image_id ͼƬ����
 *  \param  image_x ͼƬ�ü�λ��X����
 *  \param  image_y ͼƬ�ü�λ��Y����
 *  \param  image_l ͼƬ�ü�����
 *  \param  image_w ͼƬ�ü��߶�
 *  \param  masken �Ƿ�����͸������
 */
// void DisCut_Image(uint16 x,uint16 y,uint16 image_id,uint16 image_x,uint16 image_y,uint16 image_l, uint16 image_w,uint8 masken);

/*!
 *  \brief      ��ʾGIF����
 *  \param  x λ��X����
 *  \param  y λ��Y����
 *  \param  flashimage_id ͼƬ����
 *  \param  enable �Ƿ���ʾ
 *  \param  playnum ���Ŵ���
 */
// void DisFlashImage(uint16 x,uint16 y,uint16 flashimage_id,uint8 enable,uint8 playnum);

/*!
 *  \brief      ����
 *  \param  x λ��X����
 *  \param  y λ��Y����
 */
// void GUI_Dot(uint16 x,uint16 y);

/*!
 *  \brief      ����
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
// void GUI_Line(uint16 x0, uint16 y0, uint16 x1, uint16 y1);

/*!
 *  \brief      ������
 *  \param  mode ģʽ
 *  \param  dot ���ݵ�
 *  \param  dot_cnt ����
 */
void GUI_ConDots(uint8 mode, uint16 *dot, uint16 dot_cnt);

/*!
 *  \brief      ������Բ
 *  \param  x0 Բ��λ��X����
 *  \param  y0 Բ��λ��Y����
 *  \param  r �뾶
 */
// void GUI_Circle(uint16 x0, uint16 y0, uint16 r);

/*!
 *  \brief      ��ʵ��Բ
 *  \param  x0 Բ��λ��X����
 *  \param  y0 Բ��λ��Y����
 *  \param  r �뾶
 */
// void GUI_CircleFill(uint16 x0, uint16 y0, uint16 r);

/*!
 *  \brief      ������
 *  \param  x0 Բ��λ��X����
 *  \param  y0 Բ��λ��Y����
 *  \param  r �뾶
 *  \param  sa ��ʼ�Ƕ�
 *  \param  ea ��ֹ�Ƕ�
 */
// void GUI_Arc(uint16 x,uint16 y, uint16 r,uint16 sa, uint16 ea);

/*!
 *  \brief      �����ľ���
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
void GUI_Rectangle(uint16 x0, uint16 y0, uint16 x1, uint16 y1);

/*!
 *  \brief      ��ʵ�ľ���
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
void GUI_RectangleFill(uint16 x0, uint16 y0, uint16 x1, uint16 y1);

/*!
 *  \brief      ��������Բ
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
void GUI_Ellipse(uint16 x0, uint16 y0, uint16 x1, uint16 y1);

/*!
 *  \brief      ��ʵ����Բ
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
// void GUI_EllipseFill (uint16 x0, uint16 y0, uint16 x1,uint16 y1 );

/*!
 *  \brief      ����
 *  \param  x0 ��ʼλ��X����
 *  \param  y0 ��ʼλ��Y����
 *  \param  x1 ����λ��X����
 *  \param  y1 ����λ��Y����
 */
void SetBackLight(uint8 light_level);

/*!
 *  \brief   ����������
 *  \time  time ����ʱ��(���뵥λ)
 */
void SetBuzzer(uint8 time);

/*!
 *  \brief   ����������
 *  \param enable ����ʹ��
 *  \param beep_on ����������
 *  \param work_mode ��������ģʽ��0���¾��ϴ���1�ɿ����ϴ���2�����ϴ�����ֵ��3���º��ɿ����ϴ�����
 *  \param press_calibration �������������20��У׼��������0���ã�1����
 */
void SetTouchPaneOption(uint8 enbale, uint8 beep_on, uint8 work_mode, uint8 press_calibration);

/*!
 *  \brief   У׼������
 */
void CalibrateTouchPane(void);

/*!
 *  \brief  ����������
 */
void TestTouchPane(void);

/*!
 *  \brief      ���õ�ǰд��ͼ��
 *  \details  һ������ʵ��˫����Ч��(��ͼʱ������˸)��
 *  \details  uint8 layer = 0;
 *  \details  WriteLayer(layer);    //����д���
 *  \details  ClearLayer(layer);    //ʹͼ���͸��
 *  \details  //����һϵ�л�ͼָ��
 *  \details  //DisText(100,100,0,4,"hello hmi!!!");
 *  \details  DisplyLayer(layer);  //�л���ʾ��
 *  \details  layer = (layer+1)%2;  //˫�����л�
 *  \see DisplyLayer
 *  \see ClearLayer
 *  \param  layer ͼ����
 */
void WriteLayer(uint8 layer);

/*!
 *  \brief      ���õ�ǰ��ʾͼ��
 *  \param  layer ͼ����
 */
void DisplyLayer(uint8 layer);

/*!
 *  \brief      ���ͼ�㣬ʹͼ����͸��
 *  \param  layer ͼ����
 */
void ClearLayer(uint8 layer);

/*!
 *  \brief  д���ݵ��������û��洢��
 *  \param  startAddress ��ʼ��ַ
 *  \param  length �ֽ���
 *  \param  _data ��д�������
 */
void WriteUserFlash(uint32 startAddress, uint16 length, uint8 *_data);

/*!
 *  \brief  �Ӵ������û��洢����ȡ����
 *  \param  startAddress ��ʼ��ַ
 *  \param  length �ֽ���
 */
void ReadUserFlash(uint32 startAddress, uint16 length);

/*!
 *  \brief      ����ͼ��
 *  \param  src_layer ԭʼͼ��
 *  \param  dest_layer Ŀ��ͼ��
 */
void CopyLayer(uint8 src_layer, uint8 dest_layer);

/*!
 *  \brief      ���õ�ǰ����
 *  \param  screen_id ����ID
 */
void SetScreen(uint16 screen_id);

/*!
 *  \brief      ��ȡ��ǰ����
 */
void GetScreen(void);

/*!
 *  \brief     ����\���û������
 *  \details ����\����һ��ɶ�ʹ�ã����ڱ�����˸�����ˢ���ٶ�
 *  \details �÷���
 *	\details SetScreenUpdateEnable(0);//��ֹ����
 *	\details һϵ�и��»����ָ��
 *	\details SetScreenUpdateEnable(1);//��������
 *  \param  enable 0���ã�1����
 */
void SetScreenUpdateEnable(uint8 enable);

/*!
 *  \brief     ���ÿؼ����뽹��
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  focus �Ƿ�������뽹��
 */
void SetControlFocus(uint16 screen_id, uint16 control_id, uint8 focus);

/*!
 *  \brief     ��ʾ\���ؿؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  visible �Ƿ���ʾ
 */
void SetControlVisiable(uint16 screen_id, uint16 control_id, uint8 visible);

/*!
 *  \brief     ���ô����ؼ�ʹ��
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  enable �ؼ��Ƿ�ʹ��
 */
void SetControlEnable(uint16 screen_id, uint16 control_id, uint8 enable);

/*!
 *  \brief     ��ȡ�ؼ�ֵ
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void GetControlValue(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ���ð�ť״̬
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  value ��ť״̬
 */
void SetButtonValue(uint16 screen_id, uint16 control_id, uchar value);

/*!
 *  \brief     �����ı�ֵ
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  str �ı�ֵ
 */
void SetTextValue(uint16 screen_id, uint16 control_id, uchar *str);

/*!
 *  \brief      ���ý���ֵ
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void SetProgressValue(uint16 screen_id, uint16 control_id, uint32 value);

/*!
 *  \brief     �����Ǳ�ֵ
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void SetMeterValue(uint16 screen_id, uint16 control_id, uint32 value);

/*!
 *  \brief      ���û�����
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void SetSliderValue(uint16 screen_id, uint16 control_id, uint32 value);

/*!
 *  \brief      ����ѡ��ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  item ��ǰѡ��
 */
void SetSelectorValue(uint16 screen_id, uint16 control_id, uint8 item);

/*!
 *  \brief      ��ʼ���Ŷ���
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void AnimationStart(uint16 screen_id, uint16 control_id);

/*!
 *  \brief      ֹͣ���Ŷ���
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void AnimationStop(uint16 screen_id, uint16 control_id);

/*!
 *  \brief      ��ͣ���Ŷ���
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void AnimationPause(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     �����ƶ�֡
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  frame_id ֡ID
 */
void AnimationPlayFrame(uint16 screen_id, uint16 control_id, uint8 frame_id);

/*!
 *  \brief     ������һ֡
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void AnimationPlayPrev(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ������һ֡
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void AnimationPlayNext(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ���߿ؼ�-����ͨ��
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  channel ͨ����
 *  \param  color ��ɫ
 */
void GraphChannelAdd(uint16 screen_id, uint16 control_id, uint8 channel, uint16 color);

/*!
 *  \brief     ���߿ؼ�-ɾ��ͨ��
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  channel ͨ����
 */
void GraphChannelDel(uint16 screen_id, uint16 control_id, uint8 channel);

/*!
 *  \brief     ���߿ؼ�-��������
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  channel ͨ����
 *  \param  pData ��������
 *  \param  nDataLen ���ݸ���
 */
void GraphChannelDataAdd(uint16 screen_id, uint16 control_id, uint8 channel, uint8 *pData, uint16 nDataLen);

/*!
 *  \brief     ���߿ؼ�-�������
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  channel ͨ����
 */
void GraphChannelDataClear(uint16 screen_id, uint16 control_id, uint8 channel);

/*!
 *  \brief     ���߿ؼ�-������ͼ����
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  x_offset ˮƽƫ��
 *  \param  x_mul ˮƽ����ϵ��
 *  \param  y_offset ��ֱƫ��
 *  \param  y_mul ��ֱ����ϵ��
 */
// void GraphSetViewport(uint16 screen_id,uint16 control_id,int16 x_offset,uint16 x_mul,int16 y_offset,uint16 y_mul);

/*!
 *  \brief     ��ʼ��������
 *  \param  screen_id ����ID
 */
void BatchBegin(uint16 screen_id);

/*!
 *  \brief     �������°�ť�ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void BatchSetButtonValue(uint16 control_id, uint8 state);

/*!
 *  \brief     �������½������ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void BatchSetProgressValue(uint16 control_id, uint32 value);

/*!
 *  \brief     �������»������ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void BatchSetSliderValue(uint16 control_id, uint32 value);

/*!
 *  \brief     ���������Ǳ��ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  value ��ֵ
 */
void BatchSetMeterValue(uint16 control_id, uint32 value);

/*!
 *  \brief     ���������ı��ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  strings �ַ���
 */
void BatchSetText(uint16 control_id, uchar *strings);

/*!
 *  \brief     �������¶���\ͼ��ؼ�
 *  \param  control_id �ؼ�ID
 *  \param  frame_id ֡ID
 */
void BatchSetFrame(uint16 control_id, uint16 frame_id);

/*!
 *  \brief    ������������
 */
void BatchEnd(void);

/*!
 *  \brief     ���õ���ʱ�ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  timeout ����ʱ(��)
 */
void SeTimer(uint16 screen_id, uint16 control_id, uint32 timeout);

/*!
 *  \brief     ��������ʱ�ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void StartTimer(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ֹͣ����ʱ�ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void StopTimer(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ��ͣ����ʱ�ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 */
void PauseTimer(uint16 screen_id, uint16 control_id);

/*!
 *  \brief     ���ÿؼ�����ɫ
 *  \details  ֧�ֿؼ������������ı�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  color ����ɫ
 */
void SetControlBackColor(uint16 screen_id, uint16 control_id, uint16 color);

/*!
 *  \brief     ���ÿؼ�ǰ��ɫ
 * \details  ֧�ֿؼ���������
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  color ǰ��ɫ
 */
void SetControlForeColor(uint16 screen_id, uint16 control_id, uint16 color);

/*!
 *  \brief     ��ʾ\���ص����˵��ؼ�
 *  \param  screen_id ����ID
 *  \param  control_id �ؼ�ID
 *  \param  show �Ƿ���ʾ��Ϊ0ʱfocus_control_id��Ч
 *  \param  focus_control_id �������ı��ؼ�(�˵��ؼ�������������ı��ؼ�)
 */
void ShowPopupMenu(uint16 screen_id, uint16 control_id, uint8 show, uint16 focus_control_id);

/*!
 *  \brief     ��ʾ\����ϵͳ����
 *  \param  show 0���أ�1��ʾ
 *  \param  x ������ʾλ��X����
 *  \param  y ������ʾλ��Y����
 *  \param  type 0С���̣�1ȫ����
 *  \param  option 0�����ַ���1���룬2ʱ������
 *  \param  max_len ����¼���ַ���������
 */
// void ShowKeyboard(uint8 show,uint16 x,uint16 y,uint8 type,uint8 option,uint8 max_len);

void CMD_ACK_R(uint8 control_id, uint8 state);
void CMD_ACK_Move_R(uint8 control_id, uint8 state);

void CMD_ACK_W(uint8 control_id, uint8 state);
void CMD_ACK_Move_W(uint8 control_id, uint8 state);

void SET_Para_ACK(uint8 screen_id, uint8 control_id, uint8 state);
void Read_MotoPara_ACK(uint8 screen_id, uint8 control_id, uint16 moto_speed, uint8 acc, uint8 dcc, uint8 state);
void Read_MotoParas__ACK(uint8 screen_id, uint8 control_id, uint16 moto_speed, uint8 acc, uint8 dcc, uint8 state, uint16 moto_delay);

void SET_KW_ACK(uint8 screen_id, uint8 control_id, uint8 state);

void SET_KW_Move_ACK(uint16 screen_id, uint16 control_id, uint8 state);

#endif
