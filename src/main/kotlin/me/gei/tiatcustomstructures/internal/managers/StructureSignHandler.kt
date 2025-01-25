package me.gei.tiatcustomstructures.internal.managers

import me.gei.tiatcustomstructures.internal.bo.structuresign.StructureSign
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.command.CommandSign
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic.MythicMobSign
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.mythic.MythicSpawnerSign
import me.gei.tiatcustomstructures.internal.bo.structuresign.impl.npc.AdyeshachNPCSign
import java.util.*

/**
 * 建筑牌子管理器.
 */
object StructureSignHandler {
    private val structureSigns: MutableMap<String, Class<out StructureSign?>> = HashMap<String, Class<out StructureSign?>>()

    /**
     * 注册管理器.
     * 仅供内部使用
     */
    init {
        // Register default command sign impl.
        registerStructureSign("command", CommandSign::class.java)
        registerStructureSign("adynpc", AdyeshachNPCSign::class.java)
        registerStructureSign("mmmob", MythicMobSign::class.java)
        registerStructureSign("mmspawner", MythicSpawnerSign::class.java)
    }

    /**
     * 注册建筑牌子.
     *
     * @param name               牌子第一行括号里的ID
     * @param structureSignClass 需要注册的类.
     * @return true为成功注册, false为存在冲突的牌子ID.
     */
    @Suppress("SameParameterValue")
    private fun registerStructureSign(name: String, structureSignClass: Class<out StructureSign>): Boolean {
        if (structureSigns.containsKey(name.uppercase(Locale.getDefault()))) return false

        // Discourage a plugin from overriding the sub-schematic functionality.
        if (name.equals("schem", ignoreCase = true) ||
            name.equals("schematic", ignoreCase = true) ||
            name.equals("advschem", ignoreCase = true)
        ) {
            return false
        }

        structureSigns[name.uppercase(Locale.getDefault())] = structureSignClass
        return true
    }

    /**
     * 获取注册的牌子..
     */
    internal fun getStructureSign(name: String): Class<out StructureSign>? {
        return structureSigns[name.uppercase(Locale.getDefault())]
    }

    /**
     * 检查是否存在.
     */
    internal fun structureSignExists(name: String): Boolean {
        return structureSigns.containsKey(name.uppercase(Locale.getDefault()))
    }
}
