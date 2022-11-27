package com.zhang.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.reggie.dto.DishDto;
import com.zhang.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品和口味数据，操作两张表
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品及其口味
    DishDto getByIdWithFlavor(Long id);

    //更新菜品及其口味
    void updateWithFlavor(DishDto dishDto);

    void deleteWithFlavor(List<Long> ids);
}
