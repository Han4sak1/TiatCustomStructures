package me.gei.tiatcustomstructures.internal.utils

object IndexChecker {
    /**
     * 检查给定的索引值是否在有效范围内。
     *
     * @param index 要检查的索引值
     * @param length 数组或列表的长度
     * 如果索引有效，则返回索引值本身；否则抛出IndexOutOfBoundsException
     */
    fun checkIndex(index: Int, length: Int) {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("索引: $index, 长度: $length")
        }
    }
}
