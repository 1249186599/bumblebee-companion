package com.example.bumblebee.pa4453393.domain

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.max

enum class Motion { STANDING, WALKING, RUNNING, HEART, GREETING }

/** Stage coordinates are metres; +Z points back toward the user at entry. */
class CompanionLogic {
    var x = 0f
        private set
    var z = -2f
        private set
    var yawDegrees = 0f
        private set
    var followEnabled = false
        private set
    var motion = Motion.STANDING
        private set
    private var speed = 0f
    var avoidingUser = false
        private set

    fun toggleFollow() {
        followEnabled = !followEnabled
        if (!followEnabled && motion != Motion.HEART && motion != Motion.GREETING) {
            motion = Motion.STANDING
            speed = 0f
        }
    }

    fun heart(): Boolean {
        if (motion == Motion.HEART || motion == Motion.GREETING) return false
        motion = Motion.HEART
        speed = 0f
        return true
    }

    fun heartFinished() {
        if (motion == Motion.HEART) motion = Motion.STANDING
    }

    fun greet(): Boolean {
        if (motion == Motion.HEART || motion == Motion.GREETING) return false
        motion = Motion.GREETING
        speed = 0f
        return true
    }

    fun greetFinished() {
        if (motion == Motion.GREETING) motion = Motion.STANDING
    }

    fun tick(userX: Float, userZ: Float, seconds: Float): Motion {
        val separation = hypot(userX - x, userZ - z)
        avoidingUser = separation < 0.85f
        if (avoidingUser) {
            // Real people are not physics bodies. Keep the avatar outside the head's space.
            val dx = x - userX
            val dz = z - userZ
            val safeDistance = max(separation, 0.001f)
            val awayX = if (separation < 0.001f) 0f else dx / safeDistance
            val awayZ = if (separation < 0.001f) -1f else dz / safeDistance
            x = userX + awayX * 0.85f
            z = userZ + awayZ * 0.85f
            speed = 0f
            if (motion != Motion.HEART && motion != Motion.GREETING) motion = Motion.STANDING
        }
        if (motion == Motion.HEART || motion == Motion.GREETING) return motion
        val distance = hypot(userX - x, userZ - z)
        if (!followEnabled) {
            motion = Motion.STANDING
            speed = 0f
        } else if (distance <= 1.5f) {
            motion = Motion.STANDING
            speed = 0f
        } else if (motion == Motion.STANDING && distance > 2.5f) {
            motion = Motion.WALKING
        }
        if (motion == Motion.WALKING || motion == Motion.RUNNING) {
            motion = if (distance > 5f || (motion == Motion.RUNNING && distance > 4f)) Motion.RUNNING else Motion.WALKING
            face(userX, userZ)
            val dt = seconds.coerceIn(0f, 0.1f)
            val remaining = (distance - 1.5f).coerceAtLeast(0f)
            val target = min(if (motion == Motion.RUNNING) 1.2f else 0.7f, sqrt(2f * 0.9f * remaining))
            speed = (speed + (target - speed).coerceIn(-0.9f * dt, 0.9f * dt)).coerceAtLeast(0f)
            val step = min(speed * dt, remaining)
            x += (userX - x) / distance * step
            z += (userZ - z) / distance * step
            if (hypot(userX - x, userZ - z) <= 1.5f) {
                motion = Motion.STANDING
                speed = 0f
            }
        }
        return motion
    }

    fun face(userX: Float, userZ: Float) {
        yawDegrees = Math.toDegrees(atan2((userX - x).toDouble(), (userZ - z).toDouble())).toFloat()
    }
}
