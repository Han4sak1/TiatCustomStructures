package me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics

import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Configuration

/**
 * 子建筑.
 */
@StructureProperties
class SubSchematics(configuration: Configuration) {
    /**
     * 是否开启.
     */
    private var isEnabled: Boolean = false

    val schematics: MutableList<SubSchematic> = ArrayList()

    init {
        if (configuration.contains("SubSchematics")) {
            val section = checkNotNull(configuration.getConfigurationSection("SubSchematics"))
            try {
                section.getKeys(false).forEach { s ->
                    schematics.add(
                        SubSchematic(
                            section.getConfigurationSection(s)!!,
                            false
                        )
                    )
                }
            } catch (ex: RuntimeException) {
                isEnabled = false
                warning("无法开启建筑的子建筑 " + configuration.name + ".")
                warning("错误信息:")
                warning(ex.message)
            }

            isEnabled = true
        }
    }
}
