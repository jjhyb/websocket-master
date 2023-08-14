package zh.cn.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zh.cn.model.Permission;

import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;

/**
 * Socket拦截器
 * @author 17697
 */
@Component
@Slf4j
public class SocketChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String SOCKET_TOKEN_PREFIX = "webSocket:";

    /**
     * 发送消息到通道前
     * @param message
     * @param channel
     * @return
     */
    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 获取连接头信息
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 连接验证token合法性(简单模拟)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 获取头中的token
            String token = accessor.getFirstNativeHeader("token");
            if (StringUtils.hasText(token)) {
                // 查看redis中token信息
                String redisToken = null;
                try {
                    redisToken = redisTemplate.opsForValue().get(SOCKET_TOKEN_PREFIX);
                }catch (Exception e){
                    log.error("redis异常，{}",e.getMessage(),e);
                    throw e;
                }
                if (token.equals(redisToken)) {
                    // 这里可以结合 Security
//                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                    accessor.setUser(authentication);

                    // 简单处理设置对应权限。完整的应该根据用户的权限得出是否有发送/订阅到某个目的路径的权限
                    accessor.setUser(new UserPrincipal() {
                        @Override
                        public String getName() {
                            Permission permission = new Permission();
                            permission.setIsSend(true);
                            permission.setIsSubscribe(true);
                            String s = JSON.toJSONString(permission);
                            return s;
                        }
                    });

                } else {
                    throw new IllegalAccessException("未授权！！！");
                }
            } else {
                throw new IllegalAccessException("未授权！！！");
            }
            // 订阅权限认证
        }
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            UserPrincipal user = ((UserPrincipal) accessor.getUser());
            String value = user.getName();
            if (StringUtils.hasText(value)) {
                JSONObject jsonObject = JSONObject.parseObject(value);
                Boolean flag = ((Boolean) jsonObject.get("isSubscribe"));
                if (!flag) {
                    throw new IllegalAccessException("无权限订阅！！！");
                }
            } else {
                throw new IllegalAccessException("无权限订阅！！！");
            }
            // 发送权限验证
        }
        else if (StompCommand.SEND.equals(accessor.getCommand())) {
            UserPrincipal user = ((UserPrincipal) accessor.getUser());
            String value = user.getName();
            if (StringUtils.hasText(value)) {
                JSONObject jsonObject = JSONObject.parseObject(value);
                Boolean flag = ((Boolean) jsonObject.get("isSend"));
                if (!flag) {
                    throw new IllegalAccessException("无权限发送！！！");
                }
            } else {
                throw new IllegalAccessException("无权限发送！！！");
            }
        }
        return message;
    }


    /**
     * 发送消息到通道后
     * @param message
     * @param channel
     * @return
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    /**
     * 发送完成后
     * @param message
     * @param channel
     * @return
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return ChannelInterceptor.super.preReceive(channel);
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return ChannelInterceptor.super.postReceive(message, channel);
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        ChannelInterceptor.super.afterReceiveCompletion(message, channel, ex);
    }
}
