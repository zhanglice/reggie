package com.zhang.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.reggie.entity.User;

public interface UserService extends IService<User> {
    //邮件服务
    void sendSimpleMail(String from, String to, String subject, String text);
}
