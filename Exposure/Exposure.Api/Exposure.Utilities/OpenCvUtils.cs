using OpenCvSharp;
using Serilog;

namespace Exposure.Utilities;

public static class OpenCvUtils
{
    #region 相机标定

    public static Mat Calibrate(Mat src)
    {
        InputArray cameraMatrix;
        InputArray distCoeffs;

        switch (src)
        {
            // 3000 分辨率
            case { Width: 2992, Height: 3000 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 32496.711712725566, 0.0, 1734.5242659467344 }, { 0.0, 32535.746862601714, 1529.9190608439628 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -12.179942261102948, 296.5151127295539, 0.01769078563352507, -0.08133542425690382, 1.1848142136842608
                ]);
                break;
            // 1500 分辨率
            case { Width: 1488, Height: 1500 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 25795.548909748555, 0.0, 686.6541437350486 }, { 0.0, 25800.379936849415, 824.144008293167 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -32.05268777310819, 2521.411049776186, -0.03996092095458863, 0.06814315060093115, 4.013535803259616
                ]);
                break;
            // 1000 分辨率
            case { Width: 992, Height: 998 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 19985.133007483306, 0.0, 528.9050071145448 }, { 0.0, 19992.591456530063, 485.4081596993531 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -40.85361568317635, 1633.8466631639812, 0.07771399552774197, -0.06761371171905543, 1.3797482065075937
                ]);
                break;
            default:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 32496.711712725566, 0.0, 1734.5242659467344 }, { 0.0, 32535.746862601714, 1529.9190608439628 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -12.179942261102948, 296.5151127295539, 0.01769078563352507, -0.08133542425690382, 1.1848142136842608
                ]);
                break;
        }

        //根据相机内参和畸变参数矫正图片
        var mask = new Mat();
        var newCameraMatrix =
            Cv2.GetOptimalNewCameraMatrix(cameraMatrix, distCoeffs, src.Size(), 0, src.Size(), out var roi);
        // cameraMatrix 数组转换成 Mat 类型
        Cv2.Undistort(src, mask, cameraMatrix, distCoeffs, newCameraMatrix);
        // 裁剪图片并返回原始尺寸
        var res = new Mat();
        Cv2.Resize(mask[roi], res, src.Size());
        return res;
    }

    #endregion

    #region SNR

    public static double CalculateSnr(Mat src, double time)
    {
        var gray = new Mat();
        // 转换成灰度图
        Cv2.CvtColor(src, gray, ColorConversionCodes.BGR2GRAY);

        // 计算信噪比
        Cv2.MeanStdDev(gray, out var mean, out var stddev);

        // Calculate the signal power (square of the mean)
        var signalPower = Math.Pow(mean.Val0, 2);

        // Calculate the noise power (square of the standard deviation)
        var noisePower = Math.Pow(stddev.Val0, 2);

        // Adjust the noise power based on the exposure time
        var adjustedNoisePower = noisePower / time;

        // Calculate and return the adjusted SNR value
        return 10 * Math.Log10(signalPower / adjustedNoisePower);
    }

    #endregion

    #region 计算mat中的某个灰度区间的占比

    public static double CalculatePercentage(Mat mat, int min, int max)
    {
        var gray = new Mat();
        var mask = new Mat();
        // 转换成灰度图
        Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);

        var totalPixels = mat.Rows * mat.Cols;

        // 创建一个掩码，其中在指定范围内的像素为白色，其他像素为黑色
        Cv2.Threshold(gray, mask, min, max, ThresholdTypes.Binary);
        var aboveThresholdPixels = Cv2.CountNonZero(mask);
        return (double)aboveThresholdPixels / totalPixels;
    }

    #endregion

    #region 正片叠底

    public static Mat Multiply(Mat mat1, Mat mat2)
    {
        var baseType = mat1.Type();
        // 转换成CV_64FC4
        var mat3 = new Mat();
        var mat4 = new Mat();
        mat1.ConvertTo(mat3, MatType.CV_64FC4, 1.0 / 255);
        mat2.ConvertTo(mat4, MatType.CV_64FC4, 1.0 / 255);
        var dst = new Mat();
        Cv2.Multiply(mat3, mat4, dst);
        dst.ConvertTo(dst, baseType, 255);
        return dst;
    }

    #endregion

    #region 截取部分

    public static Mat CuteRoi(Mat src, string roi)
    {
        try
        {
            var arr = roi.Split(',');
            var left = double.Parse(arr[0]);
            var right = double.Parse(arr[1]);
            var top = double.Parse(arr[2]);
            var bottom = double.Parse(arr[3]);
            var width = src.Width;
            var height = src.Height;
            var x = (int)(width * left);
            var y = (int)(height * top);
            var w = (int)(width * (right - left));
            var h = (int)(height * (bottom - top));
            var rect = new Rect(x, y, w, h);
            return src[rect].Resize(new Size(width, height));
        }
        catch (Exception e)
        {
            Log.Error(e, "截取部分失败");
            return src;
        }
    }

    #endregion

    #region 中心旋转

    public static Mat Rotate(Mat src, double angle)
    {
        try
        {
            // ReSharper disable PossibleLossOfFraction
            var center = new Point2f(src.Width / 2, src.Height / 2);
            var rot = Cv2.GetRotationMatrix2D(center, angle, 1.0);
            var dst = new Mat();
            Cv2.WarpAffine(src, dst, rot, src.Size());
            return dst;
        }
        catch (Exception e)
        {
            Log.Error(e, "中心旋转失败");
            return src;
        }
    }

    #endregion
}