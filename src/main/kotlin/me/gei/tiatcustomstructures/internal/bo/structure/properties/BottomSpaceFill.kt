package me.gei.tiatcustomstructures.internal.bo.structure.properties

import me.gei.tiatcustomstructures.api.compact.otg.OTGBiomeBaser
import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import taboolib.module.configuration.Configuration
import java.util.*

/**
 * 底部填充.
 * 当建筑浮空时可用
 */
@StructureProperties
class BottomSpaceFill(configuration: Configuration) {
    private val blockMap: MutableMap<String, Material> = HashMap()
    private var defaultMaterial: Material? = null

    /**
     * 是否开启.
     */
    var isEnabled: Boolean = false
        private set

    init {
        if (configuration.contains("BottomSpaceFill")) {
            val fillSection = configuration.getConfigurationSection("BottomSpaceFill")!!

            isEnabled = true

            fillSection.getKeys(false).forEach { keyGroup ->
                val keys = keyGroup.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val fillMaterial: Material
                try {
                    fillMaterial = Material.valueOf(
                        fillSection.getString(keyGroup)!!.uppercase(Locale.getDefault())
                    )
                } catch (ex: IllegalArgumentException) {
                    throw StructureConfigurationException("未知底部填充材质 " + fillSection.getString(keyGroup))
                }

                keys.forEach loopKeys@ { key ->
                    if (key.equals("default", ignoreCase = true)) {
                        defaultMaterial = fillMaterial
                        return@loopKeys
                    }

                    blockMap[key] = fillMaterial
                }
            }
        }
    }

    /**
     * 获取指定生物群系的填充.
     *
     * @param block 方块.
     * @return 填充
     */
    fun getFillMaterial(block: Block): Material? {
        val biome: String = if (Bukkit.getPluginManager().isPluginEnabled("OpenTerrainGenerator")) {
            OTGBiomeBaser.getOTGBiomeOrVanilla(block)
        } else block.biome.name
        if (blockMap.containsKey(biome.replace("minecraft:", ""))) return blockMap[biome.replace("minecraft:", "")]
        return defaultMaterial
    }
}
