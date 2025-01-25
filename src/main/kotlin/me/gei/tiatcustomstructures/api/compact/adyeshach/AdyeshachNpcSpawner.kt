package me.gei.tiatcustomstructures.api.compact.adyeshach

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.entity.EntityTypes
import ink.ptms.adyeshach.core.entity.manager.ManagerType
import ink.ptms.adyeshach.core.entity.type.AdyHuman
import ink.ptms.adyeshach.impl.entity.trait.impl.setTraitCommands
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.npc.AdyeshachNpcHandler
import me.gei.tiatcustomstructures.internal.utils.CSUtils
import org.bukkit.Location
import taboolib.common.platform.function.warning

object AdyeshachNpcSpawner {
    fun spawnNpc(name: String, loc: Location) {
        val info: AdyeshachNpcHandler.AdyeshachNpcInfo? = AdyeshachNpcHandler.getNPCByName(name)
        if (info == null) {
            warning("无法生成AdyNpc '$name', 没有匹配的配置.")
            return
        }

        var type = EntityTypes.PLAYER
        try {
            type = EntityTypes.valueOf(info.entityType)
        } catch (ex: IllegalArgumentException) {
            warning("错误的Ady单位类型" + info.entityType + "'! 现尝试生成玩家类型.")
        }

        // 替换NPC名字中的PAPI变量
        val npcName: String = CSUtils.replacePAPIPlaceholders(info.name)

        val npc = Adyeshach.api().getPublicEntityManager(ManagerType.PERSISTENT).create(type, loc)
        npc.setCustomName(npcName)
        npc.setCustomNameVisible(true)

        // 指令
        if (info.commandsOnClick.isNotEmpty()) {
            npc.setTraitCommands(info.commandsOnClick)
        }

        // 皮肤
        if (type == EntityTypes.PLAYER && info.skin != null) {
            val playerNpc = npc as AdyHuman
            playerNpc.setTexture(info.skin!!)
            playerNpc.setName(npcName)
        }
    }
}
