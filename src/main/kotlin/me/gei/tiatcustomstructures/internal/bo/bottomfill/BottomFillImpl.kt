package me.gei.tiatcustomstructures.internal.bo.bottomfill

import com.sk89q.worldedit.math.transform.AffineTransform
import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location

/**
 * 建筑底部填充实现.
 * 使用 [BottomFillProvider] 获得实现/注册新实现
 */
interface BottomFillImpl {
    /**
     * 建筑生成底部填充时触发.
     * 当底部填充开启时才会触发.
     *
     * @param structure     建筑.
     * @param spawnLocation 生成位置.
     * @param minLoc        建筑最低点.
     * @param maxLoc        建筑最高点.
     * @param transform     对建筑的变换，用于在填充时操作向量使用.
     */
    fun performFill(
        structure: Structure,
        spawnLocation: Location,
        minLoc: Location,
        maxLoc: Location?,
        transform: AffineTransform
    )
}