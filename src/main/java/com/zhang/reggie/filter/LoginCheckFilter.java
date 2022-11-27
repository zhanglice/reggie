package com.zhang.reggie.filter;

import com.alibaba.fastjson2.JSON;
import com.zhang.reggie.common.BaseContext;
import com.zhang.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求{}",requestURI);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**", //页面可以访问，拦截数据
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };

        //2.判断是否需要处理
        boolean check = check(urls, requestURI);

        //3.放行不需要处理的请求
        if (check) {
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1.判断员工登陆状态
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            log.info("员工已登录，ID=>{}",empId);
            BaseContext.setCurrentId(empId); //id存入线程变量

            filterChain.doFilter(request, response);
            return;
        }

        //4-1.判断用户登陆状态
        if (request.getSession().getAttribute("user") != null) {
            Long empId = (Long) request.getSession().getAttribute("user");
            log.info("用户已登录，ID=>{}",empId);
            BaseContext.setCurrentId(empId); //id存入线程变量

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        //5.未登录则返回未登录结果，通过输出流返回客户端页面响应数据
        //request.js 中的响应拦截器会自动跳转
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }
    /**
     * 路径匹配，检测是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[]urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
