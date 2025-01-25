package me.gei.tiatcustomstructures.api.compact.otg

import com.pg85.otg.bukkit.world.WorldHelper
import com.pg85.otg.exception.BiomeNotFoundException
import org.bukkit.block.Block

object OTGBiomeBaser {
    fun getOTGBiomeOrVanilla(block: Block): String {
        try {
            val localWorld = WorldHelper.toLocalWorld(block.world)
            val biome = localWorld.getBiome(block.x, block.z)

            return biome.name
        } catch (ex: BiomeNotFoundException) {
            return block.biome.name
        }
    }
}