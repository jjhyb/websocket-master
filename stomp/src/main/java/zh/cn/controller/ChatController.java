package zh.cn.controller;

import org.springframework.web.bind.annotation.RestController;
import zh.cn.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 客户端发送消息入口，群发消息
     * @param chatMessage
     * @return
     */
    @MessageMapping("/chat/sendMessage")
    @SendTo({"/topic/public"})
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    /**
     * 一对一消息发送
     * @param chatMessage
     */
    @PostMapping("/chat/single")
    public void sendSingleMessage(@RequestBody ChatMessage chatMessage) {
        messagingTemplate.convertAndSendToUser(chatMessage.getToUser(),"/single",chatMessage);
    }

    /**
     * 客户端新增用户消息入口，用于群发显示：新进入xx用户
     * @param chatMessage
     * @param headerAccessor
     * @return
     */
    @MessageMapping("/chat/addUser")
    @SendTo({"/topic/public"})
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

}
