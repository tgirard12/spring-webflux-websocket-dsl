# spring-webflux-websocket-dsl
Proof of concept of spring webflux websocket kotlin dsl

```kotlin
@Configuration
class WsConfig {
    
    @Bean
    fun routerWebSocket() = webSockerRouter {
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
    
    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()    
}
```
