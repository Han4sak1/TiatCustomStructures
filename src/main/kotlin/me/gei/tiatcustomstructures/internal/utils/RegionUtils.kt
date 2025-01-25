package me.gei.tiatcustomstructures.internal.utils

import com.sk89q.worldedit.BlockVector2D
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import org.bukkit.Location

/**
 * WorldEdit选区操作
 */
object RegionUtils {
    fun getCuboidRegionPair(clipboard: Clipboard, location: Location): Pair<Vector, Vector> {
        val originalOrigin = clipboard.origin
        val newOrigin = BukkitAdapter.adapt(location).toVector()
        val weRegionCenter = clipboard.region.center

        val worldeditRegion = clipboard.region
        val xWidth = worldeditRegion.width.toDouble()
        val zLength = worldeditRegion.length.toDouble()
        val yHeight = worldeditRegion.height.toDouble()

        val pos1X = weRegionCenter.blockX - xWidth / 2
        val pos1Y = weRegionCenter.blockY - yHeight / 2
        val pos1Z = weRegionCenter.blockZ - zLength / 2

        val pos2X = weRegionCenter.blockX + xWidth / 2
        val pos2Y = weRegionCenter.blockY + yHeight / 2
        val pos2Z = weRegionCenter.blockZ + zLength / 2

        val pos1Original = Vector(pos1X, pos1Y, pos1Z)
        val pos2Original = Vector(pos2X, pos2Y, pos2Z)

        val pos1OriginalOffset = originalOrigin.subtract(pos1Original)
        val pos2OriginalOffset = originalOrigin.subtract(pos2Original)

        val pos1 = newOrigin.subtract(pos1OriginalOffset)
        val pos2 = newOrigin.subtract(pos2OriginalOffset)

        return Pair(pos1, pos2)
    }

    fun getPolygonalRegionPair(
        clipboard: Clipboard,
        location: Location,
        polygonizeMaxPoints: Int
    ): Pair<List<BlockVector2D>, Pair<Int, Int>> {
        val originalOrigin = clipboard.origin
        val newOrigin = BukkitAdapter.adapt(location).toVector()

        val worldeditRegion = clipboard.region

        val originalPoints = worldeditRegion.polygonize(polygonizeMaxPoints)
        val newPoints: MutableList<BlockVector2D> = ArrayList()
        originalPoints.forEach { point ->
            val pointOriginalOffset = originalOrigin.subtract(point.toVector())
            val newPoint = newOrigin.subtract(pointOriginalOffset)
            newPoints.add(BlockVector2D(newPoint.blockX, newPoint.blockZ))
        }
        val maxYPointOriginal = worldeditRegion.maximumPoint
        val minYPointOriginal = worldeditRegion.minimumPoint
        val maxOffset = originalOrigin.subtract(maxYPointOriginal)
        val minOffset = originalOrigin.subtract(minYPointOriginal)
        val maxY = newOrigin.subtract(maxOffset).blockY
        val minY = newOrigin.subtract(minOffset).blockY

        return Pair(newPoints, Pair(minY, maxY))
    }
}
