package zh.cn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 实现简单的消息协议配置处理,默认的是 Stomp(简单的面向文本的消息传递协议。它是一种消息协议，定义了数据交换的格式和规则)
 *
 * @author 17697
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册Stomp服务端点
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // addEndpoint 设置与客户端建立连接的url
        registry.addEndpoint("/ws")
                // 设置允许跨域
                .setAllowedOriginPatterns("*")
                // 允许SocketJs使用，是为了防止某些浏览器客户端不支持websocket协议的降级策略
                .withSockJS();
    }

    /**
     * 配置消息代理的路由规则
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 定义服务端应用目标前缀；客户端只有以这个前缀才能进入服务端方法 @MessageMapping
        registry.setApplicationDestinationPrefixes("/app/");

        // 设置rabbitmq stomp代理
        registry.enableStompBrokerRelay("/exchange","/topic","/queue","/amq/queue")
                // 设置stomp代理ip
                .setRelayHost("localhost")
                // 设置stomp代理端口(不是rabbitmq http端口，是rabbitmq的stomp插件服务端口，可在rabbitmq首页主看到)
                .setRelayPort(61613)
                // 设置stomp代理用户名
                .setClientLogin("guest")
                // 设置stomp代理密码
                .setClientPasscode("guest")
                .setVirtualHost("/");


    }
}
