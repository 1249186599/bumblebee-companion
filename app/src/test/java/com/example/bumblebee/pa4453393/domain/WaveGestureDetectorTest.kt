package com.example.bumblebee.pa4453393.domain

import com.pico.spatial.core.math.Vector3
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveGestureDetectorTest {
    @Test fun raisedPalmMustSweepTwiceAndThenCooldown() {
        val detector = WaveGestureDetector()
        val head = Vector3(0f, 1.6f, 0f)
        val xs = listOf(0.3f, 0.46f, 0.7f, 0.48f, 0.26f, 0.5f)
        val results = xs.mapIndexed { index, x ->
            detector.observe(head, Vector3(x, 1.55f, -0.25f), index * 150L)
        }
        assertTrue(results.last())
        assertTrue(results.dropLast(1).none { it })
        assertFalse(detector.observe(head, Vector3(0.3f, 1.55f, -0.25f), 900L))
    }

    @Test fun lowHandDoesNotCountAsGreeting() {
        val detector = WaveGestureDetector()
        val head = Vector3(0f, 1.6f, 0f)
        listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.2f, 0.5f).forEachIndexed { index, x ->
            assertFalse(detector.observe(head, Vector3(x, 0.8f, -0.3f), index * 150L))
        }
    }
}
