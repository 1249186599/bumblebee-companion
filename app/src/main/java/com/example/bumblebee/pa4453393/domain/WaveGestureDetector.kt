package com.example.bumblebee.pa4453393.domain

import com.pico.spatial.core.math.Vector3
import kotlin.math.hypot

/** Two deliberate side-to-side sweeps of a raised palm within 2.8 seconds. */
class WaveGestureDetector {
    private var originX: Float? = null
    private var originZ = 0f
    private var axisX = 0f
    private var axisZ = 0f
    private var direction = 1
    private var extreme = 0f
    private var reversals = 0
    private var startedAt = 0L
    private var cooldownUntil = 0L
    private var lastSeenAt = 0L

    fun observe(head: Vector3, palm: Vector3?, nowMs: Long): Boolean {
        if (nowMs < cooldownUntil) return false
        if (palm == null) {
            if (nowMs - lastSeenAt > 220L) reset()
            return false
        }
        lastSeenAt = nowMs
        if (palm.y - head.y !in -0.65f..0.35f) {
            reset()
            return false
        }
        val x = palm.x - head.x
        val z = palm.z - head.z
        if (hypot(x, z) !in 0.15f..1.1f) {
            reset()
            return false
        }
        val start = originX
        if (start == null || nowMs - startedAt > 2800L) {
            reset()
            originX = x
            originZ = z
            startedAt = nowMs
            return false
        }
        if (axisX == 0f && axisZ == 0f) {
            val dx = x - start
            val dz = z - originZ
            val distance = hypot(dx, dz)
            if (distance < 0.08f) return false
            axisX = dx / distance
            axisZ = dz / distance
            extreme = distance
            return false
        }
        val value = (x - start) * axisX + (z - originZ) * axisZ
        if (direction > 0) {
            if (value > extreme) extreme = value
            else if (extreme - value >= 0.1f) {
                direction = -1
                extreme = value
                reversals++
            }
        } else {
            if (value < extreme) extreme = value
            else if (value - extreme >= 0.1f) {
                direction = 1
                extreme = value
                reversals++
            }
        }
        if (reversals < 2) return false
        reset()
        cooldownUntil = nowMs + 4000L
        return true
    }

    private fun reset() {
        originX = null
        axisX = 0f
        axisZ = 0f
        direction = 1
        extreme = 0f
        reversals = 0
    }
}
