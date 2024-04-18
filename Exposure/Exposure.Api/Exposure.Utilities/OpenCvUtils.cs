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
                    { 25084.866788553183, 0.0, 1490.4840423876637 }, { 0.0, 24953.07878266964, 1036.6913576658844 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -6.714961926680041, 36.45951919419504, 0.11381577335005356, 0.012718469865032982,
                    -0.09751213606627142
                ]);
                break;
            // 1500 分辨率
            case { Width: 1488, Height: 1500 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 13043.982070571883, 0.0, 740.7185361307056 }, { 0.0, 12987.689563610029, 541.4143327555635 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -7.19141774541563, 28.710192805407473, 0.10897004294135913, 0.013334599463637546,
                    0.29155982674105035
                ]);
                break;
            // 1000 分辨率
            case { Width: 992, Height: 998 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 10066.90980810934, 0.0, 356.1090956224924 }, { 0.0, 10104.216767255499, 472.4125568043425 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -9.08285993829159, -112.07125081784204, 0.022485367380474335, 0.1431247144709765, -0.489913386337676
                ]);
                break;
            default:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 25084.866788553183, 0.0, 1490.4840423876637 }, { 0.0, 24953.07878266964, 1036.6913576658844 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -6.714961926680041, 36.45951919419504, 0.11381577335005356, 0.012718469865032982,
                    -0.09751213606627142
                ]);
                break;
        }

        //根据相机内参和畸变参数矫正图片
        var mask = new Mat();

        try
        {
            var newCameraMatrix =
                Cv2.GetOptimalNewCameraMatrix(cameraMatrix, distCoeffs, src.Size(), 0, src.Size(), out var roi);
            // cameraMatrix 数组转换成 Mat 类型
            Cv2.Undistort(src, mask, cameraMatrix, distCoeffs, newCameraMatrix);
            // 裁剪图片并返回原始尺寸
            var res = new Mat();
            Cv2.Resize(mask[roi], res, src.Size());
            return res;
        }
        finally
        {
            // 释放资源
            mask.Dispose();
        }
    }

    #endregion

    #region SNR

    public static double CalculateSnr(Mat image, double time)
    {
        var gray = new Mat();
        try
        {
            // 转换成灰度图
            Cv2.CvtColor(image, gray, ColorConversionCodes.BGR2GRAY);

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
        finally
        {
            // 释放资源
            gray.Dispose();
        }
    }

    #endregion

    #region 计算mat中的某个灰度区间的占比

    public static double CalculatePercentage(Mat mat, int min, int max)
    {
        var gray = new Mat();
        var mask = new Mat();
        try
        {
            // 转换成灰度图
            Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);

            var totalPixels = mat.Rows * mat.Cols;

            // 创建一个掩码，其中在指定范围内的像素为白色，其他像素为黑色
            Cv2.Threshold(gray, mask, min, max, ThresholdTypes.Binary);
            var aboveThresholdPixels = Cv2.CountNonZero(mask);
            return (double)aboveThresholdPixels / totalPixels;
        }
        finally
        {
            gray.Dispose();
            mask.Dispose();
        }
    }

    #endregion

    #region 正片叠底

    public static Mat Multiply(Mat mat1, Mat mat2)
    {
        var baseType = mat1.Type();

        // 转换成CV_64FC4
        var mat3 = new Mat();
        var mat4 = new Mat();

        try
        {
            mat1.ConvertTo(mat3, MatType.CV_64FC4, 1.0 / 255);
            mat2.ConvertTo(mat4, MatType.CV_64FC4, 1.0 / 255);
            var dst = new Mat();
            Cv2.Multiply(mat3, mat4, dst);
            dst.ConvertTo(dst, baseType, 255);
            return dst;
        }
        finally
        {
            mat3.Dispose();
            mat4.Dispose();
        }
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
            Log.Error("截取部分失败：" + e.Message);
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
            Log.Error("中心旋转失败：" + e.Message);
            return src;
        }
    }

    #endregion
}