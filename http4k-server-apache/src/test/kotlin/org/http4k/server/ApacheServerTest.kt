package org.http4k.server

import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Method.Companion.DELETE
import org.http4k.core.Method.Companion.GET
import org.http4k.core.Method.Companion.HEAD
import org.http4k.core.Method.Companion.OPTIONS
import org.http4k.core.Method.Companion.PATCH
import org.http4k.core.Method.Companion.POST
import org.http4k.core.Method.Companion.PURGE
import org.http4k.core.Method.Companion.PUT
import org.http4k.core.Method.Companion.TRACE

class ApacheServerTest : ServerContract(::ApacheServer, ApacheClient(),
    arrayOf(GET, POST, PUT, DELETE, OPTIONS, TRACE, PATCH, PURGE, HEAD).filter { it != Method.PURGE }.toTypedArray())
