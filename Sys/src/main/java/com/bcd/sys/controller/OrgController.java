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
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.service.OrgService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/org")
public class OrgController extends BaseController {

    @Autowired
    private OrgService orgService;



    /**
     * 查询机构列表
     * @return
     */
    @RequiresNotePermissions(NotePermission.org_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询机构列表",notes = "查询机构列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "parentId", value = "父组织id", dataType = "String"),
        @ApiImplicitParam(name = "code", value = "组织层级编码(格式为1_2_3_,必须以_结尾)", dataType = "String"),
        @ApiImplicitParam(name = "name", value = "组织名称", dataType = "String"),
        @ApiImplicitParam(name = "address", value = "地址", dataType = "String"),
        @ApiImplicitParam(name = "phone", value = "电话", dataType = "String"),
        @ApiImplicitParam(name = "remark", value = "备注", dataType = "String")
    })
    @ApiResponse(code = 200,message = "机构列表")
    public JsonMessage<List<OrgBean>> list(
        @RequestParam(value = "id",required = false) Long id,
        @RequestParam(value = "parentId",required = false) Long parentId,
        @RequestParam(value = "code",required = false) String code,
        @RequestParam(value = "name",required = false) String name,
        @RequestParam(value = "address",required = false) String address,
        @RequestParam(value = "phone",required = false) String phone,
        @RequestParam(value = "remark",required = false) String remark
    ){
        UserBean curUser= ShiroUtil.getCurrentUser();
        code=curUser.getType()==1?code:curUser.getOrgCode();
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("code",code, StringCondition.Handler.LEFT_LIKE),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("address",address, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(orgService.findAll(condition));
    }

    /**
     * 查询机构分页
     * @return
     */
    @RequiresNotePermissions(NotePermission.org_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询机构列表",notes = "查询机构分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "parentId", value = "父组织id", dataType = "String"),
        @ApiImplicitParam(name = "code", value = "组织层级编码(格式为1_2_3_,必须以_结尾)", dataType = "String"),
        @ApiImplicitParam(name = "name", value = "组织名称", dataType = "String"),
        @ApiImplicitParam(name = "address", value = "地址", dataType = "String"),
        @ApiImplicitParam(name = "phone", value = "电话", dataType = "String"),
        @ApiImplicitParam(name = "remark", value = "备注", dataType = "String"),
        @ApiImplicitParam(name = "pageNum", value = "分页参数(页数)", dataType = "String"),
        @ApiImplicitParam(name = "pageSize", value = "分页参数(页大小)", dataType = "String")
    })
    @ApiResponse(code = 200,message = "机构分页结果集")
    public JsonMessage<Page<OrgBean>> page(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "parentId",required = false) Long parentId,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "address",required = false) String address,
            @RequestParam(value = "phone",required = false) String phone,
            @RequestParam(value = "remark",required = false) String remark,
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @RequestParam(value = "pageSize",required = false) Integer pageSize
    ){
        UserBean curUser= ShiroUtil.getCurrentUser();
        code=curUser.getType()==1?code:curUser.getOrgCode();
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("code",code, StringCondition.Handler.LEFT_LIKE),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("address",address, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(orgService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存机构
     * @param org
     * @return
     */
    @RequiresNotePermissions(NotePermission.org_edit)
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存机构",notes = "保存机构")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "机构实体") @Validated @RequestBody OrgBean org){
        orgService.save(org);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除机构
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.org_edit)
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除机构",notes = "删除机构")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "机构id数组") @RequestParam Long[] ids){
        orgService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
