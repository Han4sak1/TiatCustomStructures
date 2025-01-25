package me.gei.tiatcustomstructures.internal.utils

import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import org.bukkit.Location
import java.util.concurrent.ThreadLocalRandom

/**
 * 处理字符串和区间
 */
object NumberStylizer {
    /**
     * 从范围中获取随机数.
     * 输入例子: [4;10]
     */
    fun getStylizedInt(input: String): Int {
        // [20;25]
        if (input.contains(";")) {
            val v = input.replace("[", "").replace("]", "")
            val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                val num1 = out[0].toInt()
                val num2 = out[1].toInt()

                return ThreadLocalRandom.current().nextInt(num1, num2 + 1)
            } catch (ex: NumberFormatException) {
                return 1
            } catch (ex: ArrayIndexOutOfBoundsException) {
                return 1
            }
        } else {
            return try {
                input.toInt()
            } catch (ex: NumberFormatException) {
                1
            }
        }
    }

    /**
     * 由字符串得到区间.
     * 输入例子: [4; 10]
     */
    fun parseRangedInput(input: String): IntRange {
        // [20;25]
        if (input.contains(";")) {
            val v = input.replace("[", "").replace("]", "")
            val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                val num1 = out[0].toInt()
                val num2 = out[1].toInt()

                if (num1 > num2) {
                    throw NumberFormatException("格式错误: 前面的数必须大于后面!")
                }

                return IntRange(num1, num2)
            } catch (ex: NumberFormatException) {
                throw NumberFormatException("格式错误: 输入的不是范围.")
            } catch (ex: ArrayIndexOutOfBoundsException) {
                throw NumberFormatException("格式错误: 输入的不是范围.")
            }
        } else {
            throw NumberFormatException("格式错误: 输入的不是范围.")
        }
    }

    /**
     * 从范围中随机选取数字.
     * 例子: [4; 10]
     */
    fun retrieveRangedInput(input: String): Int {
        // [20;25]
        if (input.contains(";")) {
            val v = input.replace("[", "").replace("]", "")
            val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                val num1 = out[0].toInt()
                val num2 = out[1].toInt()

                if (num1 > num2) {
                    throw NumberFormatException("格式错误: 前面的数必须大于后面!")
                }

                return ThreadLocalRandom.current().nextInt(num1, num2 + 1)
            } catch (ex: NumberFormatException) {
                throw NumberFormatException("格式错误: 输入的不是范围.")
            } catch (ex: ArrayIndexOutOfBoundsException) {
                throw NumberFormatException("格式错误: 输入的不是范围.")
            }
        } else {
            throw NumberFormatException("格式错误: 输入的不是范围.")
        }
    }

    /**
     * 获取SpawnY.
     *
     * @param value    SpawnY.
     * @param location “最顶端”方块.
     */
    fun getStylizedSpawnY(value: String, location: Location?): Int {
        // Ensure that the spawnY is configured correctly for the void.

        if (location == null) {
            if (value.startsWith("+")) throw StructureConfigurationException("生成在虚空中的建筑必须再用绝对的Y值")
            if (value.startsWith("-")) throw StructureConfigurationException("生成在虚空中的建筑必须再用绝对的Y值")
        }

        // Get the highest block at the specified location.
        var currentHeight = -1
        if (location != null) {
            currentHeight = location.blockY
        }

        // If it is a range
        if (value.contains(";")) {
            //If +[num;num]
            if (value.startsWith("+")) {
                val v = value.replace("[", "").replace("]", "").replace("+", "")
                val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                try {
                    val num1 = out[0].toInt()
                    val num2 = out[1].toInt()

                    if (num1 > num2) throw StructureConfigurationException("SpawnY 前面的值必须大于后面 '[value1;value2]'.")

                    val randomValue = ThreadLocalRandom.current().nextInt(num1, num2 + 1)
                    return currentHeight + randomValue
                } catch (ex: NumberFormatException) {
                    return currentHeight
                } catch (ex: ArrayIndexOutOfBoundsException) {
                    return currentHeight
                }
            } else if (value.startsWith("-")) {
                val v = value.replace("[", "").replace("]", "").replace("-", "")
                val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                try {
                    val num1 = out[0].toInt()
                    val num2 = out[1].toInt()

                    if (num1 > num2) throw StructureConfigurationException("SpawnY 前面的值必须大于后面 '[value1;value2]'.")

                    val randomValue = ThreadLocalRandom.current().nextInt(num1, num2 + 1)
                    return currentHeight - randomValue
                } catch (ex: NumberFormatException) {
                    return currentHeight
                } catch (ex: ArrayIndexOutOfBoundsException) {
                    return currentHeight
                }
            } else {
                val v = value.replace("[", "").replace("]", "")
                val out = v.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                try {
                    val num1 = out[0].toInt()
                    val num2 = out[1].toInt()

                    if (num1 > num2) throw StructureConfigurationException("SpawnY 前面的值必须大于后面 '[value1;value2]'.")

                    return ThreadLocalRandom.current().nextInt(num1, num2 + 1)
                } catch (ex: NumberFormatException) {
                    return currentHeight
                } catch (ex: ArrayIndexOutOfBoundsException) {
                    return currentHeight
                }
            }
        } else if (value.startsWith("+[")) {
            val v = value.replace("+", "").replace("[", "").replace("]", "")
            try {
                val num = v.toInt()
                return currentHeight + num
            } catch (ex: NumberFormatException) {
                return currentHeight
            }
        } else if (value.startsWith("-[")) {
            val v = value.replace("-", "").replace("[", "").replace("]", "")

            try {
                val num = v.toInt()
                return currentHeight - num
            } catch (ex: NumberFormatException) {
                return currentHeight
            }
        } else {
            return try {
                value.toInt()
            } catch (ex: NumberFormatException) {
                currentHeight
            }
        }
    }
}
