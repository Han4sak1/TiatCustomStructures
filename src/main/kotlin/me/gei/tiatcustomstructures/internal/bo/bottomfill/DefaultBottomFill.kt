package me.gei.tiatcustomstructures.internal.bo.bottomfill

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.Vector2D
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.math.transform.AffineTransform
import me.gei.tiatcustomstructures.internal.managers.BlockIgnoreManager
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location
import org.bukkit.Material
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

/**
 * 默认底部填充实现.
 */
class DefaultBottomFill :BottomFillImpl {
    private var structure: Structure? = null
    private var spawnLocation: Location? = null
    private var fillMaterial: Material? = null
    private var minY = 0
    private lateinit var groundPlane: Queue<Vector2D> // The 2D plane which the blocks will be ground placed on

    override fun performFill(
        structure: Structure,
        spawnLocation: Location,
        minLoc: Location,
        maxLoc: Location?,
        transform: AffineTransform
    ) {
        val fillMaterial: Material? = structure.bottomSpaceFill.getFillMaterial(spawnLocation.block)
        if (fillMaterial != null) {
            this.fillMaterial = fillMaterial
        } else return

        this.structure = structure
        this.spawnLocation = spawnLocation
        this.groundPlane = LinkedList()
        this.minY = minLoc.blockY

        submitAsync {
            // ---- This part of code should be safe to run async ----
            // To get the ground plane, we need to read the schematic
            val file =
                File(newFile(getDataFolder(), "Schematics"), structure.schematic)
            val format = ClipboardFormat.findByFile(file)
            if (format == null) {
                warning("无效的schematic格式" + structure.schematic)
                warning("请提供有效的schematic")
                return@submitAsync
            }
            try {
                format.getReader(FileInputStream(file)).use { reader ->
                    val clipboard = reader.read(BukkitAdapter.adapt(spawnLocation.world).worldData)
                    // The new origin point which the structure is pasted onto
                    val oX = spawnLocation.blockX
                    val oY = spawnLocation.blockY
                    val oZ = spawnLocation.blockZ

                    val clipboardMinY = clipboard.minimumPoint.blockY
                    for (x in clipboard.minimumPoint.blockX..clipboard.maximumPoint.blockX) {
                        for (z in clipboard.minimumPoint.blockZ..clipboard.maximumPoint.blockZ) {
                            // Loop through bottom plane of the region

                            if (BukkitAdapter.adapt(spawnLocation.world).worldData.blockRegistry.getMaterial(
                                    clipboard.getBlock(
                                        Vector(x, clipboardMinY, z)
                                    )
                                )!!.isMovementBlocker
                            ) {
                                // Find the certain point of the bottom plane which bottom fill should start at

                                var groundPoint = Vector(x, clipboardMinY, z)

                                groundPoint = groundPoint.subtract(
                                    clipboard.origin.blockX,
                                    clipboard.origin.blockY,
                                    clipboard.origin.blockZ
                                ) // Translate point back to origin (0,0)
                                val transformed = transform.apply(
                                    Vector(
                                        groundPoint.x,
                                        groundPoint.y,
                                        groundPoint.z
                                    )
                                ) // Apply transformation (rotation, etc.)
                                val groundPoint2: Vector =
                                    transformed.add(oX, oY, oZ).toBlockPoint() // Translate point back (to new origin)

                                val groundPoint2final = Vector2D(groundPoint2.x, groundPoint2.z)

                                groundPlane.add(groundPoint2final)
                            }
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                warning("找不到schematic文件: " + file.path)
                warning("底部填充将无法应用于 " + structure.name)
                return@submitAsync
            } catch (e: IOException) {
                warning("读取schematic文件时发生意外: " + file.path)
                warning("底部填充将无法应用于 " + structure.name)
                return@submitAsync
            }

            // ---- Then do the block placement on the main thread ----
            submit(period = 2) {
                val world = spawnLocation.world
                if (world == null) {
                    warning("世界 " + structure.name + " 未加载")
                    warning("底部填充将无法应用于 " + structure.name)
                    cancel()
                    return@submit
                }

                // Select 8 ground points in a single tick
                repeat(8) {
                    val groundPoint = groundPlane.poll()
                    if (groundPoint == null) {
                        cancel()
                        return@submit
                    }

                    var y = minY - 1
                    val x = groundPoint.blockX
                    val z = groundPoint.blockZ

                    // Fill the bottom space of the selected ground points down to 64 blocks
                    repeat(64) {
                        val shouldFill =  // If the block is empty
                            world.getBlockAt(x, y, z).isEmpty ||  // Or if the block is in the list of ignore blocks.
                                    BlockIgnoreManager.ignoreBlocks.blocks.contains(
                                        world.getBlockAt(
                                            x,
                                            y,
                                            z
                                        ).type
                                    ) ||  // Or if it is water (if it is set to be ignored)
                                    (structure.structureProperties.shouldIgnoreWater && world.getBlockAt(
                                        x,
                                        y,
                                        z
                                    ).type == Material.WATER)
                        if (shouldFill) {
                            world.getBlockAt(x, y--, z).type = fillMaterial
                        } else return@submit
                    }
                }
            }
        }
    }
}