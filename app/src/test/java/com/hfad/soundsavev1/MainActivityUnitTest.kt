package com.hfad.soundsavev1

import androidx.lifecycle.get

import com.hfad.soundsavev1.MainActivity
import com.hfad.soundsavev1.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityUnitTest {
    // ... (your existing test methods)

    private lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        mainActivity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
    }

    @Test
    fun testUpdateTrafficLight_recommendationsDisplayed() {
        // Test for green light recommendation
        mainActivity.updateTrafficLight(50)
        assertEquals(
            mainActivity.getString(R.string.green_light),
            mainActivity.binding.recommendations.text
        )

        // Test for yellow light recommendation
        mainActivity.updateTrafficLight(75)
        assertEquals(
            mainActivity.getString(R.string.yellow_light),
            mainActivity.binding.recommendations.text
        )

        // Test for red light recommendation
        mainActivity.updateTrafficLight(100)
        assertEquals(
            mainActivity.getString(R.string.red_light),
            mainActivity.binding.recommendations.text
        )
    }
}