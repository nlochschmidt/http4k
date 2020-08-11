package org.http4k.server

import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.GracefulShutdownHandler
import io.undertow.util.HttpString
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestSource
import org.http4k.core.Response
import org.http4k.core.Uri
import org.http4k.core.safeLong
import org.http4k.core.then
import org.http4k.core.toParametersMap
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.server.ServerConfig.StopMode
import org.http4k.server.ServerConfig.StopMode.Delayed
import org.http4k.server.ServerConfig.StopMode.Graceful
import org.http4k.server.ServerConfig.StopMode.Immediate
import org.http4k.server.ServerConfig.UnsupportedStopMode
import java.net.InetSocketAddress
import io.undertow.server.HttpHandler as UndertowHttpHandler

/**
 * Exposed to allow for insertion into a customised Undertow server instance
 */
class HttpUndertowHandler(handler: HttpHandler, stopMode: StopMode = Immediate) : UndertowHttpHandler {
    constructor(handler: HttpHandler) : this(handler, Immediate)

    private val undertowHandler: UndertowHttpHandler
    private val stopAction: () -> Unit

    init {
        val blockingHandler = BlockingHandler(UndertowHttpHandler { exchange ->
            CatchAll().then(handler)(exchange.asRequest()).into(exchange)
        })
        when (stopMode) {
            Immediate -> undertowHandler = blockingHandler.also { stopAction = {} }
            is Graceful -> undertowHandler = GracefulShutdownHandler(blockingHandler).also {
                stopAction = {
                    it.shutdown()
                    it.awaitShutdown(stopMode.timeout.toMillis())
                }
            }
            is Delayed -> throw UnsupportedStopMode(stopMode)
        }
    }

    fun stop() = stopAction()

    private fun Response.into(exchange: HttpServerExchange) {
        exchange.statusCode = status.code
        headers.toParametersMap().forEach { (name, values) ->
            exchange.responseHeaders.putAll(HttpString(name), values.toList())
        }
        body.stream.use { it.copyTo(exchange.outputStream) }
    }

    private fun HttpServerExchange.asRequest(): Request =
        Request(Method.valueOf(requestMethod.toString()), Uri.of("$relativePath?$queryString"))
            .headers(requestHeaders
                .flatMap { header -> header.map { header.headerName.toString() to it } })
            .body(inputStream, requestHeaders.getFirst("Content-Length").safeLong())
            .source(RequestSource(sourceAddress.hostString, sourceAddress.port, requestScheme))

    override fun handleRequest(exchange: HttpServerExchange) = undertowHandler.handleRequest(exchange)
}

data class Undertow(val port: Int = 8000, val enableHttp2: Boolean, override val stopMode: StopMode = Immediate) : ServerConfig {
    constructor(port: Int = 8000) : this(port, false)
    constructor(port: Int = 8000, enableHttp2: Boolean) : this(port, enableHttp2, Immediate)

    override fun toServer(httpHandler: HttpHandler): Http4kServer =
        object : Http4kServer {

            val httpUndertowHandler = HttpUndertowHandler(httpHandler, stopMode)

            val server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setServerOption(ENABLE_HTTP2, enableHttp2)
                .setHandler(httpUndertowHandler).build()

            override fun start() = apply { server.start() }

            override fun stop() = apply {
                httpUndertowHandler.stop()
                server.stop()
            }

            override fun port(): Int = if (port > 0) port else (server.listenerInfo[0].address as InetSocketAddress).port
        }
}
