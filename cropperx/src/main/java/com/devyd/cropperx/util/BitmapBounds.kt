package com.devyd.cropperx.util

import kotlin.math.max
import kotlin.math.min

internal object BitmapBounds {

    /** points 배열에서 짝수 인덱스(0,2,4,6…)는 X 좌표, 홀수 인덱스(1,3,5,7…)는 Y 좌표입니다. */

    fun left(points: FloatArray): Float {
        // 짝수 인덱스의 값들 중 최소값
        return points
            .filterIndexed { index, _ -> index % 2 == 0 }
            .minOrNull()
            ?: 0f
    }

    fun top(points: FloatArray): Float {
        // 홀수 인덱스의 값들 중 최소값
        return points
            .filterIndexed { index, _ -> index % 2 == 1 }
            .minOrNull()
            ?: 0f
    }

    fun right(points: FloatArray): Float {
        // 짝수 인덱스의 값들 중 최대값
        return points
            .filterIndexed { index, _ -> index % 2 == 0 }
            .maxOrNull()
            ?: 0f
    }

    fun bottom(points: FloatArray): Float {
        // 홀수 인덱스의 값들 중 최대값
        return points
            .filterIndexed { index, _ -> index % 2 == 1 }
            .maxOrNull()
            ?: 0f
    }

}