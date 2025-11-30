package core.domain

import kotlinx.coroutines.Dispatchers
actual val io: kotlinx.coroutines.CoroutineDispatcher
    get() = Dispatchers.Default.limitedParallelism(100)
actual val dbStorage: core.domain.DbStorage
    get() = TODO("Not yet implemented")