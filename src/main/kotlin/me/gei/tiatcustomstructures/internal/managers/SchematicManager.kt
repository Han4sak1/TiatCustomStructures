package me.gei.tiatcustomstructures.internal.managers

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.transform.AffineTransform
import com.sk89q.worldedit.session.ClipboardHolder
import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.api.event.StructureRegionCreateEvent
import me.gei.tiatcustomstructures.api.event.StructureSpawnEvent
import me.gei.tiatcustomstructures.internal.bo.bottomfill.BottomFillProvider
import me.gei.tiatcustomstructures.internal.pojo.StructureSpawnInfo
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.utils.SchematicLocationUtils
import me.gei.tiatcustomstructures.internal.utils.SchematicSignReplacer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.Sign
import taboolib.common.io.newFile
import taboolib.common.platform.function.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Schematic 操作管理.
 */
object SchematicManager {
    /**
     * 放置建筑schematic.
     * 主线程执行.
     *
     * @param loc       - 位置
     * @param filename  - 文件名
     * @param useAir    - 复制空气
     * @param structure - 建筑.
     * @param iteration - 迭代数.
     *
     * @throws WorldEditException 复制错误时抛出.
     * @throws IOException       文件读取时抛出.
     */
    @Suppress("DEPRECATION")
    @Throws(IOException::class, WorldEditException::class)
    fun placeSchematic(loc: Location, filename: String, useAir: Boolean, structure: Structure, iteration: Int) {
        if (iteration > structure.structureLimitations.iterationLimit) {
            severe("检测到子建筑溢出, 终止生成.")
            severe("建筑 '" + structure.name + "' 通过递归生成了太多的子建筑.")
            return
        }

        val schematicFile = File(newFile(getDataFolder(), "Schematics"), filename)
        // Check to see if the schematic is a thing.
        if (!schematicFile.exists() && iteration == 0) {
            Bukkit.broadcastMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "&b[&aTiatCustomStructures&b] &c出现严重错误."
                )
            )
            warning("schematic文件 $filename 未找到!")
            disablePlugin()
            return
        } else if (!schematicFile.exists()) {
            warning("schematic文件 $filename 未找到!")
            throw RuntimeException("无法找到schematic文件!")
        }

        val format = ClipboardFormat.findByFile(schematicFile)
        val clipboard: Clipboard

        if (format == null) {
            warning("无效的schematic文件格式 $filename!")
            return
        }

        format.getReader(Files.newInputStream(schematicFile.toPath())).use { reader ->
            clipboard = reader.read(
                BukkitAdapter.adapt(loc.world).worldData
            )
        }
        val ch = ClipboardHolder(clipboard, BukkitAdapter.adapt(loc.world).worldData)
        var transform = AffineTransform()

        // Define rotation y with the default base rotation.
        var rotY = Math.toDegrees(structure.baseRotation)

        // If random rotation is enabled, rotate the clipboard
        if (structure.structureProperties.shouldRandomRotation && iteration == 0) {
            rotY = (Random().nextInt(4) * 90).toDouble()
            transform = transform.rotateY(rotY)
            ch.transform = ch.transform.combine(transform)
        } else if (iteration != 0) {
            rotY = Math.toDegrees(structure.subSchemRotation)
            transform = transform.rotateY(rotY)
            ch.transform = ch.transform.combine(transform)
        }

        WorldEdit.getInstance().editSessionFactory
            .getEditSession(BukkitAdapter.adapt(loc.world), -1).let { editSession ->
                val operation = ch.createPaste(editSession, BukkitAdapter.adapt(loc.world).worldData)
                    .to(Vector(loc.x, loc.y, loc.z)).ignoreAirBlocks(!useAir).build()
                Operations.complete(operation)
                if (TiatCustomStructures.isDebug) {
                    debug("在 (${loc.world!!}) 成功生成了 ${structure.name} 位置 ${loc.x}, ${loc.y}, ${loc.z} 旋转角度 $rotY")
                }

                editSession.close()
            }
        //Call StructureRegionCreateEvent
        StructureRegionCreateEvent(structure, loc, clipboard).call()

        // If enabled, perform a bottom space fill.
        if (structure.bottomSpaceFill.isEnabled) {
            val minLoc: Location = SchematicLocationUtils.getMinimumLocation(clipboard, loc, rotY)
            val maxLoc: Location = SchematicLocationUtils.getMaximumLocation(clipboard, loc, rotY)
            val lowX = min(minLoc.blockX.toDouble(), maxLoc.blockX.toDouble()).toInt()
            val lowY = min(minLoc.blockY.toDouble(), maxLoc.blockY.toDouble()).toInt()
            val lowZ = min(minLoc.blockZ.toDouble(), maxLoc.blockZ.toDouble()).toInt()
            val highX = max(minLoc.blockX.toDouble(), maxLoc.blockX.toDouble()).toInt()
            val highY = max(minLoc.blockY.toDouble(), maxLoc.blockY.toDouble()).toInt()
            val highZ = max(minLoc.blockZ.toDouble(), maxLoc.blockZ.toDouble()).toInt()
            BottomFillProvider.provideDefault().performFill(
                structure,
                loc,
                Location(minLoc.world, lowX.toDouble(), lowY.toDouble(), lowZ.toDouble()),
                Location(minLoc.world, highX.toDouble(), highY.toDouble(), highZ.toDouble()),
                transform
            )
        }

        // Schedule the signs & containers replacement task
        val finalRotY = rotY
        // Run a task later. This is done so async plugins have time to paste as needed.
        submit(delay = (structure.structureLimitations.replacementBlocksDelay * 20).toLong()) {
            val signsLocations: MutableList<Location> = replaceBlocks(ch.clipboard, loc, finalRotY, structure)

            signsLocations.forEach { location ->
                if (location.block.state is Sign) {
                    val minLoc: Location = SchematicLocationUtils.getMinimumLocation(clipboard, loc, finalRotY)
                    val maxLoc: Location = SchematicLocationUtils.getMaximumLocation(clipboard, loc, finalRotY)
                    SchematicSignReplacer.processAndReplaceSign(location, minLoc, maxLoc, structure, finalRotY)
                }
                // If the sign still exists, it could be a sub-schematic sign.
                if (location.block.state is Sign) {
                    SchematicSignReplacer.replaceSignWithSchematic(location, structure, iteration)
                }
            }

            // Call the event for use by other plugins (only if it is the first iteration though.)
            if (iteration < 1) {
                val structureSpawnInfo: StructureSpawnInfo = StructureSpawnInfo(
                    SchematicLocationUtils.getMinimumLocation(clipboard, loc, 0.0),
                    SchematicLocationUtils.getMaximumLocation(clipboard, loc, 0.0), signsLocations
                )
                StructureSpawnEvent(structure, loc, finalRotY, structureSpawnInfo).call()
            }
        }
    }

    /**
     * 放置建筑schematic.
     */
    @Throws(IOException::class, WorldEditException::class)
    fun placeSchematic(loc: Location, filename: String, useAir: Boolean, structure: Structure) {
        placeSchematic(loc, filename, useAir, structure, 0)
    }

    /**
     * 获取需要替换的方块的位置列表(包括建筑牌子).
     *
     * @param clipboard     剪切板
     * @param pasteLocation 位置
     * @param rotation      旋转角(弧度)
     *
     * @return 列表
     */
    private fun replaceBlocks(
        clipboard: Clipboard,
        pasteLocation: Location,
        rotation: Double,
        structure: Structure
    ): MutableList<Location> {
        val minLoc: Location = SchematicLocationUtils.getMinimumLocation(clipboard, pasteLocation, rotation)
        val maxLoc: Location = SchematicLocationUtils.getMaximumLocation(clipboard, pasteLocation, rotation)
        val locations: MutableList<Location> = ArrayList()

        val lowX = min(minLoc.blockX.toDouble(), maxLoc.blockX.toDouble()).toInt()
        val lowY = min(minLoc.blockY.toDouble(), maxLoc.blockY.toDouble()).toInt()
        val lowZ = min(minLoc.blockZ.toDouble(), maxLoc.blockZ.toDouble()).toInt()

        for (x in 0..abs((minLoc.blockX - maxLoc.blockX).toDouble()).toInt()) {
            for (y in 0..abs((minLoc.blockY - maxLoc.blockY).toDouble()).toInt()) {
                for (z in 0..abs((minLoc.blockZ - maxLoc.blockZ).toDouble()).toInt()) {
                    val location = Location(
                        pasteLocation.world,
                        (lowX + x).toDouble(),
                        (lowY + y).toDouble(),
                        (lowZ + z).toDouble()
                    )
                    val block = location.block
                    val blockState = location.block.state

                    if (blockState is Sign) {
                        locations.add(location)
                    } else {
                        // For the block replacement system.
                        if (structure.structureLimitations.getBlockReplacement().isNotEmpty()) {
                            if (structure.structureLimitations.getBlockReplacement().containsKey(block.type)) {
                                block.type = structure.structureLimitations.getBlockReplacement().get(block.type)!!
                                block.state.update()
                            }
                        }
                    }
                }
            }
        }
        return locations
    }
}