package com.zktony.android.ui.utils

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

fun <T : Any> LazyListScope.items(
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    contentType: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    items(
        count = items.itemCount,
        key = items.itemKey(key),
        contentType = items.itemKey(contentType)
    ) loop@{ index ->
        val item = items[index] ?: return@loop
        itemContent(item)
    }
}

fun <T : Any> LazyListScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    contentType: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, T) -> Unit
) {
    items(
        count = items.itemCount,
        key = items.itemKey(key),
        contentType = items.itemKey(contentType)
    ) loop@{ index ->
        val item = items[index] ?: return@loop
        itemContent(index, item)
    }
}

fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    contentType: ((T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(T) -> Unit
) {
    items(
        count = items.itemCount,
        key = items.itemKey(key),
        contentType = items.itemKey(contentType)
    ) loop@{ index ->
        val item = items[index] ?: return@loop
        itemContent(item)
    }
}

fun <T : Any> LazyGridScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((T) -> Any)? = null,
    contentType: ((T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, T) -> Unit
) {
    items(
        count = items.itemCount,
        key = items.itemKey(key),
        contentType = items.itemKey(contentType)
    ) loop@{ index ->
        val item = items[index] ?: return@loop
        itemContent(index, item)
    }
}

fun <T : Any> LazyPagingItems<T>.toList() = this.itemSnapshotList.items