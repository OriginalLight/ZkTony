package com.zktony.android.utils.service

import com.zktony.android.data.dao.CurveDao
import com.zktony.android.data.entities.Curve
import com.zktony.android.utils.extra.appState
import com.zktony.android.utils.extra.calculateLinearRelation
import com.zktony.android.utils.extra.fitQuadraticCurve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CurveService(private val dao: CurveDao) : AbstractService() {

    /**
     * 初始化曲线
     * 1. 从数据库中获取所有曲线
     * 2. 计算曲线函数
     * 3. 将曲线函数保存到appState中
     */
    override fun setup() {
        scope.launch { dao.getAll().collect { calculate(it) } }
    }

    /**
     * 计算曲线函数
     * 1. 如果曲线未启用，则曲线函数为y=x*100
     * 2. 如果曲线启用，则根据曲线点计算曲线函数
     * 3. 如果曲线点不满足要求，则曲线函数为y=x*100
     * 4. 如果曲线点满足要求，则根据曲线点计算曲线函数
     *
     * @param curves List<Curve>
     */
    private fun calculate(curves: List<Curve>) {
        curves.forEach { curve ->
            try {
                if (!curve.enable) {
                    appState.curve[curve.index] = { x -> x * 100 }
                    return@forEach
                }

                val points = curve.points
                val check = points.all { it.x > 0.0 && it.y > 0.0 }

                val curveFunction = when (points.size) {
                    0 -> { x -> x * 100 }
                    1 -> if (check) calculateLinearRelation(points[0]) else { x -> x * 100 }
                    2 -> if (check) calculateLinearRelation(
                        points[0],
                        points[1]
                    ) else { x -> x * 100 }

                    else -> if (check) fitQuadraticCurve(points) else { x -> x * 100 }
                }

                appState.curve[curve.index] = curveFunction
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
