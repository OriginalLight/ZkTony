using System.Runtime.InteropServices;
using System.Text;
using static Exposure.Helpers.VisionHelper;

namespace Exposure.Helpers;

public static class VisionHelper
{
    public delegate bool FrameProcCbFunc(IntPtr pFrameInShort, ushort iFrameID, int iFrameWidth, int iFrameHeight,
        IntPtr pParam);

    //Init
    public static void Init()
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.Init();
        }
        else
        {
            VisionX86.Init();
        }
    }

    //SearchforDevice
    public static int SearchforDevice() =>
        Environment.Is64BitProcess ? VisionX64.SearchforDevice() : VisionX86.SearchforDevice();

    //GetDeviceID
    public static IntPtr GetDeviceID(uint i) =>
        Environment.Is64BitProcess ? VisionX64.GetDeviceID(i) : VisionX86.GetDeviceID(i);

    // GetDeviceInfo
    public static void GetDeviceInfo(IntPtr pDeviceID, ref SDeviceInfo pDeviceInfo)
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.GetDeviceInfo(pDeviceID, ref pDeviceInfo);
        }
        else
        {
            VisionX86.GetDeviceInfo(pDeviceID, ref pDeviceInfo);
        }
    }

    // GetLocalNICInfo
    public static void GetLocalNICInfo(IntPtr pDeviceID, ref SNICInfo pNICInfo)
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.GetLocalNICInfo(pDeviceID, ref pNICInfo);
        }
        else
        {
            VisionX86.GetLocalNICInfo(pDeviceID, ref pNICInfo);
        }
    }

    //ForceIP
    public static bool ForceIP(IntPtr pDeviceID, IntPtr pNewIP, IntPtr pNewMask, IntPtr pNewGateway) =>
        Environment.Is64BitProcess
            ? VisionX64.ForceIP(pDeviceID, pNewIP, pNewMask, pNewGateway)
            : VisionX86.ForceIP(pDeviceID, pNewIP, pNewMask, pNewGateway);

    //OpenDevice
    public static bool OpenDevice(IntPtr pDeviceID) => Environment.Is64BitProcess
        ? VisionX64.OpenDevice(pDeviceID)
        : VisionX86.OpenDevice(pDeviceID);

    //GetNumberOfAttribute
    public static int GetNumberOfAttribute(IntPtr pDeviceID) => Environment.Is64BitProcess
        ? VisionX64.GetNumberOfAttribute(pDeviceID)
        : VisionX86.GetNumberOfAttribute(pDeviceID);

    //GetAttributeName
    public static IntPtr GetAttributeName(IntPtr pDeviceID, uint i) => Environment.Is64BitProcess
        ? VisionX64.GetAttributeName(pDeviceID, i)
        : VisionX86.GetAttributeName(pDeviceID, i);

    //GetAttributeType
    public static bool GetAttributeType(IntPtr pDeviceID, IntPtr pAttrName, ref int iAttrType) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttributeType(pDeviceID, pAttrName, ref iAttrType)
            : VisionX86.GetAttributeType(pDeviceID, pAttrName, ref iAttrType);

    //GetAttrInt
    public static bool GetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iValue, int iAttrLocation) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrInt(pDeviceID, pAttrName, ref iValue, iAttrLocation)
            : VisionX86.GetAttrInt(pDeviceID, pAttrName, ref iValue, iAttrLocation);

    //GetAttrMaxInt
    public static bool GetAttrMaxInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMaximum) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrMaxInt(pDeviceID, pAttrName, ref iMaximum)
            : VisionX86.GetAttrMaxInt(pDeviceID, pAttrName, ref iMaximum);

    //GetAttrMinInt
    public static bool GetAttrMinInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMinimum) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrMinInt(pDeviceID, pAttrName, ref iMinimum)
            : VisionX86.GetAttrMinInt(pDeviceID, pAttrName, ref iMinimum);

    //GetAttrIncInt
    public static bool GetAttrIncInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iIncrement) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrIncInt(pDeviceID, pAttrName, ref iIncrement)
            : VisionX86.GetAttrIncInt(pDeviceID, pAttrName, ref iIncrement);

    //GetAttrFloat
    public static bool GetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fValue, int iAttrLocation) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrFloat(pDeviceID, pAttrName, ref fValue, iAttrLocation)
            : VisionX86.GetAttrFloat(pDeviceID, pAttrName, ref fValue, iAttrLocation);

    //GetAttrMaxFloat
    public static bool GetAttrMaxFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMaximum) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrMaxFloat(pDeviceID, pAttrName, ref fMaximum)
            : VisionX86.GetAttrMaxFloat(pDeviceID, pAttrName, ref fMaximum);

    //GetAttrMinFloat
    public static bool GetAttrMinFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMinimum) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrMinFloat(pDeviceID, pAttrName, ref fMinimum)
            : VisionX86.GetAttrMinFloat(pDeviceID, pAttrName, ref fMinimum);

    //GetAttrIncFloat
    public static bool GetAttrIncFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fIncrement) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrIncFloat(pDeviceID, pAttrName, ref fIncrement)
            : VisionX86.GetAttrIncFloat(pDeviceID, pAttrName, ref fIncrement);

    //GetAttrString
    public static bool
        GetAttrString(IntPtr pDeviceID, IntPtr pAttrName, StringBuilder sAttrString, int iAttrLocation) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttrString(pDeviceID, pAttrName, sAttrString, iAttrLocation)
            : VisionX86.GetAttrString(pDeviceID, pAttrName, sAttrString, iAttrLocation);

    //GetNumberOfEntry
    public static int GetNumberOfEntry(IntPtr pDeviceID, IntPtr pAttrName) => Environment.Is64BitProcess
        ? VisionX64.GetNumberOfEntry(pDeviceID, pAttrName)
        : VisionX86.GetNumberOfEntry(pDeviceID, pAttrName);

    //GetEntryID
    public static int GetEntryID(IntPtr pDeviceID, IntPtr pAttrName, uint i) => Environment.Is64BitProcess
        ? VisionX64.GetEntryID(pDeviceID, pAttrName, i)
        : VisionX86.GetEntryID(pDeviceID, pAttrName, i);

    //GetEntryName
    public static IntPtr GetEntryName(IntPtr pDeviceID, IntPtr pAttrName, uint i) => Environment.Is64BitProcess
        ? VisionX64.GetEntryName(pDeviceID, pAttrName, i)
        : VisionX86.GetEntryName(pDeviceID, pAttrName, i);

    //GetEntryNameByID
    public static IntPtr GetEntryNameByID(IntPtr pDeviceID, IntPtr pAttrName, uint iEntryID) =>
        Environment.Is64BitProcess
            ? VisionX64.GetEntryNameByID(pDeviceID, pAttrName, iEntryID)
            : VisionX86.GetEntryNameByID(pDeviceID, pAttrName, iEntryID);

    //GetAttributeAccessMode
    public static bool GetAttributeAccessMode(IntPtr pDeviceID, IntPtr pAttrName, ref int iAccessMode) =>
        Environment.Is64BitProcess
            ? VisionX64.GetAttributeAccessMode(pDeviceID, pAttrName, ref iAccessMode)
            : VisionX86.GetAttributeAccessMode(pDeviceID, pAttrName, ref iAccessMode);

    //SetAttrInt
    public static bool SetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, long iValue, int iAttrLocation) =>
        Environment.Is64BitProcess
            ? VisionX64.SetAttrInt(pDeviceID, pAttrName, iValue, iAttrLocation)
            : VisionX86.SetAttrInt(pDeviceID, pAttrName, iValue, iAttrLocation);

    //SetAttrFloat
    public static bool SetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, double fValue, int iAttrLocation) =>
        Environment.Is64BitProcess
            ? VisionX64.SetAttrFloat(pDeviceID, pAttrName, fValue, iAttrLocation)
            : VisionX86.SetAttrFloat(pDeviceID, pAttrName, fValue, iAttrLocation);

    //CalibrateCapture
    public static bool CalibrateCapture(IntPtr pDeviceID, uint iFrameNum) => Environment.Is64BitProcess
        ? VisionX64.CalibrateCapture(pDeviceID, iFrameNum)
        : VisionX86.CalibrateCapture(pDeviceID, iFrameNum);

    //CalibrateReset
    public static bool CalibrateReset(IntPtr pDeviceID) => Environment.Is64BitProcess
        ? VisionX64.CalibrateReset(pDeviceID)
        : VisionX86.CalibrateReset(pDeviceID);

    //CalibrateDark
    public static bool CalibrateDark(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir) => Environment.Is64BitProcess
        ? VisionX64.CalibrateDark(pDeviceID, iModeIndex, pWorkDir)
        : VisionX86.CalibrateDark(pDeviceID, iModeIndex, pWorkDir);

    //CalibrateBright
    public static bool CalibrateBright(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir) => Environment.Is64BitProcess
        ? VisionX64.CalibrateBright(pDeviceID, iModeIndex, pWorkDir)
        : VisionX86.CalibrateBright(pDeviceID, iModeIndex, pWorkDir);

    //DownloadCalDataToDevice
    public static bool DownloadCalDataToDevice(IntPtr pDeviceID, uint iModeIndex, int iCalType, bool bFlashEnable,
        IntPtr pWorkDir, bool bVerify) =>
        Environment.Is64BitProcess
            ? VisionX64.DownloadCalDataToDevice(pDeviceID, iModeIndex, iCalType, bFlashEnable, pWorkDir, bVerify)
            : VisionX86.DownloadCalDataToDevice(pDeviceID, iModeIndex, iCalType, bFlashEnable, pWorkDir, bVerify);

    //OpenCalibrate
    public static bool OpenCalibrate(IntPtr pDeviceID, uint iModeIndex) => Environment.Is64BitProcess
        ? VisionX64.OpenCalibrate(pDeviceID, iModeIndex)
        : VisionX86.OpenCalibrate(pDeviceID, iModeIndex);

    //CloseCalibrate
    public static bool CloseCalibrate(IntPtr pDeviceID) => Environment.Is64BitProcess
        ? VisionX64.CloseCalibrate(pDeviceID)
        : VisionX86.CloseCalibrate(pDeviceID);

    //CalibrateDefect
    public static bool CalibrateDefect(IntPtr pDeviceID, int iStepType, IntPtr pWorkDir) => Environment.Is64BitProcess
        ? VisionX64.CalibrateDefect(pDeviceID, iStepType, pWorkDir)
        : VisionX86.CalibrateDefect(pDeviceID, iStepType, pWorkDir);

    //CreateStream
    public static IntPtr CreateStream(IntPtr pDeviceID) => Environment.Is64BitProcess
        ? VisionX64.CreateStream(pDeviceID)
        : VisionX86.CreateStream(pDeviceID);

    //GetStreamStatus
    public static int GetStreamStatus(IntPtr pStream) => Environment.Is64BitProcess
        ? VisionX64.GetStreamStatus(pStream)
        : VisionX86.GetStreamStatus(pStream);

    //StartStream
    public static bool StartStream(IntPtr pStream, bool bAsync, FrameProcCbFunc? pFrameProcCb, IntPtr pCbParam) =>
        Environment.Is64BitProcess
            ? VisionX64.StartStream(pStream, bAsync, pFrameProcCb, pCbParam)
            : VisionX86.StartStream(pStream, bAsync, pFrameProcCb, pCbParam);

    //GetRawFrame
    public static IntPtr GetRawFrame(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight,
        ref int iPixelBits) =>
        Environment.Is64BitProcess
            ? VisionX64.GetRawFrame(pStream, ref iFrameID, ref iWidth, ref iHeight, ref iPixelBits)
            : VisionX86.GetRawFrame(pStream, ref iFrameID, ref iWidth, ref iHeight, ref iPixelBits);

    //GetRawPixelValue
    public static ushort GetRawPixelValue(IntPtr pFrame, int iPixelBits, int iPixelIndex) => Environment.Is64BitProcess
        ? VisionX64.GetRawPixelValue(pFrame, iPixelBits, iPixelIndex)
        : VisionX86.GetRawPixelValue(pFrame, iPixelBits, iPixelIndex);

    //SetFrameProc
    public static bool SetFrameProc(IntPtr pStream, int iProcType, int iProcParam) => Environment.Is64BitProcess
        ? VisionX64.SetFrameProc(pStream, iProcType, iProcParam)
        : VisionX86.SetFrameProc(pStream, iProcType, iProcParam);

    //GetFrameProc
    public static int GetFrameProc(IntPtr pStream, int iProcType) => Environment.Is64BitProcess
        ? VisionX64.GetFrameProc(pStream, iProcType)
        : VisionX86.GetFrameProc(pStream, iProcType);

    //GetFrameInShort
    public static IntPtr GetFrameInShort(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight) =>
        Environment.Is64BitProcess
            ? VisionX64.GetFrameInShort(pStream, ref iFrameID, ref iWidth, ref iHeight)
            : VisionX86.GetFrameInShort(pStream, ref iFrameID, ref iWidth, ref iHeight);

    //SoftTrigger
    public static bool SoftTrigger(IntPtr pDeviceMAC) => Environment.Is64BitProcess
        ? VisionX64.SoftTrigger(pDeviceMAC)
        : VisionX86.SoftTrigger(pDeviceMAC);

    //StopStream
    public static void StopStream(IntPtr pStream)
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.StopStream(pStream);
        }
        else
        {
            VisionX86.StopStream(pStream);
        }
    }

    //DestroyStream
    public static void DestroyStream(IntPtr pStream)
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.DestroyStream(pStream);
        }
        else
        {
            VisionX86.DestroyStream(pStream);
        }
    }

    //CloseDevice
    public static void CloseDevice(IntPtr pDeviceID)
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.CloseDevice(pDeviceID);
        }
        else
        {
            VisionX86.CloseDevice(pDeviceID);
        }
    }

    //GetLastErrorText
    public static IntPtr GetLastErrorText() =>
        Environment.Is64BitProcess ? VisionX64.GetLastErrorText() : VisionX86.GetLastErrorText();

    //GetVersionText
    public static IntPtr GetVersionText() =>
        Environment.Is64BitProcess ? VisionX64.GetVersionText() : VisionX86.GetVersionText();

    //SetCallLogEnable
    public static bool SetCallLogEnable(bool bEnable) => Environment.Is64BitProcess
        ? VisionX64.SetCallLogEnable(bEnable)
        : VisionX86.SetCallLogEnable(bEnable);

    //SetStreamLogEnable
    public static bool SetStreamLogEnable(IntPtr pStream, bool bEnable) => Environment.Is64BitProcess
        ? VisionX64.SetStreamLogEnable(pStream, bEnable)
        : VisionX86.SetStreamLogEnable(pStream, bEnable);

    //Uninit
    public static void Uninit()
    {
        if (Environment.Is64BitProcess)
        {
            VisionX64.Uninit();
        }
        else
        {
            VisionX86.Uninit();
        }
    }

    //GetDeviceInfo
    [StructLayout(LayoutKind.Sequential)]
    public struct SDeviceInfo
    {
        public IntPtr pMAC;
        public IntPtr pIP;
        public int iCtrlPort;
        public int iDataPort;
        public IntPtr pMask;
        public IntPtr pGateway;
        public IntPtr pVenderName;
        public IntPtr pModelName;
        public IntPtr pVersion;
        public IntPtr pSerialNumber;
        public bool bReachable;
    }

    //GetLocalNICInfo
    [StructLayout(LayoutKind.Sequential)]
    public struct SNICInfo
    {
        public IntPtr pMAC;
        public IntPtr pIP;
        public IntPtr pMask;
        public IntPtr pInterfaceName;
        public IntPtr pBroadcast;
    }
}

public static class VisionX64
{
    private const string Dll = @"Libraries\x64\Vision.dll";

    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Init();

    //SearchforDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int SearchforDevice();

    //GetDeviceID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetDeviceID(uint i);

    // GetDeviceInfo
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetDeviceInfo(IntPtr pDeviceID, ref SDeviceInfo pDeviceInfo);

    // GetLocalNICInfo
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetLocalNICInfo(IntPtr pDeviceID, ref SNICInfo pNICInfo);

    //ForceIP
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool ForceIP(IntPtr pDeviceID, IntPtr pNewIP, IntPtr pNewMask, IntPtr pNewGateway);

    //OpenDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenDevice(IntPtr pDeviceID);

    //GetNumberOfAttribute
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfAttribute(IntPtr pDeviceID);

    //GetAttributeName
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetAttributeName(IntPtr pDeviceID, uint i);

    //GetAttributeType
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeType(IntPtr pDeviceID, IntPtr pAttrName, ref int iAttrType);

    //GetAttrInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iValue, int iAttrLocation);

    //GetAttrMaxInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMaximum);

    //GetAttrMinInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMinimum);

    //GetAttrIncInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iIncrement);

    //GetAttrFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fValue, int iAttrLocation);

    //GetAttrMaxFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMaximum);

    //GetAttrMinFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMinimum);

    //GetAttrIncFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fIncrement);

    //GetAttrString
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrString(IntPtr pDeviceID, IntPtr pAttrName, StringBuilder sAttrString,
        int iAttrLocation);

    //GetNumberOfEntry
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfEntry(IntPtr pDeviceID, IntPtr pAttrName);

    //GetEntryID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetEntryID(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryName
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryName(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryNameByID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryNameByID(IntPtr pDeviceID, IntPtr pAttrName, uint iEntryID);

    //GetAttributeAccessMode
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeAccessMode(IntPtr pDeviceID, IntPtr pAttrName, ref int iAccessMode);

    //SetAttrInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, long iValue, int iAttrLocation);

    //SetAttrFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, double fValue, int iAttrLocation);

    //CalibrateCapture
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateCapture(IntPtr pDeviceID, uint iFrameNum);

    //CalibrateReset
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateReset(IntPtr pDeviceID);

    //CalibrateDark
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDark(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //CalibrateBright
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateBright(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //DownloadCalDataToDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool DownloadCalDataToDevice(IntPtr pDeviceID, uint iModeIndex, int iCalType,
        bool bFlashEnable, IntPtr pWorkDir, bool bVerify);

    //OpenCalibrate
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenCalibrate(IntPtr pDeviceID, uint iModeIndex);

    //CloseCalibrate
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CloseCalibrate(IntPtr pDeviceID);

    //CalibrateDefect
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDefect(IntPtr pDeviceID, int iStepType, IntPtr pWorkDir);

    //CreateStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr CreateStream(IntPtr pDeviceID);

    //GetStreamStatus
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetStreamStatus(IntPtr pStream);

    //StartStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool StartStream(IntPtr pStream, bool bAsync, FrameProcCbFunc? pFrameProcCb, IntPtr pCbParam);

    //GetRawFrame
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetRawFrame(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight,
        ref int iPixelBits);

    //GetRawPixelValue
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern ushort GetRawPixelValue(IntPtr pFrame, int iPixelBits, int iPixelIndex);

    //SetFrameProc
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetFrameProc(IntPtr pStream, int iProcType, int iProcParam);

    //GetFrameProc
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetFrameProc(IntPtr pStream, int iProcType);

    //GetFrameInShort
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetFrameInShort(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight);

    //SoftTrigger
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SoftTrigger(IntPtr pDeviceMAC);

    //StopStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void StopStream(IntPtr pStream);

    //DestroyStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void DestroyStream(IntPtr pStream);

    //CloseDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void CloseDevice(IntPtr pDeviceID);

    //GetLastErrorText
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetLastErrorText();

    //GetVersionText
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetVersionText();

    //SetCallLogEnable
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetCallLogEnable(bool bEnable);

    //SetStreamLogEnable
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetStreamLogEnable(IntPtr pStream, bool bEnable);

    //Uninit
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Uninit();
}

public static class VisionX86
{
    private const string Dll = @"Libraries\x86\Vision.dll";

    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Init();

    //SearchforDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int SearchforDevice();

    //GetDeviceID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetDeviceID(uint i);

    // GetDeviceInfo
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetDeviceInfo(IntPtr pDeviceID, ref SDeviceInfo pDeviceInfo);

    // GetLocalNICInfo
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetLocalNICInfo(IntPtr pDeviceID, ref SNICInfo pNICInfo);

    //ForceIP
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool ForceIP(IntPtr pDeviceID, IntPtr pNewIP, IntPtr pNewMask, IntPtr pNewGateway);

    //OpenDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenDevice(IntPtr pDeviceID);

    //GetNumberOfAttribute
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfAttribute(IntPtr pDeviceID);

    //GetAttributeName
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetAttributeName(IntPtr pDeviceID, uint i);

    //GetAttributeType
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeType(IntPtr pDeviceID, IntPtr pAttrName, ref int iAttrType);

    //GetAttrInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iValue, int iAttrLocation);

    //GetAttrMaxInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMaximum);

    //GetAttrMinInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMinimum);

    //GetAttrIncInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iIncrement);

    //GetAttrFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fValue, int iAttrLocation);

    //GetAttrMaxFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMaximum);

    //GetAttrMinFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMinimum);

    //GetAttrIncFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fIncrement);

    //GetAttrString
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrString(IntPtr pDeviceID, IntPtr pAttrName, StringBuilder sAttrString,
        int iAttrLocation);

    //GetNumberOfEntry
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfEntry(IntPtr pDeviceID, IntPtr pAttrName);

    //GetEntryID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetEntryID(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryName
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryName(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryNameByID
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryNameByID(IntPtr pDeviceID, IntPtr pAttrName, uint iEntryID);

    //GetAttributeAccessMode
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeAccessMode(IntPtr pDeviceID, IntPtr pAttrName, ref int iAccessMode);

    //SetAttrInt
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, long iValue, int iAttrLocation);

    //SetAttrFloat
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, double fValue, int iAttrLocation);

    //CalibrateCapture
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateCapture(IntPtr pDeviceID, uint iFrameNum);

    //CalibrateReset
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateReset(IntPtr pDeviceID);

    //CalibrateDark
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDark(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //CalibrateBright
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateBright(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //DownloadCalDataToDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool DownloadCalDataToDevice(IntPtr pDeviceID, uint iModeIndex, int iCalType,
        bool bFlashEnable, IntPtr pWorkDir, bool bVerify);

    //OpenCalibrate
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenCalibrate(IntPtr pDeviceID, uint iModeIndex);

    //CloseCalibrate
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CloseCalibrate(IntPtr pDeviceID);

    //CalibrateDefect
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDefect(IntPtr pDeviceID, int iStepType, IntPtr pWorkDir);

    //CreateStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr CreateStream(IntPtr pDeviceID);

    //GetStreamStatus
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetStreamStatus(IntPtr pStream);

    //StartStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool StartStream(IntPtr pStream, bool bAsync, FrameProcCbFunc? pFrameProcCb, IntPtr pCbParam);

    //GetRawFrame
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetRawFrame(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight,
        ref int iPixelBits);

    //GetRawPixelValue
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern ushort GetRawPixelValue(IntPtr pFrame, int iPixelBits, int iPixelIndex);

    //SetFrameProc
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetFrameProc(IntPtr pStream, int iProcType, int iProcParam);

    //GetFrameProc
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetFrameProc(IntPtr pStream, int iProcType);

    //GetFrameInShort
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetFrameInShort(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight);

    //SoftTrigger
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SoftTrigger(IntPtr pDeviceMAC);

    //StopStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void StopStream(IntPtr pStream);

    //DestroyStream
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void DestroyStream(IntPtr pStream);

    //CloseDevice
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void CloseDevice(IntPtr pDeviceID);

    //GetLastErrorText
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetLastErrorText();

    //GetVersionText
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetVersionText();

    //SetCallLogEnable
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetCallLogEnable(bool bEnable);

    //SetStreamLogEnable
    [DllImport(Dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetStreamLogEnable(IntPtr pStream, bool bEnable);

    //Uninit
    [DllImport(Dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Uninit();
}