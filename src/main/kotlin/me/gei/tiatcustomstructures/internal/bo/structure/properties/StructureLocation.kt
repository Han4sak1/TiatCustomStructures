package me.gei.tiatcustomstructures.internal.bo.structure.properties

import me.gei.tiatcustomstructures.api.compact.otg.OTGBiomeBaser
import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import me.gei.tiatcustomstructures.internal.utils.NumberStylizer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import taboolib.module.configuration.Configuration
import kotlin.math.max

/**
 * 建筑生成位置.
 */
@StructureProperties
class StructureLocation(fileConfiguration: Configuration) {
    /**
     * 可生成世界.
     */
    private val worlds: List<String>
    /**
     * 不可生成世界.
     */
    private val worldBlacklist: List<String>
    /**
     * 获取Y限制.
     */
    val spawnSettings: StructureYSpawning
    /**
     * 生物群系限制.
     */
    private val biomes: List<String>
    /**
     * 和其他建筑的距离.
     */
    var distanceFromOthers: Double = 100.0
        private set
    /**
     * 和同类建筑的距离.
     */
    var distanceFromSame: Double = 100.0
        private set
    /**
     * X限制.
     */
    var xLimitation: Int = 0
        private set

    /**
     * Z限制.
     */
    var zLimitation: Int = 0
        private set

    /**
     * 是否可在世界生成.
     */
    fun canSpawnInWorld(world: World): Boolean {
        if (worlds.isNotEmpty()) {
            if (!worlds.contains(world.name)) return false
        }

        if (worldBlacklist.contains(world.name)) {
            return false
        }

        return true
    }

    /**
     * 是否可在生物群系生成.
     * 如果biomes为空则为所有生物群系
     */
    fun checkBiome(b: Block): Boolean {
        val biome: String = if (Bukkit.getPluginManager().isPluginEnabled("OpenTerrainGenerator")) {
            OTGBiomeBaser.getOTGBiomeOrVanilla(b)
        } else b.biome.name
        if (biomes.isEmpty()) return true
        biomes.forEach {
            if (biome.lowercase().replace("minecraft:", "") == it) return true
        }
        return false
    }

    init {
        val cs = fileConfiguration.getConfigurationSection("StructureLocation")
            ?: throw StructureConfigurationException("缺失StructureLocation选项")
        if (cs.contains("Worlds")) this.worlds = cs.getStringList("Worlds")
        else this.worlds = emptyList()
        if (cs.contains("WorldBlacklist")) this.worldBlacklist = cs.getStringList("WorldBlacklist")
        else this.worldBlacklist = emptyList()
        this.spawnSettings = StructureYSpawning(fileConfiguration)
        if (cs.contains("Biome")) this.biomes = cs.getStringList("Biome")
        else this.biomes = emptyList()
        if (cs.contains("DistanceFromOthers")) this.distanceFromOthers = max(0.0, cs.getDouble("DistanceFromOthers"))
        if (cs.contains("DistanceFromSame")) this.distanceFromSame = max(0.0, cs.getDouble("DistanceFromSame"))
        if (cs.contains("SpawnDistance.x")) {
            xLimitation = cs.getInt("SpawnDistance.x")
        }
        if (cs.contains("SpawnDistance.z")) {
            zLimitation = cs.getInt("SpawnDistance.z")
        }
    }

    /**
     * Y限制.
     */
    class StructureYSpawning(fc: Configuration) {
        /**
         * 是否最先检查SpawnY.
         */
        var isCalculateSpawnYFirst: Boolean = true
            private set

        /**
         * 获取SpawnY.
         */
        val value: String

        /**
         * 获取该位置最高的方块.
         */
        fun getHighestBlock(loc: Location): Block {
            return loc.world!!.getHighestBlockAt(loc)
        }

        /**
         * 获取最终高度.
         * @param location 需要计算高度的方块(通常是最顶部的方块) Null时为虚空.
         */
        fun getHeight(location: Location?): Int {
            return NumberStylizer.getStylizedSpawnY(value, location)
        }

        init {
            if (!fc.contains("StructureLocation.SpawnY")) throw StructureConfigurationException("The structure must have a SpawnY value!")
            value = fc.getString("StructureLocation.SpawnY")!!
            if (fc.contains("StructureLocation.CalculateSpawnFirst")) {
                isCalculateSpawnYFirst = fc.getBoolean("StructureLocation.CalculateSpawnFirst")
            }
        }
    }
}
