package me.gei.tiatcustomstructures.internal.managers

import me.gei.tiatcustomstructures.TiatCustomStructures
import me.gei.tiatcustomstructures.internal.dto.*
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location
import taboolib.common.io.newFile
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.database.HostSQL
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 数据库管理器
 * 负责建筑的保存与查询
 */
object DatabaseManager {

    private lateinit var database: Database

    fun setup() {
        try {
            if (TiatCustomStructures.conf.getBoolean("Database.enable")) {
                setupDatabase(TiatCustomStructures.conf.getConfigurationSection("Database")!!)
            } else {
                setupDatabase(newFile(getDataFolder(), "data.db"))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            disablePlugin()
            return
        }
    }

    private fun setupDatabase(
        conf: ConfigurationSection,
        table: String = conf.getString("table", "")!!,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
    ) {
        val hostSQL = HostSQL(conf)
        if (clearFlags) {
            hostSQL.flags.clear()
        }
        hostSQL.flags.addAll(flags)
        if (ssl != null) {
            hostSQL.flags -= "useSSL=false"
            hostSQL.flags += "sslMode=$ssl"
        }
        database = Database(TypeSQL(hostSQL, table))
    }

    private fun setupDatabase(file: File = newFile(getDataFolder(), "data.db"), table: String? = null) {
        database = Database(TypeSQLite(file, table))
    }

    /**
     * 将建筑存入数据库.
     * @param loc    建筑生成的位置.
     * @param struct 建筑实例.
     */
    internal fun putSpawnedStructure(loc: Location, struct: Structure) {
        database.add(struct.name, loc)
    }


    /**
     * 从数据库中寻找附近建筑
     * @param request 请求
     */
    internal fun findNearby(request: NearbyStructuresRequest): CompletableFuture<NearbyStructuresResponse> {
        return database.getNearest(request)
    }

    /**
     * 删除特定建筑的记录
     */
    internal fun remove(struct: Structure) {
        database.remove(struct.name)
    }
}