package me.gei.tiatcustomstructures.internal.utils

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * 建筑schematic操作.
 */
object SchematicLocationUtils {
    /**
     * 旋转向量.
     *
     * @param point  点
     * @param center 中心点
     * @param angle  角度
     * @return 最终结果.
     */
    private fun rotateAround(point: Vector, center: Vector, angle: Double): Vector {
        var newAngle = angle
        newAngle = Math.toRadians(newAngle * -1)
        val rotatedX = cos(newAngle) * (point.x - center.x) - sin(newAngle) * (point.z - center.z) + center.x
        val rotatedZ = sin(newAngle) * (point.x - center.x) + cos(newAngle) * (point.z - center.z) + center.z

        return Vector(rotatedX, point.y, rotatedZ)
    }

    /**
     * 旋转位置.
     *
     * @param point  点
     * @param center 中心点
     * @param angle  角度
     * @return 最终结果.
     */
    fun rotateAround(point: Location, center: Location, angle: Double): Location {
        var newAngle = angle
        newAngle = Math.toRadians(newAngle * -1)
        val rotatedX =
            cos(newAngle) * (point.blockX - center.blockX) - sin(newAngle) * (point.blockZ - center.blockZ) + center.blockX
        val rotatedZ =
            sin(newAngle) * (point.blockX - center.blockX) + cos(newAngle) * (point.blockZ - center.blockZ) + center.blockZ

        return Location(point.world, floor(rotatedX), point.y, floor(rotatedZ))
    }

    /**
     * 获取剪切板选区最低点的实际位置.
     *
     * @param clipboard     剪切板.
     * @param pasteLocation 目的地.
     * @param rotation      旋转角.
     *
     * @return 实际最低点.
     */
    fun getMinimumLocation(clipboard: Clipboard, pasteLocation: Location, rotation: Double): Location {
        val originalOrigin = clipboard.origin
        val originalMinimumPoint = clipboard.region.minimumPoint

        val originalMinimumOffset = originalOrigin.subtract(originalMinimumPoint)

        val newOrigin = BukkitAdapter.adapt(pasteLocation).toVector()
        val newMinimumPoint = newOrigin.subtract(originalMinimumOffset)

        val newRotatedMinimumPoint = rotateAround(newMinimumPoint, newOrigin, rotation)

        return Location(
            pasteLocation.world,
            newRotatedMinimumPoint.x,
            newRotatedMinimumPoint.y,
            newRotatedMinimumPoint.z
        )
    }


    /**
     * 获取剪切板选区最高点的实际位置.
     *
     * @param clipboard     剪切板.
     * @param pasteLocation 目的地.
     * @param rotation      旋转角.
     *
     * @return 实际最高点.
     */
    fun getMaximumLocation(clipboard: Clipboard, pasteLocation: Location, rotation: Double): Location {
        val originalOrigin = clipboard.origin
        val originalMaximumPoint = clipboard.region.maximumPoint

        val originalMaximumOffset = originalOrigin.subtract(originalMaximumPoint)

        val newOrigin = BukkitAdapter.adapt(pasteLocation).toVector()
        val newMaximumPoint = newOrigin.subtract(originalMaximumOffset)

        val newRotatedMaximumPoint = rotateAround(newMaximumPoint, newOrigin, rotation)

        return Location(
            pasteLocation.world,
            newRotatedMaximumPoint.x,
            newRotatedMaximumPoint.y,
            newRotatedMaximumPoint.z
        )
    }
}
