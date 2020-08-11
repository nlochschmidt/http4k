package org.http4k.server

import org.http4k.client.ApacheClient

class SunHttpStopTest : ServerStopContract({ SunHttp(0, it) }, ApacheClient(), {
    enableGracefulStop()
})
