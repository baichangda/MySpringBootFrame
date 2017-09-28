package com.bcd.sys.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.RDBUtil;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.define.ErrorDefine;
import com.bcd.sys.service.MenuService;
import io.swagger.annotations.*;
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
        return SuccessDefine.SUCCESS_SAVE_SUCCESSED.toJsonMessage();
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
        menuService.deleteWithNoReferred(menuIdArr);
        return SuccessDefine.SUCCESS_DELETE_SUCCESSED.toJsonMessage();
    }


    /**
     * 查询菜单
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiOperation(value = "查询菜单",notes="查询菜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "menuId",value = "菜单id",dataType = "Long",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "菜单列表")})
    public JsonMessage list(@RequestParam(value = "menuId",required = false) Long menuId){
        SimplePropertyPreFilter[] filters= RDBUtil.getOneDeepJsonFilter(MenuBean.class);
        return JsonMessage.successed(JsonUtil.toDefaultJSONString(menuService.findOne(menuId),filters));
    }


    /**
     * 字段唯一性验证
     * @param fieldName
     * @param val
     * @return
     */
    @RequestMapping(value = "/isUniqueCheck",method = RequestMethod.GET)
    @ApiOperation(value = "字段唯一性验证",notes = "字段唯一性验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fieldName",value = "字段名称",dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "fieldValue",value = "字段的值",dataType = "String",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "true(可用) false(不可用)")})
    public JsonMessage isUniqueCheck(
            @RequestParam(value = "fieldName",required = true) String fieldName,
            @RequestParam(value = "fieldValue",required = true) String val){
        boolean flag = menuService.isUnique(fieldName, val);
        if (flag==false){
            return ErrorDefine.ERROR_FIELD_VALUE_EXISTED.toJsonMessage();
        }else {
            return JsonMessage.successed();
        }
    }

}
