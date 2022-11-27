package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhang.reggie.Utils.CodeUtils;
import com.zhang.reggie.common.R;
import com.zhang.reggie.entity.User;
import com.zhang.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    /**
     * 邮箱验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取邮箱号(前端是手机这里没有改动)
        log.info("User=>{}",user);
        String mail = user.getPhone();
        if(StringUtils.hasText(mail)){
            //随机生成邮箱验证码
            String code = CodeUtils.generateValidateCode(4).toString();

            //调用邮箱服务，完成发送  这里为了便于调试展示不用验证
//            userService.sendSimpleMail(
//                    "1829922348@qq.com",
//                    mail,
//                    "邮箱验证码",
//                    "你的邮箱验证码:"+code+"（五分钟内有效）！"
//            );

            //保存生成验证码到Session
//            session.setAttribute(mail,code);

            //验证码缓存到redis中
            redisTemplate.opsForValue().set(mail,code,5, TimeUnit.MINUTES);

            log.info("code=>{}",code);
            return R.success("邮箱验证码发送成功");
        }
        return R.error("验证码发送失败！");
    }

    /**
     * 移动端用户登录校验
     * @param map
     * @param session
     * @return
     */

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();
        
        //session取得验证码
//        Object codeInSession = session.getAttribute(phone);

        //从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //验证码比对
        if(codeInSession!=null && codeInSession.equals(code)){
            //判断新用户，自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if (user == null){
                //自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //登录成功，删除缓存
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
