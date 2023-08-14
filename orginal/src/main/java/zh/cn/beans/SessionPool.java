package zh.cn.beans;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话连接池
 * @author 17697
 */
@Getter
@Setter
public class SessionPool {

    /**
     * key:userId,value:Session会话信息
     */
    public final static Map<String, Session> sessions = new ConcurrentHashMap<>();


    /**
     * 根据sessionId移除回话
     * @param sessionId
     * @throws IOException
     */
    public static void remove(String sessionId) throws IOException {
        if (StringUtils.hasText(sessionId)){
            for (Map.Entry<String,Session> entry : sessions.entrySet()){
                if (sessionId.equals(entry.getValue().getId())){
                    sessions.remove(entry.getKey());
                }
            }
        }
    }

    /**
     * 群发消息
     * @param message
     */
    public static void sendMessage(String message) {
        for(String sessionId : SessionPool.sessions.keySet())
        {
            SessionPool.sessions.get(sessionId).getAsyncRemote().sendText(message);
        }
    }

    /**
     * 一对一消息
     * @param jsonObject
     */
    public static void sendMessage(JSONObject jsonObject) {
        //  {"fromUserId": userId,"toUserId": toUserId,"msg": msg};
        String toUserId = ((String) jsonObject.get("toUserId"));
        String fromUserId = ((String) jsonObject.get("fromUserId"));
        String msg = ((String) jsonObject.get("msg"));
        msg = "来自" + fromUserId + "的消息：" + msg;
        Session session = sessions.get(toUserId);
        if(session != null)
        {
            session.getAsyncRemote().sendText(msg);
        }
    }

}
