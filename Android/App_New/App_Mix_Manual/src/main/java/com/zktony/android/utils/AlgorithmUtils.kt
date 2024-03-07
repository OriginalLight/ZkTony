package com.zktony.android.utils

import com.zktony.android.data.entities.internal.Point

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:06
 */

object AlgorithmUtils {
    fun calculateLinearRelation(point: Point): (Double) -> Double? {
        // 计算斜率
        val slope = point.y / point.x

        return { x ->
            slope * x
        }
    }

    fun calculateLinearRelation(point1: Point, point2: Point): (Double) -> Double? {
        // 计算斜率
        val slope = (point2.y - point1.y) / (point2.x - point1.x)

        // 计算截距
        val intercept = point1.y - slope * point1.x

        return { x ->
            slope * x + intercept
        }
    }

    fun calculateCalibrationFactorNew(pulse: Int, point: Double): (Double) -> Double {
        if (point == 0.0) return { x -> x * 100 }
        val slopeList = mutableListOf<Double>()
        slopeList.add(pulse / point)
        if (slopeList.isEmpty()) {
            return { x -> x * 100 }
        } else {
            return { x -> x * slopeList.average() }
        }
    }

    fun calculateCalibrationFactor(points: List<Point>): (Double) -> Double {
        if (points.isEmpty()) return { x -> x * 100 }
        val slopeList = mutableListOf<Double>()
        points.forEach {
            if (it.x == 0.0) return@forEach
            slopeList.add(it.y / it.x)
        }
        if (slopeList.isEmpty()) {
            return { x -> x * 100 }
        } else {
            return { x -> x * slopeList.average() }
        }
    }

    fun fitQuadraticCurve(points: List<Point>): (Double) -> Double? {
        val n = points.size

        if (n < 3) {
            throw IllegalArgumentException("至少需要3个点来拟合曲线")
        }

        // 初始化矩阵和向量
        val a = Array(3) { DoubleArray(3) }
        val b = DoubleArray(3)

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                a[i][j] = 0.0
            }
            b[i] = 0.0
        }

        // 构建矩阵 A 和向量 B
        for (i in 0 until n) {
            val x = points[i].x
            val y = points[i].y
            val x2 = x * x

            a[0][0] += 1.0
            a[0][1] += x
            a[0][2] += x2

            a[1][0] += x
            a[1][1] += x2
            a[1][2] += x * x2

            a[2][0] += x2
            a[2][1] += x * x2
            a[2][2] += x2 * x2

            b[0] += y
            b[1] += x * y
            b[2] += x2 * y
        }

        // 解线性方程组以获得多项式系数
        val coefficients = solveLinearSystem(a, b)

        // 构建曲线函数
        return { x ->
            if (coefficients.size >= 3) {
                coefficients[0] + coefficients[1] * x + coefficients[2] * x * x
            } else {
                null
            }
        }
    }

    fun solveLinearSystem(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        // 使用高斯消元法求解线性方程组
        val n = b.size

        for (i in 0 until n) {
            val maxIdx = findMaxRow(a, i, n)
            swapRows(a, i, maxIdx)
            swapElements(b, i, maxIdx)

            val pivot = a[i][i]

            for (j in i + 1 until n) {
                val factor = a[j][i] / pivot
                b[j] -= factor * b[i]
                for (k in i until n) {
                    a[j][k] -= factor * a[i][k]
                }
            }
        }

        // 回代求解
        val x = DoubleArray(n)

        for (i in n - 1 downTo 0) {
            var sum = 0.0

            for (j in i + 1 until n) {
                sum += a[i][j] * x[j]
            }

            x[i] = (b[i] - sum) / a[i][i]
        }

        return x
    }

    fun findMaxRow(matrix: Array<DoubleArray>, col: Int, n: Int): Int {
        var maxIdx = col
        var maxVal = matrix[col][col]

        for (i in col + 1 until n) {
            val currentVal = matrix[i][col]
            if (currentVal > maxVal) {
                maxVal = currentVal
                maxIdx = i
            }
        }

        return maxIdx
    }

    fun swapRows(matrix: Array<DoubleArray>, row1: Int, row2: Int) {
        val tempRow = matrix[row1]
        matrix[row1] = matrix[row2]
        matrix[row2] = tempRow
    }

    fun swapElements(array: DoubleArray, idx1: Int, idx2: Int) {
        val temp = array[idx1]
        array[idx1] = array[idx2]
        array[idx2] = temp
    }
}

