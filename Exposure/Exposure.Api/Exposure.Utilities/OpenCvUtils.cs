using OpenCvSharp;
using Serilog;

namespace Exposure.Utilities;

public static class OpenCvUtils
{
    #region 相机标定

    public static Mat Calibrate(Mat src, double[,]? cameraMatrix, double[]? distCoeffs)
    {
        if (cameraMatrix == null || distCoeffs == null || cameraMatrix.Length == 0 || distCoeffs.Length == 0)
        {
            Log.Error("相机标定参数为空");
            return src;
        }
        //根据相机内参和畸变参数矫正图片
        var mask = new Mat();
        var newCameraMatrix =
            Cv2.GetOptimalNewCameraMatrix(InputArray.Create(cameraMatrix), InputArray.Create(distCoeffs), src.Size(), 0, src.Size(), out var roi);
        // cameraMatrix 数组转换成 Mat 类型
        Cv2.Undistort(src, mask, InputArray.Create(cameraMatrix), InputArray.Create(distCoeffs), newCameraMatrix);
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

    #region 畸形校正
    
    public static void CalibrateCamera(List<string> images, out double[,]? cameraMatrix, out double[]? distCoeffs)
    {
        // 定义棋盘格的内角点数量
        var chessboardWidth = 17;  // 棋盘格每行内角点数量
        var chessboardHeight = 17; // 棋盘格每列内角点数量
        var chessboardSize = chessboardWidth * chessboardHeight; // 棋盘格总内角点数量
        var allImagePoints = new List<List<Point2f>>();
        var allObjectPoints = new List<List<Point3f>>();
        var imageSize = new Size(0, 0);
        //方格边长
        var squareSize = 6;

        foreach (var image in images)
        {
            // 生成棋盘格角点的三维坐标
            var objectPoints = new Point3f[chessboardSize];
            for (var i = 0; i < chessboardHeight; i++)
            {
                for (var j = 0; j < chessboardWidth; j++)
                {
                    objectPoints[i * chessboardWidth + j] = new Point3f(j, i, 0);
                    // 棋盘格每个方格的边长为 10mm
                    objectPoints[i * chessboardWidth + j] *= squareSize;
                }
            }
            
            
            // 读取图片
            var mat = new Mat(image, ImreadModes.Grayscale);
            imageSize = mat.Size();
            
            // 查找棋盘格角点
            var found = Cv2.FindChessboardCorners(mat, new Size(chessboardWidth, chessboardHeight), out var imagePoints, ChessboardFlags.AdaptiveThresh);

            if (!found) continue;
            // 亚像素精确化
            Cv2.CornerSubPix(mat, imagePoints, new Size(11, 11), new Size(-1, -1), new TermCriteria(CriteriaTypes.Eps | CriteriaTypes.MaxIter, 30, 0.1));
            allImagePoints.Add(imagePoints.ToList());
            allObjectPoints.Add(objectPoints.ToList());

        }

        if (allImagePoints.Count == 0)
        {
            cameraMatrix = null;
            distCoeffs = null;
            return;
        }
        
        // 标定相机
        cameraMatrix = new double[3, 3];
        distCoeffs = new double[5];
        Cv2.CalibrateCamera(allObjectPoints, allImagePoints, imageSize, cameraMatrix, distCoeffs, out _, out _);
    }

    #endregion
}