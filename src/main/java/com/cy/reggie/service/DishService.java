package com.cy.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.vo.DishDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService extends IService<Dish> {

    public  void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    public void setStatusTo(List<Long> ids);

}
