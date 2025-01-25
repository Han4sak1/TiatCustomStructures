package me.gei.tiatcustomstructures.api.event

import me.gei.tiatcustomstructures.internal.pojo.StructureSpawnInfo
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location
import taboolib.platform.type.BukkitProxyEvent

class StructureSpawnEvent(val structure: Structure, val location: Location, val rotation: Double,val holder: StructureSpawnInfo) : BukkitProxyEvent()
