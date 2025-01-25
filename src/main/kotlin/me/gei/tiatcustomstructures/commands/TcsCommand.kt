package me.gei.tiatcustomstructures.commands

import me.gei.tiatcustomstructures.TiatCustomStructures.conf
import me.gei.tiatcustomstructures.TiatCustomStructures.isDebug
import me.gei.tiatcustomstructures.TiatCustomStructures.prefix
import me.gei.tiatcustomstructures.api.CustomStructuresAPI
import me.gei.tiatcustomstructures.api.ignoreblocks.IgnoreBlocks
import me.gei.tiatcustomstructures.internal.dto.NearbyStructuresResponse
import me.gei.tiatcustomstructures.internal.managers.BlockIgnoreManager
import me.gei.tiatcustomstructures.internal.managers.DatabaseManager
import me.gei.tiatcustomstructures.internal.managers.StructureManager
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structure.properties.StructureLimitations
import me.gei.tiatcustomstructures.internal.bo.structure.properties.StructureLocation
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.command.SignCommandsHandler
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic.MythicMobsHandler
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.npc.AdyeshachNpcHandler
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

@CommandHeader("tcs", aliases = ["tiatcustomstructure", "structure", "customstructure", "cs"], permissionDefault = PermissionDefault.OP)
object TcsCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            prefix = conf.getString("Prefix")!!.colored()
            isDebug = conf.getBoolean("debug", false)

            StructureManager.load()
            SignCommandsHandler.reload()
            AdyeshachNpcHandler.reload()
            MythicMobsHandler.reload()

            sender.sendMessage(prefix + "&a重载成功!".colored())
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("$prefix&e建筑列表:")
            StructureManager.getStructures().forEach {
                sender.sendMessage(it.name)
            }
        }
    }

    @CommandBody
    val forceSpawn = subCommand {
        dynamic("structure") {
            execute<CommandSender> { sender, context, _ ->
                if (sender !is Player) {
                    sender.sendMessage(prefix + "&c只可由玩家执行".colored())
                    return@execute
                }
                val structure: Structure? = StructureManager.getStructure(context["structure"])
                if (structure == null) {
                    sender.sendMessage(prefix + "&c参数错误".colored())
                    return@execute
                }

                structure.spawn(sender.location)
            }
        }
    }

    @CommandBody
    val testSpawn = subCommand {
        dynamic("structure") {
            execute<CommandSender> { sender, context, _ ->
                if (sender !is Player) {
                    sender.sendMessage(prefix + "&c只可由玩家执行".colored())
                }
                val structure: Structure? = StructureManager.getStructure(context["structure"])
                if (structure == null) {
                    sender.sendMessage(prefix + "&c参数错误".colored())
                    return@execute
                }

                sender.sendMessage("&b=================[&6" + structure.name + "&b]=================".colored())
                pseudoCalculate(sender, structure, (sender as Player).location.block, sender.location.chunk)
                sender.sendMessage("&b=================[&6" + structure.name + "&b]=================".colored())
            }
        }
    }

    @CommandBody
    val nearBy = subCommand {
        dynamic("structure") {
            int("limit") {
                execute<CommandSender> { sender, context, _ ->
                    if (sender !is Player) {
                        sender.sendMessage(prefix + "&c只可由玩家执行".colored())
                        return@execute
                    }
                    val structure = StructureManager.getStructure(context["structure"])
                    val limit = context.int("limit")
                    if (structure == null) {
                        sender.sendMessage(prefix + "&c参数错误".colored())
                        return@execute
                    }

                    CustomStructuresAPI.getNearbyStructures(
                        sender.location,
                        structure.name,
                        limit
                    ).thenAccept { response: NearbyStructuresResponse ->
                        if (!response.hasEntries()) {
                            sender.sendMessage(prefix + "&c暂时找不到任何建筑!".colored())
                            return@thenAccept
                        }
                        sender.sendMessage(
                            "&a附近建筑 (&c${context["structure"]}&a, 限制个数 &c${context.int("limit")}&a):".colored()
                        )
                        response.response.forEach { nearbyStructure ->
                            sender.sendMessage(
                                "&a查询到 &6${nearbyStructure.getStructure()!!.name} &a位于 &6${nearbyStructure.location.blockX}&a, &6${nearbyStructure.location.blockY}&a, &6${nearbyStructure.location.blockZ}&a (&6${nearbyStructure.location} &a格)!".colored()
                            )
                        }
                    }.exceptionally { _: Throwable? ->
                        sender.sendMessage("请稍后再试.".colored())
                        null
                    }
                }
            }

            execute<CommandSender>{ sender, context, _ ->
                if (sender !is Player) {
                    sender.sendMessage(prefix + "&c只可由玩家执行".colored())
                    return@execute
                }
                val structure = StructureManager.getStructure(context["structure"])
                if (structure == null) {
                    sender.sendMessage(prefix + "&c参数错误".colored())
                    return@execute
                }

                CustomStructuresAPI.getNearbyStructures(
                    sender.location,
                    structure.name,
                    1
                ).thenAccept { response: NearbyStructuresResponse ->
                    if (!response.hasEntries()) {
                        sender.sendMessage(prefix + "&c暂时找不到任何建筑!".colored())
                        return@thenAccept
                    }
                    sender.sendMessage(
                        "&a附近建筑 (&c${context["structure"]}&a, 限制个数 &c1&a):".colored()
                    )
                    response.response.forEach { nearbyStructure ->
                        sender.sendMessage(
                            "&a查询到 &6${nearbyStructure.getStructure()!!.name} &a位于 &6${nearbyStructure.location.blockX}&a, &6${nearbyStructure.location.blockY}&a, &6${nearbyStructure.location.blockZ}&a (&6${nearbyStructure.distance} &a格)!".colored()
                        )
                    }
                }.exceptionally { _: Throwable? ->
                    sender.sendMessage("&c请稍后再试.".colored())
                    null
                }
            }
        }

        execute<CommandSender>{ sender, _, _ ->
            if (sender !is Player) {
                sender.sendMessage(prefix + "&c只可由玩家执行".colored())
                return@execute
            }

            CustomStructuresAPI.getNearbyStructures(
                sender.location,
                null,
                1
            ).thenAccept { response: NearbyStructuresResponse ->
                if (!response.hasEntries()) {
                    sender.sendMessage(prefix + "&c暂时找不到任何建筑!".colored())
                    return@thenAccept
                }
                sender.sendMessage(
                    "&a附近建筑 (&c所有&a, 限制个数 &c1&a):".colored()
                )
                response.response.forEach { nearbyStructure ->
                    sender.sendMessage(
                        "&a查询到 &6${nearbyStructure.getStructure()!!.name} &a位于 &6${nearbyStructure.location.blockX}&a, &6${nearbyStructure.location.blockY}&a, &6${nearbyStructure.location.blockZ}&a (&6${nearbyStructure.distance} &a格)!".colored()
                    )
                }
            }.exceptionally { _: Throwable? ->
                sender.sendMessage("&c请稍后再试.".colored())
                null
            }
        }
    }

    @CommandBody
    val wipe = subCommand {
        dynamic("structure") {
            execute<CommandSender> { sender, context, _ ->
                val structure = StructureManager.getStructure(context["structure"])
                if (structure == null) {
                    sender.sendMessage(prefix + "&c参数错误".colored())
                    return@execute
                }

                DatabaseManager.remove(structure)
            }
        }
    }


    private fun pseudoCalculate(p: CommandSender, structure: Structure, bl: Block, ch: Chunk) {
        var newBl = bl
        val ignoreBlocks: IgnoreBlocks = BlockIgnoreManager.ignoreBlocks

        val structureSpawnSettings: StructureLocation.StructureYSpawning = structure.structureLocation.spawnSettings

        newBl = structureSpawnSettings.getHighestBlock(newBl.location)

        // Calculate the chance.
        canSpawn(p, structure, newBl, ch)

        // Allows the structures to no longer spawn on plant life.
        if (structure.structureProperties.shouldIgnoringPlants && ignoreBlocks.blocks.contains(newBl.type)) {
            for (i in newBl.y downTo 4) {
                if (!ignoreBlocks.blocks.contains(ch.getBlock(8, i, 8).type) && ch.getBlock(
                        8,
                        i,
                        8
                    ).type != Material.AIR
                ) {
                    newBl = ch.getBlock(8, i, 8)
                    break
                }
            }
        }

        // calculate SpawnY if first is true
        if (structureSpawnSettings.isCalculateSpawnYFirst) {
            newBl = ch.getBlock(8, structureSpawnSettings.getHeight(newBl.location), 8)
            p.sendMessage("&a生成 Y: ${newBl.y}".colored())
        }

        if (!structure.structureLimitations.hasWhitelistBlock(newBl)) {
            p.sendMessage("&c白名单检查失败n! 无法生成在 ${newBl.type}!".colored())
            return
        }

        if (structure.structureLimitations.hasBlacklistBlock(newBl)) {
            p.sendMessage("&c黑名单检查失败! 无法生成在 ${newBl.type}!".colored())
            return
        }

        // If it can spawn in water
        if (!structure.structureProperties.shouldSpawnInWater) {
            if (newBl.type == Material.WATER) {
                p.sendMessage("&c检查失败! 不能生成在水中!".colored())
                return
            }
        }

        // If the structure can spawn in lava
        if (!structure.structureProperties.shouldSpawnInLavaLakes) {
            if (newBl.type == Material.LAVA) {
                p.sendMessage("&c检查失败! 不能生成在岩浆中!".colored())
                return
            }
        }

        // calculate SpawnY if first is false
        if (!structureSpawnSettings.isCalculateSpawnYFirst) {
            newBl = ch.getBlock(8, structureSpawnSettings.getHeight(newBl.location), 8)
            p.sendMessage("&a生成 Y: ${newBl.y}".colored())
        }

        // If the structure is going to be cut off by the world height limit, pick a new structure.
        if (structure.structureLimitations.worldHeightRestriction != -1 &&
            newBl.location.y > ch.world.maxHeight - structure.structureLimitations.worldHeightRestriction) {
            p.sendMessage("&c高度检查失败!".colored())
            return
        }

        // If the structure can follows block level limit.
        // This only triggers ifmode it spawns on the top.
        if (structure.structureLimitations.blockLevelLimit.isEnabled) {
            val limit: StructureLimitations.BlockLevelLimit = structure.structureLimitations.blockLevelLimit
            if (limit.mode.lowercase() == "flat") {
                for (x in limit.x1 + newBl.x..limit.x2 + newBl.x) {
                    for (z in limit.z1 + newBl.z..limit.z2 + newBl.z) {
                        val top = ch.world.getBlockAt(x, newBl.y + 1, z)
                        val bottom = ch.world.getBlockAt(x, newBl.y - 1, z)
                        if ((top.type != Material.AIR || ignoreBlocks.blocks.contains(top.type))) {
                            // Output debug info if in debug mode.
                            if (isDebug) {
                                p.sendMessage(top.location.toString() + " || TOP FAIL")
                                p.sendMessage(top.type.toString() + " || TOP FAIL")
                            }
                            p.sendMessage("&c生成失败! 地面不平整!".colored())
                            return
                        }
                        if (bottom.type == Material.AIR) {
                            if (isDebug) {
                                p.sendMessage(bottom.location.toString() + " || BOTTOM FAIL")
                            }
                            p.sendMessage("&c生成失败! 地面不平整!".colored())
                            return
                        }
                    }
                }
            } else if (limit.mode.lowercase() == "flat_error") {
                var total = 0
                var error = 0
                for (x in limit.x1 + newBl.x..limit.x2 + newBl.x) {
                    for (z in limit.z1 + newBl.z..limit.z2 + newBl.z) {
                        val top = ch.world.getBlockAt(x, newBl.y + 1, z)
                        val bottom = ch.world.getBlockAt(x, newBl.y - 1, z)
                        if ((top.type != Material.AIR || ignoreBlocks.blocks.contains(top.type))) error++
                        if (bottom.type == Material.AIR) error++

                        total += 2
                    }
                }
                // Debug the percent failure.
                if (isDebug) {
                    p.sendMessage("随机失败: " + (error.toDouble() / total) + " / " + limit.error)
                }
                if ((error.toDouble() / total) > limit.error) {
                    p.sendMessage("&c检查失败! 地面不够平整!".colored())
                }
            }
        }
    }

    private fun canSpawn(p: CommandSender, structure: Structure, block: Block, chunk: Chunk) {
        if (!structure.structureLocation.canSpawnInWorld(chunk.world)) {
            p.sendMessage("&c检查失败! 不能生成在当前世界!".colored())
        }

        // Check to see if the structure is far enough away from spawn.
        if (abs(block.x.toDouble()) < structure.structureLocation.xLimitation)
            p.sendMessage("&cX坐标检查失败! 无法生成在 X=" + structure.structureLocation.xLimitation + " 附近!".colored())
        if (abs(block.z.toDouble()) < structure.structureLocation.zLimitation)
            p.sendMessage("&cZ坐标检查失败! 无法生成在 Z=" + structure.structureLocation.zLimitation + " 附近!".colored())

        StructureManager.validDistance(structure, block.location)
            .thenAccept { response ->
                if (!response) {
                    p.sendMessage("&c检查失败! 不能生成在其他建筑附近!".colored())
                }
            }

        StructureManager.validDistance(structure, block.location)
            .thenAccept { response ->
                if (!response) {
                    p.sendMessage("&c检查失败! 不能生成在同类建筑附近!".colored())
                }
            }

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, structure.probabilityNumerator + 1) > structure.probabilityDenominator)
            p.sendMessage("&e随机检查失败! (${structure.probabilityNumerator}/${structure.probabilityDenominator} 概率)".colored())

        // Check to see if the structure can spawn in the current biome.
        if (!structure.structureLocation.checkBiome(block))
            p.sendMessage("&c检查失败! 无法生成在当前生物群系!".colored())
    }
}