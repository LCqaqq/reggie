package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.*;
import com.cy.reggie.service.CategoryService;
import com.cy.reggie.service.SetmealDishService;
import com.cy.reggie.service.SetmealService;
import com.cy.reggie.vo.DishDto;
import com.cy.reggie.vo.SetmealDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //执行查询
        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //获取分类id
            Long id = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(id);
            if(category!=null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return R.success(dtoPage);

    }

    /**
     * 新增加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }

    /**
     * 根据套餐id批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>deleteByIds(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        //根据菜品id先获取再删除
        setmealService.removeWiithDish(ids);
        /*
        for (Long id:ids
        ) {
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal.getStatus()==1){
                return R.error("套餐并未停售");
            }else {
                //根据套餐id删除套餐信息
                setmealService.removeById(id);
                //获取套餐和菜品的关系
                LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SetmealDish::getSetmealId, id);
                setmealDishService.remove(queryWrapper);
            }
        }*/
        return R.success("删除成功");
    }

    /**
     * 根据id获取套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto>get(@PathVariable Long id){
        log.info("套餐id",id);
        SetmealDto id1 = setmealService.getByIdWithSetmeal(id);
        return R.success(id1);
    }

    /**
     * 批量停售套餐
     * @param ids
     * @return
             */
    @PostMapping("/status/0")
    public R<String> setstatus0(@RequestParam List<Long> ids){

        setmealService.setStatusTo(ids);
        return R.success("停售成功");
    }

    /**
     * 批量启售套餐
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> setstatus1(@RequestParam List<Long> ids){

        setmealService.setStatusTo(ids);
        return R.success("启售成功");
    }

    /**
     *通过套餐类型获取套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
