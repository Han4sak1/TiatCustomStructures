package me.gei.tiatcustomstructures.api.event

import com.sk89q.worldedit.extent.clipboard.Clipboard
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location
import taboolib.platform.type.BukkitProxyEvent

/**
 * WorldEdit专用
 */
class StructureRegionCreateEvent(val structure: Structure, val location: Location, val clipboard: Clipboard) : BukkitProxyEvent()