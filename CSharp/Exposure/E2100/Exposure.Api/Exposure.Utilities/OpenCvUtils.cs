﻿using OpenCvSharp;
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
        var newCameraMatrix = new Mat();
        var roiMat = new Mat();
        try
        {
            newCameraMatrix = Cv2.GetOptimalNewCameraMatrix(InputArray.Create(cameraMatrix), InputArray.Create(distCoeffs), src.Size(), 0, src.Size(), out var roi);
            // cameraMatrix 数组转换成 Mat 类型
            Cv2.Undistort(src, mask, InputArray.Create(cameraMatrix), InputArray.Create(distCoeffs), newCameraMatrix);
            // 裁剪图片并返回原始尺寸
            var res = new Mat();
            roiMat = mask[roi];
            Cv2.Resize(roiMat, res, src.Size());
            Log.Information("图片标定成功");
            return res;
        }
        finally
        {
            mask.Dispose();
            newCameraMatrix.Dispose();
            roiMat.Dispose();
        }
       
    }

    #endregion

    #region SNR

    public static double CalculateSnr(Mat src, double time)
    {
        var gray = new Mat();
        try
        {
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
            var snr = 10 * Math.Log10(signalPower / adjustedNoisePower);
            Log.Information("信噪比计算成功: " + snr);
            return snr;
        }
        finally
        {
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
            var percent = (double)aboveThresholdPixels / totalPixels;
            Log.Information("计算白色区间占比成功: " + percent);
            return percent;
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
        // 转换成CV_64FC4
        var mat3 = new Mat();
        var mat4 = new Mat();

        try
        {
            var baseType = mat1.Type();
            mat1.ConvertTo(mat3, MatType.CV_64FC4, 1.0 / 65535);
            mat2.ConvertTo(mat4, MatType.CV_64FC4, 1.0 / 65535);
            var dst = new Mat();
            Cv2.Multiply(mat3, mat4, dst);
            dst.ConvertTo(dst, baseType, 65535);
            Log.Information("正片叠底成功");
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
        var dst = new Mat();
        try
        {
            if (roi == "0,1,0,1") return src;
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
            Log.Information("截取部分成功");
            dst = src[rect];
            var res = dst.Resize(new Size(width, height));
            return res;
        }
        catch (Exception e)
        {
            Log.Error(e, "截取部分失败");
            return src;
        }
        finally
        {
            dst.Dispose();
        }
    }

    #endregion

    #region 中心旋转

    public static Mat Rotate(Mat src, double angle)
    {
        var rot = new Mat();
        try
        {
            // ReSharper disable PossibleLossOfFraction
            if (angle == 0) return src;
            var center = new Point2f(src.Width / 2, src.Height / 2);
            rot = Cv2.GetRotationMatrix2D(center, angle, 1.0);
            var dst = new Mat();
            Cv2.WarpAffine(src, dst, rot, src.Size());
            Log.Information("中心旋转成功");
            return dst;
        }
        catch (Exception e)
        {
            Log.Error(e, "中心旋转失败");
            return src;
        }
        finally
        {
            rot.Dispose();
        }
    }

    #endregion

    #region 畸形校正

    public static void CalibrateCamera(List<string> images, out double[,]? cameraMatrix, out double[]? distCoeffs)
    {
        // 定义棋盘格的内角点数量
        var chessboardWidth = 17; // 棋盘格每行内角点数量
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
            for (var j = 0; j < chessboardWidth; j++)
            {
                objectPoints[i * chessboardWidth + j] = new Point3f(j, i, 0);
                // 棋盘格每个方格的边长为 10mm
                objectPoints[i * chessboardWidth + j] *= squareSize;
            }


            // 读取图片
            var mat = new Mat(image, ImreadModes.Grayscale);
            imageSize = mat.Size();

            // 查找棋盘格角点
            var found = Cv2.FindChessboardCorners(mat, new Size(chessboardWidth, chessboardHeight), out var imagePoints,
                ChessboardFlags.AdaptiveThresh);

            if (!found) continue;
            // 亚像素精确化
            Cv2.CornerSubPix(mat, imagePoints, new Size(11, 11), new Size(-1, -1),
                new TermCriteria(CriteriaTypes.Eps | CriteriaTypes.MaxIter, 30, 0.1));
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
        Log.Information("相机标定成功");
    }

    #endregion

    #region 统计灰度直方图

    public static int Histogram(Mat src, double percentage)
    {
        // 计算灰度直方图
        var hist = new Mat();
        try
        {
            Cv2.CalcHist([src], [0], null, hist, 1, [65535], [new Rangef(0, 65535)]);

            var total = src.Rows * src.Cols;
            var pixels = total * percentage;
        
            // 白底从前往后找，黑底从后往前找
            var threshold = 65535;
            float count = 0;
            for (var i = 65535; i >= 0; i--)
            {
                count += hist.At<float>(i);
                if (count >= pixels)
                {
                    threshold = i;
                    break;
                }
            }

            Log.Information("计算灰度直方图成功: " + threshold);
            return threshold;
        }
        finally
        {
            hist.Dispose();
        }
    }
    

    #endregion
    
    #region LUT线性变换
    
    public static Mat LutLinearTransform(Mat src, int inLow, int inHigh, int outLow, int outHigh)
    {
        // check if the input range is valid
        if (inHigh == inLow || (inHigh - inLow) == (outHigh - outLow)) return src;
        // 计算斜率和截距
        var scale = (outHigh - outLow) / (inHigh - inLow);
        var shift = outLow - inLow * scale;

        var enhancedImage = new Mat(src.Rows, src.Cols, MatType.CV_16U);

        unsafe
        {
            var srcPtr = (ushort*)src.Data.ToPointer();
            var dstPtr = (ushort*)enhancedImage.Data.ToPointer();

            var totalPixels = src.Rows * src.Cols;

            for (var i = 0; i < totalPixels; i++)
            {
                var pixelValue = srcPtr[i];

                // 线性变换
                var newValue = pixelValue * scale + shift;
                dstPtr[i] = (ushort)Math.Max(0, Math.Min(65535, newValue));
            }
        }

        return enhancedImage;
    }
    
    #endregion

    #region 自动灰阶

    public static void AdjustImageColorLevelAuto(Mat src, out int leftGrayLevel, out int rightGrayLevel)
    {
        // 统计灰度直方图
        var grayHist = new Mat();

        try
        {
            Cv2.CalcHist([src], [0], null, grayHist, 1, [65535], [new Rangef(0, 65535)]);

            // 设置LowCut和HighCut
            var size = src.Rows * src.Cols;
            var lowCut = 0.2f;
            var highCut = 0.2f;

            // 根据LowCut和HighCut查找最大值最小值
            int bMax = 0, bMin = 0;
            var lowTh = lowCut * 0.01f * size;
            var highTh = highCut * 0.01f * size;

            // B通道查找最小最大值
            var sumTempB = 0;
            for (var i = 0; i < 65536; i++)
            {
                sumTempB += grayHist.At<int>(i);
                if (sumTempB >= lowTh)
                {
                    bMin = i;
                    break;
                }
            }
            sumTempB = 0;
            for (var i = 65535; i >= 0; i--)
            {
                sumTempB += grayHist.At<int>(i);
                if (sumTempB >= highTh)
                {
                    bMax = i;
                    break;
                }
            }

            // 让左右色阶至少相差1
            if (bMin == bMax)
            {
                if (bMin > 1)
                {
                    bMin -= 1;
                }
                else
                {
                    bMax += 1;
                }
            }

            // 建立分段线性查找表
            leftGrayLevel = bMin;
            rightGrayLevel = bMax;

            // B分量查找表
            var bTable = new int[65536];
            for (var i = 0; i < 65536; i++)
            {
                if (i <= bMin)
                    bTable[i] = 0;
                else if (i > bMin && i < bMax)
                    bTable[i] = (int)Math.Round((float)(i - bMin) / (bMax - bMin) * 65535);
                else
                    bTable[i] = 65535;
            }

            // 用相应的查找表进行分段线性拉伸
            unsafe
            {
                var srcPtr = (ushort*)src.Data.ToPointer();
                for (var i = 0; i < size; i++)
                {
                    srcPtr[i] = (ushort)bTable[srcPtr[i]];
                }
            }
        }
        finally
        {
            grayHist.Dispose();
        }
    }

    #endregion
    
}