package me.gei.tiatcustomstructures.internal.bo.structure

import me.gei.tiatcustomstructures.internal.autoregister.AutoRegisterFrame
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import me.gei.tiatcustomstructures.internal.bo.structure.properties.*
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.AdvancedSubSchematics
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.SubSchematics
import taboolib.module.configuration.Configuration
import java.io.File

class StructureBuilder {

    lateinit var name: String
        private set
    lateinit var schematic: String
        private set

    var probabilityNumerator: Int = 1
    var probabilityDenominator: Int = 500

    var priority: Int = 100
        private set
    var baseRotation: Int = 0
        private set

    lateinit var structureLocation: StructureLocation
    lateinit var structureProperties: StructureProperties
    lateinit var structureLimitations: StructureLimitations

    lateinit var subSchematics: SubSchematics
        private set
    lateinit var advancedSubSchematics: AdvancedSubSchematics
        private set
    lateinit var bottomSpaceFill: BottomSpaceFill
        private set
    lateinit var worldGuardIntegration: WorldGuardIntegration
        private set

    fun build(): Structure {
        return Structure(this)
    }

    companion object {
        fun fromConfig(file: File): StructureBuilder {
            val conf = Configuration.loadFromFile(file)
            checkValid(conf)
            val builder = StructureBuilder()

            builder.name = file.nameWithoutExtension
            builder.schematic = conf.getString("Schematic")!!
            builder.probabilityNumerator = conf.getInt("Probability.Numerator")
            builder.probabilityDenominator = conf.getInt("Probability.Denominator")
            builder.priority = conf.getInt("Priority", 100)

            initProperties(builder, conf)

            return builder
        }

        private fun checkValid(conf: Configuration) {
            if (!conf.contains("Schematic")) {
                throw StructureConfigurationException("无效的配置: schematic文件未找到!")
            }
            if (!conf.contains("Probability.Numerator")) {
                throw StructureConfigurationException("无效的配置: `Probability.Numerator` 未找到!")
            }
            if (!conf.contains("Probability.Denominator")) {
                throw StructureConfigurationException("无效的配置: `Probability.Denominator` 未找到!")
            }
            if (!conf.isInt("Probability.Numerator") || conf.getInt("Probability.Numerator") < 1) {
                throw StructureConfigurationException("无效的配置: `Probability.Numerator` 必须大于1!")
            }
            if (!conf.isInt("Probability.Denominator") || conf.getInt("Probability.Denominator") < 1) {
                throw StructureConfigurationException("无效的配置: `Probability.Denominator` 必须大于1!")
            }
        }

        private fun initProperties(builder: StructureBuilder, conf: Configuration) {
            AutoRegisterFrame.getPropertiesInstances(conf).forEach { property ->
                when (property) {
                    is StructureLocation -> builder.structureLocation = property
                    is StructureProperties -> builder.structureProperties = property
                    is StructureLimitations -> builder.structureLimitations = property
                    is SubSchematics -> builder.subSchematics = property
                    is AdvancedSubSchematics -> builder.advancedSubSchematics = property
                    is BottomSpaceFill -> builder.bottomSpaceFill = property
                    is WorldGuardIntegration -> builder.worldGuardIntegration = property
                }
            }
        }
    }
}