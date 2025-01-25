package me.gei.tiatcustomstructures.internal.utils

import com.sk89q.worldedit.BlockVector
import com.sk89q.worldedit.BlockVector2D
import com.sk89q.worldedit.Vector
import com.sk89q.worldguard.bukkit.WGBukkit
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.gei.tiatcustomstructures.TiatCustomStructures
import org.bukkit.World
import taboolib.common.platform.function.info

/**
 * WorldGuard操作
 */
object WorldGuardUtils {
    fun createCuboidRegion(world: World?, uuid: String, vectorPair: Pair<Vector, Vector>) {
        val region: ProtectedRegion = ProtectedCuboidRegion(
            uuid,
            BlockVector(vectorPair.first.x, vectorPair.first.y, vectorPair.first.z),
            BlockVector(vectorPair.second.x, vectorPair.second.y, vectorPair.second.z)
        )

        WGBukkit.getRegionManager(world).addRegion(region)
        if (TiatCustomStructures.isDebug) {
            info("尝试创建WorldGuard Region $uuid")
        }
    }

    fun createPolyRegion(world: World?, uuid: String, polyPair: Pair<List<BlockVector2D?>, Pair<Int, Int>>) {
        val region: ProtectedRegion = ProtectedPolygonalRegion(
            uuid,
            polyPair.first,
            polyPair.second.first,
            polyPair.second.second
        )

        WGBukkit.getRegionManager(world).addRegion(region)
        if (TiatCustomStructures.isDebug) {
            info("尝试创建WorldGuard Region $uuid")
        }
    }
}
