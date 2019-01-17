package com.bcd.sys.rdb.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.bcd.sys.rdb.bean.OrgBean;
import com.bcd.sys.rdb.service.OrgService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/org")
public class OrgController extends BaseController {

    @Autowired
    private OrgService orgService;



    /**
     * 查询组织机构列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询组织机构列表",notes = "查询组织机构列表")
    @ApiResponse(code = 200,message = "组织机构列表")
    public JsonMessage<List<OrgBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父组织id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "组织名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "地址")
            @RequestParam(value = "address",required = false) String address,
            @ApiParam(value = "电话")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("address",address, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(orgService.findAll(condition));
    }

    /**
     * 查询组织机构分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询组织机构列表",notes = "查询组织机构分页")
    @ApiResponse(code = 200,message = "组织结构分页结果集")
    public JsonMessage<Page<OrgBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父组织id",example="1")
            @RequestParam(value = "parentId",required = false) Long parentId,
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
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("address",address, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(orgService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存组织机构
     * @param org
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存组织机构",notes = "保存组织机构")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "组织机构实体") @RequestBody @Validated OrgBean org){
        orgService.save(org);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除组织机构
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除组织机构",notes = "删除组织机构")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "组织机构id数组") @RequestParam Long[] ids){
        orgService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
