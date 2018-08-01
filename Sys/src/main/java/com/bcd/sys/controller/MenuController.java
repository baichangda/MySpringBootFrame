package com.bcd.sys.controller;

import org.springframework.http.converter.json.MappingJacksonValue;
import com.bcd.base.json.jackson.filter.SimpleFilterBean;
import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.rdb.util.FilterUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
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
    @ApiResponses(value={@ApiResponse(code=200,response = JsonMessage.class,message = "查询菜单列表")})
    public MappingJacksonValue list(
            @ApiParam(value = "主键",example="1")@RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父菜单id",example="1")@RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "菜单名称")@RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "url地址")@RequestParam(value = "url",required = false) String url,
            @ApiParam(value = "图标")@RequestParam(value = "icon",required = false) String icon,
            @ApiParam(value = "排序",example="1")@RequestParam(value = "orderNum",required = false) Integer orderNum,
            @ApiParam(value = "创建时间开始")@RequestParam(value = "createTimeBegin",required = false) Date createTimeBegin,
            @ApiParam(value = "创建时间截止")@RequestParam(value = "createTimeEnd",required = false) Date createTimeEnd,
            @ApiParam(value = "创建人id",example="1")@RequestParam(value = "createUserId",required = false) Long createUserId,
            @ApiParam(value = "创建人姓名")@RequestParam(value = "createUserName",required = false) String createUserName,
            @ApiParam(value = "更新时间开始")@RequestParam(value = "updateTimeBegin",required = false) Date updateTimeBegin,
            @ApiParam(value = "更新时间截止")@RequestParam(value = "updateTimeEnd",required = false) Date updateTimeEnd,
            @ApiParam(value = "更新人id",example="1")@RequestParam(value = "updateUserId",required = false) Long updateUserId,
            @ApiParam(value = "更新人姓名")@RequestParam(value = "updateUserName",required = false) String updateUserName,
            @ApiParam(value = "分页参数(页数)",example="1") @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20") @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        SimpleFilterBean [] filters=FilterUtil.getSimpleJsonFilter(MenuBean.class);
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("url",url, StringCondition.Handler.ALL_LIKE),
            new StringCondition("icon",icon, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("orderNum",orderNum, NumberCondition.Handler.EQUAL),
            new DateCondition("createTime",createTimeBegin, DateCondition.Handler.GE),
            new DateCondition("createTime",createTimeEnd, DateCondition.Handler.LE),
            new NumberCondition("createUserId",createUserId, NumberCondition.Handler.EQUAL),
            new StringCondition("createUserName",createUserName, StringCondition.Handler.ALL_LIKE),
            new DateCondition("updateTime",updateTimeBegin, DateCondition.Handler.GE),
            new DateCondition("updateTime",updateTimeEnd, DateCondition.Handler.LE),
            new NumberCondition("updateUserId",updateUserId, NumberCondition.Handler.EQUAL),
            new StringCondition("updateUserName",updateUserName, StringCondition.Handler.ALL_LIKE)
        );
        if(pageNum==null||pageSize==null){
            return JsonMessage.success(menuService.findAll(condition)).toMappingJacksonValue(filters);
        }else{
            return JsonMessage.success(menuService.findAll(condition,PageRequest.of(pageNum-1,pageSize))).toMappingJacksonValue(filters);
        }

    }

    /**
     * 保存菜单
     * @param menu
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存菜单",notes = "保存菜单")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存菜单")})
    public JsonMessage save(@ApiParam(value = "菜单实体") @RequestBody MenuBean menu){
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
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除菜单")})
    public JsonMessage delete(@ApiParam(value = "菜单id数组") @RequestParam Long[] ids){
        menuService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
