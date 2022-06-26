package com.cy.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.reggie.entity.Setmeal;
import com.cy.reggie.vo.SetmealDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 通过id获取套餐信息
     * @param id
     * @return
     */
    public SetmealDto getByIdWithSetmeal(Long id);

    /**
     * 批量修改套餐售卖状态
     * @param ids
     */
    public void setStatusTo(List<Long> ids);

    /**
     * 批量删除
     * @param ids
     */
    public void removeWiithDish(List<Long> ids);
}
 