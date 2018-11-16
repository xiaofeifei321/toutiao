package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.handler.LoginExceptionHandler;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现了InitializingBean, ApplicationContextAware接口,ApplicationContextAware可以获得当前的applicationContext，
 * 之后是InitializingBean接口中的afterPropertiesSet方法，在里面配置好config。
 * 首先拿到所有实现EventHandler接口的所有类。下面起一个线程从队列中消费
 */

/**
 * 从消息队列中获取事件交给Handler类进行处理。
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    //用来存储各种type事件的Handler
    private Map<EventType, List<EventHandler>> config = new HashMap<>();
    private ApplicationContext applicationContext;
    @Autowired
    private JedisAdapter jedisAdapter;

    /**
     * InitializingBean初始化方法
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }

                    // 注册每个事件的处理函数
                    config.get(type).add(entry.getValue());
                }
            }
        }

        // 启动线程去消费事件
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 从队列一直消费
                while (true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    //从Redis数据库的键为key的set集合中获取存储的事件（事件Event为序列化过的，String类型）
                    List<String> messages = jedisAdapter.brpop(0, key);
                    // 第一个元素是队列名字
                    for (String message : messages) {
                        if (message.equals(key)) {
                            continue;
                        }

                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        // 找到这个事件的处理handler列表
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }
/**
 * 消费活动，在初始化前，先得到Handler接口所有的实现类，遍历实现类。
 * 通过getSupportEventType得到每个实现类对应处理的活动类型。
 * 反过来记录在config哈希表中，
 * config中的key是活动的类型，比如说是LIKE，COMMENT，是枚举里的成员，
 * value是一个ArrayList的数组，里面存放的是各种实现方法。见代码中的。
 * 当从队列中获得一个活动时，这里用的是从右向外pop()一个活动实体。进行解析。
 * 这里的config.get(eventModel.getType())是一个数组，里面存放着所有关于这个活动要执行的实现类。
 * 遍历这个数组，开始执行实现类里的方法。
 */
                        for (EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
