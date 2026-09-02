package com.example

import com.example.ui.motion.FrontierGameMotion
import com.example.ui.motion.MotionDuration
import com.example.ui.motion.MotionEasing
import com.example.ui.motion.MotionSpeed
import com.example.ui.motion.ResourceDeltaCoalescer
import com.example.ui.motion.VisualNotification
import com.example.ui.motion.VisualNotificationController
import com.example.ui.motion.VisualNotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Motion & Visual Effects System (Point 36).
 * Ensures mathematical correctness, duration scaling, accessibility reduced motion,
 * notification queue invariants, and delta coalescing.
 */
class MotionSystemTest {

    @Test
    fun `reduced motion sets all durations to zero`() {
        val reducedMotion = FrontierGameMotion(speed = MotionSpeed.REDUCED_MOTION)

        assertEquals(0, reducedMotion.instant)
        assertEquals(0, reducedMotion.quick)
        assertEquals(0, reducedMotion.standard)
        assertEquals(0, reducedMotion.emphasis)
        assertEquals(0, reducedMotion.ambient)
    }

    @Test
    fun `normal motion has expected base durations`() {
        val normalMotion = FrontierGameMotion(speed = MotionSpeed.NORMAL)

        assertEquals(MotionDuration.Instant, normalMotion.instant)
        assertEquals(MotionDuration.Quick, normalMotion.quick)
        assertEquals(MotionDuration.Standard, normalMotion.standard)
        assertEquals(MotionDuration.Emphasis, normalMotion.emphasis)
        assertEquals(MotionDuration.Ambient, normalMotion.ambient)
    }

    @Test
    fun `fast motion halves duration values`() {
        val fastMotion = FrontierGameMotion(speed = MotionSpeed.FAST)

        assertEquals(MotionDuration.Quick / 2, fastMotion.quick)
        assertEquals(MotionDuration.Standard / 2, fastMotion.standard)
        assertEquals(MotionDuration.Emphasis / 2, fastMotion.emphasis)
    }

    @Test
    fun `slow motion doubles duration values`() {
        val slowMotion = FrontierGameMotion(speed = MotionSpeed.SLOW)

        assertEquals(MotionDuration.Quick * 2, slowMotion.quick)
        assertEquals(MotionDuration.Standard * 2, slowMotion.standard)
        assertEquals(MotionDuration.Emphasis * 2, slowMotion.emphasis)
    }

    @Test
    fun `easing curves and tween specifications are non-null and valid`() {
        val motion = FrontierGameMotion()
        val standardSpec = motion.standardTween<Float>()
        val quickSpec = motion.quickTween<Float>()
        val emphasisSpec = motion.emphasisTween<Float>()

        assertNotNull(standardSpec)
        assertNotNull(quickSpec)
        assertNotNull(emphasisSpec)
        assertNotNull(MotionEasing.Standard)
        assertNotNull(MotionEasing.Enter)
        assertNotNull(MotionEasing.Exit)
        assertNotNull(MotionEasing.Emphasis)
    }

    @Test
    fun `resource delta coalescer accumulates rapid delta updates`() {
        val coalescer = ResourceDeltaCoalescer()

        val delta1 = coalescer.processUpdate(100, 115)
        assertEquals(15, delta1)
        assertEquals(15, coalescer.accumulatedDelta)

        val delta2 = coalescer.processUpdate(115, 125)
        assertEquals(25, delta2)
        assertEquals(25, coalescer.accumulatedDelta)

        val delta3 = coalescer.processUpdate(125, 120)
        assertEquals(20, delta3)
        assertEquals(20, coalescer.accumulatedDelta)

        coalescer.reset()
        assertEquals(0, coalescer.accumulatedDelta)
    }

    @Test
    fun `visual notification controller queues and advances notifications in FIFO order`() {
        val controller = VisualNotificationController(maxQueueSize = 3)
        assertNull(controller.currentNotification)

        val notif1 = VisualNotification(title = "First", message = "One", type = VisualNotificationType.INFO)
        val notif2 = VisualNotification(title = "Second", message = "Two", type = VisualNotificationType.SUCCESS)
        val notif3 = VisualNotification(title = "Third", message = "Three", type = VisualNotificationType.WARNING)

        controller.show(notif1)
        assertEquals("First", controller.currentNotification?.title)
        assertTrue(controller.isQueueEmpty)

        controller.show(notif2)
        controller.show(notif3)
        assertFalse(controller.isQueueEmpty)
        assertEquals(2, controller.queueSize)

        // Dismiss first notification -> second becomes active
        controller.dismissCurrent()
        assertEquals("Second", controller.currentNotification?.title)
        assertEquals(1, controller.queueSize)

        // Dismiss second notification -> third becomes active
        controller.dismissCurrent()
        assertEquals("Third", controller.currentNotification?.title)
        assertEquals(0, controller.queueSize)
        assertTrue(controller.isQueueEmpty)

        // Dismiss third notification -> queue empty, active is null
        controller.dismissCurrent()
        assertNull(controller.currentNotification)
    }

    @Test
    fun `visual notification controller clear removes active and queued items`() {
        val controller = VisualNotificationController()
        controller.show(VisualNotification(title = "N1", message = "1"))
        controller.show(VisualNotification(title = "N2", message = "2"))

        assertNotNull(controller.currentNotification)
        assertEquals(1, controller.queueSize)

        controller.clear()
        assertNull(controller.currentNotification)
        assertEquals(0, controller.queueSize)
        assertTrue(controller.isQueueEmpty)
    }
}
