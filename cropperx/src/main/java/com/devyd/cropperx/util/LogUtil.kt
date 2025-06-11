package com.devyd.cropperx.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log

object LogUtil {

    fun logMemoryStats(context: Context? = null) {
        // 1. Java 힙
        val runtime = Runtime.getRuntime()
        val maxHeap = runtime.maxMemory()       // JVM에 허용된 최대 힙 (bytes)
        val totalHeap = runtime.totalMemory()     // 현재 할당된 힙 크기 (bytes)
        val freeHeap = runtime.freeMemory()      // 할당된 힙 중 사용되지 않은 부분 (bytes)
        val availHeap = maxHeap - totalHeap + freeHeap
        Log.i(
            "Deok",
            "oom test Java heap → max=${maxHeap / 1024 / 1024}MB, total=${totalHeap / 1024 / 1024}MB, free=${freeHeap / 1024 / 1024}MB, avail=${availHeap / 1024 / 1024}MB"
        )

        // 2. Native 힙 (비트맵 등 네이티브 할당 대상)
        val nativeSize = Debug.getNativeHeapSize()         // 전체 네이티브 힙(예약) 크기
        val nativeAlloc = Debug.getNativeHeapAllocatedSize()// 이미 사용 중인 네이티브 바이트
        val nativeFree = Debug.getNativeHeapFreeSize()     // 남은 네이티브 바이트
        Log.i(
            "Deok",
            "oom test Native heap → size=${nativeSize / 1024 / 1024}MB, alloc=${nativeAlloc / 1024 / 1024}MB, free=${nativeFree / 1024 / 1024}MB"
        )

        // 3. 앱 메모리 클래스 & 시스템 메모리 상태
        if (context != null) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memClass = am.memoryClass            // 기본 Java 힙 한도 (MB)
            val largeMemClass = am.largeMemoryClass       // largeHeap=true 시 (MB)
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)

            Log.i("Deok", "oom test MemoryClass → normal=${memClass}MB, large=${largeMemClass}MB")
            Log.i(
                "Deok",
                "oom test System Memory → avail=${memInfo.availMem / 1024 / 1024}MB, lowMemory=${memInfo.lowMemory}, threshold=${memInfo.threshold / 1024 / 1024}MB"
            )
        }
    }
}