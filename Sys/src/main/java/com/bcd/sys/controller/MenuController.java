package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.service.MenuService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/menu")
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;



    /**
     * 查询菜单列表
     * @return
     */
    @RequiresNotePermissions(NotePermission.menu_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询菜单列表",notes = "查询菜单列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "parentId", value = "父菜单id", dataType = "String"),
        @ApiImplicitParam(name = "name", value = "菜单名称", dataType = "String"),
        @ApiImplicitParam(name = "url", value = "url地址", dataType = "String"),
        @ApiImplicitParam(name = "icon", value = "图标", dataType = "String"),
        @ApiImplicitParam(name = "orderNum", value = "排序", dataType = "String")
    })
    @ApiResponse(code = 200,message = "菜单列表")
    public JsonMessage<List<MenuBean>> list(
        @RequestParam(value = "id",required = false) Long id,
        @RequestParam(value = "parentId",required = false) Long parentId,
        @RequestParam(value = "name",required = false) String name,
        @RequestParam(value = "url",required = false) String url,
        @RequestParam(value = "icon",required = false) String icon,
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
    @RequiresNotePermissions(NotePermission.menu_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询菜单列表",notes = "查询菜单分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "parentId", value = "父菜单id", dataType = "String"),
        @ApiImplicitParam(name = "name", value = "菜单名称", dataType = "String"),
        @ApiImplicitParam(name = "url", value = "url地址", dataType = "String"),
        @ApiImplicitParam(name = "icon", value = "图标", dataType = "String"),
        @ApiImplicitParam(name = "orderNum", value = "排序", dataType = "String"),
        @ApiImplicitParam(name = "pageNum", value = "分页参数(页数)", dataType = "String"),
        @ApiImplicitParam(name = "pageSize", value = "分页参数(页大小)", dataType = "String")
    })
    @ApiResponse(code = 200,message = "菜单分页结果集")
    public JsonMessage<Page<MenuBean>> page(
        @RequestParam(value = "id", required = false) Long id,
        @RequestParam(value = "parentId", required = false) Long parentId,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "url", required = false) String url,
        @RequestParam(value = "icon", required = false) String icon,
        @RequestParam(value = "orderNum", required = false) Integer orderNum,
        @RequestParam(value = "pageNum",required = false)Integer pageNum,
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
    @RequiresNotePermissions(NotePermission.menu_edit)
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存菜单",notes = "保存菜单")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "菜单实体") @Validated @RequestBody MenuBean menu){
        menuService.save(menu);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除菜单
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.menu_edit)
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除菜单",notes = "删除菜单")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "菜单id数组") @RequestParam Long[] ids){
        menuService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

    /**
     * 查询当前用户所属组织的admin拥有的权限的菜单树
     * @return
     */
    @RequiresNotePermissions(NotePermission.menu_authorize)
    @RequestMapping(value = "/adminMenuTree",method = RequestMethod.POST)
    @ApiOperation(value = "查询当前用户所属组织的admin拥有的权限的菜单树",notes = "查询当前用户所属组织的admin拥有的权限的菜单树")
    @ApiResponse(code = 200,message = "菜单树")
    public JsonMessage adminMenuTree(){
        List<MenuBean> menuBeanList= menuService.adminMenuTree();
        return JsonMessage.success(menuBeanList);
    }

    /**
     * 查询用户拥有的权限的菜单树
     * @param userId
     * @return
     */
    @RequiresNotePermissions(NotePermission.menu_authorize)
    @RequestMapping(value = "/userMenuTree",method = RequestMethod.POST)
    @ApiOperation(value = "查询用户拥有的权限的菜单树",notes = "查询用户拥有的权限的菜单树")
    @ApiResponse(code = 200,message = "菜单树")
    public JsonMessage userMenuTree(@ApiParam(value = "用户id",example="1")
                                    @RequestParam(value = "userId",required = false) Long userId){
        List<MenuBean> menuBeanList= menuService.userMenuTree(userId);
        return JsonMessage.success(menuBeanList);
    }

    /**
     * 查询当前用户拥有的权限的菜单树
     * @return
     */
    @RequiresNotePermissions(NotePermission.menu_search)
    @RequestMapping(value = "/selfMenuTree",method = RequestMethod.POST)
    @ApiOperation(value = "查询当前用户拥有的权限的菜单树",notes = "查询当前用户拥有的权限的菜单树")
    @ApiResponse(code = 200,message = "菜单树")
    public JsonMessage selfMenuTree(){
        UserBean userBean= ShiroUtil.getCurrentUser();
        List<MenuBean> menuBeanList= menuService.userMenuTree(userBean.getId());
        return JsonMessage.success(menuBeanList);
    }

}
