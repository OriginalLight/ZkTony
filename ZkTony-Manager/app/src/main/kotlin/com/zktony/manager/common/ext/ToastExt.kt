package com.zktony.manager.common.ext

import android.widget.Toast

fun String.showShortToast() = Toast.makeText(Ext.ctx, this, Toast.LENGTH_SHORT).show()

fun String.showLongToast() = Toast.makeText(Ext.ctx, this, Toast.LENGTH_LONG).show()

fun Int.showShortToast() = Toast.makeText(Ext.ctx, this, Toast.LENGTH_SHORT).show()

fun Int.showLongToast() = Toast.makeText(Ext.ctx, this, Toast.LENGTH_LONG).show()