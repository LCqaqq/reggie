package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.BaseContext;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.Orders;
import com.cy.reggie.service.OrderDetailService;
import com.cy.reggie.service.OrdersService;
import com.cy.reggie.vo.DishDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("orders={}",orders);
        ordersService.submit(orders);
        return R.success("提交订单成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize, String name){
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.eq(Orders::getUserId, BaseContext.getId());

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行查询
        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
}
