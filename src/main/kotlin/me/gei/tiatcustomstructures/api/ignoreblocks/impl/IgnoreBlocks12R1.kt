package me.gei.tiatcustomstructures.api.ignoreblocks.impl

import me.gei.tiatcustomstructures.api.ignoreblocks.IgnoreBlocks
import org.bukkit.Material

class IgnoreBlocks12R1 : IgnoreBlocks {
    override val blocks: List<Material> =
        listOf(
            Material.SNOW,  // Grasses
            Material.GRASS,
            Material.LONG_GRASS,
            Material.DEAD_BUSH,
            Material.CACTUS,  // Tree Items
            Material.LEAVES,
            Material.LEAVES_2,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.HUGE_MUSHROOM_1,
            Material.HUGE_MUSHROOM_2,
            Material.VINE,
            Material.LOG,
            Material.LOG_2,  // Flowers
            Material.YELLOW_FLOWER,
            Material.RED_ROSE,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM
        )
}
