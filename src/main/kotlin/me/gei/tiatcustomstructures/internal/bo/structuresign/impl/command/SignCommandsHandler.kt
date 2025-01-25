package me.gei.tiatcustomstructures.internal.bo.structuresign.impl.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

/**
 * 指令组牌子管理.
 */
object SignCommandsHandler {
    @Config("signcommands.yml", autoReload = true)
    private lateinit var conf: ConfigFile

    private val signCommands: MutableMap<String, List<String>> = HashMap()

    @Awake(LifeCycle.ENABLE)
    private fun load() {
        conf.getKeys(false).forEach { sectionKey ->
            val commands = conf.getStringList(sectionKey)
            if (commands.isEmpty()) {
                warning("牌子指令组 $sectionKey 没有指令! 请确保配置正确.")
            }
            signCommands[sectionKey] = commands
        }
    }

    /**
     * 清理.
     */
    fun reload() {
        signCommands.clear()
        conf.getKeys(false).forEach { sectionKey ->
            val commands = conf.getStringList(sectionKey)
            if (commands.isEmpty()) {
                warning("牌子指令组 $sectionKey 没有指令! 请确保配置正确.")
            }
            signCommands[sectionKey] = commands
        }
    }

    /**
     * 获取指令组.
     *
     * @param name 指令组名字.
     */
    fun getCommands(name: String): List<String>? {
        return signCommands[name]
    }
}
