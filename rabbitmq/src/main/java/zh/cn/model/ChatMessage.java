package zh.cn.model;

import lombok.Data;

/**
 * 消息模型类
 */
@Data
public class ChatMessage {

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息正文
     */
    private String content;

    /**
     * 消息发送者
     */
    private String sender;

    /**
     * 消息接收者
     */
    private String toUser;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
