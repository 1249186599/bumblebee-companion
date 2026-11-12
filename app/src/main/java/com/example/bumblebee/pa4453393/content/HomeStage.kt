package com.example.bumblebee.pa4453393.content

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bumblebee.pa4453393.R
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.tracking.controller.ControllerTrackingProvider
import com.pico.spatial.tracking.hmd.HMDTrackingProvider
import com.pico.spatial.tracking.hand.HandTrackingProvider
import com.pico.spatial.tracking.hand.HandJoint
import com.example.bumblebee.pa4453393.domain.WaveGestureDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val CUE_ID = "bumblebee-cue"
private val mechaShape = RoundedCornerShape(16.dp)
private val mechaPanel = Color(0xFF15191D) // design-style: fixed-figma-color Bumblebee mech visual reference
private val mechaYellow = Color(0xFFFFC928) // design-style: fixed-figma-color Bumblebee mech visual reference
private val mechaWhite = Color(0xFFF5F3E9) // design-style: fixed-figma-color Bumblebee mech visual reference
private val mechaMuted = Color(0xFFB7BDC2) // design-style: fixed-figma-color Bumblebee mech visual reference
private val mechaSlot = Color(0xFF25231F) // design-style: fixed-figma-color Bumblebee mech visual reference

@Composable
fun HomeStage() {
    val game = remember { CompanionRuntime() }
    val hmd = remember { HMDTrackingProvider() }
    val controllers = remember { ControllerTrackingProvider() }
    val hands = remember { HandTrackingProvider() }
    val main = remember { Handler(Looper.getMainLooper()) }
    val leftWave = remember { WaveGestureDetector() }
    val rightWave = remember { WaveGestureDetector() }

    DisposableEffect(game, hmd, controllers, hands) {
        var wasA = false
        var wasX = false
        val listener = ControllerTrackingProvider.ControllerActionListener { action ->
            val a = action.right.aButtonPressed
            val x = action.left.xButtonPressed
            val aEdge = a && !wasA
            val xEdge = x && !wasX
            wasA = a
            wasX = x
            if (aEdge || xEdge) main.post {
                if (aEdge) game.toggleFollow()
                if (xEdge) {
                    val p = hmd.latestData?.hmdPose?.position
                    game.heart(p?.x ?: 0f, p?.z ?: 0f)
                }
            }
        }
        controllers.addControllerActionListener(listener)
        onDispose {
            controllers.removeControllerActionListener(listener)
            controllers.stop()
            hands.stop()
            hmd.stop()
            game.close()
        }
    }

    LaunchedEffect(game.ready) {
        if (game.ready) {
            val hmdResult = hmd.start()
            val controllerResult = controllers.start()
            val handResult = hands.start()
            Log.i("Bumblebee", "Tracking start: hmd=$hmdResult/${hmd.supportState}/${hmd.state}, controller=$controllerResult/${controllers.supportState}/${controllers.state}, hand=$handResult/${hands.supportState}/${hands.state}")
        }
    }

    LaunchedEffect(game, hmd, hands) {
        var last = SystemClock.elapsedRealtimeNanos()
        var loggedHead = false
        while (isActive) {
            val now = SystemClock.elapsedRealtimeNanos()
            val seconds = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.1f)
            last = now
            val p = hmd.latestData?.hmdPose?.position
            if (p != null) {
                if (!loggedHead) {
                    Log.i("Bumblebee", "Initial HMD position: x=${p.x}, y=${p.y}, z=${p.z}")
                    loggedHead = true
                }
                game.tick(p.x, p.z, seconds)
            }
            val handData = hands.latestData
            val head = hmd.latestData?.hmdPose?.position
            if (head != null && handData != null) {
                val leftPalm = handData.left?.get(HandJoint.Index.PALM)?.position
                val rightPalm = handData.right?.get(HandJoint.Index.PALM)?.position
                val millis = SystemClock.elapsedRealtime()
                if (leftWave.observe(head, leftPalm, millis) || rightWave.observe(head, rightPalm, millis)) {
                    game.greet(head.x, head.z)
                }
            }
            game.updateHandTouch(
                handData?.left?.get(HandJoint.Index.INDEX_TIP)?.position,
                handData?.right?.get(HandJoint.Index.INDEX_TIP)?.position,
            )
            delay(33)
        }
    }

    SpatialView(
        modifier = Modifier.fillMaxSize(),
        attachments = {
            AttachmentPanel(id = CUE_ID) {
                Box(
                    modifier = Modifier.width(320.dp).clip(RoundedCornerShape(28.dp)).background(mechaPanel),
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        drawRoundRect(mechaYellow.copy(alpha = 0.75f), cornerRadius = CornerRadius(28.dp.toPx()), style = Stroke(2.dp.toPx()))
                        drawLine(mechaYellow, Offset(32.dp.toPx(), 3.dp.toPx()), Offset(size.width * 0.38f, 3.dp.toPx()), 3.dp.toPx())
                        drawLine(mechaYellow, Offset(size.width * 0.72f, size.height - 3.dp.toPx()), Offset(size.width - 32.dp.toPx(), size.height - 3.dp.toPx()), 3.dp.toPx())
                    }
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.width(170.dp)) {
                                Text("大黄蜂", color = mechaYellow, fontSize = 30.sp)
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(mechaSlot).padding(horizontal = 12.dp, vertical = 6.dp),
                                ) { Text("●  ${game.status}", color = mechaWhite) }
                            }
                            Image(
                                painter = painterResource(R.drawable.bumblebee_portrait),
                                contentDescription = null,
                                modifier = Modifier.size(90.dp),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MechaAction("Ⓐ  跟随", active = true, enabled = game.ready, modifier = Modifier.weight(1f)) {
                                game.toggleFollow()
                            }
                            MechaAction("Ⓧ  比心", active = false, enabled = game.ready, modifier = Modifier.weight(1f)) {
                                val p = hmd.latestData?.hmdPose?.position
                                game.heart(p?.x ?: 0f, p?.z ?: 0f)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MechaSlot("挥手", "鞠躬回应", Modifier.weight(1f))
                            MechaSlot("＋", "", Modifier.weight(1f))
                            MechaSlot("＋", "", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { MechaSlot("＋", "", Modifier.weight(1f)) }
                        }
                    }
                }
            }
        },
        initial = { content, attachments ->
            try {
                val pivot = Entity()
                content.addEntity(pivot)
                val model = Entity.loadSuspend("asset://model/bumblebee.glb")
                pivot.addChild(model)
                val cue = attachments.entity(CUE_ID)
                if (cue != null) pivot.addChild(cue)
                game.attach(pivot, model, cue)
            } catch (cause: Throwable) {
                game.fail(cause)
            }
        },
    )
}

@Composable
private fun MechaAction(label: String, active: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val fill = if (active) mechaYellow else mechaSlot
    Box(
        modifier = modifier.height(42.dp).clip(RoundedCornerShape(50)).background(fill)
            .spatialHoverEffect().clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (active) mechaPanel else mechaWhite) }
}

@Composable
private fun MechaSlot(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.height(50.dp).clip(RoundedCornerShape(12.dp)).background(mechaSlot).padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = if (subtitle.isEmpty()) mechaMuted else mechaYellow)
        if (subtitle.isNotEmpty()) Text(subtitle, color = mechaWhite)
    }
}
