package zh.cn.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zh.cn.beans.SessionPool;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * websocket服务端点
 *
 * @author 17697
 */
@ServerEndpoint("/websocket/{userId}")
@Component
@Slf4j
public class WebSocketEndpoint {

    /**
     * 建立连接处理
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // 可以拓展进行身份校验
        log.info("用户 {} 建立连接...", userId);
        log.info("当前建立连接数：{}", SessionPool.sessions.size());
        SessionPool.sessions.put(userId, session);

    }


    /**
     * 发送消息处理
     *
     * @param session
     * @param message
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        // 如果是心跳检测的消息，则返回pong作为心跳回应
        if (message.equalsIgnoreCase("ping")) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("type", "pong");
                session.getBasicRemote().sendText(JSON.toJSONString(params));
                log.info("应答客户端的消息:" + JSON.toJSONString(params));
            } catch (Exception e1) {
                log.error("心跳响应出现异常：{}",e1.getMessage(),e1);
            }
        } else {
            // 约定消息格式
            // {"fromUserId": userId,"toUserId": toUserId,"msg": msg};
            JSONObject jsonObject = JSON.parseObject(message);
            String toUser = ((String) jsonObject.get("toUserId"));
            String fromUserId = ((String) jsonObject.get("fromUserId"));
            if (StringUtils.hasText(toUser)){
                // 单发
                SessionPool.sendMessage(jsonObject);
            }else{
                // 群发
                SessionPool.sendMessage(message);
            }
        }
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) throws IOException {
        SessionPool.remove(session.getId());
        session.close();
        log.info("用户 {} 断开连接...", userId);
        log.info("当前建立连接数：{}", SessionPool.sessions.size());
    }

    /**
     * 异常处理
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket发生错误：{}", error.getMessage(), error);
    }

}
