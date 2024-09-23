package com.hfad.soundsavev1

import org.junit.Test
import android.media.MediaRecorder
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import kotlin.math.log10

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMeasureDecibelLevel_zeroAmplitude() {
        val mainActivity = MainActivity() // Create an instance of your activity
        // We can't directly set maxAmplitude, so we test the case when it would be 0
        val decibelLevel = mainActivity.measureDecibelLevel()
        assertEquals(0, decibelLevel)
    }

    @Test
    fun testMeasureDecibelLevel_positiveAmplitude() {
        val mainActivity = MainActivity()
        val mockMediaRecorder = mock(MediaRecorder::class.java)
        `when`(mockMediaRecorder.maxAmplitude).thenReturn(100)

        // Using reflection to set the mock MediaRecorder (not ideal in production)
        val mediaRecorderField = MainActivity::class.java.getDeclaredField("mediaRecorder")
        mediaRecorderField.isAccessible = true
        mediaRecorderField.set(mainActivity, mockMediaRecorder)

        val expectedDecibelLevel = (20 * log10(100.0)).toInt()
        val actualDecibelLevel = mainActivity.measureDecibelLevel()
        assertEquals(expectedDecibelLevel, actualDecibelLevel)
    }

    private lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        mainActivity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
    }

    @Test
    fun testUpdateTrafficLight_recommendationsDisplayed() {
        // Test for green light recommendation
        mainActivity.updateTrafficLight(50)
        assertEquals(mainActivity.getString(R.string.green_light), mainActivity.binding.recommendations.text)

        // Test for yellow light recommendation
        mainActivity.updateTrafficLight(75)
        assertEquals(mainActivity.getString(R.string.yellow_light), mainActivity.binding.recommendations.text)

        // Test for red light recommendation
        mainActivity.updateTrafficLight(100)
        assertEquals(mainActivity.getString(R.string.red_light), mainActivity.binding.recommendations.text)
    }

   
}