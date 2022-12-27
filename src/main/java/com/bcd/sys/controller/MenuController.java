package com.bcd.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.support_satoken.anno.NotePermission;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.MenuService;
import com.bcd.sys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/menu")
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;

    /**
     * 保存菜单
     *
     * @param menu
     * @return
     */
    @SaCheckNotePermissions(NotePermission.menu_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @Operation(description = "保存菜单")
    @ApiResponse(responseCode = "200", description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "菜单实体") @Validated @RequestBody MenuBean menu) {
        menuService.save(menu);
        return JsonMessage.success().message("保存成功");
    }


    /**
     * 删除菜单
     *
     * @param ids
     * @return
     */
    @SaCheckNotePermissions(NotePermission.menu_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Operation(description = "删除菜单")
    @ApiResponse(responseCode = "200", description = "删除结果")
    public JsonMessage delete(@Parameter(description = "菜单id数组") @RequestParam Long[] ids) {
        menuService.deleteByIds(ids);
        return JsonMessage.success().message("删除成功");
    }

    /**
     * 查询当前用户所属组织的admin拥有的权限的菜单树
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.menu_authorize)
    @RequestMapping(value = "/adminMenuTree", method = RequestMethod.POST)
    @Operation(description = "查询当前用户所属组织的admin拥有的权限的菜单树")
    @ApiResponse(responseCode = "200", description = "菜单树")
    public JsonMessage<List<MenuBean>> adminMenuTree() {
        List<MenuBean> menuBeanList = menuService.adminMenuTree();
        return JsonMessage.success(menuBeanList);
    }

    /**
     * 查询用户拥有的权限的菜单树
     *
     * @param userId
     * @return
     */
    @SaCheckNotePermissions(NotePermission.menu_authorize)
    @RequestMapping(value = "/userMenuTree", method = RequestMethod.POST)
    @Operation(description = "查询用户拥有的权限的菜单树")
    @ApiResponse(responseCode = "200", description = "菜单树")
    public JsonMessage<List<MenuBean>> userMenuTree(@Parameter(description = "用户id")
                                                    @RequestParam(required = false) Long userId) {
        List<MenuBean> menuBeanList = menuService.userMenuTree(userId);
        return JsonMessage.success(menuBeanList);
    }

    /**
     * 查询当前用户拥有的权限的菜单树
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.menu_search)
    @RequestMapping(value = "/selfMenuTree", method = RequestMethod.POST)
    @Operation(description = "查询当前用户拥有的权限的菜单树")
    @ApiResponse(responseCode = "200", description = "菜单树")
    public JsonMessage<List<MenuBean>> selfMenuTree() {
        UserBean userBean = userService.findOne(new StringCondition("username", StpUtil.getLoginIdAsString()));
        List<MenuBean> menuBeanList = menuService.userMenuTree(userBean.id);
        return JsonMessage.success(menuBeanList);
    }

}
