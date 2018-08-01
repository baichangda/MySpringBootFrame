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
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.service.OrgService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/org")
public class OrgController extends BaseController {

    @Autowired
    private OrgService orgService;



    /**
     * 查询组织机构列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询组织机构列表",notes = "查询组织机构列表")
    @ApiResponses(value={@ApiResponse(code=200,response = JsonMessage.class,message = "查询组织机构列表")})
    public MappingJacksonValue list(
            @ApiParam(value = "主键",example="1")@RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "父组织id",example="1")@RequestParam(value = "parentId",required = false) Long parentId,
            @ApiParam(value = "组织名称")@RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "地址")@RequestParam(value = "address",required = false) String address,
            @ApiParam(value = "电话")@RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "备注")@RequestParam(value = "remark",required = false) String remark,
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
        SimpleFilterBean [] filters=FilterUtil.getSimpleJsonFilter(OrgBean.class);
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("parentId",parentId, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("address",address, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE),
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
            return JsonMessage.success(orgService.findAll(condition)).toMappingJacksonValue(filters);
        }else{
            return JsonMessage.success(orgService.findAll(condition,PageRequest.of(pageNum-1,pageSize))).toMappingJacksonValue(filters);
        }

    }

    /**
     * 保存组织机构
     * @param org
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存组织机构",notes = "保存组织机构")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存组织机构")})
    public JsonMessage save(@ApiParam(value = "组织机构实体") @RequestBody OrgBean org){
        orgService.save(org);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除组织机构
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除组织机构",notes = "删除组织机构")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除组织机构")})
    public JsonMessage delete(@ApiParam(value = "组织机构id数组") @RequestParam Long[] ids){
        orgService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
