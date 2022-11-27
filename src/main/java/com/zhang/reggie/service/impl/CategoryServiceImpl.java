package com.zhang.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.reggie.common.CustomException;
import com.zhang.reggie.entity.Category;
import com.zhang.reggie.entity.Dish;
import com.zhang.reggie.entity.Setmeal;
import com.zhang.reggie.mapper.CategoryMapper;
import com.zhang.reggie.service.CategoryService;
import com.zhang.reggie.service.DishService;
import com.zhang.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {

        //查询分类是否关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        long dishCount = dishService.count(dishLambdaQueryWrapper);

        if(dishCount>0){
             //关联菜品，抛出异常
            throw new CustomException("当前分类下关联菜品，不能删除");
        }

        //查询分类是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        long setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if(setmealCount>0){
            //关联套餐，抛出异常
            throw new CustomException("当前分类下关联套餐，不能删除");
        }

        //正常删除分类
        super.removeById(id);

    }
}
