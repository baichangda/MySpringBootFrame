package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.service.MenuService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/menu")
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;



    /**
     * 查询菜单列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询菜单列表",notes = "查询菜单列表")
    public JsonMessage<List<MenuBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父菜单id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "菜单名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "url地址")
            @RequestParam(value = "url",required = false) String url,
            @ApiParam(value = "图标")
            @RequestParam(value = "icon",required = false) String icon,
            @ApiParam(value = "排序",example="1")
            @RequestParam(value = "orderNum",required = false) Integer orderNum
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("url",url, StringCondition.Handler.ALL_LIKE),
            new StringCondition("icon",icon, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("orderNum",orderNum, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(menuService.findAll(condition));
    }

    /**
     * 查询菜单分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询菜单列表",notes = "查询菜单分页")
    public JsonMessage<Page<MenuBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父菜单id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "菜单名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "url地址")
            @RequestParam(value = "url",required = false) String url,
            @ApiParam(value = "图标")
            @RequestParam(value = "icon",required = false) String icon,
            @ApiParam(value = "排序",example="1")
            @RequestParam(value = "orderNum",required = false) Integer orderNum,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("url",url, StringCondition.Handler.ALL_LIKE),
            new StringCondition("icon",icon, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("orderNum",orderNum, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(menuService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存菜单
     * @param menu
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存菜单",notes = "保存菜单")
    public JsonMessage save(@ApiParam(value = "菜单实体") @RequestBody @Validated MenuBean menu){
        menuService.save(menu);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除菜单
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除菜单",notes = "删除菜单")
    public JsonMessage delete(@ApiParam(value = "菜单id数组") @RequestParam Long[] ids){
        menuService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
