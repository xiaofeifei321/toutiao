package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.controller.IndexController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import redis.clients.jedis.*;

import java.util.List;

/**
 *redis常用操作
 * PV  //讨论区里的浏览数在Redis中存储
 * 点赞 //点赞，userId放在Redis中
 * 关注 //人与人之间关注，关注者是个集合，放进Redis，如果取消关注，从集合中删除即可
 * 排行榜 //登陆数，登陆一次，数值加1
 * 验证码 //验证码，用到set timeout时间，后台生成验证码，放进Redis，设置过期时间，比如三分钟还没收到用户验证码，自动从Redis删除验证码。
 * 缓存 //牛客网上访问，打开网页用户头像，用户名昵称，如果用户不更新，用户信息等在MySQL上可以做一层缓存，如果用户没更新信息，可以直接从缓存读，如果缓存没有，直接去数据库取，大大降低数据库的压力。
 * 异步队列 //点了赞，谁评论了帖子，发生事件，放进redis，后面有线程去执行
 * 判题队列  //用户提交判题，放入判题队列，在Redis中，服务器连判题队列执行，如果碰到高峰期，可以加机器
 *
 * Redis可以设置有效期，在登陆的时候可以用到，把用户登陆服务器下发的token存到Redis中，
 * 设置过期时间，时间到自动删除了。比存在数据库中判断expired_time更方便。
 *
 * 在高并发的情况下，比如秒杀，很多用户访问网页，评论网页，我们要记录商品数，浏览量，评论数，
 * 如果每一次请求后都去数据库中update字段，服务器很容易卡死。因为在更新数据库的时候，
 * 对该字段的行是上锁的。解决的办法是将数组存到某一个地方，每次收到请求后服务器先记录，
 * 每隔一段时间写回数据库中，这样用户看到的是一秒前的访问量，过了一秒，访问量蹭的上去了，
 * 对于用户来说一秒反应是没什么感觉的，对于服务器能防止卡死。可以将值存到Redis中。
 *
 * */
@Service
public class JedisAdapter implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);
    public static void print(int index, Object obj) {
        System.out.println(String.format("%d,%s", index, obj.toString()));
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        jedis.flushAll();
        // get,set
        jedis.set("hello", "world");
        print(1, jedis.get("hello"));

        jedis.rename("hello", "newhello");
        print(1, jedis.get("newhello"));
        //设置过期时间
        jedis.setex("hello2", 1, "world22");
        // 数值操作 pv评论的数目
        jedis.set("pv", "100");
        //增加 一次加一
        jedis.incr("pv");
        //增加  一次减少五
        jedis.decrBy("pv", 5);
        print(2, jedis.get("pv"));
        print(3, jedis.keys("*"));

        // 列表操作, 最近来访, 粉丝列表，消息队列
        String listName = "list";
        jedis.del(listName);
        //list底层是双向链表
        for (int i = 0; i < 10; ++i) {
            jedis.lpush(listName, "a" + String.valueOf(i));
        }
        print(4, jedis.lrange(listName, 0, 10)); // 最近来访10个id
        print(5, jedis.llen(listName));
        print(6, jedis.lpop(listName));
        print(7, jedis.llen(listName));
        print(8, jedis.lrange(listName, 0, 10)); // 最近来访10个id
        //是取出来之后的数据中第三个  注意
        print(9, jedis.lindex(listName, 3));
        //插入数据
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER, "a4", "xx"));
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE, "a4", "bb"));
        print(11, jedis.lrange(listName, 0, 12));

        // hash, 可变字段
        String userKey = "userxx";
        jedis.hset(userKey, "name", "jim");
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "18666666666");
        print(12, jedis.hget(userKey, "name"));
        print(13, jedis.hgetAll(userKey));
        //删掉一个字段
        jedis.hdel(userKey, "phone");
        print(14, jedis.hgetAll(userKey));
        //判断是否存在某个字段
        print(15, jedis.hexists(userKey, "email"));
        print(16, jedis.hexists(userKey, "age"));
        print(14, jedis.hgetAll(userKey));
        //列出所有键的字段
        print(17, jedis.hkeys(userKey));
        print(18, jedis.hvals(userKey));
        //如果不存在，我们设置
        jedis.hsetnx(userKey, "school", "zju");
        jedis.hsetnx(userKey, "name", "yxy");
        print(19, jedis.hgetAll(userKey));


        // set 集合，点赞用户群, 共同好友
        String likeKey1 = "newsLike1";
        String likeKey2 = "newsLike2";
        for (int i = 0; i < 10; ++i) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i * 2));
        }
        // Redis Smembers 命令返回集合中的所有的成员
        print(20, jedis.smembers(likeKey1));
        print(21, jedis.smembers(likeKey2));
        //集合方面的相关操作
        //并集
        print(22, jedis.sunion(likeKey1, likeKey2));
        //likeKey1中有，likeKey2中没有的
        print(23, jedis.sdiff(likeKey1, likeKey2));
        //交集
        print(24, jedis.sinter(likeKey1, likeKey2));
        //判断是否存在某个视频
        print(25, jedis.sismember(likeKey1, "12"));
        print(26, jedis.sismember(likeKey2, "12"));
        //直接删除
        jedis.srem(likeKey1, "5");
        print(27, jedis.smembers(likeKey1));
        // 从1移动到2
        jedis.smove(likeKey2, likeKey1, "14");
        print(28, jedis.smembers(likeKey1));
        print(29, jedis.scard(likeKey1));



        // zset 排序集合，有限队列，排行榜
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "Jim");
        jedis.zadd(rankKey, 60, "Ben");
        jedis.zadd(rankKey, 90, "Lee");
        jedis.zadd(rankKey, 75, "Lucy");
        jedis.zadd(rankKey, 80, "Mei");
        print(30, jedis.zcard(rankKey));

        //在某个区间内有多少人
        print(31, jedis.zcount(rankKey, 61, 100));
        // 某一个人多少分
        print(32, jedis.zscore(rankKey, "Lucy"));
        jedis.zincrby(rankKey, 2, "Lucy");
        //其中某一个用户集合
        print(33, jedis.zscore(rankKey, "Lucy"));
        jedis.zincrby(rankKey, 2, "Luc");
        print(34, jedis.zscore(rankKey, "Luc"));
        print(35, jedis.zcount(rankKey, 0, 100));
        // 1-4 名 Luc
        print(36, jedis.zrange(rankKey, 0, 10));
        //范围内第一名到第三名是怎么样的
        print(36, jedis.zrange(rankKey, 1, 3));
        //从大到小排序
        print(36, jedis.zrevrange(rankKey, 1, 3));
        //打印分值
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {
            print(37, tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
        }

        print(38, jedis.zrank(rankKey, "Ben"));
        print(39, jedis.zrevrank(rankKey, "Ben"));

        String setKey = "zset";
        jedis.zadd(setKey, 1, "a");
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");
        print(40, jedis.zlexcount(setKey, "-", "+"));
        print(41, jedis.zlexcount(setKey, "(b", "[d"));
        print(42, jedis.zlexcount(setKey, "[b", "[d"));
        jedis.zrem(setKey, "b");
        print(43, jedis.zrange(setKey, 0, 10));
        jedis.zremrangeByLex(setKey, "(c", "+");
        print(44, jedis.zrange(setKey, 0, 2));

	        /*
	        jedis.lpush("aaa", "A");
	        jedis.lpush("aaa", "B");
	        jedis.lpush("aaa", "C");
	        print(45, jedis.brpop(0, "aaa"));
	        print(45, jedis.brpop(0, "aaa"));
	        print(45, jedis.brpop(0, "aaa"));
	        */


        JedisPool pool = new JedisPool();
        for (int i = 0; i < 100; ++i) {
            Jedis j = pool.getResource();
            j.get("a");
            j.close();
        }
    }

    private Jedis jedis = null;
    private JedisPool pool = null;

    /**
     * 凡是继承该接口的类，在初始化bean的时候会执行该方法。
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //jedis = new Jedis("localhost");
        pool = new JedisPool("localhost", 6379);
    }

    private Jedis getJedis() {
        //return jedis;
        return pool.getResource();
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return getJedis().get(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * /**
     * 向Redis中Set集合添加值:点赞
     * @return
     */
    public long sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     *   /**
     * 移除：取消点赞
     * @param key
     * @param value
     * @return
     */
    public long srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    //记录有多少人喜欢
    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setex(String key, String value) {
        // 验证码, 防机器注册，记录上次注册时间，有效期3天
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.setex(key, 10, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setObject(String key, Object obj) {
        set(key, JSON.toJSONString(obj));
    }

    public <T> T getObject(String key, Class<T> clazz) {
        String value = get(key);
        if (value != null) {
            //把文本直接取出的函数
            return JSON.parseObject(value, clazz);
        }
        return null;
    }
}


