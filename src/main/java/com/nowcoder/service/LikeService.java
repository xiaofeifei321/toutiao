package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nowcoder on 2016/7/13.
 */
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 判断某个用户对某种元素特别喜欢
     * 如果喜欢 返回1 不喜欢返回零
     */
    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
       //用户是否存在
        if(jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        //
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
    }

    /**
     * 喜欢
     */
    public long like(int userId, int entityType, int entityId) {
        // 在喜欢集合里增加
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
        //将用户插入进去
        jedisAdapter.sadd(likeKey, String.valueOf(userId));
        // 从反对里删除
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        jedisAdapter.srem(disLikeKey, String.valueOf(userId));
        //返回当前有多少人  喜欢
        return jedisAdapter.scard(likeKey);
    }

    /**
     * 不喜欢
     */
    public long disLike(int userId, int entityType, int entityId) {
        // 在反对集合里增加
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));
        // 从喜欢里删除
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
        jedisAdapter.srem(likeKey, String.valueOf(userId));
        return jedisAdapter.scard(likeKey);
    }
}
