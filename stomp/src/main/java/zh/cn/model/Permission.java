package zh.cn.model;

import lombok.Data;

/**
 * websocket 权限类
 */
@Data
public class Permission {

    /**
     * 是否有订阅权限
     */
    private Boolean isSubscribe;

    /**
     * 是否有发送权限
     */
    private Boolean isSend;
}
