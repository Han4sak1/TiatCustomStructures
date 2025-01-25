package me.gei.tiatcustomstructures.internal.managers

import me.gei.tiatcustomstructures.api.ignoreblocks.IgnoreBlocks
import me.gei.tiatcustomstructures.api.ignoreblocks.impl.IgnoreBlocks12R1

object BlockIgnoreManager {

    lateinit var ignoreBlocks: IgnoreBlocks
        private set

    fun load() {
        ignoreBlocks = IgnoreBlocks12R1()
    }
}