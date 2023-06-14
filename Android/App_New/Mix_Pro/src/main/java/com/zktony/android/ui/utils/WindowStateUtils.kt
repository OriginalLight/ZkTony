package com.zktony.android.ui.utils

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class NavigationType {
    NONE, NAVIGATION_RAIL, PERMANENT_NAVIGATION_DRAWER
}

/**
 * Different type of page supported by app depending on each screen.
 */
enum class PageType {
    LIST, EDIT, AUTH, START, RUNTIME
}