package me.gei.tiatcustomstructures.api.event

import com.sk89q.worldedit.regions.Region
import org.bukkit.World
import taboolib.platform.type.BukkitProxyEvent

/**
 * WorldGuard专用
 */
class StructureLightingUpdateEvent(val world: World, val region: Region) : BukkitProxyEvent()

