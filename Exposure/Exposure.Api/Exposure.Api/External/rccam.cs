using System.Runtime.InteropServices;
using System.Text;

namespace Exposure.External;

internal class Rccam
{
    public delegate bool FrameProcCbFunc(IntPtr pFrameInShort, ushort iFrameID, int iFrameWidth, int iFrameHeight,
        IntPtr pParam);

    private const string dll = @"External\Libraries\Vision.dll";

    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Init();

    //SearchforDevice
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int SearchforDevice();

    //GetDeviceID
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetDeviceID(uint i);

    // GetDeviceInfo
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetDeviceInfo(IntPtr pDeviceID, ref SDeviceInfo pDeviceInfo);

    // GetLocalNICInfo
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern void GetLocalNICInfo(IntPtr pDeviceID, ref SNICInfo pNICInfo);

    //ForceIP
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool ForceIP(IntPtr pDeviceID, IntPtr pNewIP, IntPtr pNewMask, IntPtr pNewGateway);

    //OpenDevice
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenDevice(IntPtr pDeviceID);

    //GetNumberOfAttribute
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfAttribute(IntPtr pDeviceID);

    //GetAttributeName
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetAttributeName(IntPtr pDeviceID, uint i);

    //GetAttributeType
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeType(IntPtr pDeviceID, IntPtr pAttrName, ref int iAttrType);

    //GetAttrInt
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iValue, int iAttrLocation);

    //GetAttrMaxInt
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMaximum);

    //GetAttrMinInt
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iMinimum);

    //GetAttrIncInt
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncInt(IntPtr pDeviceID, IntPtr pAttrName, ref long iIncrement);

    //GetAttrFloat
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fValue, int iAttrLocation);

    //GetAttrMaxFloat
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMaxFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMaximum);

    //GetAttrMinFloat
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrMinFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fMinimum);

    //GetAttrIncFloat
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrIncFloat(IntPtr pDeviceID, IntPtr pAttrName, ref double fIncrement);

    //GetAttrString
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttrString(IntPtr pDeviceID, IntPtr pAttrName, StringBuilder sAttrString,
        int iAttrLocation);

    //GetNumberOfEntry
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetNumberOfEntry(IntPtr pDeviceID, IntPtr pAttrName);

    //GetEntryID
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetEntryID(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryName
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryName(IntPtr pDeviceID, IntPtr pAttrName, uint i);

    //GetEntryNameByID
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetEntryNameByID(IntPtr pDeviceID, IntPtr pAttrName, uint iEntryID);

    //GetAttributeAccessMode
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool GetAttributeAccessMode(IntPtr pDeviceID, IntPtr pAttrName, ref int iAccessMode);

    //SetAttrInt
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrInt(IntPtr pDeviceID, IntPtr pAttrName, long iValue, int iAttrLocation);

    //SetAttrFloat
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetAttrFloat(IntPtr pDeviceID, IntPtr pAttrName, double fValue, int iAttrLocation);

    //CalibrateCapture
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateCapture(IntPtr pDeviceID, uint iFrameNum);

    //CalibrateReset
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateReset(IntPtr pDeviceID);

    //CalibrateDark
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDark(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //CalibrateBright
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateBright(IntPtr pDeviceID, uint iModeIndex, IntPtr pWorkDir);

    //DownloadCalDataToDevice
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool DownloadCalDataToDevice(IntPtr pDeviceID, uint iModeIndex, int iCalType,
        bool bFlashEnable, IntPtr pWorkDir, bool bVerify);

    //OpenCalibrate
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool OpenCalibrate(IntPtr pDeviceID, uint iModeIndex);

    //CloseCalibrate
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CloseCalibrate(IntPtr pDeviceID);

    //CalibrateDefect
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool CalibrateDefect(IntPtr pDeviceID, int iStepType, IntPtr pWorkDir);

    //CreateStream
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr CreateStream(IntPtr pDeviceID);

    //GetStreamStatus
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetStreamStatus(IntPtr pStream);

    //StartStream
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool StartStream(IntPtr pStream, bool bAsync, FrameProcCbFunc? pFrameProcCb, IntPtr pCbParam);

    //GetRawFrame
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetRawFrame(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight,
        ref int iPixelBits);

    //GetRawPixelValue
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern ushort GetRawPixelValue(IntPtr pFrame, int iPixelBits, int iPixelIndex);

    //SetFrameProc
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetFrameProc(IntPtr pStream, int iProcType, int iProcParam);

    //GetFrameProc
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern int GetFrameProc(IntPtr pStream, int iProcType);

    //GetFrameInShort
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetFrameInShort(IntPtr pStream, ref ushort iFrameID, ref int iWidth, ref int iHeight);

    //SoftTrigger
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SoftTrigger(IntPtr pDeviceMAC);

    //StopStream
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void StopStream(IntPtr pStream);

    //DestroyStream
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void DestroyStream(IntPtr pStream);

    //CloseDevice
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void CloseDevice(IntPtr pDeviceID);

    //GetLastErrorText
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetLastErrorText();

    //GetVersionText
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr GetVersionText();

    //SetCallLogEnable
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetCallLogEnable(bool bEnable);

    //SetStreamLogEnable
    [DllImport(dll, CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
    public static extern bool SetStreamLogEnable(IntPtr pStream, bool bEnable);

    //Uninit
    [DllImport(dll, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Uninit();

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