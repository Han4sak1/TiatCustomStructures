package me.gei.tiatcustomstructures.internal.bo.structure.properties

import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import taboolib.module.configuration.Configuration

/**
 * 其他建筑选项.
 */
@StructureProperties
class StructureProperties(configuration: Configuration) {
    /**
     * 是否放置空气.
     */
    var shouldPlaceAir: Boolean = true
        private set
    /**
     * 是否随机旋转.
     */
    var shouldRandomRotation: Boolean = false
        private set
    /**
     * 是否忽略植物.
     */
    var shouldIgnoringPlants: Boolean = true
        private set
    /**
     * 是否可以生成在水中.
     */
    var shouldSpawnInWater: Boolean = true
        private set
    /**
     * 是否可生成在岩浆中.
     */
    var shouldSpawnInLavaLakes: Boolean = true
        private set
    /**
     * 是否可生成在虚空.
     */
    var shouldSpawnInVoid: Boolean = false
        private set
    /**
     * 是否忽略水.
     */
    var shouldIgnoreWater: Boolean = false
        private set

    init {
        val cs = configuration.getConfigurationSection("StructureProperties")
        if (cs != null) {
            this.shouldPlaceAir = cs.contains("PlaceAir") && cs.getBoolean("PlaceAir")
            this.shouldRandomRotation = cs.contains("RandomRotation") && cs.getBoolean("RandomRotation")
            this.shouldIgnoringPlants = cs.contains("IgnorePlants") && cs.getBoolean("IgnorePlants")
            this.shouldSpawnInWater = cs.contains("SpawnInWater") && cs.getBoolean("SpawnInWater")
            this.shouldSpawnInLavaLakes = cs.contains("SpawnInLavaLakes") && cs.getBoolean("SpawnInLavaLakes")
            this.shouldSpawnInVoid = cs.contains("SpawnInVoid") && cs.getBoolean("SpawnInVoid")
            this.shouldIgnoreWater = cs.contains("IgnoreWater") && cs.getBoolean("IgnoreWater")
        }
    }
}
