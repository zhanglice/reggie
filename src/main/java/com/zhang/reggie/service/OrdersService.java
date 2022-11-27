package com.zhang.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    //用户下单
    void submit(Orders orders);
}
