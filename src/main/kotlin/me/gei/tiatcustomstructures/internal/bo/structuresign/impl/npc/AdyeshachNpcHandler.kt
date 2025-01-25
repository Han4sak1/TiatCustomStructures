package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.npc

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object AdyeshachNpcHandler {
    @Config("adynpcs.yml", autoReload = true)
    private lateinit var conf: ConfigFile

    private val npcInfoMap: MutableMap<String, AdyeshachNpcInfo> = HashMap()

    @Awake(LifeCycle.ENABLE)
    private fun load() {
        conf.getKeys(false).forEach { npcKey ->
            val section = conf.getConfigurationSection(npcKey) ?: return@forEach

            val npcInfo = AdyeshachNpcInfo()
            npcInfo.name = section.getString("name", "")!!
            npcInfo.skin = section.getString("skin")
            npcInfo.entityType = section.getString("entityType", "VILLAGER")!!
            val commandsOnClick = section.getStringList("commandsOnClick")
            if (commandsOnClick.isNotEmpty()) {
                npcInfo.commandsOnClick = commandsOnClick
            }
            npcInfoMap[npcKey] = npcInfo
        }
    }

    fun reload() {
        npcInfoMap.clear()
        load()
    }

    fun getNPCByName(name: String): AdyeshachNpcInfo? {
        return npcInfoMap[name]
    }

    class AdyeshachNpcInfo {
        /**
         * 名字.
         */
        var name: String = ""

        /**
         * 皮肤.
         */
        var skin: String? = ""


        /**
         * 类型.
         */
        var entityType: String = "VILLAGER"


        /**
         * 点击NPC后执行的指令.
         */
        var commandsOnClick: List<String> = ArrayList()
    }
}