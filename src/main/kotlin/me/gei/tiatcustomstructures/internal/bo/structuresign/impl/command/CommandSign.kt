package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.command

import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import me.gei.tiatcustomstructures.internal.utils.CSUtils
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

class CommandSign : StructureSign() {
    override fun onStructureSpawn(location: org.bukkit.Location, structure: Structure): Boolean {

        if (!hasArgument(0)) {
            warning("无效的指令牌子! (${structure.name})")
            return true
        }

        val commands: List<String>? = SignCommandsHandler.getCommands(getStringArgument(0))
        if (commands != null) {
            commands.forEach { command ->
                var newCommand = command
                newCommand = CSUtils.replacePlaceHolders(
                    newCommand,
                    location,
                    structureMinimumLocation,
                    structureMaximumLocation,
                    structure
                )
                newCommand = CSUtils.replacePAPIPlaceholders(newCommand)
                org.bukkit.Bukkit.getServer().dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), newCommand)
                if (TiatCustomStructures.isDebug) {
                    info("执行指令: '$newCommand'")
                }
            }
        } else {
            warning("无法执行指令组 '${getStringArgument(0)}', 未找到配置!")
        }

        return true
    }
}
