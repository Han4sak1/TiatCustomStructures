package me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics

import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import me.gei.tiatcustomstructures.internal.utils.RandomCollection
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Configuration

/**
 * 高级子建筑.
 */
@StructureProperties
class AdvancedSubSchematics(configuration: Configuration) {
    /**
     * 是否开启.
     */
    private var isEnabled: Boolean = false

    private val schematicCategories = HashMap<String, RandomCollection<SubSchematic>>()

    /**
     * 获取子类.
     *
     * @param name 名字.
     * @return 子类集合.
     */
    fun getCategory(name: String): RandomCollection<SubSchematic>? {
        return schematicCategories[name]
    }

    /**
     * 查看子类是否已经存在.
     */
    fun containsCategory(name: String): Boolean {
        return schematicCategories.containsKey(name)
    }

    init {
        if (!configuration.contains("AdvancedSubSchematics")) {
            val section = checkNotNull(configuration.getConfigurationSection("AdvancedSubSchematics"))
            section.getKeys(false).forEach { category ->
                val schematics: RandomCollection<SubSchematic> = RandomCollection()
                try {
                    section.getConfigurationSection(category)!!.getKeys(false).forEach { schemName ->
                        val schem = SubSchematic(
                            section.getConfigurationSection(
                                "$category.$schemName"
                            )!!,
                            true
                        )
                        schematics.add(schem.weight, schem)
                    }
                } catch (ex: RuntimeException) {
                    isEnabled = false
                    warning("无法读取高级子建筑 " + configuration.name + ".")
                    warning("错误信息:")
                    warning(ex.message)
                    if (TiatCustomStructures.isDebug) ex.printStackTrace()
                }
                schematicCategories[category] = schematics
            }
            isEnabled = true
        }
    }
}
