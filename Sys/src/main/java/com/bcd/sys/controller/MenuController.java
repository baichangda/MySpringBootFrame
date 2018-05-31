package com.bcd.sys.controller;

import com.bcd.base.define.SuccessDefine;
import com.bcd.base.jackson.impl.SimpleFilterBean;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.FilterUtil;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.service.MenuService;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author acemma
 * Created by Administrator on 2017/5/10.
 */
@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/menu")
public class MenuController extends BaseController{
    @Autowired
    private MenuService menuService;

    /**
     * 保存菜单
     * @param
     * @return 保存的菜单
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value="保存菜单",notes = "保存菜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "menu",value = "菜单实体",dataType = "SysMenuDTO",paramType = "body")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "保存的菜单")})
    public JsonMessage save(@RequestBody MenuBean menu){
        menuService.save(menu);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }

    /**
     * 删除菜单
     * @param menuIdArr
     * @return
     */
    @RequestMapping(value = "/deleteMenu",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除菜单",notes="删除菜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "menuIdArr",value = "菜单id数组",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除菜单")})
    public JsonMessage delete(@RequestParam Long[] menuIdArr){
        menuService.delete(menuIdArr);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }


    /**
     * 查询菜单
     * @return
     */
    @RequiresRoles("abc")
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiOperation(value = "查询菜单",notes="查询菜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "menuId",value = "菜单id",dataType = "Long",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "菜单列表")})
    public JsonMessage list(@RequestParam(value = "menuId",required = false) Long menuId){
        SimpleFilterBean[] filters= FilterUtil.getOneDeepJsonFilter(MenuBean.class);
        return JsonMessage.success(JsonUtil.toJSONResult(menuService.findOne(menuId),filters));
    }


}
