package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhang.reggie.common.R;
import com.zhang.reggie.dto.DishDto;
import com.zhang.reggie.entity.Category;
import com.zhang.reggie.entity.Dish;
import com.zhang.reggie.entity.DishFlavor;
import com.zhang.reggie.service.CategoryService;
import com.zhang.reggie.service.DishFlavorService;
import com.zhang.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);

        //删除所有缓存
//        Set<Object> keys = redisTemplate.keys("dish*");
//        redisTemplate.delete(keys);

        //精确清理要修改的缓存
        String key = "dish" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return  R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        //分页构造器 Dish没有菜品种类
        //没有dishDtoService所以必须使用dish查询赋值给DTO
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.eq(Dish::getIsDeleted,0);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();  //records: page中携带数据的字段
        List<DishDto> list = records.stream().map((item) -> { //将dish中的records用流处理，返回成dishDto的records
            DishDto dishDto = new DishDto();
            Long categoryId = item.getCategoryId();
            //根据ID查询分类对象
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            BeanUtils.copyProperties(item, dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味，修改菜品信息时回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        //删除所有缓存
//        Set<Object> keys = redisTemplate.keys("dish*");
//        redisTemplate.delete(keys);

        //精确清理要修改的缓存
        String key = "dish" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return  R.success("修改菜品成功");
    }

    /**
     * 起售停售菜品
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status,@RequestParam List<Long> ids){
        for (Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        //删除所有缓存
        Set<Object> keys = redisTemplate.keys("dish*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        return R.success("售卖状态已改变");
    }

    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){

        dishService.deleteWithFlavor(ids);

        //删除所有缓存
        Set<Object> keys = redisTemplate.keys("dish*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        return R.success("删除成功");
    }

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return

    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){  //传来的是categoryId，用dish接受增加通用性

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getIsDeleted,0);
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){  //传来的是categoryId，用dish接受增加通用性
        List<DishDto> dishDtoList = null;

        //动态构造key
        String key = "dish" + dish.getCategoryId() + "_" + dish.getStatus();

        //从redis获取缓存
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList != null){
            //存在数据直接返回数据
            return R.success(dishDtoList);
        }

        //不存在，查询数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getIsDeleted,0);
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //将dish中的records用流处理，返回成dishDto的records
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            //根据ID查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //追加数据
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());
        //查询数据库之后添加缓存
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
