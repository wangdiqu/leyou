package com.leyou.cart.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.conf.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties jprop;
    //线程域
    private static final ThreadLocal<UserInfo> tl=new ThreadLocal<>();

    public UserInterceptor(JwtProperties jprop) {
          this.jprop=jprop;
    }

    /**
     * 前置拦截器
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //获取token
        String token = CookieUtils.getCookieValue(request, jprop.getCookieName());
        //解析token
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, jprop.getPublicKey());
            //传递user
            tl.set(user);
            //request.setAttribute("user",user);
            //方行
            return true;
        } catch (Exception e) {
            log.error("[购物车微服务] 用户身份解析失败！",e);
            return false;
        }
    }

    /**
     * 视图渲染完成后执行，此时controller已经执行完毕
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //使用完数据进行销毁
        tl.remove();
    }
    //获取登录用户
    public static UserInfo getUser(){
        return tl.get();
    }
}
