package com.zktony.www.model

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:40
 */
/**
 * 先进先出的任务队列
 */
class Queue<T> {
    private val queue = mutableListOf<T>()

    /**
     * 入队
     */
    fun enqueue(element: T) {
        queue.add(element)
    }

    /**
     * 出队
     */
    fun dequeue(): T? {
        return if (queue.isEmpty()) {
            null
        } else {
            queue.removeAt(0)
        }
    }

    /**
     * 获取队列长度
     */
    fun size(): Int {
        return queue.size
    }

    /**
     * 获取队列头部元素
     */
    fun peek(): T? {
        return if (queue.isEmpty()) {
            null
        } else {
            queue[0]
        }
    }

    /**
     * 是否包含某个元素
     */
    fun contains(element: T): Boolean {
        return queue.contains(element)
    }

    /**
     * 清空队列
     */
    fun clear() {
        queue.clear()
    }

    /**
     * 队列是否为空
     */
    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }
}