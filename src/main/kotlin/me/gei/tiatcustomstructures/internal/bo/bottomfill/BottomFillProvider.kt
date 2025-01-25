package me.gei.tiatcustomstructures.internal.bo.bottomfill

/**
 * 底部填充管理.
 */
object BottomFillProvider {
    private val providers = HashMap<String, BottomFillImpl>()

    /**
     * 注册实现.
     */
    fun addImplementation(id: String, bottomFill: BottomFillImpl) {
        providers[id] = bottomFill
    }

    /**
     * 获取实现.
     * 目前只获取默认实现
     */
    fun provideDefault(): BottomFillImpl {
        return DefaultBottomFill()
    }

    /**
     * 获取实现
     *
     * @return 如果存在则获取实现，不存在则返回默认实现.
     */
    fun provide(id: String): BottomFillImpl? {
        if (providers.containsKey(id)) {
            return providers[id]
        }

        return DefaultBottomFill()
    }
}
