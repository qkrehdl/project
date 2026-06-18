package com.examflow.app.domain.scheduler

import kotlin.math.floor

class ReviewAllocator {
    fun regularBlocks(totalBlocks: Int): Int = floor(totalBlocks * 0.8).toInt()
    fun reviewBlocks(totalBlocks: Int): Int = totalBlocks - regularBlocks(totalBlocks)
}
