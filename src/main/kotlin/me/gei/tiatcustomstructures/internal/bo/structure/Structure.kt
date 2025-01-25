package me.gei.tiatcustomstructures.internal.bo.structure

import com.sk89q.worldedit.WorldEditException
import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.internal.managers.SchematicManager
import me.gei.tiatcustomstructures.internal.managers.StructureManager
import me.gei.tiatcustomstructures.internal.bo.structure.properties.*
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.*
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

/**
 * 建筑实例.
 * 使用 [StructureBuilder] 创建新实例.
 * 使用 [StructureSpawnEvent]. 监听生成事件
 */
class Structure (builder: StructureBuilder) {
    /**
     * 获取名字.
     */
    val name: String = builder.name
    /**
     * schematic文件名字(包括后缀名).
     */
    val schematic: String = builder.schematic
    /**
     * 概率分母.
     */
    val probabilityNumerator: Int = builder.probabilityNumerator
    /**
     * 概率分子.
     */
    val probabilityDenominator: Int = builder.probabilityDenominator
    /**
     * 生成优先级.
     */
    val priority: Int = builder.priority
    /**
     * 旋转弧度.
     */
    val baseRotation: Double
    /**
     * 获取位置信息.
     */
    val structureLocation: StructureLocation
    /**
     * 获取其他信息.
     */
    val structureProperties: StructureProperties
    /**
     * 获取限制信息.
     */
    val structureLimitations: StructureLimitations
    /**
     * 获取子建筑信息.
     */
    val subSchematics: SubSchematics
    /**
     * 获取高级子建筑信息.
     */
    val advancedSubSchematics: AdvancedSubSchematics
    /**
     * 获取底部填充信息.
     */
    val bottomSpaceFill: BottomSpaceFill
    /**
     * 获取WorldGuard信息
     */
    val worldGuardIntegration: WorldGuardIntegration
    /**
     * 子建筑旋转(弧度).
     * 仅供内部使用.
     */
    var subSchemRotation: Double = 0.0

    init {
        this.structureLocation = builder.structureLocation
        this.structureProperties = builder.structureProperties
        this.structureLimitations = builder.structureLimitations
        this.subSchematics = builder.subSchematics
        this.advancedSubSchematics = builder.advancedSubSchematics
        this.bottomSpaceFill = builder.bottomSpaceFill
        this.baseRotation = builder.baseRotation.toDouble()
        this.worldGuardIntegration = builder.worldGuardIntegration
    }

    /**
     * 检查是否可以生成.
     *
     * @param block 生成位置的方块 (Null为虚空.)
     * @param chunk 生成区块
     */
    fun canSpawn(block: Block?, chunk: Chunk): CompletableFuture<Boolean> {
        val spawnResult = CompletableFuture<Boolean>()

        var result = true

        // Check to see if the structure can spawn in the current world.
        if (!structureLocation.canSpawnInWorld(chunk.world)) result = false

        // If the block is null, that means it is in the void, check if it can spawn in the void.
        if (block == null && !structureProperties.shouldSpawnInVoid) result = false
        else if (block == null) {
            if (ThreadLocalRandom.current().nextInt(0, probabilityDenominator + 1) > probabilityNumerator) result =
                false

            if (!structureLocation.checkBiome(chunk.getBlock(0, 20, 0))) result = false
        }

        checkNotNull(block)
        // Check to see if the structure is far enough away from spawn.
        if (abs(block.x.toDouble()) < structureLocation.xLimitation) result = false
        if (abs(block.z.toDouble()) < structureLocation.zLimitation) result = false

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, probabilityDenominator + 1) > probabilityNumerator) result = false

        if (!structureLocation.checkBiome(block)) result = false

        //最后检查距离
        val finalResult = result
        StructureManager.validDistance(this, block.location)
            .thenAccept acceptOthers@ { response ->
                if (!response) {
                    spawnResult.complete(false)
                    return@acceptOthers
                }
                StructureManager.validSameDistance(this, block.location)
                    .thenAccept acceptSame@ { response2 ->
                        if (!response2) {
                            spawnResult.complete(false)
                            return@acceptSame
                        }
                        //如果距离检查通过则检查其它结果
                        spawnResult.complete(finalResult)
                    }
            }

        return spawnResult
    }

    /**
     * 生成建筑.
     * 使用此方法生成的建筑不会被保存到数据库
     * 手动调用 [StructureHandler.putSpawnedStructure] 来添加其到数据库
     *
     * @param location 生成位置.
     * @return 是否成功生成.
     */
    fun spawn(location: Location): Boolean {
        try {
            SchematicManager.placeSchematic(
                location,
                schematic,
                structureProperties.shouldPlaceAir,
                this
            )
            return true
        } catch (ex: IOException) {
            if (TiatCustomStructures.isDebug) {
                ex.printStackTrace()
            }
            return false
        } catch (ex: WorldEditException) {
            if (TiatCustomStructures.isDebug) {
                ex.printStackTrace()
            }
            return false
        }
    }
}
