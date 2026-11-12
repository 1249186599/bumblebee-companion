package com.example.bumblebee.pa4453393.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionLogicTest {
    @Test fun followsOnlyAfterDistanceThresholdAndStopsNearby() {
        val game = CompanionLogic()
        assertEquals(Motion.STANDING, game.tick(0f, 1f, 0.03f))
        game.toggleFollow()
        assertEquals(Motion.STANDING, game.tick(0f, 0f, 0.03f))
        assertEquals(Motion.WALKING, game.tick(0f, 1f, 0.03f))
        repeat(200) { game.tick(0f, 1f, 0.1f) }
        assertEquals(Motion.STANDING, game.motion)
        assertTrue(kotlin.math.abs(game.z - (-0.5f)) < 0.01f)
    }

    @Test fun walksBeforeRunningAndStartsGradually() {
        val game = CompanionLogic()
        game.toggleFollow()
        assertEquals(Motion.WALKING, game.tick(0f, 1f, 0.1f))
        assertTrue(game.z < -2f + 0.02f)
        assertEquals(Motion.RUNNING, game.tick(0f, 4f, 0.1f))
        assertEquals(Motion.WALKING, game.tick(0f, 1f, 0.1f))
    }

    @Test fun heartPausesMotionAndKeepsToggleForAfterward() {
        val game = CompanionLogic()
        game.toggleFollow()
        game.tick(0f, 1f, 0.03f)
        assertTrue(game.heart())
        assertFalse(game.heart())
        val position = game.z
        game.toggleFollow()
        game.tick(0f, 3f, 0.1f)
        assertEquals(Motion.HEART, game.motion)
        assertEquals(position, game.z)
        game.heartFinished()
        assertEquals(Motion.STANDING, game.tick(0f, 3f, 0.1f))
        assertFalse(game.followEnabled)
    }

    @Test fun avatarMakesRoomWhenHeadApproaches() {
        val game = CompanionLogic()
        game.tick(0f, -1.6f, 0.03f)
        assertTrue(game.avoidingUser)
        assertTrue(kotlin.math.hypot(game.x, game.z + 1.6f) >= 0.849f)
        assertEquals(Motion.STANDING, game.motion)
    }

    @Test fun greetingPausesFollowingAndFinishesOnce() {
        val game = CompanionLogic()
        game.toggleFollow()
        game.tick(0f, 1f, 0.1f)
        assertTrue(game.greet())
        assertFalse(game.greet())
        val position = game.z
        game.tick(0f, 3f, 0.1f)
        assertEquals(Motion.GREETING, game.motion)
        assertEquals(position, game.z)
        game.greetFinished()
        assertEquals(Motion.WALKING, game.tick(0f, 3f, 0.1f))
    }
}
