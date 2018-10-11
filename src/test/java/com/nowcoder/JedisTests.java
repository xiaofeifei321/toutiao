package com.nowcoder;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.NewsDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.*;
import com.nowcoder.util.JedisAdapter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ToutiaoApplication.class)
public class JedisTests {
    @Autowired
    JedisAdapter jedisAdapter;

    @Test
    public void testJedis() {
        jedisAdapter.set("hello", "world");
        Assert.assertEquals("world", jedisAdapter.get("hello"));
    }
    @Test
    public void testObject() {
        User user = new User();
        user.setHeadUrl("http://image.baidu.com/search/detail?ct=503316480&z=0&tn=baiduimagedetail&ipn=d&cl=2&cm=1&sc=0&lm=-1&ie=gb18030&pn=2&rn=1&di=126025443060&ln=30&word=%D0%A3%BB%A8&os=4133014662,189260671&cs=909527261,979081178&objurl=http%3A%2F%2Fimg4q.duitang.com%2Fuploads%2Fblog%2F201407%2F14%2F20140714204623_cmTxA.jpeg&bdtype=0&simid=4107160482,445645199&pi=0&adpicid=0&timingneed=0&spn=0&is=0,0&fr=ala&ala=1&alatpl=adress&pos=1&oriquery=%E6%A0%A1%E8%8A%B1&hs=2&xthttps=000000");
        user.setName("user1");
        user.setPassword("abc");
        user.setSalt("def");
        //设置用户信息
        jedisAdapter.setObject("user1", user);
         //获得用户信息
        User u = jedisAdapter.getObject("user1", User.class);
        //反序列化
        System.out.print(ToStringBuilder.reflectionToString(u));
    }

}
