package zh.cn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import zh.cn.interceptor.SocketChannelInterceptor;

/**
 * 实现简单的消息协议配置处理,默认的是 Stomp(简单的面向文本的消息传递协议。它是一种消息协议，定义了数据交换的格式和规则)
 * @author 17697
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private SocketChannelInterceptor socketChanelInterceptor;

    /**
     * 注册Stomp服务端点
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
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 定义服务端应用目标前缀；客户端只有以这个前缀才能进入服务端方法 @MessageMapping
        registry.setApplicationDestinationPrefixes("/app/");
        // 定义SimpleBroker处理的消息前缀；只有消息以这个为前缀才会被SimpleBroker处理转发
        registry.enableSimpleBroker("/topic/","/user/");
        // 设置一对一消息前缀，默认的是"/user/"，可通过该方法修改
        registry.setUserDestinationPrefix("/user/");

    }

    /**
     * 配置客户端入站通道拦截器，用于传递从WebSocket客户端接收到的消息
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketChanelInterceptor);
    }

    /**
     * 配置客户端出站通道拦截器，用于向WebSocket客户端发送服务器消息
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        //registration.interceptors(socketChanelInterceptor);
    }
}
