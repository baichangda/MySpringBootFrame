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
    @ApiResponse(code = 200,message = "机构列表")
    public JsonMessage<List<OrgBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父组织id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "组织层级编码(格式为1_2_3_,必须以_结尾)")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "组织名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "地址")
            @RequestParam(value = "address",required = false) String address,
            @ApiParam(value = "电话")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "备注")
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
    @ApiResponse(code = 200,message = "机构分页结果集")
    public JsonMessage<Page<OrgBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父组织id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "组织层级编码(格式为1_2_3_,必须以_结尾)")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "组织名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "地址")
            @RequestParam(value = "address",required = false) String address,
            @ApiParam(value = "电话")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
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
