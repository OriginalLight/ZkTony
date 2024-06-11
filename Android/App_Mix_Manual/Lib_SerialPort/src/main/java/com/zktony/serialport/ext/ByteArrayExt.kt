package com.zktony.serialport.ext

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

// https://www.jianshu.com/p/8ee33de703e2

fun ByteArray.toHexString(hasSpace: Boolean = true) =
    this.joinToString(if (hasSpace) " " else "") { String.format("%02X", it) }

fun ByteArray.toAsciiString(hasSpace: Boolean = false) =
    this.map { it.toInt().toChar() }.joinToString(if (hasSpace) " " else "")

fun ByteArray.readInt8(offset: Int = 0): Int {
    throwOffsetError(this, offset, 1)
    return this[offset].toInt()
}

fun ByteArray.readUInt8(offset: Int = 0): Int {
    throwOffsetError(this, offset, 1)
    return this[offset].toInt() and 0xFF
}

fun ByteArray.readInt16BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return (this[offset].toInt() shl 8) + (this[offset + 1].toInt() and 0xFF)
}

fun ByteArray.readUInt16BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
}


fun ByteArray.readInt16LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return (this[offset + 1].toInt() shl 8) + (this[offset].toInt() and 0xFF)
}

fun ByteArray.readUInt16LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 2)
    return ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset].toInt() and 0xFF)
}

fun ByteArray.readInt32BE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 4)
    return (this[offset].toInt()) shl 24 or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)
}

fun ByteArray.readUInt32BE(offset: Int = 0): Long {
    throwOffsetError(this, offset, 4)
    return (((this[offset].toInt() and 0xFF).toLong() shl 24) or
            ((this[offset + 1].toInt() and 0xFF).toLong() shl 16) or
            ((this[offset + 2].toInt() and 0xFF).toLong() shl 8) or
            (this[offset + 3].toInt() and 0xFF).toLong())
}

fun ByteArray.readInt32LE(offset: Int = 0): Int {
    throwOffsetError(this, offset, 4)
    return (this[offset + 3].toInt()) shl 24 or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            (this[offset].toInt() and 0xFF)
}

fun ByteArray.readUInt32LE(offset: Int = 0): Long {
    throwOffsetError(this, offset, 4)
    return (((this[offset + 3].toInt() and 0xFF).toLong() shl 24) or
            ((this[offset + 2].toInt() and 0xFF).toLong() shl 16) or
            ((this[offset + 1].toInt() and 0xFF).toLong() shl 8) or
            (this[offset].toInt() and 0xFF).toLong())
}

// 四字节 float
fun ByteArray.readFloatBE(offset: Int = 0) =
    java.lang.Float.intBitsToFloat(this.readInt32BE(offset))

// 四字节 float
fun ByteArray.readFloatLE(offset: Int = 0) =
    java.lang.Float.intBitsToFloat(this.readInt32LE(offset))

//
//// 四字节 时间
@SuppressLint("SimpleDateFormat")
fun ByteArray.readTimeBE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(pattern).format(this.readUInt32BE(offset) * 1000)

@SuppressLint("SimpleDateFormat")
fun ByteArray.readTimeLE(offset: Int = 0, pattern: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(pattern).format(this.readUInt32LE(offset) * 1000)

// 读取ByteArray
fun ByteArray.readByteArrayBE(offset: Int, byteLength: Int): ByteArray {
    throwOffsetError(this, offset)
    return this.copyOfRange(
        offset,
        if ((offset + byteLength) > this.size) this.size else offset + byteLength
    )
}

fun ByteArray.readByteArrayLE(offset: Int, byteLength: Int): ByteArray {
    throwOffsetError(this, offset)
    return this.readByteArrayBE(offset, byteLength).reversedArray()
}

// 读取指定范围输出字符串
fun ByteArray.readStringBE(
    offset: Int,
    byteLength: Int,
    encoding: String = "hex",
    hasSpace: Boolean = encoding.lowercase(Locale.ROOT) == "hex"
): String {
    return when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> this.readByteArrayBE(offset, byteLength).toHexString(hasSpace)
        "ascii" -> this.readByteArrayBE(offset, byteLength).map { it.toInt().toChar() }
            .joinToString(if (hasSpace) " " else "")

        else -> ""
    }
}

fun ByteArray.readStringLE(
    offset: Int,
    byteLength: Int,
    encoding: String = "hex",
    hasSpace: Boolean = encoding.lowercase(Locale.ROOT) == "hex"
): String {
    return when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> this.readByteArrayLE(offset, byteLength).toHexString(hasSpace)
        "ascii" -> this.readByteArrayLE(offset, byteLength).map { it.toInt().toChar() }
            .joinToString(if (hasSpace) " " else "")

        else -> ""
    }
}

// ********************************************** 写 **********************************************
fun ByteArray.writeInt8(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset)
    // 无符号Int 执行写入
    this[offset] = value.toByte()
    return this
}

fun ByteArray.writeInt16BE(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 2)
    // 执行写入
    this[offset] = (value and 0xff00 ushr 8).toByte()
    this[offset + 1] = (value and 0xff).toByte()
    return this
}

fun ByteArray.writeInt16LE(value: Int, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 2)
    // 无符号Int 执行写入
    this[offset] = (value and 0xff).toByte()
    this[offset + 1] = (value and 0xff00 ushr 8).toByte()
    return this
}

fun ByteArray.writeInt32BE(value: Long, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    // 无符号Int 执行写入
    this[offset + 3] = (value and 0xff).toByte()
    this[offset + 2] = (value and 0xff00 ushr 8).toByte()
    this[offset + 1] = (value and 0xff0000 ushr 16).toByte()
    this[offset] = (value and 0xff000000 ushr 24).toByte()
    return this
}

fun ByteArray.writeInt32LE(value: Long, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    // 无符号Int 执行写入
    this[offset] = (value and 0xff).toByte()
    this[offset + 1] = (value and 0xff00 ushr 8).toByte()
    this[offset + 2] = (value and 0xff0000 ushr 16).toByte()
    this[offset + 3] = (value and 0xff000000 ushr 24).toByte()
    return this
}

// 写入Float类型
fun ByteArray.writeFloatBE(value: Float, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    this.writeInt32BE(java.lang.Float.floatToIntBits(value).toLong(), offset)
    return this
}

fun ByteArray.writeFloatLE(value: Float, offset: Int = 0): ByteArray {
    throwOffsetError(this, offset, 4)
    this.writeInt32LE(java.lang.Float.floatToIntBits(value).toLong(), offset)
    return this
}

// 写入时间
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("SimpleDateFormat")
fun ByteArray.writeTimeBE(
    time: String,
    offset: Int = 0,
    pattern: String = "yyyy-MM-dd HH:mm:ss"
): ByteArray {
    this.writeInt32BE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
    return this
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("SimpleDateFormat")
fun ByteArray.writeTimeLE(
    time: String,
    offset: Int = 0,
    pattern: String = "yyyy-MM-dd HH:mm:ss"
): ByteArray {
    this.writeInt32LE(SimpleDateFormat(pattern).parse(time).time / 1000, offset)
    return this
}

// 指定位置写入ByteArray
fun ByteArray.writeByteArrayBE(
    byteArray: ByteArray,
    offset: Int = 0,
    length: Int = byteArray.size
): ByteArray {
    this.writeStringBE(byteArray.toHexString(), offset, length)
    return this
}

fun ByteArray.writeByteArrayLE(
    byteArray: ByteArray,
    offset: Int = 0,
    length: Int = byteArray.size
): ByteArray {
    this.writeStringLE(byteArray.toHexString(), offset, length)
    return this
}

// ********************************************** 其他 **********************************************

// 指定位置插入ByteArray
fun ByteArray.insertByteArrayBE(
    insertArray: ByteArray,
    originalIndex: Int = 0,
    insertArrayOffset: Int = 0,
    insertArrayLength: Int = insertArray.size - insertArrayOffset
): ByteArray {
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex, this.size)
    val insertFinalArray =
        insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
    return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
}

fun ByteArray.insertByteArrayLE(
    insertArray: ByteArray,
    originalIndex: Int = 0,
    insertArrayOffset: Int = 0,
    insertArrayLength: Int = insertArray.size - insertArrayOffset
): ByteArray {
    insertArray.reverse()
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex, this.size)
    val insertFinalArray =
        insertArray.copyOfRange(insertArrayOffset, insertArrayOffset + insertArrayLength)
    return byteArrayPre.plus(insertFinalArray).plus(byteArrayLast)
}

// 指定位置开始替换ByteArray
fun ByteArray.replaceByteArrayBE(
    replaceArray: ByteArray,
    originalIndex: Int = 0,
    replaceArrayOffset: Int = 0,
    replaceArrayLength: Int = replaceArray.size - replaceArrayOffset
): ByteArray {
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex + replaceArrayLength, this.size)
    val replaceFinalArray =
        replaceArray.copyOfRange(replaceArrayOffset, replaceArrayOffset + replaceArrayLength)
    return byteArrayPre.plus(replaceFinalArray).plus(byteArrayLast)
}

fun ByteArray.replaceByteArrayLE(
    replaceArray: ByteArray,
    originalIndex: Int = 0,
    replaceArrayOffset: Int = 0,
    replaceArrayLength: Int = replaceArray.size - replaceArrayOffset
): ByteArray {
    replaceArray.reverse()
    val byteArrayPre = this.copyOfRange(0, originalIndex)
    val byteArrayLast = this.copyOfRange(originalIndex + replaceArrayLength, this.size)
    val replaceFinalArray =
        replaceArray.copyOfRange(replaceArrayOffset, replaceArrayOffset + replaceArrayLength)
    return byteArrayPre.plus(replaceFinalArray).plus(byteArrayLast)
}

// 指定位置写入String
/**
 * @str 写入的字符串
 * @encoding  Hex & ASCII
 */
fun ByteArray.writeStringBE(str: String, offset: Int = 0, encoding: String = "hex"): ByteArray {
    throwOffsetError(this, offset)
    when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> {
            val hex = str.replace(" ", "")
            throwHexError(hex)
            for (i in 0 until hex.length / 2) {
                if (i + offset < this.size) {
                    this[i + offset] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            }
        }

        "ascii" -> {
            val hex = str.toCharArray().map { it.code }.joinToString("") { it.toString(16) }
            this.writeStringBE(hex, offset, "hex")
        }
    }
    return this
}

// length: 写入长度
fun ByteArray.writeStringLE(str: String, offset: Int = 0, encoding: String = "hex"): ByteArray {
    when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> {
            val hex = str.reversalEvery2Charts()
            this.writeStringBE(hex, offset, encoding)
        }

        "ascii" -> {
            val hex = str.toCharArray().map { it.code }.joinToString("") { it.toString(16) }
            this.writeStringLE(hex, offset, "hex")
        }
    }
    return this
}

fun ByteArray.writeStringBE(
    str: String,
    offset: Int,
    length: Int,
    encoding: String = "hex"
): ByteArray {
    throwOffsetError(this, offset, length)
    when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> {
            val hex = str.replace(" ", "").padStart(length * 2, '0').substring(0, length * 2)
            throwHexError(hex)
            this.writeStringBE(hex, offset)
        }

        "ascii" -> {
            val hex = str.toCharArray().map { it.code }.joinToString("") { it.toString(16) }
            this.writeStringBE(hex, offset, length, "hex")
        }
    }
    return this
}

fun ByteArray.writeStringLE(
    str: String,
    offset: Int,
    length: Int,
    encoding: String = "hex"
): ByteArray {
    when (encoding.lowercase(Locale.ROOT)) {
        "hex" -> {
            val hex = str.reversalEvery2Charts().padEnd(length * 2, '0').substring(0, length * 2)
            this.writeStringBE(hex, offset, length, encoding)
        }

        "ascii" -> {
            val hex = str.toCharArray().map { it.code }.joinToString("") { it.toString(16) }
            this.writeStringLE(hex, offset, length, "hex")
        }
    }
    return this
}

// 无符号int
private fun readUnsigned(byteArray: ByteArray, len: Int, offset: Int, littleEndian: Boolean): Long {
    var value = 0L
    for (count in 0 until len) {
        val shift = (if (littleEndian) count else (len - 1 - count)) shl 3
        value =
            value or (0xff.toLong() shl shift and (byteArray[offset + count].toLong() shl shift))
    }
    return value
}

/****  异常  ****/
private fun throwLenError(byteArray: ByteArray, byteLength: Int) {
    if (byteLength <= 0 || byteLength > 4) throw IllegalArgumentException("The value of \"byteLength\" is out of range. It must be >= 1 and <= 4. Received $byteLength")
    if (byteLength > byteArray.size) throw IllegalArgumentException("Attempt to write outside ByteArray bounds.")
}

private fun throwHexError(hex: String) {
    if (hex.length % 2 != 0) throw IllegalArgumentException("The value of \"hex\".length is out of range. It must be an even number")
}

private fun throwOffsetError(
    byteArray: ByteArray,
    offset: Int,
    length: Int = 1,
    byteLength: Int = 0
) {
    if (offset > byteArray.size - length - byteLength) throw IllegalArgumentException("The value of \"offset\" is out of range. It must be >= 0 and <= ${byteArray.size - length - byteLength}. Received $offset")
}


// 根据命令格式分割成多个ByteArray

fun ByteArray.splitByteArray(head: ByteArray, end: ByteArray): List<ByteArray> {
    if (this.size <= head.size + end.size) return listOf()
    var byteArray = this.copyOfRange(0, this.size)
    val list = mutableListOf<ByteArray>()
    while (byteArray.isNotEmpty()) {
        val headIndex = byteArray.indexOf(head)
        val endIndex = byteArray.indexOf(end)
        if (headIndex != -1 && endIndex != -1) {
            val bytes = byteArray.copyOfRange(headIndex, endIndex + end.size)
            list.add(bytes)
            if (bytes.size == byteArray.size) {
                break
            } else {
                byteArray = byteArray.copyOfRange(bytes.size, byteArray.size)
            }
        } else {
            list.add(byteArray)
            break
        }
    }
    return list
}

fun ByteArray.indexOf(bytes: ByteArray): Int {
    if (bytes.isEmpty() || this.isEmpty()) return -1
    val index = this.indexOf(bytes.first())
    val array = this.copyOfRange(index, index + bytes.size)
    return if (array.contentEquals(bytes)) index else -1
}