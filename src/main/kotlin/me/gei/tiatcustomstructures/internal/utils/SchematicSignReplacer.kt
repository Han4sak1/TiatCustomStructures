package me.gei.tiatcustomstructures.internal.utils

import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.internal.managers.SchematicManager
import me.gei.tiatcustomstructures.internal.managers.StructureSignHandler
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.AdvancedSubSchematics
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.SubSchematic
import me.gei.tiatcustomstructures.internal.bo.structure.properties.schematics.SubSchematics
import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.util.Vector
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass
import java.lang.reflect.InvocationTargetException
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 建筑牌子替换.
 */
object SchematicSignReplacer {
    /**
     * 替换牌子(除子建筑).
     *
     * @param location          牌子位置.
     * @param minLoc            建筑最低点.
     * @param maxLoc            建筑最高点.
     * @param structure         建筑实例.
     * @param structureRotation 建筑旋转角.
     */
    internal fun processAndReplaceSign(
        location: Location,
        minLoc: Location,
        maxLoc: Location,
        structure: Structure,
        structureRotation: Double
    ) {
        if (location.block.state !is Sign) {
            return
        }

        val sign = location.block.state as Sign
        val firstLine = sign.getLine(0).trim { it <= ' ' }
        val secondLine = sign.getLine(1).trim { it <= ' ' }
        val thirdLine = sign.getLine(2).trim { it <= ' ' }
        val fourthLine = sign.getLine(3).trim { it <= ' ' }

        if (!firstLine.startsWith("[")) return

        val signName = firstLine.replace("\\[".toRegex(), "").replace("]".toRegex(), "")

        if (!StructureSignHandler.structureSignExists(signName)) return

        var signRotation: Double
        // Allow this to work with both wall signs and normal signs.
        val signData = sign.data as org.bukkit.material.Sign
        if (!signData.isWallSign) {
            val direction = location.block.location.direction
            signRotation = atan2(direction.z, direction.x)
            if (direction.x != 0.0) {
                signRotation -= (Math.PI / 2)
            } else {
                signRotation += (Math.PI / 2)
            }
        } else {
            val facing = signData.facing
            val direction = when (facing) {
                BlockFace.WEST -> {
                    Vector(-1, 0, 0)
                }

                BlockFace.EAST -> {
                    Vector(1, 0, 0)
                }

                BlockFace.SOUTH -> {
                    Vector(0, 0, 1)
                }

                BlockFace.NORTH -> {
                    Vector(0, 0, -1)
                }

                BlockFace.NORTH_EAST -> {
                    Vector(1 / sqrt(2.0), 0.0, -1 / sqrt(2.0))
                }

                BlockFace.NORTH_WEST -> {
                    Vector(-1 / sqrt(2.0), 0.0, -1 / sqrt(2.0))
                }

                BlockFace.SOUTH_EAST -> {
                    Vector(1 / sqrt(2.0), 0.0, 1 / sqrt(2.0))
                }

                BlockFace.SOUTH_WEST -> {
                    Vector(-1 / sqrt(2.0), 0.0, 1 / sqrt(2.0))
                }

                else -> {
                    Vector(sqrt(2.0), 0.0, sqrt(2.0))
                }
            }
            signRotation = atan2(direction.z, direction.x)
            if (direction.x != 0.0) {
                signRotation -= (Math.PI / 2)
            } else {
                signRotation += (Math.PI / 2)
            }
        }

        val structureSignClass: Class<out StructureSign> = StructureSignHandler.getStructureSign(signName) ?: return
        try {
            val structureSign: StructureSign = ReflexClass.of(structureSignClass).newInstance() as StructureSign

            val args = arrayOf(secondLine, thirdLine, fourthLine)
            structureSign.initialize(args, signRotation, structureRotation, minLoc, maxLoc)

            // Replace the sign with air if desired.
            if (structureSign.onStructureSpawn(location, structure)) {
                location.block.type = Material.AIR
            }
        } catch (exception: NoSuchMethodException) {
            severe("无法处理建筑牌子 $signName, 建筑物ID ${structure.name}")
            severe("请检查建筑物牌子实现class有无默认构造函数")
        } catch (exception: InstantiationException) {
            severe("无法处理建筑牌子 $signName, 建筑物ID ${structure.name}")
            severe("请检查建筑物牌子实现class有无默认构造函数")
        } catch (exception: IllegalAccessException) {
            severe("无法处理建筑牌子 $signName, 建筑物ID ${structure.name}")
            severe("请检查建筑物牌子实现class有无默认构造函数")
        } catch (exception: InvocationTargetException) {
            severe("无法处理建筑牌子 $signName, 建筑物ID ${structure.name}")
            severe("请检查建筑物牌子实现class有无默认构造函数")
        }
    }

    /**
     * 替换牌子为子建筑.
     *
     * @param location        牌子位置.
     * @param parentStructure 母建筑.
     * @param iteration       迭代数.
     */
    internal fun replaceSignWithSchematic(location: Location, parentStructure: Structure, iteration: Int) {
        var newLocation = location

        val subSchematics: SubSchematics = parentStructure.subSchematics
        val advancedSubSchematics: AdvancedSubSchematics = parentStructure.advancedSubSchematics

        val sign = newLocation.block.state as Sign
        val firstLine = sign.getLine(0).trim { it <= ' ' }
        val secondLine = sign.getLine(1).trim { it <= ' ' }

        // Allow this to work with both wall signs and normal signs.
        val signData = sign.data as org.bukkit.material.Sign
        if (!signData.isWallSign) {
            val direction = newLocation.block.location.direction
            var rotation = atan2(direction.z, direction.x)
            if (direction.x != 0.0) {
                rotation -= (Math.PI / 2)
            } else {
                rotation += (Math.PI / 2)
            }
            parentStructure.subSchemRotation = rotation
        } else {
            val facing = signData.facing
            val direction = when (facing) {
                BlockFace.WEST -> {
                    Vector(-1, 0, 0)
                }

                BlockFace.EAST -> {
                    Vector(1, 0, 0)
                }

                BlockFace.SOUTH -> {
                    Vector(0, 0, 1)
                }

                BlockFace.NORTH -> {
                    Vector(0, 0, -1)
                }

                BlockFace.NORTH_EAST -> {
                    Vector(1 / sqrt(2.0), 0.0, -1 / sqrt(2.0))
                }

                BlockFace.NORTH_WEST -> {
                    Vector(-1 / sqrt(2.0), 0.0, -1 / sqrt(2.0))
                }

                BlockFace.SOUTH_EAST -> {
                    Vector(1 / sqrt(2.0), 0.0, 1 / sqrt(2.0))
                }

                BlockFace.SOUTH_WEST -> {
                    Vector(-1 / sqrt(2.0), 0.0, 1 / sqrt(2.0))
                }

                else -> {
                    Vector(sqrt(2.0), 0.0, sqrt(2.0))
                }
            }
            var rotation = atan2(direction.z, direction.x)
            if (direction.x != 0.0) {
                rotation -= (Math.PI / 2)
            } else {
                rotation += (Math.PI / 2)
            }
            parentStructure.subSchemRotation = rotation
        }

        // Normal Sub-Schematic
        if (firstLine.equals("[schematic]", ignoreCase = true) || firstLine.equals("[schem]", ignoreCase = true)) {
            val number = if (secondLine.startsWith("[")) {
                try {
                    NumberStylizer.retrieveRangedInput(secondLine)
                } catch (ex: NumberFormatException) {
                    warning("无效的子建筑牌子. 无法处理牌子上的区间.")
                    return
                }
            } else {
                try {
                    secondLine.toInt()
                } catch (ex: NumberFormatException) {
                    warning("无效的子建筑牌子. 无法处理牌子上的数字.")
                    return
                }
            }

            if (number < -1 || number >= subSchematics.schematics.size) {
                warning("无效的子建筑牌子. schematic超出索引界限.")
                return
            }

            // Remove the sign after placing the schematic.
            newLocation.block.type = Material.AIR

            val subSchem: SubSchematic = subSchematics.schematics[number]

            // Disable rotation if the structure is not using it.
            if (!subSchem.isUsingRotation) parentStructure.subSchemRotation = 0.0
            try {
                if (subSchem.verticalRepositioning != null) {
                    val vertRep: SubSchematic.VerticalRepositioning = subSchem.verticalRepositioning!!
                    val heightBlock = newLocation.world!!.getHighestBlockAt(newLocation).location

                    var newSpawnY: Int = vertRep.getSpawnY(heightBlock)
                    // Check if their 1) is a range and 2) the New SpawnY is in the range.
                    if (vertRep.getRange() == null || CSUtils.isInLocalRange(
                            vertRep.getRange()!!,
                            newLocation.blockY,
                            newSpawnY
                        )
                    ) {
                        newLocation = Location(
                            newLocation.world,
                            newLocation.blockX.toDouble(),
                            newSpawnY.toDouble(),
                            newLocation.blockZ.toDouble()
                        )
                    } else {
                        if (vertRep.noPointSolution.uppercase() == "CURRENT") {
                            // Do Nothing, keep the current location.
                        } else if (vertRep.noPointSolution.uppercase() == "PREVENT_SPAWN") {
                            return
                        } else {
                            newSpawnY = NumberStylizer.getStylizedSpawnY(vertRep.noPointSolution, newLocation)
                            newLocation = Location(
                                newLocation.world,
                                newLocation.blockX.toDouble(),
                                newSpawnY.toDouble(),
                                newLocation.blockZ.toDouble()
                            )
                        }
                    }
                }
                SchematicManager.placeSchematic(
                    newLocation,
                    subSchem.file,
                    subSchem.isPlacingAir,
                    parentStructure,
                    iteration + 1
                )
            } catch (ex: Exception) {
                warning("粘贴子建筑时发生错误.")
                if (TiatCustomStructures.isDebug) {
                    ex.printStackTrace()
                }
            }
        } else if (firstLine.equals("[advschem]", ignoreCase = true)) {
            if (!advancedSubSchematics.containsCategory(secondLine)) {
                warning("无法处理高级建筑牌子.")
                warning("目录 \"$secondLine\" 不存在!")
                return
            }

            // Remove the sign after placing the schematic.
            newLocation.block.type = Material.AIR

            // 放置子建筑
            if (advancedSubSchematics.getCategory(secondLine)!!.isEmpty) return
            val subSchem: SubSchematic = advancedSubSchematics.getCategory(secondLine)!!.next()

            // Disable rotation if the structure is not using it.
            if (!subSchem.isUsingRotation) parentStructure.subSchemRotation = 0.0
            try {
                if (subSchem.verticalRepositioning != null) {
                    val vertRep: SubSchematic.VerticalRepositioning = subSchem.verticalRepositioning!!

                    val heightBlock = newLocation.world!!.getHighestBlockAt(newLocation).location
                    var newSpawnY: Int = vertRep.getSpawnY(heightBlock)

                    // Check if their 1) is a range and 2) the New SpawnY is in the range.
                    if (vertRep.getRange() == null || CSUtils.isInLocalRange(
                            vertRep.getRange()!!,
                            newLocation.blockY,
                            newSpawnY
                        )
                    ) {
                        newLocation = Location(
                            newLocation.world,
                            newLocation.blockX.toDouble(),
                            newSpawnY.toDouble(),
                            newLocation.blockZ.toDouble()
                        )
                    } else {
                        if (vertRep.noPointSolution.uppercase() == "CURRENT") {
                            // Do Nothing, keep the current location.
                        } else if (vertRep.noPointSolution.uppercase() == "PREVENT_SPAWN") {
                            return
                        } else {
                            newSpawnY = NumberStylizer.getStylizedSpawnY(vertRep.noPointSolution, newLocation)
                            newLocation = Location(
                                newLocation.world,
                                newLocation.blockX.toDouble(),
                                newSpawnY.toDouble(),
                                newLocation.blockZ.toDouble()
                            )
                        }
                    }
                }

                SchematicManager.placeSchematic(
                    newLocation,
                    subSchem.file,
                    subSchem.isPlacingAir,
                    parentStructure,
                    iteration + 1
                )
            } catch (ex: Exception) {
                warning("粘贴高级子建筑时发生错误.")
                if (TiatCustomStructures.isDebug) {
                    ex.printStackTrace()
                }
            }
        }
    }
}
