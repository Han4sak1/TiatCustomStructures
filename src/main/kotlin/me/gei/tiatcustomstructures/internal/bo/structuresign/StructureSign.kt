package me.gei.tiatcustomstructures.internal.bo.structuresign

import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.utils.IndexChecker
import me.gei.tiatcustomstructures.internal.utils.NumberStylizer
import org.bukkit.Location

/**
 * 建筑牌子.
 * [SignName] <br></br>
 * Argument #0 <br></br>
 * Argument #1 <br></br>
 * Argument #2 <br></br>
 *
 * 继承该类时不要添加构造函数
 *
 */
abstract class StructureSign
/**
 * 内部使用
 * 不要重载此构造函数
 */
{
    private lateinit var arguments: Array<String>

    /**
     * 获取旋转角(弧度)
     */
    var signRotation: Double = 0.0
        private set

    /**
     * 获取旋转角(弧度)
     */
    var structureRotation: Double = 0.0
        private set

    /**
     * 获取最低点
     */
    lateinit var structureMinimumLocation: Location
        private set

    /**
     * 获取最高点
     */
    lateinit var structureMaximumLocation: Location
        private set

    /**
     * 初始化.
     * 内部使用
     *
     * @param arguments                参数.
     * @param signRotation             牌子旋转角.
     * @param structureRotation        建筑旋转角.
     * @param structureMinimumLocation 建筑最低点.
     * @param structureMaximumLocation 建筑最高点.
     */
    fun initialize(
        arguments: Array<String>,
        signRotation: Double,
        structureRotation: Double,
        structureMinimumLocation: Location,
        structureMaximumLocation: Location
    ) {
        this.arguments = arguments
        this.signRotation = signRotation
        this.structureRotation = structureRotation
        this.structureMinimumLocation = structureMinimumLocation
        this.structureMaximumLocation = structureMaximumLocation
    }

    /**
     * 建筑生成时触发.
     * 用户实现
     *
     * @return True表示该牌子需要移除.
     */
    abstract fun onStructureSpawn(location: Location, structure: Structure): Boolean

    /**
     * 检查牌子参数是否存在.
     */
    fun hasArgument(argNumber: Int): Boolean {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return arguments[argNumber].isNotEmpty()
    }

    /**
     * 获取参数.
     */
    fun getStringArgument(argNumber: Int): String {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return arguments[argNumber]
    }

    /**
     * 获取Int参数
     */
    fun getIntArgument(argNumber: Int): Int {
        IndexChecker.checkIndex(argNumber, arguments.size)

        return arguments[argNumber].toInt()
    }

    /**
     * 获取Int参数
     */
    fun getIntArgument(argNumber: Int, defaultValue: Int): Int {
        return try {
            getIntArgument(argNumber)
        } catch (exception: NumberFormatException) {
            defaultValue
        }
    }

    /**
     * 获取Double参数
     */
    fun getDoubleArgument(argNumber: Int): Double {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return arguments[argNumber].toDouble()
    }

    /**
     * 获取Double参数
     */
    fun getDoubleArgument(argNumber: Int, defaultValue: Double): Double {
        return try {
            getDoubleArgument(argNumber)
        } catch (exception: NumberFormatException) {
            defaultValue
        }
    }

    /**
     * 获取范围参数.
     * 参数例子: [5;10]
     */
    fun getRangedIntArgument(argNumber: Int): IntRange {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return NumberStylizer.parseRangedInput(arguments[argNumber])
    }

    /**
     * 获取范围参数.
     * 参数例子: [5;10]
     */
    fun getRangedIntArgument(argNumber: Int, defaultLowerValue: Int, defaultUpperValue: Int): IntRange {
        return try {
            getRangedIntArgument(argNumber)
        } catch (exception: NumberFormatException) {
            IntRange(defaultLowerValue, defaultUpperValue)
        }
    }

    /**
     * 获取范围参数的随机值.
     * 参数例子: [5;10] 返回5~10的随机值
     */
    fun calculateRangedIntArgument(argNumber: Int): Int {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return NumberStylizer.retrieveRangedInput(arguments[argNumber])
    }

    /**
     * 获取范围参数的随机值.
     * 参数例子: [5;10] 返回5~10的随机值
     */
    fun calculateRangedIntArgument(argNumber: Int, defaultValue: Int): Int {
        return try {
            calculateRangedIntArgument(argNumber)
        } catch (exception: NumberFormatException) {
            defaultValue
        }
    }

    /**
     * 获取格式化后的参数.
     * 与 [.calculateRangedIntArgument], 类似, 但也接受单个整数
     * [5;10]
     * [-4;3]
     * 20
     * -5
     */
    fun getStylizedIntArgument(argNumber: Int): Int {
        IndexChecker.checkIndex(argNumber, arguments.size)
        return NumberStylizer.getStylizedInt(arguments[argNumber])
    }
}
