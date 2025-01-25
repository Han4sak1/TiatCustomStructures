package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic

import ink.ptms.um.Mythic
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import org.bukkit.Location
import taboolib.common.platform.function.warning

class MythicMobSign : StructureSign() {
    override fun onStructureSpawn(location: Location, structure: Structure): Boolean {
        if (!hasArgument(0)) {
            warning("无效的MM怪物牌子! (${structure.name})")
            return true
        }

        val mythicMob: String = getStringArgument(0)
        var level = 1.0
        if (hasArgument(1)) {
            level = getDoubleArgument(1)
        }
        Mythic.API.getMobType(mythicMob)?.spawn(location, level) ?: warning("无效的MM怪物! (${structure.name})")

        return true
    }
}
