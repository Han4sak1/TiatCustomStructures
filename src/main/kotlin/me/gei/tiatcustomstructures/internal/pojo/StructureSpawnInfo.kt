package me.gei.tiatcustomstructures.internal.pojo

import me.gei.tiatcustomstructures.api.event.StructureSpawnEvent
import org.bukkit.Location

/**
 * 存储建筑物的生成信息.
 * 用于 [StructureSpawnEvent].
 */
class StructureSpawnInfo(val minimumPoint: Location, val maximumPoint: Location, val signsLocations: List<Location>)
