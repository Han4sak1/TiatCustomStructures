package me.gei.tiatcustomstructures.internal.utils

import me.clip.placeholderapi.PlaceholderAPI
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

/**
 * 常用工具.
 */
object CSUtils {
    /**
     * 替换变量.
     * 用于牌子指令组之中
     *
     * @param command      命令.
     * @param signLocation 牌子位置.
     * @param minLoc       建筑最低点.
     * @param maxLoc       建筑最高点.
     * @param structure    建筑实例.
     */
    fun replacePlaceHolders(
        command: String,
        signLocation: Location,
        minLoc: Location,
        maxLoc: Location,
        structure: Structure
    ): String {
        return command
            .replace("<world>", signLocation.world!!.name)
            .replace("<x>", "" + signLocation.blockX)
            .replace("<y>", "" + signLocation.blockY)
            .replace("<z>", "" + signLocation.blockZ)
            .replace("<structX1>", "" + minLoc.blockX)
            .replace("<structY1>", "" + minLoc.blockY)
            .replace("<structZ1>", "" + minLoc.blockZ)
            .replace("<structX2>", "" + maxLoc.blockX)
            .replace("<structY2>", "" + maxLoc.blockY)
            .replace("<structZ2>", "" + maxLoc.blockZ)
            .replace("<minX>", "" + minLoc.blockX)
            .replace("<minY>", "" + minLoc.blockY)
            .replace("<minZ>", "" + minLoc.blockZ)
            .replace("<maxX>", "" + maxLoc.blockX)
            .replace("<maxY>", "" + maxLoc.blockY)
            .replace("<maxZ>", "" + maxLoc.blockZ)
            .replace("<uuid>", UUID.randomUUID().toString())
            .replace("<structName>", structure.name)
    }

    /**
     * 替换PAPI变量.
     *
     * @param text 文本.
     * @return 替换后文本.
     */
    fun replacePAPIPlaceholders(text: String): String {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(null, text)
        }
        return text
    }

    /**
     * 检查是否在范围内.
     *
     * @param range     范围.
     * @param localPin 区间需要移动的值.
     * @param value    被检查的值.
     */
    fun isInLocalRange(range: IntRange, localPin: Int, value: Int): Boolean {
        if (range.first + localPin > value) return false
        return range.last + localPin > value
    }
}
