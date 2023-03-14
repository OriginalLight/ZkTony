package com.zktony.www.ui.container

import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor() : BaseViewModel()

data class PlateUiState(
    val plate: Plate? = null,
    val holes: List<Hole> = emptyList()
)