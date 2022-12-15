package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.serialport.util.Serial
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Container
import com.zktony.www.data.repository.ContainerRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor(
    private val repo: ContainerRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val serial = SerialManager.instance

    private val _container: MutableStateFlow<Container> = MutableStateFlow(Container())
    val container = _container.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect {
               if (it.isNotEmpty()) {
                   _container.value = it.first()
               }
            }
        }
    }

    /**
     * 更新容器
     * @param container [Container] 容器
     */
    fun replaceContainer(container: Container) {
        viewModelScope.launch {
            repo.insert(container)
        }
    }

    /**
     * 测试 移动到废液槽
     */
    fun toWasteY() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.wasteY, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    fun toWasteZ() {
        val motor = appViewModel.settings.value.motorUnits
        val move = motor.toMotionHex(container.value.wasteY, container.value.wasteZ)
        serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
        serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
        serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
    }


    /**
     * 测试 移动到洗液槽
     */
    fun toWashY() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.washY, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    fun toWashZ() {
        val motor = appViewModel.settings.value.motorUnits
        val move = motor.toMotionHex(container.value.washY, container.value.washZ)
        serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
        serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
        serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
    }

    /**
     * 测试 移动到阻断液槽
     */
    fun toBlockY() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.blockY, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    fun toBlockZ() {
        val motor = appViewModel.settings.value.motorUnits
        val move = motor.toMotionHex(container.value.blockY, container.value.blockZ)
        serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
        serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
        serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
    }

    /**
     * 测试 移动到抗体一槽
     */
    fun toOneY() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.oneY, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toOneZ() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.oneY, container.value.oneZ)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toRecycleOneZ() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.oneY, container.value.recycleOneZ)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 移动到抗体二槽
     */
    fun toTwoY() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.twoY, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 抗体二槽针头下降
     */
    fun toTwoZ() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(container.value.twoY, container.value.twoZ)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }
    }

    /**
     * 测试 回到原点
     */
    fun toZero() {
        viewModelScope.launch {
            val motor = appViewModel.settings.value.motorUnits
            val move = motor.toMotionHex(0f, 0f)
            serial.sendHex(Serial.TTYS0, V1.multiPoint(move))
            serial.sendHex(Serial.TTYS1, V1.multiPoint("0,0,0,"))
            serial.sendHex(Serial.TTYS2, V1.multiPoint("0,0,0,"))
        }

    }
}