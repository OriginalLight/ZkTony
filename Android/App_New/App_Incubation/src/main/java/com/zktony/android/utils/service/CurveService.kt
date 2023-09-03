package com.zktony.android.utils.service

import com.zktony.android.data.dao.CurveDao
import com.zktony.android.data.entities.Curve
import com.zktony.android.utils.extra.appState
import com.zktony.android.utils.extra.calculateLinearRelation
import com.zktony.android.utils.extra.fitQuadraticCurve
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CurveService @Inject constructor(
    private val dao: CurveDao
) : AbstractService() {
    /**
     * 初始化曲线
     * 1. 从数据库中获取所有曲线
     * 2. 计算曲线函数
     * 3. 将曲线函数保存到appState中
     */
    override fun create() {
        job = scope.launch {
            dao.getAll().collect { curves ->
                curves.forEach { curve ->
                    val curveFunction = calculateCurveFunction(curve)
                    appState.curve[curve.index] = curveFunction
                }
            }
        }
    }

    private fun calculateCurveFunction(curve: Curve): (Double) -> Double? {
        if (!curve.enable) {
            return { x -> x * 100 }
        }

        val points = curve.points
        val check = points.all { it.x > 0.0 && it.y > 0.0 }

        return when (points.size) {
            0 -> { x -> x * 100 }
            1 -> if (check) calculateLinearRelation(points[0]) else { x -> x * 100 }
            2 -> if (check) calculateLinearRelation(points[0], points[1]) else { x -> x * 100 }
            else -> if (check) fitQuadraticCurve(points) else { x -> x * 100 }
        }
    }
}
