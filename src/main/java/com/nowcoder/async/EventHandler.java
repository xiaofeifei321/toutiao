package com.nowcoder.async;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/14.
 */
public interface EventHandler {
    void doHandle(EventModel model); //定义接口，针对活动要执行的动作
    //关注哪一些EventType
    List<EventType> getSupportEventTypes(); //关注的活动类型有哪些
}
