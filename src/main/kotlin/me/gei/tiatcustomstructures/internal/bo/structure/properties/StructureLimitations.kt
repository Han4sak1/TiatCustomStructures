package me.gei.tiatcustomstructures.internal.bo.structure.properties

import me.gei.tiatcustomstructures.internal.autoregister.StructureProperties
import me.gei.tiatcustomstructures.internal.exceptions.StructureConfigurationException
import org.bukkit.Material
import org.bukkit.block.Block
import taboolib.module.configuration.Configuration
import java.util.*
import kotlin.math.max

/**
 * 建筑生成限制.
 */
@StructureProperties
class StructureLimitations(configuration: Configuration) {
    /**
     * 获取高度限制.
     */
    val worldHeightRestriction: Int =
        if (!configuration.contains("StructureLimitations.WorldHeightRestriction")) -1
        else max(0.0, configuration.getInt("StructureLimitations.WorldHeightRestriction").toDouble())
        .toInt()

    /**
     * 获取迭代限制.
     */
    val iterationLimit: Int =
        if (!configuration.contains("StructureLimitations.IterationLimit")) 2
        else configuration.getInt("StructureLimitations.IterationLimit")

    /**
     * 白名单方块.
     */
    private val whitelistBlocks: List<String> =
        if (!configuration.contains("StructureLimitations.WhitelistSpawnBlocks")) ArrayList()
        else configuration.getStringList("StructureLimitations.WhitelistSpawnBlocks")

    /**
     * 黑名单方块.
     */
    private val blacklistBlocks: List<String> =
        if (!configuration.contains("StructureLimitations.BlacklistSpawnBlocks")) ArrayList()
        else configuration.getStringList("StructureLimitations.BlacklistSpawnBlocks")

    /**
     * 获取区域限制.
     */
    val blockLevelLimit: BlockLevelLimit = BlockLevelLimit(configuration)

    private val blockReplacement: MutableMap<Material, Material>

    /**
     * 获取替换延时.
     */
    var replacementBlocksDelay: Double = 0.0
        private set

    /**
     * 检查白名单.
     */
    fun hasWhitelistBlock(b: Block): Boolean {
        if (whitelistBlocks.isEmpty()) return true
        whitelistBlocks.forEach { block ->
            if (block.equals(b.type.toString(), ignoreCase = true)) return true
        }
        return false
    }

    /**
     * 检查黑名单.
     */
    fun hasBlacklistBlock(b: Block): Boolean {
        if (blacklistBlocks.isEmpty()) return false
        blacklistBlocks.forEach { block ->
            if (block.equals(b.type.toString(), ignoreCase = true)) return true
        }
        return false
    }

    /**
     * 获取待替换方块.
     */
    fun getBlockReplacement(): Map<Material, Material> {
        return blockReplacement
    }

    init {
        replacementBlocksDelay =
            if (!configuration.contains("StructureLimitations.ReplaceBlockDelay")) 0.0
            else configuration.getDouble("StructureLimitations.ReplaceBlockDelay")

        blockReplacement = EnumMap(org.bukkit.Material::class.java)
        if (configuration.contains("StructureLimitations.ReplaceBlocks")) {
            configuration.getConfigurationSection("StructureLimitations.ReplaceBlocks")!!.getKeys(false).forEach { s ->
                val firstMaterial = Material.getMaterial(s)
                val secondMaterial = Material.getMaterial(
                    configuration.getString(
                        "StructureLimitations.ReplaceBlocks.$s"
                    )!!
                )
                blockReplacement[firstMaterial] = secondMaterial
            }
        }
    }

    /**
     * 区域限制.
     * 模式:
     * flat, flat_error
     * flat - 区域的地面不得为空气，且地面上方的方块必须为空气或植物.
     * flat_error - 与上方相同但允许例外. 如果 error被设置为 0.33 则有1/3的容错率
     */
    class BlockLevelLimit(fileConfiguration: Configuration) {
        /**
         * 是否开启.
         */
        val isEnabled: Boolean
            get() = !mode.equals("none", ignoreCase = true)
        /**
         * 获取模式.
         */
        var mode: String = "NONE"

        /**
         * 限制
         */
        var x1: Int = 0
        var z1: Int = 0
        var x2: Int = 0
        var z2: Int = 0

        /**
         * 获取容错率.
         * flat_error模式专用
         */
        var error: Double = -1.0
            private set

        init {
            if (fileConfiguration.contains("StructureLimitations.BlockLevelLimit")) {
                val cs = checkNotNull(fileConfiguration.getConfigurationSection("StructureLimitations.BlockLevelLimit"))
                this.mode = cs.getString("Mode")!!
                this.x1 = cs.getInt("CornerOne.x")
                this.z1 = cs.getInt("CornerOne.z")
                this.x2 = cs.getInt("CornerTwo.x")
                this.z2 = cs.getInt("CornerTwo.z")
                if (cs.contains("Error")) {
                    error = cs.getDouble("Error")
                    if (error < 0 || error > 1) throw StructureConfigurationException("'BlockLevelLimit.Error' 必须介于0 1之间.")
                }
                if (mode.equals("flat_error", ignoreCase = true) && !cs.contains("Error")) {
                    throw StructureConfigurationException("'flat_error' 必须包含Error容错率设定!")
                }
            }
        }
    }
}
