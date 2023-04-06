package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.dao.ActionDao
import com.zktony.www.data.local.dao.ProgramDao
import com.zktony.www.data.local.entity.Action
import com.zktony.www.data.local.entity.getActionEnum
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*

class ActionViewModel constructor(
    private val programDao: ProgramDao,
    private val actionDao: ActionDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ActionUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * 加载程序列表
     * @param id [String] 程序ID
     */
    fun init(id: String) {
        viewModelScope.launch {
            if (id.isNotEmpty()) {
                actionDao.getByProgramId(id).collect { actionList ->
                    _uiState.value = _uiState.value.copy(
                        actionList = actionList,
                        programId = id
                    )
                }
            }
        }
    }

    /**
     * 添加程序
     */
    fun insert() {
        viewModelScope.launch {
            val uiState = _uiState.value
            actionDao.insert(
                Action(
                    id = UUID.randomUUID().toString(),
                    programId = uiState.programId,
                    order = uiState.order,
                    mode = uiState.action,
                    time = uiState.time,
                    temperature = uiState.temp,
                    liquidVolume = uiState.volume.toFloat(),
                    count = uiState.count,
                )
            )
            _uiState.value = _uiState.value.copy(
                order = uiState.order + 1,
            )
            delay(500L)
            updateActions()
        }
    }

    /**
     * 删除步骤
     * @param action [Action] 步骤
     */
    fun delete(action: Action) {
        viewModelScope.launch {
            actionDao.delete(action)
            delay(500L)
            PopTip.show("已删除")
            updateActions()
        }
    }

    /**
     * 更新程序中的actions
     */
    private fun updateActions() {
        viewModelScope.launch {
            actionDao.getByProgramId(_uiState.value.programId).firstOrNull()?.let {
                val str = StringBuilder()
                if (it.isNotEmpty()) {
                    it.forEachIndexed { index, action ->
                        str.append(getActionEnum(action.mode).value)
                        if (index != it.size - 1) {
                            str.append(" -> ")
                        }
                    }
                } else {
                    str.append("没有任何操作，去添加吧...")
                }
                programDao.getById(_uiState.value.programId).firstOrNull()?.let { program ->
                    programDao.update(
                        program.copy(
                            actions = str.toString(),
                            actionCount = it.size
                        )
                    )
                }
            }
        }
    }


    /**
     * 切换步骤
     */
    fun switchAction(index: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                action = index
            )
            delay(100L)
            recommend()
        }
    }

    /**
     * 切换盒子
     */
    fun switchBox(index: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                box = index
            )
            delay(100L)
            recommend()
        }
    }

    /**
     * 验证步骤
     * @return [Boolean] 是否有效
     */
    fun validate(): Boolean {
        val uiState = _uiState.value
        return uiState.order > 0
                && uiState.time > 0f
                && uiState.temp > 0
                && uiState.volume > 0
                && (uiState.action != 3 || uiState.count > 0)
    }

    private fun recommend() {
        val recommended = Recommended.values()
            .first { it.action == _uiState.value.action && it.index == _uiState.value.box }
        _uiState.value = _uiState.value.copy(
            time = recommended.time,
            temp = recommended.temp,
            volume = recommended.volume,
            count = recommended.count
        )
    }

    fun editOrder(order: Int) {
        _uiState.value = _uiState.value.copy(
            order = order
        )
    }

    fun editTime(time: Float) {
        _uiState.value = _uiState.value.copy(
            time = time
        )
    }

    fun editTemp(temp: Float) {
        _uiState.value = _uiState.value.copy(
            temp = temp
        )
    }

    fun editVolume(volume: Int) {
        _uiState.value = _uiState.value.copy(
            volume = volume
        )
    }

    fun editCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            count = count
        )
    }

}

data class ActionUiState(
    val actionList: List<Action> = emptyList(),
    val programId: String = "",
    val box: Int = 0,
    val action: Int = 0,
    val order: Int = 1,
    val time: Float = 1f,
    val temp: Float = 35f,
    val volume: Int = 5000,
    val count: Int = 1
)

// 推荐配置
enum class Recommended(
    val action: Int,
    val index: Int,
    val time: Float,
    val temp: Float,
    val volume: Int,
    val count: Int
) {
    // 完整
    WHOLE_BLOCKING_LIQUID(0, 0, 1f, 35f, 10000, 1),
    WHOLE_ANTIBODY_ONE(1, 0, 12f, 5f, 8500, 1),
    WHOLE_ANTIBODY_TWO(2, 0, 1f, 35f, 10000, 1),
    WHOLE_WASHING(3, 0, 5f, 35f, 13000, 3),

    // 半个
    HALF_BLOCKING_LIQUID(0, 1, 1f, 35f, 8000, 1),
    HALF_ANTIBODY_ONE(1, 1, 12f, 5f, 6000, 1),
    HALF_ANTIBODY_TWO(2, 1, 1f, 35f, 8000, 1),
    HALF_WASHING(3, 1, 5f, 35f, 10000, 3),

    // 四分之一盒
    QUARTER_BLOCKING_LIQUID(0, 2, 1f, 35f, 4000, 1),
    QUARTER_ANTIBODY_ONE(1, 2, 12f, 5f, 3000, 1),
    QUARTER_ANTIBODY_TWO(2, 2, 1f, 35f, 4000, 1),
    QUARTER_WASHING(3, 2, 5f, 35f, 5000, 3),
}

enum class Box(val index: Int, val value: String) {
    WHOLE(0, "全盒"),
    HALF(1, "半盒"),
    THIRD(2, "1/4盒")
}