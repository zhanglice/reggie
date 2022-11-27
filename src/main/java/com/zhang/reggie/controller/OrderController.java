package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhang.reggie.common.R;
import com.zhang.reggie.entity.Orders;
import com.zhang.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据=>{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, LocalDateTime beginTime, LocalDateTime endTime){
        Page<Orders> ordersPage  = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.like(number!=null,Orders::getNumber,number);
        queryWrapper.between(beginTime!=null && endTime!=null,Orders::getCheckoutTime,beginTime,endTime);

        ordersService.page(ordersPage, queryWrapper);
        return R.success(ordersPage);
    }

}
