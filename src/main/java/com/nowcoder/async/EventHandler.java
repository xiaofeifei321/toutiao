package com.nowcoder.async;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/14.
 */

/**
 * 后台一直有一个线程在消费给队列。如何实现呢，考虑到拓展性，
 * 首先会定义一个service层的接口，EventHandler，
 * 定义其中的方法doHandler要做的事情和当前event涉及到的所有活动类型EventType。
 */


public interface EventHandler {
    void doHandle(EventModel model); //定义接口，针对活动要执行的动作
    //关注哪一些EventType
    List<EventType> getSupportEventTypes(); //关注的活动类型有哪些
}
