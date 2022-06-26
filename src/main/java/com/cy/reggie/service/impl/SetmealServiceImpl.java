package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.common.CustomException;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.entity.Setmeal;
import com.cy.reggie.entity.SetmealDish;
import com.cy.reggie.mapper.SetmealMapper;
import com.cy.reggie.service.SetmealDishService;
import com.cy.reggie.service.SetmealService;
import com.cy.reggie.vo.SetmealDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 通过id获取套餐信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithSetmeal(Long id) {

        SetmealDto setmealDto = new SetmealDto();

        Setmeal setmeal = this.getById(id);

        //构造条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件构造器
        queryWrapper.eq(SetmealDish::getSetmealId, id);

        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;

    }

    @Override
    public void setStatusTo(List<Long> ids) {
        for (Long id : ids
        ) {
            //查询菜品基本信息
            Setmeal setmeal = this.getById(id);
            //判断出售状态
            if (setmeal != null) {
                if (setmeal.getStatus() == 0) {
                    setmeal.setStatus(1);
                } else {
                    setmeal.setStatus(0);
                }

            }
            //更新数据
            this.updateById(setmeal);
        }
    }

    /**
     * 新增套餐同时保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作sermeal表，执行insert操作
        this.save(setmealDto);
        //保存菜单和菜品的关联关系，操作sermeal_dish,执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //为每一条菜品数据关联菜单
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 批量删除停售的套餐，并且删除套餐与菜品的关系
     * @param ids
     */
    @Override
    public void removeWiithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if(count!=0){
            throw new CustomException("有套餐正在售卖中，不能删除");
        }
        //删除套餐
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除套餐与菜品关系
        setmealDishService.remove(queryWrapper1);
    }
}
