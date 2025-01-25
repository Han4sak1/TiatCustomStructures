package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic

import me.gei.tiatcustomstructures.api.compact.mythic.MythicMobsSpawner
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import org.bukkit.Location
import taboolib.common.platform.function.warning

class MythicSpawnerSign : StructureSign() {
    override fun onStructureSpawn(location: Location, structure: Structure): Boolean {
        if (!hasArgument(0)) {
            warning("无效的MM刷怪点牌子. (${structure.name})")
            return true
        }

        MythicMobsSpawner.setupSpawner(getStringArgument(0), location)
        return true
    }
}
