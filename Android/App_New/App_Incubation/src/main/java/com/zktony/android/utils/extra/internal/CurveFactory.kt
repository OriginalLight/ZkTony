package com.zktony.android.utils.extra.internal

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
class CurveFactory : KoinComponent {
    private val dao: CurveDao by inject()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun setup() {
        scope.launch {
            dao.getAll().collect { calculate(it) }
        }
    }

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
