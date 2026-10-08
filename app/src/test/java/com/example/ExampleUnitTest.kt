package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun localization_defaultThai() {
    val thaiNewChat = com.example.i18n.I18n.get("chat.newChat", com.example.i18n.Language.TH)
    assertEquals("แชทใหม่", thaiNewChat)

    val enNewChat = com.example.i18n.I18n.get("chat.newChat", com.example.i18n.Language.EN)
    assertEquals("New Chat", enNewChat)
  }
}
