package com.zktony.android.ext

import android.widget.Toast

/**
 * showShortToast
 */
fun String.showShortToast() {
    Toast.makeText(Ext.ctx, this, Toast.LENGTH_SHORT).show()
}

/**
 * showLongToast
 */
fun String.showLongToast() {
    Toast.makeText(Ext.ctx, this, Toast.LENGTH_LONG).show()
}

/**
 * showShortToast
 */
fun Int.showShortToast() {
    Toast.makeText(Ext.ctx, this, Toast.LENGTH_SHORT).show()
}

/**
 * showLongToast
 */
fun Int.showLongToast() {
    Toast.makeText(Ext.ctx, this, Toast.LENGTH_LONG).show()
}