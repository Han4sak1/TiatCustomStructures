package me.gei.tiatcustomstructures.internal.managers

import com.boydti.fawe.FaweAPI
import com.boydti.fawe.`object`.FaweQueue
import com.sk89q.worldedit.BlockVector2D
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Polygonal2DRegion
import com.sk89q.worldedit.regions.Region
import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.api.event.StructureLightingUpdateEvent
import me.gei.tiatcustomstructures.api.event.StructureRegionCreateEvent
import me.gei.tiatcustomstructures.api.ignoreblocks.IgnoreBlocks
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structure.properties.StructureLimitations
import me.gei.tiatcustomstructures.internal.bo.structure.properties.StructureLocation
import me.gei.tiatcustomstructures.internal.bo.structure.properties.WorldGuardIntegration
import me.gei.tiatcustomstructures.internal.utils.PriorityStructureQueue
import me.gei.tiatcustomstructures.internal.utils.RegionUtils
import me.gei.tiatcustomstructures.internal.utils.WorldGuardUtils
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.world.ChunkLoadEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.submit
import java.io.IOException
import java.util.*
import javax.annotation.Nullable

/**
 * 建筑选择管理器
 * 负责在区块加载时挑选建筑生成/修复光照/创建WorldGuard Region
 * 一区块一实例模式
 */
class StructurePicker(@Nullable private val bl: Block?, private val ch: Chunk) {


    companion object {
        @SubscribeEvent
        private fun onChunkLoad(e: ChunkLoadEvent) {

            if (!e.isNewChunk) return


            val b: Block = e.chunk.getBlock(8, 5, 8) //Grabs the block 8, 5, 8 in that chunk.

            try {
                if (StructureManager.canStructureSpawnInWorldGlobally(e.world)) {
                    StructurePicker(b, e.chunk)
                }
            } catch (ex: RuntimeException) {
                // ignore, error already logged.
            }
        }

        @SubscribeEvent
        private fun onStructureLightingUpdate(e: StructureLightingUpdateEvent) {
            if (Bukkit.getServer().pluginManager.getPlugin("FastAsyncWorldEdit") != null) {
                FaweAPI.fixLighting(
                    BukkitAdapter.adapt(e.world),
                    e.region,
                    null,
                    FaweQueue.RelightMode.ALL
                )
            }
        }

        @SubscribeEvent
        private fun onStructurePreSpawn(e: StructureRegionCreateEvent) {
            val region: Region?
            val clipboard = e.clipboard
            val structure = e.structure
            val spawnLocation = e.location
            if (Bukkit.getServer().pluginManager.getPlugin("WorldGuard") != null) {
                val worldGuardIntegration: WorldGuardIntegration = structure.worldGuardIntegration
                val isPoly = worldGuardIntegration.maxPoints != 0

                if (worldGuardIntegration.isEnabled && Bukkit.getServer().pluginManager.isPluginEnabled("WorldGuard")) {
                    val uuid = "CustomStructuresAutoGen_" + worldGuardIntegration.regionName + "_" + UUID.randomUUID()
                    if (!isPoly) {
                        val vectorPair: Pair<Vector, Vector> = RegionUtils.getCuboidRegionPair(clipboard, spawnLocation)
                        WorldGuardUtils.createCuboidRegion(spawnLocation.world, uuid, vectorPair)
                        region = CuboidRegion(vectorPair.first, vectorPair.second)
                    } else {
                        val polyPair: Pair<List<BlockVector2D>, Pair<Int, Int>> = RegionUtils.getPolygonalRegionPair(
                            clipboard,
                            spawnLocation,
                            worldGuardIntegration.maxPoints
                        )
                        WorldGuardUtils.createPolyRegion(spawnLocation.world, uuid, polyPair)
                        region = Polygonal2DRegion(
                            BukkitAdapter.adapt(spawnLocation.world),
                            polyPair.first,
                            polyPair.second.first,
                            polyPair.second.second
                        )
                    }
                } else {
                    val vectorPair: Pair<Vector, Vector> = RegionUtils.getCuboidRegionPair(clipboard, spawnLocation)
                    region = CuboidRegion(vectorPair.first, vectorPair.second)
                }
            } else {
                val vectorPair: Pair<Vector, Vector> = RegionUtils.getCuboidRegionPair(clipboard, spawnLocation)
                region = CuboidRegion(vectorPair.first, vectorPair.second)
            }

            val lightingUpdateEvent = StructureLightingUpdateEvent(spawnLocation.world, region)
            Bukkit.getServer().pluginManager.callEvent(lightingUpdateEvent)
        }
    }


    private val priorityStructureQueue: PriorityStructureQueue =
        PriorityStructureQueue(StructureManager.getStructures(), bl!!, ch)
    private val ignoreBlocks: IgnoreBlocks = BlockIgnoreManager.ignoreBlocks

    // Variable that contains the structureBlock of the current structure being processed.
    private lateinit var structureBlock: Block

    init {
        submit(delay = 1, period = 10) {
            var gStructure: Structure? = null
            try {
                if (!priorityStructureQueue.isInitialised) return@submit
                if (!priorityStructureQueue.hasNextStructure()) {
                    this.cancel()
                    return@submit
                }

                gStructure = priorityStructureQueue.nextStructure
                val structure: Structure = checkNotNull(gStructure)
                val structureSpawnSettings: StructureLocation.StructureYSpawning = structure.structureLocation.spawnSettings

                // Get the highest block according to the settings for the structure.
                structureBlock = structureSpawnSettings.getHighestBlock(bl!!.location)

                // Allows the structures to no longer spawn on plant life.
                if (structure.structureProperties.shouldIgnoringPlants && ignoreBlocks.blocks.contains(
                        structureBlock.type
                    )
                ) {
                    for (i in structureBlock.y downTo 4) {
                        if (!ignoreBlocks.blocks.contains(ch.getBlock(8, i, 8).type) && ch.getBlock(
                                8,
                                i,
                                8
                            ).type != Material.AIR
                        ) {
                            structureBlock = ch.getBlock(8, i, 8)
                            break
                        }
                    }
                }

                // calculate SpawnY if first is true
                if (structureSpawnSettings.isCalculateSpawnYFirst) {
                    structureBlock = ch.getBlock(8, structureSpawnSettings.getHeight(structureBlock.location), 8)
                }

                if (!structure.structureLimitations.hasWhitelistBlock(structureBlock)) return@submit

                if (structure.structureLimitations.hasBlacklistBlock(structureBlock)) return@submit

                // If it can spawn in water
                if (!structure.structureProperties.shouldSpawnInWater) {
                    if (structureBlock.type == Material.WATER) return@submit
                }

                // If the structure can spawn in lava
                if (!structure.structureProperties.shouldSpawnInLavaLakes) {
                    if (structureBlock.type == Material.LAVA) return@submit
                }

                // calculate SpawnY if first is false
                if (!structureSpawnSettings.isCalculateSpawnYFirst) {
                    structureBlock = ch.getBlock(8, structureSpawnSettings.getHeight(structureBlock.location), 8)
                }

                // If the structure is going to be cut off by the world height limit, pick a new structure.
                if (structure.structureLimitations.worldHeightRestriction != -1 &&
                    structureBlock.location.y > ch.world.maxHeight - structure.structureLimitations
                        .worldHeightRestriction
                ) return@submit

                // If the structure can follows block level limit.
                // This only triggers if it spawns on the top.
                if (structure.structureLimitations.blockLevelLimit.isEnabled) {
                    val limit: StructureLimitations.BlockLevelLimit = structure.structureLimitations.blockLevelLimit
                    if (limit.mode.lowercase() == "flat") {
                        for (x in limit.x1 + structureBlock.x..limit.x2 + structureBlock.x) {
                            for (z in limit.z1 + structureBlock.z..limit.z2 + structureBlock.z) {
                                val top = ch.world.getBlockAt(x, structureBlock.y + 1, z)
                                val bottom = ch.world.getBlockAt(x, structureBlock.y - 1, z)
                                if ((top.type != Material.AIR || ignoreBlocks.blocks.contains(top.type))) return@submit
                                if (bottom.type == Material.AIR) return@submit
                            }
                        }
                    } else if (limit.mode.lowercase() == "flat_error") {
                        var total = 0
                        var error = 0
                        for (x in limit.x1 + structureBlock.x..limit.x2 + structureBlock.x) {
                            for (z in limit.z1 + structureBlock.z..limit.z2 + structureBlock.z) {
                                val top = ch.world.getBlockAt(x, structureBlock.y + 1, z)
                                val bottom = ch.world.getBlockAt(x, structureBlock.y - 1, z)
                                if ((top.type != Material.AIR || ignoreBlocks.blocks.contains(top.type))) error++
                                if (bottom.type == Material.AIR) error++

                                total += 2
                            }
                        }

                        if ((error.toDouble() / total) > limit.error) return@submit
                    }
                }

                // Now to finally paste the schematic
                submit {
                    // It is assumed at this point that the structure has been spawned.
                    // Add it to the list of spawned structures.
                    DatabaseManager.putSpawnedStructure(
                        structureBlock.location,
                        structure
                    )
                    try {
                        SchematicManager.placeSchematic(
                            structureBlock.location,
                            structure.schematic,
                            structure.structureProperties.shouldPlaceAir,
                            structure
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: WorldEditException) {
                        e.printStackTrace()
                    }
                }

                this.cancel() // return after pasting
            } catch (ex: StructureConfigurationException) {
                this.cancel()
                if (gStructure != null) {
                    severe("生成建筑时发生配置文件错误: " + gStructure.name)
                } else {
                    severe("配置文件发生错误.")
                }
                severe(ex.message)
            } catch (ex: Exception) {
                this.cancel()
                severe("复制建筑schematic时发生错误.")
                severe("生成进程已安全结束!")
                severe("开启debug查看更多错误.")
                if (TiatCustomStructures.isDebug) ex.printStackTrace()
            }
        }
    }
}
