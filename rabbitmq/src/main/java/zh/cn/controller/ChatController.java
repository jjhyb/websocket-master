package zh.cn.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zh.cn.model.ChatMessage;


@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 客户端发送消息入口，群发消息
     * @param chatMessage
     * @return
     */
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 发送到指定fanoutQueue队列
        messagingTemplate.convertAndSend("/amq/queue/fanoutQueue",chatMessage);
    }

    /**
     * 客户端发送消息入口，群发消息（rabbitTemplate）
     * @param chatMessage
     * @return
     */
    @PostMapping("/chat/sendMessage")
    public void sendMsg(@RequestBody ChatMessage chatMessage) {
        String str = JSON.toJSONString(chatMessage);
        rabbitTemplate.convertAndSend("WebSocket","*",str);
    }

    /**
     * 一对一消息发送
     * @param chatMessage
     */
    @PostMapping("/chat/single")
    public void sendSingleMessage(@RequestBody ChatMessage chatMessage) {
        // RabbitTemplate
        String str = JSON.toJSONString(chatMessage);
        rabbitTemplate.convertAndSend("WebSocket","single." + chatMessage.getToUser(),str);
        // Stomp 协议形式
//        messagingTemplate.convertAndSend("/topic/single"+chatMessage.getToUser(),chatMessage);
    }

    /**
     * 客户端新增用户消息入口，用于群发显示：新进入xx用户
     * @param chatMessage
     * @param headerAccessor
     * @return
     */
    @MessageMapping("/chat/addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        messagingTemplate.convertAndSend("/amq/queue/fanoutQueue",chatMessage);
    }

}
