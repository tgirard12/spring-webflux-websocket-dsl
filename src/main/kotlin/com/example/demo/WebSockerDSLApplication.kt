package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*


//
// Kotlin DSL
//
fun webSockerRouter(f: WebSockerRouterDsl.() -> Unit): HandlerMapping =
    WebSockerRouterDsl().let {
        f(it)
        SimpleUrlHandlerMapping().apply {
            urlMap = it.paths.toMap()
            order = -1
        }
    }

class WebSockerRouterDsl {

    internal val paths = mutableMapOf<String, WebSocketHandler>()

    operator fun String.invoke(f: (WebSocketSession) -> Mono<Void>) {
        paths[this] = WebSocketHandler { f(it) }
    }

    fun WS(path: String, f: (WebSocketSession) -> Mono<Void>) {
        paths[path] = WebSocketHandler { f(it) }
    }
}


//
// Router
//
val wsRouter = webSockerRouter {
    "/ws1" {
        it.send(
            it.receive()
                .map(WebSocketMessage::retain)
                .delayElements(Duration.ofSeconds(1))
                .log()
        )
    }
    WS("/ws2") {
        it.send(
            it.receive()
                .map(WebSocketMessage::retain)
                .delayElements(Duration.ofSeconds(2))
                .log()
        )
    }
}

//
// Spring Boot App
//
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Configuration
class WsConfig {
    @Bean
    fun routerWebSocket() = wsRouter

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/path"] = MyWebSocketHandler()

        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = -1 // before annotated controllers
        return mapping
    }
}


class MyWebSocketHandler : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> =
        session.send(
            session.receive()
                .map(WebSocketMessage::retain)
                .delayElements(Duration.ofSeconds(1)).log()
        )
}
