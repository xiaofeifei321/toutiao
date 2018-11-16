package com.nowcoder.async;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 进入队列的具体的活动
 * Created by nowcoder on 2016/7/14.
 */
/*
 *发生的事件的数据都打包成一个Model(然后对这个model中数据进行序列化)
 */
public class EventModel {
    //事件是什么类型的
    private EventType type;
    //谁触发的
    private int actorId;
     //触发的对象是哪个
    private int entityId;
    private int entityType;
    //触发对象 拥有者是什么
    private int entityOwnerId;
    //扩展信息
    private Map<String, String> exts = new HashMap<>();
    public Map<String, String> getExts() {
        return exts;
    }
    public EventModel() {
    }
    public EventModel(EventType type) {
        this.type = type;
    }

    public String getExt(String name) {
        return exts.get(name);
    }

    public EventModel setExt(String name, String value) {
        exts.put(name, value);
        return this;
    }

    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }
}
