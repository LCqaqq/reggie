package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.mapper.DishMapper;
import com.cy.reggie.service.DishFlavorService;
import com.cy.reggie.service.DishService;
import com.cy.reggie.vo.DishDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时插入菜品对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表
        this.save(dishDto);

        //菜品Id
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //封装菜品id信息
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味数据表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据菜品id获取菜品信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id){

        DishDto dishDto = new DishDto();
        //查询菜品基本信息
        Dish dish = this.getById(id);
        //查询对应的口味信息

        //构造条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件构造器
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品信息的口味信息--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //重新插入口味信息--dish_flavor表的insert操作
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //封装菜品id信息
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味数据表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void setStatusTo(List<Long> ids) {
        for (Long id:ids
             ) {
            //查询菜品基本信息
            Dish dish = this.getById(id);
            //判断出售状态
            if (dish!=null){
                if (dish.getStatus()==0){
                    dish.setStatus(1);
                }else {
                    dish.setStatus(0);
                }

            }
            //更新数据
            this.updateById(dish);
        }
    }
}
