package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Category;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.service.CategoryService;
import com.cy.reggie.service.DishFlavorService;
import com.cy.reggie.service.DishService;
import com.cy.reggie.vo.DishDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息的分页查询
     * @param page     页码
     * @param pageSize 每一页的数据量
     * @param name      用户名
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝,除了菜品信息之外的分类信息
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        //获取菜品信息
        List<Dish> records = pageInfo.getRecords();

        //分别获取每一个菜品的ID，然后查询分类信息，最后封装成含有分类名称DishDto对象
        List<DishDto> list =records.stream().map((item)->{
            //创建DishDto对象
            DishDto dishDto = new DishDto();
            //拷贝菜品信息
            BeanUtils.copyProperties(item,dishDto);
            //获取分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category!=null){//查询信息不为空
                //获取分类名称
                String categoryName = category.getName();
                //将名称给DishDto对象
                dishDto.setCategoryName(categoryName);
            }

            //返回含有分类名称DishDto对象
            return dishDto;
        }).collect(Collectors.toList());
        //拷贝包括分类名称的菜品信息
        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品信息成功");
    }

    /**
     * 根据id批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteByIds(@RequestParam List<Long> ids){

        //根据菜品id先获取再删除
        for (Long id:ids
             ) {
            DishDto dishDto = dishService.getByIdWithFlavor(id);
            //根据菜品id删除菜品信
            dishService.removeById(id);
            //获取菜品口味信息
            List<DishFlavor> flavors = dishDto.getFlavors();
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq( DishFlavor::getDishId,flavors);
            dishFlavorService.remove(queryWrapper);

        }

        return R.success("删除成功");
    }

    /**
     * 根据条件查询对应菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(dish.getCategoryId()!= null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加查询条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list =dishService.list(queryWrapper);
        //分别获取每一个菜品的ID，然后查询分类信息，最后封装成含有分类名称DishDto对象
        List<DishDto> list1 = list.stream().map((item)->{
            //创建DishDto对象
            DishDto dishDto = new DishDto();
            //拷贝菜品信息
            BeanUtils.copyProperties(item,dishDto);
            //获取分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            /*
            if(category!=null){//查询信息不为空
                //获取分类名称
                String categoryName = category.getName();
                //将名称给DishDto对象
                dishDto.setCategoryName(categoryName);
            }

             */
            //获取菜品Id
            Long dishId = item.getId();
            //查询菜品口味信息
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            //将口味信息放入dishDto中
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavorList);
            //返回含有分类名称DishDto对象
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(list1);
    }
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(dish.getCategoryId()!= null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加查询条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list =dishService.list(queryWrapper);

        return R.success(list);
    }
    */
    /**
     * 批量停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> setstatus0(@RequestParam List<Long> ids){

        dishService.setStatusTo(ids);
        return R.success("停售成功");
    }

    /**
     * 批量启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> setstatus1(@RequestParam List<Long> ids){

        dishService.setStatusTo(ids);
        return R.success("启售成功");
    }
}
