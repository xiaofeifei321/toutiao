package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by nowcoder on 2016/7/3.
 * 配置拦截器执行的先后顺序
 */
@Component
public class ToutiaoWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    /**
     * 注入拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截位置放在登陆验证PassportInterceptor的后面，通过登陆验证看HostHolder类中有没有线程本地变量user类，如果有，证明能确定是谁登陆的。如果没有，那没有权限访问/setting*相关的页面。通过PassportInterceptor的PreHandler()方法后，
        // 进入LoginRequiredInterceptor的PreHandler方法，如果没有拿到user类，返回false。不能进入controller层，重定向到首页，并且约定pop=1，弹出登陆框。
        registry.addInterceptor(passportInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).
                addPathPatterns("/msg/*").addPathPatterns("/like").addPathPatterns("/dislike");
        super.addInterceptors(registry);
    }
}
