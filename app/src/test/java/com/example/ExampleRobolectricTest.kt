package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.TeleprompterViewModel
import org.junit.Assert.assertEquals
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
    assertEquals("Teleprompter Pro", appName)
  }

  @Test
  fun `calculate word count accurately`() {
    val text = "Welcome to Teleprompter Pro studio editor! Here are ten words."
    val count = TeleprompterViewModel.calculateWordCount(text)
    assertEquals(10, count)
  }

  @Test
  fun `calculate reading time accurately`() {
    val wordCount = 130
    val readTime = TeleprompterViewModel.calculateReadingTime(wordCount, wpm = 130)
    assertEquals("1m 0s", readTime)
  }
}
