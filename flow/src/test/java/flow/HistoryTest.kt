/*
 * Copyright 2016 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flow

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail.fail
import org.junit.Test

class HistoryTest {

  @Test
  fun builderCanPushPeekAndPopObjects() {
    try {
      history {
        val objects = listOf(ABLE, BAKER, CHARLIE)
        objects.forEach { push(it) }
        for (obj in objects.reversed()) {
          assertThat(peek()).isSameAs(obj)
          assertThat(pop()).isSameAs(obj)
        }
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun builderCanPopTo() {
    history {
      push(ABLE)
      push(BAKER)
      push(CHARLIE)
      popTo(ABLE)
      assertThat(peek()).isSameAs(ABLE)
    }
  }

  @Test
  fun builderPopToExplodesOnMissingState() {
    try {
      history {
        push(ABLE)
        push(BAKER)
        push(CHARLIE)
        try {
          popTo(Any())
          fail("Missing state object, should have thrown")
        } catch (ignored: IllegalArgumentException) {
          // Correct!
        }
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun builderCanPopCount() {
    try {
      history {
        push(ABLE)
        push(BAKER)
        push(CHARLIE)
        pop(1)
        assertThat(peek()).isSameAs(BAKER)
        pop(2)
        assertThat(isEmpty)
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun builderPopExplodesIfCountIsTooLarge() {
    history {
      push(ABLE)
      push(BAKER)
      push(CHARLIE)
      try {
        pop(4)
        fail("Count is too large, should have thrown")
      } catch (ignored: IllegalArgumentException) {
        // Success!
      }
    }
  }

  @Test
  fun framesFromBottom() {
    val paths = listOf(ABLE, BAKER, CHARLIE)
    val history = historyOf(paths)
    val iterator = history.framesFromBottom<Any>().iterator()

    assertThat(iterator.next()).isSameAs(ABLE)
    assertThat(iterator.next()).isSameAs(BAKER)
    assertThat(iterator.next()).isSameAs(CHARLIE)
    assertThat(iterator.hasNext()).isFalse()
  }

  @Test
  fun framesFromTop() {
    val paths = listOf(ABLE, BAKER, CHARLIE)
    val history = historyOf(paths)
    val iterator = history.framesFromTop<Any>().iterator()

    assertThat(iterator.next()).isSameAs(CHARLIE)
    assertThat(iterator.next()).isSameAs(BAKER)
    assertThat(iterator.next()).isSameAs(ABLE)
    assertThat(iterator.hasNext()).isFalse()
  }

  @Test
  fun emptyBuilderPeekIsNullable() {
    try {
      history {
        assertThat(peek()).isNull()
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun emptyBuilderPopThrows() {
    try {
      history {
        try {
          pop()
          fail("Should throw")
        } catch (e: IllegalStateException) {
          // pass
        }
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun isEmpty() {
    try {
      history {
        assertThat(isEmpty).isTrue()
        push("foo")
        assertThat(isEmpty).isFalse()
        pop()
        assertThat(isEmpty).isTrue()
      }
    } catch (exception: IllegalArgumentException) {
      // pass
    }
  }

  @Test
  fun historyIndexAccess() {
    val history = historyOf(ABLE, BAKER, CHARLIE)

    assertThat(history.peek<Any>(0)).isEqualTo(CHARLIE)
    assertThat(history.peek<Any>(1)).isEqualTo(BAKER)
    assertThat(history.peek<Any>(2)).isEqualTo(ABLE)
  }

  @Test
  fun historyIsIsolatedFromItsBuilder() {
    history {
      push(ABLE)
      push(BAKER)
      push(CHARLIE)
      val history = build()
      pop()
      val key = history.peek<Any>(0)
      assertThat(key).isEqualTo(CHARLIE)
    }
  }

  companion object {
    private val ABLE = TestKey("able")
    private val BAKER = TestKey("baker")
    private val CHARLIE = TestKey("charlie")
  }
}
