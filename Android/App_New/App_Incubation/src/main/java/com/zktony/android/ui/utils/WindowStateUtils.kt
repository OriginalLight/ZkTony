package com.zktony.android.ui.utils

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class NavigationType {
    NONE, ONLY_RAIL, ONLY_TOP, RAIL_AND_TOP
}


enum class NavigationContentPosition {
    TOP, CENTER
}

/**
 * Different type of page supported by app depending on each screen.
 */
enum class PageType {
    LIST, START, RUNTIME,
    CALIBRATION_LIST, CALIBRATION_DETAIL,
    PROGRAM_LIST, PROGRAM_DETAIL,
    SETTINGS, AUTH, CONFIG, MOTOR_LIST, MOTOR_DETAIL
}