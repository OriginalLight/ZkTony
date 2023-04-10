package com.zktony.www.ui.container

import com.zktony.common.base.BaseViewModel
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate

class ContainerViewModel : BaseViewModel()

data class PlateUiState(
    val plate: Plate? = null,
    val holes: List<Hole> = emptyList()
)