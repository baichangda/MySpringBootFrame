package com.bcd.sys.controller;

import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.service.MenuService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/menu")
@Api(tags = "菜单管理/MenuController")
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;

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
        return JsonMessage.success().withData(menuBeanList);
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
        return JsonMessage.success().withData(menuBeanList);
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
        return JsonMessage.success().withData(menuBeanList);
    }

}
