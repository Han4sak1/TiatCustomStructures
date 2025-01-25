package me.gei.tiatcustomstructures.api.compact.mythic

import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt
import io.lumine.xikage.mythicmobs.spawning.spawners.MythicSpawner
import io.lumine.xikage.mythicmobs.spawning.spawners.SpawnerManager
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic.MythicMobsHandler
import org.bukkit.Location
import taboolib.common.platform.function.warning
import java.util.*

object MythicMobsSpawner {
    fun setupSpawner(name: String, loc: Location) {
        val info: MythicMobsHandler.MMSpawnerInfo? = MythicMobsHandler.getSpawnerByName(name)
        if (info == null) {
            warning("无法生成MM刷怪点 '$name', 没有匹配的配置.")
            return
        }

        MythicMobs.inst().use { mythicMobs ->
            val spawnerManager: SpawnerManager = mythicMobs.spawnerManager
            //生成名字
            val uuid = UUID.randomUUID()
            val spawnerName = "CustomStructuresAutoGen_" + info.type + "_" + loc.world.name + "_" + uuid

            val spawner: MythicSpawner = spawnerManager.createSpawner(spawnerName, loc, info.type)

            spawner.spawnRadius = info.radius
            spawner.maxMobs = PlaceholderInt(info.maxMobs.toString())
            spawner.cooldownSeconds = info.cooldown
            if (info.conditions.isNotEmpty()) {
                info.conditions.forEach {
                    spawner.conditionList.add(it)
                }
            }
            spawnerManager.saveSpawners()
        }
    }
}
