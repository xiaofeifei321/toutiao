package com.nowcoder.util;

/**
 * Created by nowcoder on 2016/7/13.
 * 根据规范，生成一些key
 *
 */
public class RedisKeyUtil {
    //分割符
    private static String SPLIT = ":";
    //喜欢
    private static String BIZ_LIKE = "LIKE";
    //不喜欢
    private static String BIZ_DISLIKE = "DISLIKE";
   //event
    private static String BIZ_EVENT = "EVENT";

    public static String getEventQueueKey() {
        return BIZ_EVENT;
    }

    public static String getLikeKey(int entityId, int entityType) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDisLikeKey(int entityId, int entityType) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }
}
