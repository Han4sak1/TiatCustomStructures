package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object MythicMobsHandler {
    @Config("mobspawners.yml")
    private lateinit var conf: ConfigFile

    private val mmSpawnerInfoMap: MutableMap<String, MMSpawnerInfo> = HashMap()

    @Awake(LifeCycle.ENABLE)
    private fun load() {
        conf.getKeys(false).forEach { npcKey ->
            val section = conf.getConfigurationSection(npcKey) ?: return@forEach

            val mmSpawnerInfo = MMSpawnerInfo()
            mmSpawnerInfo.type = conf.getString("type", "test")!!
            mmSpawnerInfo.radius = conf.getInt("radius", 5)
            mmSpawnerInfo.maxMobs = conf.getInt("maxmobs", 5)
            mmSpawnerInfo.cooldown = conf.getInt("cooldown", 5)
            val conditions = section.getStringList("conditions")
            if (conditions.isNotEmpty()) {
                mmSpawnerInfo.conditions = conditions
            }
            mmSpawnerInfoMap[npcKey] = mmSpawnerInfo
        }
    }

    fun reload() {
        mmSpawnerInfoMap.clear()
        load()
    }

    fun getSpawnerByName(name: String): MMSpawnerInfo? {
        return mmSpawnerInfoMap[name]
    }

    class MMSpawnerInfo {
        var type: String = "test"

        var radius: Int = 5

        var maxMobs: Int = 5

        var cooldown: Int = 5

        var conditions: List<String> = ArrayList()
    }
}
