@echo off

set executable=Exposure.Api.exe
set appName="Exposure Api"

REM 获取当前脚本所在目录
set scriptPath=%~dp0

REM 构建可执行文件的完整路径
set executablePath=%scriptPath%%executable%

REM 添加到注册表的开机启动项
reg add HKCU\Software\Microsoft\Windows\CurrentVersion\Run /v %appName% /t REG_SZ /d "%executablePath%" /f

echo %appName% 已成功添加到开机启动项。

@echo off

REM 设置注册表键路径和值
set regValue=AllowEdgeSwipe

REM 设置禁用边缘滑动手势的值
set regData=0

REM 创建或修改注册表项

reg add HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\EdgeUI /v "%regValue%" /t REG_DWORD /d "%regData%" /f

echo 边缘滑动手势已成功禁用。

REM 等待用户按下任意键后关闭窗口

pause