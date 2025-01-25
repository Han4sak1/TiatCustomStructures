package me.gei.tiatcustomstructures.internal.utils

import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import me.gei.tiatcustomstructures.internal.bo.structure.properties.StructureLocation
import org.bukkit.Chunk
import org.bukkit.block.Block
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 建筑生成优先队列.
 */
class PriorityStructureQueue(structures: List<Structure>, block: Block, chunk: Chunk) {
    private var priorityStructureSet: TreeSet<PriorityStructure>? = null

    /**
     * 检查队列是否初始化完成
     * 在获取内容前必须保证初始化完成
     */
    var isInitialised: Boolean = false
        private set

    /**
     * 创建优先队列.
     * canSpawn 为true的建筑将被添加到队列后被生成
     */
    init {
        val tempSet = TreeSet<PriorityStructure>()
        val futureList: MutableList<CompletableFuture<*>> = ArrayList()

        structures.forEach { structure ->
            val structureSpawnSettings: StructureLocation.StructureYSpawning = structure.structureLocation.spawnSettings

            // Get the highest block according to the settings for the structure.
            val structureBlock: Block = structureSpawnSettings.getHighestBlock(block.location)

            //异步初始化
            val canSpawnFuture: CompletableFuture<Boolean> = structure.canSpawn(structureBlock, chunk)
            futureList.add(canSpawnFuture)
            canSpawnFuture
                .thenAccept { response: Boolean ->
                    if (response) synchronized(tempSet) {
                        tempSet.add(PriorityStructure(structure))
                    }
                }
        }

        val allFutures = CompletableFuture.allOf(*futureList.toTypedArray<CompletableFuture<*>>())
        allFutures.thenRun {
            priorityStructureSet = tempSet
            isInitialised = true
        }
    }

    /**
     * 是否还有建筑
     */
    fun hasNextStructure(): Boolean {
        if (priorityStructureSet == null) return false
        return !priorityStructureSet!!.isEmpty()
    }

    val nextStructure: Structure?
        /**
         * 获取队列头的建筑
         */
        get() {
            if (priorityStructureSet == null) return null
            if (priorityStructureSet!!.isEmpty()) {
                return null
            }

            return priorityStructureSet!!.pollFirst()!!.getStructure()
        }

    /**
     * 用于队列排序.
     * Note: 此排序较为特殊, 故单独实现compareTo方法
     */
    private class PriorityStructure(private val structure: Structure) : Comparable<PriorityStructure> {
        private val probability = structure.probabilityNumerator.toDouble() / structure.probabilityDenominator.toDouble()

        /**
         * 获取建筑实例.
         */
        fun getStructure(): Structure {
            return structure
        }

        override fun compareTo(other: PriorityStructure): Int {
            if (structure.priority == other.structure.priority) {
                return if (probability < other.probability) -1 else 1
            }
            return if (structure.priority < other.structure.priority) -1 else 1
        }
    }
}
