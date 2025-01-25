package me.gei.tiatcustomstructures

import me.gei.tiatcustomstructures.internal.managers.BlockIgnoreManager
import me.gei.tiatcustomstructures.internal.managers.DatabaseManager
import me.gei.tiatcustomstructures.internal.managers.StructureManager
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object TiatCustomStructures : Plugin() {
    @Config("config.yml", autoReload = true)
    lateinit var conf: ConfigFile
        private set

    var isDebug: Boolean = false
    lateinit var prefix: String

    override fun onEnable() {
        info("&a  _____ _       _    ____          _                  ____  _                   _                       ".colored())
        info("&a |_   _(_) __ _| |_ / ___|   _ ___| |_ ___  _ __ ___ / ___|| |_ _ __ _   _  ___| |_ _   _ _ __ ___  ___ ".colored())
        info("&a   | | | |/ _` | __| |  | | | / __| __/ _ \\| '_ ` _ \\\\___ \\| __| '__| | | |/ __| __| | | | '__/ _ \\/ __|".colored())
        info("&a   | | | | (_| | |_| |__| |_| \\__ \\ || (_) | | | | | |___) | |_| |  | |_| | (__| |_| |_| | | |  __/\\__ \\".colored())
        info("&a   |_| |_|\\__,_|\\__|\\____\\__,_|___/\\__\\___/|_| |_| |_|____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  \\___||___/".colored())
        info("&a                                                                                                        ".colored())

        prefix = conf.getString("Prefix")!!.colored()
        isDebug = conf.getBoolean("debug", false)
        BlockIgnoreManager.load()
        StructureManager.load()
        DatabaseManager.setup()
    }
}