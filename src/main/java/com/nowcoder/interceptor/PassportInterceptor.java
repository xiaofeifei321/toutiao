package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 登录验证
 * 看用户是否为登录用户，对所有页面进行处理
 * SpringBoot拦截器只需要实现接口即可
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketDAO loginTicketDAO;
    //用户
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 1：自动登录：
     *  服务器会把浏览器的sessionId和服务器的数据库关联，在提交请求的时候服务器会设置拦截器去找sessionId，如果这个sessionId和我服务器上的ticket关联了起来，并且设置的过期时间没有过期，
     *  那在登陆主页前我就可以知道是某个用户，在controller层中可以进行渲染。
     * 可以做到自动登陆的功能。
     *
     *
     *
     * 下次在已经登陆的用户，进行其他点击后。在进入controller前，调用preHandle方法处理，
     * 它可以检查客户端提交的cookie中是否有服务器之前下发的ticket，如果有证明这个请求是已经登陆的用户了。
     * 把登陆的用户放到线程本地变量。在此时才进入controller，可以拿到具体的用户HostHolder类，这是线程本地变量，
     * 可以根据登陆的用户进行个性化渲染，比如关注用户的动态，个人收藏等。
     * 作用:
     *该方法将在请求处理之前进行调用，只有该方法返回true，才会继续执行后续的Interceptor和Controller，
     * 当返回值为true 时就会继续调用下一个Interceptor的preHandle 方法，如果已经是最后一个Interceptor的时候就会是调用当前请求的Controller方法
     *preHandler -> Controller -> postHandler -> model渲染-> afterCompletion
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            //取出cookier
            for (Cookie cookie : httpServletRequest.getCookies()) {
                //是否有ticket
                if (cookie.getName().equals("ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        //如果有Ticket
        if (ticket != null) {
            //从数据库取出 Ticket看是否相同：
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
                //无效的
                return true;
            }
            //有效的：记下来
            User user = userDAO.selectById(loginTicket.getUserId());
            //setAttribute里面
            hostHolder.setUser(user);
        }
        return true;
    }

    /**
     * 在业务处理器处理请求执行完成后，生成视图之前执行。
     * 后处理（调用了Service并返回ModelAndView，但未进行页面渲染），有机会修改ModelAndView
     */
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //结束了
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    /**
     * 在DispatcherServlet完全处理完请求后被调用，可用于清理资源等。返回处理（已经渲染了页面
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
