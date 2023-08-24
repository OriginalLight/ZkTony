package com.zktony.android.ui.utils

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class NavigationType {
    NONE, NAVIGATION_RAIL, PERMANENT_NAVIGATION_DRAWER
}


enum class NavigationContentPosition {
    TOP, CENTER
}

/**
 * Different type of page supported by app depending on each screen.
 */
enum class PageType {
    HOME,
    CALIBRATION_LIST, CALIBRATION_DETAIL,
    PROGRAM_LIST, PROGRAM_DETAIL,
    SETTINGS, AUTH, CONFIG, MOTOR_LIST, MOTOR_DETAIL
}