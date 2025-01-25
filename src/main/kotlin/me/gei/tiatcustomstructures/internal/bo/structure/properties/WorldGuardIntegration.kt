package me.gei.tiatcustomstructures.internal.bo.structure.properties

import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import taboolib.module.configuration.Configuration

/**
 * WorldGuard设定
 */
@StructureProperties
class WorldGuardIntegration(configuration: Configuration) {
    var isEnabled: Boolean = false
    /**
     * 区域名称
     */
    lateinit var regionName: String
        private set
    /**
     * 多边形化最大节点数
     */
    var maxPoints = 0
        private set

    init {
        val cs = configuration.getConfigurationSection("WorldGuard")
        if (cs != null) {
            if (cs.contains("WGEnabled")) this.isEnabled = cs.getBoolean("WGEnabled")
            else this.isEnabled = false

            if (isEnabled) {
                if (cs.contains("WGregionName")) {
                    this.regionName = cs.getString("WGregionName")!!
                } else throw StructureConfigurationException("WorldGuard配置无效!")
                if (cs.contains("WGpolygonizeMaxPoints")) {
                    this.maxPoints = cs.getInt("WGpolygonizeMaxPoints")
                }
            }
        }
    }
}
