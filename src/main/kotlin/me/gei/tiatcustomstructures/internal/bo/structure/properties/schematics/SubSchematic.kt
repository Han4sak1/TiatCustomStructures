package me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics

import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import me.gei.tiatcustomstructures.internal.utils.NumberStylizer
import org.bukkit.Location
import taboolib.library.configuration.ConfigurationSection

/**
 * 建筑子类
 * 用于子建筑和高级子建筑的创建
 */
class SubSchematic(section: ConfigurationSection, advanced: Boolean) {
    /**
     * 获取权重.
     */
    val weight: Double

    /**
     * 获取schematic文件.
     */
    val file: String

    /**
     * 是否放置空气方块.
     */
    var isPlacingAir: Boolean = false
        private set

    /**
     * 是否使用牌子的旋转角.
     */
    var isUsingRotation: Boolean = false
        private set

    /**
     * 获取垂直定位.
     */
    var verticalRepositioning: VerticalRepositioning? = null
        private set

    init {
        if (!section.contains("Weight") && advanced) throw StructureConfigurationException("配置错误: " + section.name + " 未包含权重!")
        weight = if (advanced) section.getDouble("Weight") else 0.0
        if (!section.contains("File")) throw StructureConfigurationException("配置错误: " + section.name + " 未包含文件!")
        file = section.getString("File")!!
        if (section.contains("PlaceAir")) isPlacingAir = section.getBoolean("PlaceAir")
        if (section.contains("UseRotation")) isUsingRotation = section.getBoolean("UseRotation")
        if (section.contains("VerticalRepositioning")) {
            this.verticalRepositioning = VerticalRepositioning(
                section.name,
                section.getConfigurationSection("VerticalRepositioning")!!
            )
        }
    }


    /**
     * 垂直定位.
     */
    class VerticalRepositioning(sectionName: String?, section: ConfigurationSection) {
        private var range: String =
            if (section.contains("Range")) section.getString("Range")!!
            else ""

        /**
         * 原始 SpawnY.
         */
        private var rawSpawnY: String

        /**
         * 获取 no-point solution.
         */
        var noPointSolution: String

        /**
         * 获取范围.
         */
        fun getRange(): IntRange? {
            if (range.isEmpty()) {
                return null
            }
            return NumberStylizer.parseRangedInput(range)
        }

        /**
         * 获取格式化后的 SpawnY.
         * @param location 最顶部方块的位置
         */
        fun getSpawnY(location: Location?): Int {
            return NumberStylizer.getStylizedSpawnY(rawSpawnY, location)
        }

        init {
            if (section.contains("SpawnY")) rawSpawnY = section.getString("SpawnY")!!
            else throw RuntimeException("无法找到建筑 $sectionName 的 SpawnY!")


            noPointSolution = if (section.contains("NoPointSolution")) section.getString("NoPointSolution")!!
            else "CURRENT"
        }
    }
}
