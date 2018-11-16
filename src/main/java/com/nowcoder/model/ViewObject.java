package com.nowcoder.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义类ViewObject，是个map的包装类，方便把每一条资讯的信息（包括新闻信息，发帖的用户信息）
 * 放到一起。传到moder，方便前端拿到，做展示。类定义如下
 */
public class ViewObject {
    private Map<String, Object> objs = new HashMap<String, Object>();
    public void set(String key, Object value) {
        objs.put(key, value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
