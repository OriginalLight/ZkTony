package com.zktony.www.ui.container

import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.entity.Hole
import com.zktony.www.data.local.entity.Plate

class ContainerViewModel : BaseViewModel()

data class PlateUiState(
    val plate: Plate? = null,
    val holes: List<Hole> = emptyList()
)