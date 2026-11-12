package com.example.bumblebee.pa4453393.content

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.bumblebee.pa4453393.domain.CompanionLogic
import com.example.bumblebee.pa4453393.domain.Motion
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.animation.AnimationPlaybackController
import com.pico.spatial.core.ecs.resource.AnimationResource
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3

class CompanionRuntime {
    val logic = CompanionLogic()
    var status by mutableStateOf("正在准备伙伴")
        private set
    var ready by mutableStateOf(false)
        private set

    private var pivot: Entity? = null
    private var cue: Entity? = null
    private var skinned: Entity? = null
    private var clips = emptyMap<String, AnimationResource>()
    private var heartOnce: AnimationResource? = null
    private var waveOnce: AnimationResource? = null
    private var playback: AnimationPlaybackController? = null
    private var playedMotion: Motion? = null
    private var touching = false

    fun attach(pivotEntity: Entity, model: Entity, cueEntity: Entity?) {
        pivot = pivotEntity
        cue = cueEntity
        cueEntity?.components?.get(TransformComponent::class.java)?.setScaleVector(Vector3(3.5f, 3.5f, 3.5f))
        val bounds = model.getVisualBounds(pivotEntity)
        val height = bounds.size.y
        require(height > 0.01f) { "Model has no visual height" }
        val scale = 1.5f / height
        Log.i("Bumblebee", "Model bounds: height=$height, scale=$scale, initial position=(0,0,-2)")
        model.components[TransformComponent::class.java]?.apply {
            setScaleVector(Vector3(scale, scale, scale))
            setPosition(Vector3(-bounds.center.x * scale, -bounds.min.y * scale, -bounds.center.z * scale))
        }
        val mesh = model.findSkinnedMeshEntity().firstOrNull()
            ?: error("No skinned mesh in Bumblebee GLB")
        skinned = mesh
        clips = mesh.getAnimationResources().associateBy { it.getName() }
        require(listOf("standing_relax.001", "swagger.001", "run.001", "heart_pose.001", "bow.001").all { it in clips }) {
            "Required Bumblebee animation missing: ${clips.keys}"
        }
        heartOnce = clips.getValue("heart_pose.001").repeat(0)
        waveOnce = clips.getValue("bow.001").repeat(0)
        ready = true
        status = "跟随关闭"
        syncAnimation()
        syncTransform()
    }

    fun fail(cause: Throwable) {
        Log.e("Bumblebee", "Model initialization failed", cause)
        ready = false
        status = "伙伴加载失败，请重新进入"
    }

    fun toggleFollow() {
        if (!ready) return
        logic.toggleFollow()
        updateStatus()
        syncAnimation()
    }

    fun heart(userX: Float, userZ: Float) {
        if (!ready || !logic.heart()) return
        logic.face(userX, userZ)
        syncTransform()
        updateStatus()
        syncAnimation()
    }

    fun greet(userX: Float, userZ: Float) {
        if (!ready || !logic.greet()) return
        logic.face(userX, userZ)
        syncTransform()
        updateStatus()
        syncAnimation()
    }

    fun tick(userX: Float, userZ: Float, seconds: Float) {
        if (!ready) return
        val finishedHeart = logic.motion == Motion.HEART && playback?.isComplete() == true
        val finishedGreeting = logic.motion == Motion.GREETING && playback?.isComplete() == true
        if (finishedHeart) {
            logic.heartFinished()
        }
        if (finishedGreeting) logic.greetFinished()
        val before = logic.motion
        val wasAvoiding = logic.avoidingUser
        logic.tick(userX, userZ, seconds)
        syncTransform()
        if (wasAvoiding != logic.avoidingUser) Log.i("Bumblebee", "Head clearance active=${logic.avoidingUser}")
        if (finishedHeart || finishedGreeting || before != logic.motion) {
            updateStatus()
            syncAnimation()
        }
    }

    fun updateHandTouch(left: Vector3?, right: Vector3?) {
        val root = pivot ?: return
        fun touches(point: Vector3?): Boolean {
            if (point == null) return false
            val local = root.convertPositionFrom(point, null)
            return local.y in 0.45f..1.55f && kotlin.math.abs(local.x) < 0.48f && kotlin.math.abs(local.z) < 0.42f
        }
        val nowTouching = touches(left) || touches(right)
        if (nowTouching != touching) {
            touching = nowTouching
            Log.i("Bumblebee", "Hand touch active=$touching")
            updateStatus()
        }
    }

    private fun syncTransform() {
        pivot?.components?.get(TransformComponent::class.java)?.apply {
            setPosition(Vector3(logic.x, 0f, logic.z))
            setEulerAngles(EulerAngles(0f, logic.yawDegrees, 0f))
        }
        cue?.components?.get(TransformComponent::class.java)?.setPosition(Vector3(1.18f, 1.12f, 0f))
    }

    private fun syncAnimation() {
        if (!ready || playedMotion == logic.motion) return
        playback?.stop()
        playback?.close()
        val clip = when (logic.motion) {
            Motion.STANDING -> clips["standing_relax.001"]
            Motion.WALKING -> clips["swagger.001"]
            Motion.RUNNING -> clips["run.001"]
            Motion.HEART -> heartOnce
            Motion.GREETING -> waveOnce
        }
        playback = clip?.let { skinned?.playAnimation(it) }
        Log.i("Bumblebee", "Motion changed to ${logic.motion}")
        playedMotion = logic.motion
    }

    private fun updateStatus() {
        if (touching && logic.motion != Motion.GREETING && logic.motion != Motion.HEART) {
            status = "摸到大黄蜂啦"
            return
        }
        status = when (logic.motion) {
            Motion.HEART -> if (logic.followEnabled) "比心中 · 结束后继续跟随" else "比心中 · 结束后留在原地"
            Motion.GREETING -> "大黄蜂向你鞠躬"
            Motion.RUNNING -> "正在跟随"
            Motion.WALKING -> "正在慢走跟随"
            Motion.STANDING -> if (logic.followEnabled) "跟随开启" else "跟随关闭"
        }
    }

    fun close() {
        playback?.stop()
        playback?.close()
        playback = null
        heartOnce?.close()
        heartOnce = null
        waveOnce?.close()
        waveOnce = null
        pivot?.destroy(true)
        pivot = null
    }
}
