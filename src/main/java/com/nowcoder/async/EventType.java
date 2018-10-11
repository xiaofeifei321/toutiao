package com.nowcoder.async;

/**
 * 获得活动的类型，可以有点赞，评论，登陆等待
 */
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3);
//like ,comment ,login,mail
    private int value;
    //构造函数
    EventType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
