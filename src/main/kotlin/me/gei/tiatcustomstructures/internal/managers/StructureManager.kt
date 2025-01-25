package me.gei.tiatcustomstructures.internal.managers

import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.api.CustomStructuresAPI
import me.gei.tiatcustomstructures.internal.dto.NearbyStructuresResponse
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import me.gei.tiatcustomstructures.internal.exceptions.StructureDatabaseException
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structure.StructureBuilder
import org.bukkit.Location
import org.bukkit.World
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.severe
import taboolib.module.chat.colored
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 建筑管理器
 * 负责所有建筑的托管以及对建筑属性的操作
 */
object StructureManager {

    /** 配置建筑 **/
    private val structures = ArrayList<Structure>()

    fun load() {
        val structureFile = File(getDataFolder(), "Structures")
        if (!structureFile.exists()) {
            releaseResourceFile("Structures/demo.yml", true)
        }
        val schematicFile = File(getDataFolder(), "Schematics")
        if (!schematicFile.exists()) {
            releaseResourceFile("Schematics/demo.schematic", true)
        }

        val structureFolder = File(getDataFolder(), "Structures")
        structureFolder.walk()
            .filter { it.isFile }
            .filter { it.extension == "yml" }
            .filter { configStructure -> TiatCustomStructures.conf.getStringList("Structures").any { configStructure.nameWithoutExtension == it } }
            .forEach { fromConfig(it) }
        info("&a${structures.size} 个建筑加载完成!".colored())
    }

    private fun fromConfig(file: File) {
        try {
            StructureBuilder
                .fromConfig(file)
                .build()
                .let { structures.add(it) }
        } catch (ex: StructureConfigurationException) {
            severe("加载建筑配置 ${file.name} 时发生意外! 请确保配置正确!")
        }
    }

    /**
     * 获取所有建筑.
     */
    fun getStructures(): List<Structure> {
        return structures
    }

    /**
     * 获取建筑
     */
    fun getStructure(name: String): Structure? {
        return structures.firstOrNull { struct: Structure -> struct.name == name }
    }

    /**
     * 检查和其他建筑的距离是否符合要求.
     *
     * @param struct   建筑实例.
     * @param location 建筑生成的位置.
     */
    internal fun validDistance(struct: Structure, location: Location): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        CustomStructuresAPI.getNearbyStructures(
            location,
            null,
            1
        ).thenAccept { response: NearbyStructuresResponse ->
            if (!response.hasEntries()) result.complete(true)
            val nearbyStructure: NearbyStructuresResponse.NearbyStructureContainer = response.response.first()
            result.complete(struct.structureLocation.distanceFromOthers < nearbyStructure.distance)
        }.exceptionally {
            throw StructureDatabaseException("validSameDistance查询失败")
        }

        return result
    }

    /**
     * 检查和同类建筑的距离是否符合要求.
     *
     * @param struct   建筑实例.
     * @param location 建筑生成的位置.
     */
    internal fun validSameDistance(struct: Structure, location: Location): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()

        CustomStructuresAPI.getNearbyStructures(
            location,
            struct.name,
            1
        ).thenAccept { response: NearbyStructuresResponse ->
            if (!response.hasEntries()) result.complete(true)
            val nearbyStructure: NearbyStructuresResponse.NearbyStructureContainer = response.response.first()
            result.complete(struct.structureLocation.distanceFromSame < nearbyStructure.distance)
        }.exceptionally {
            throw StructureDatabaseException("validSameDistance查询失败")
        }

        return result
    }

    /**
     * 检查建筑全局是否可生成(根据全局黑白名单).
     */
    internal fun canStructureSpawnInWorldGlobally(world: World): Boolean {
        val whitelist: List<String> = TiatCustomStructures.conf.getStringList("GlobalWorldWhitelist")
        val blacklist: List<String> = TiatCustomStructures.conf.getStringList("GlobalWorldBlacklist")

        if (whitelist.isNotEmpty() && !whitelist.contains(world.name)) return false
        if (blacklist.contains(world.name)) return false
        return true
    }
}