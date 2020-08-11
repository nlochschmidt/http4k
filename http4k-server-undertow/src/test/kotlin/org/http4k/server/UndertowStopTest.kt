package org.http4k.server

import org.http4k.client.ApacheClient

class UndertowStopTest : ServerStopContract({ Undertow(0, false, it) }, ApacheClient(), {
    enableGracefulStop()
})
