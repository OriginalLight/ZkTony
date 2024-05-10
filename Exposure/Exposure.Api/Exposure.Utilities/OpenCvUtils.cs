using Newtonsoft.Json.Linq;
using OpenCvSharp;
using Serilog;

namespace Exposure.Utilities;

public static class OpenCvUtils
{
    #region 相机标定

    public static Mat Calibrate(Mat src, Dictionary<string, Object> dic)
    {
        InputArray cameraMatrix;
        InputArray distCoeffs;

        switch (src)
        {
            // 3000 分辨率
            case { Width: 2992, Height: 3000 }:
                var m1 = dic["Matrix_3000"].ToString();
                var c1 = dic["Coeffs_3000"].ToString();
                if (m1 != null && c1 != null)
                {
                    var m1d = JArray.Parse(m1).ToObject<double[,]>();
                    var c1d = JArray.Parse(c1).ToObject<double[]>();
                    if (m1d == null || c1d == null)
                    {
                        Log.Error("相机标定参数为空");
                        return src;
                    }
                    cameraMatrix = InputArray.Create(m1d);
                    distCoeffs = InputArray.Create(c1d);
                }
                else
                {
                    Log.Error("相机标定参数为空");
                    return src;
                }

                break;
            // 1500 分辨率
            case { Width: 1488, Height: 1500 }:
                var m2 = dic["Matrix_1500"].ToString();
                var c2 = dic["Coeffs_1500"].ToString();
                if (m2 != null && c2 != null)
                {
                    var m2d = JArray.Parse(m2).ToObject<double[,]>();
                    var c2d = JArray.Parse(c2).ToObject<double[]>();
                    if (m2d == null || c2d == null)
                    {
                        Log.Error("相机标定参数为空");
                        return src;
                    }
                    cameraMatrix = InputArray.Create(m2d);
                    distCoeffs = InputArray.Create(c2d);
                }
                else
                {
                    Log.Error("相机标定参数为空");
                    return src;
                }
                break;
            // 1000 分辨率
            case { Width: 992, Height: 998 }:
                var m3 = dic["Matrix_1000"].ToString();
                var c3 = dic["Coeffs_1000"].ToString();
                if (m3 != null && c3 != null)
                {
                    var m3d = JArray.Parse(m3).ToObject<double[,]>();
                    var c3d = JArray.Parse(c3).ToObject<double[]>();
                    if (m3d == null || c3d == null)
                    {
                        Log.Error("相机标定参数为空");
                        return src;
                    }
                    cameraMatrix = InputArray.Create(m3d);
                    distCoeffs = InputArray.Create(c3d);
                }
                else
                {
                    Log.Error("相机标定参数为空");
                    return src;
                }
                break;
            default:
                var m4 = dic["Matrix_3000"].ToString();
                var c4 = dic["Coeffs_3000"].ToString();
                if (m4 != null && c4 != null)
                {
                    var m4d = JArray.Parse(m4).ToObject<double[,]>();
                    var c4d = JArray.Parse(c4).ToObject<double[]>();
                    if (m4d == null || c4d == null)
                    {
                        Log.Error("相机标定参数为空");
                        return src;
                    }
                    cameraMatrix = InputArray.Create(m4d);
                    distCoeffs = InputArray.Create(c4d);
                }
                else
                {
                    Log.Error("相机标定参数为空");
                    return src;
                }
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
        Log.Information("图片标定成功");
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
        mat1.ConvertTo(mat3, MatType.CV_64FC4, 1.0 / 65535);
        mat2.ConvertTo(mat4, MatType.CV_64FC4, 1.0 / 65535);
        var dst = new Mat();
        Cv2.Multiply(mat3, mat4, dst);
        dst.ConvertTo(dst, baseType, 65535);
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