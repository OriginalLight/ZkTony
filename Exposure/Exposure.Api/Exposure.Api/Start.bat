@echo off

set executable=Exposure.Api.exe
set appName="Exposure Api"

REM ��ȡ��ǰ�ű�����Ŀ¼
set scriptPath=%~dp0

REM ������ִ���ļ�������·��
set executablePath=%scriptPath%%executable%

REM ��ӵ�ע���Ŀ���������
reg add HKCU\Software\Microsoft\Windows\CurrentVersion\Run /v %appName% /t REG_SZ /d "%executablePath%" /f

echo %appName% �ѳɹ���ӵ����������

@echo off

REM ����ע����·����ֵ
set regValue=AllowEdgeSwipe

REM ���ý��ñ�Ե�������Ƶ�ֵ
set regData=0

REM �������޸�ע�����

reg add HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\EdgeUI /v "%regValue%" /t REG_DWORD /d "%regData%" /f

echo ��Ե���������ѳɹ����á�

REM �ȴ��û������������رմ���

pause