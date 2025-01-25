package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.npc

import me.gei.tiatcustomstructures.api.compact.adyeshach.AdyeshachNpcSpawner
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import org.bukkit.Location
import taboolib.common.platform.function.warning

class AdyeshachNPCSign : StructureSign() {
    override fun onStructureSpawn(location: Location, structure: Structure): Boolean {
        if (!hasArgument(0)) {
            warning("无效的AdyNpc牌子. (${structure.name})")
            return true
        }

        AdyeshachNpcSpawner.spawnNpc(getStringArgument(0), location)
        return true
    }
}
