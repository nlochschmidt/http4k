package org.http4k.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class MethodTest {

    @Test
    fun `toString is just the name`() {
        assertThat(Method("boo").name, equalTo("boo"))
    }

}
