package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.api.GeminiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Desfrangando", appName)
  }

  @Test
  fun `test main activity launch`() {
    try {
      val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
      val activity = controller.get()
      assertNotNull(activity)
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
  }
}


