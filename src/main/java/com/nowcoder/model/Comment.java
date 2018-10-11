package com.nowcoder.model;

import java.util.Date;

/**
 * 按常规思路 评论的数据库创建字段有 id、content、user_id、created_date、status、news_id这些。
 * 这里数据库的设计就有点像程序设计中的面向接口编程还是面向实体编程。如果字段中有news_id的话，
 * 本意是将评论和新闻关联起来，但是评论不光只在新闻下有，在别人的评论下也可以有评论。所以字段要设计成如下，
 * 将news_id替换成entity_id， entity_type。entity_id可以表示news_id，也可以表示成 comment_id。
 * entity_type可以是news，也可以代表comment。这里我们可以做好如下约定。entity_type为1表示是新闻，
 * entity_id是新闻id；2表示为comment，表示评论的评论
 */
public class Comment {
    private int id;
    private int userId;
    private int entityId;
    private int entityType;
    private String content;
    private Date createdDate;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
