package com.bcd.sys.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.define.ErrorDefine;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.FilterUtil;
import com.bcd.sys.bean.EnumItemBean;
import com.bcd.sys.service.EnumItemService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2017/5/18.
 */
@RestController
@RequestMapping("/api/sys/enumItem")
public class EnumItemController extends BaseController{
    @Autowired
    private EnumItemService enumItemService;
    /**
     * 查询所有枚举项
     * @param id
     * @param name
     * @param code
     * @param typeId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询所有枚举项",notes = "查询所有枚举项")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "主键", dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "name",value = "枚举项name", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "枚举项code", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "typeId",value = "枚举类型id", dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "pageNum",value = "当前页数(分页参数)",dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页显示记录数(分页参数)",dataType = "int",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "所有枚举项(根据是否传入分页参数来决定返回值的数据类型)")})
    @SuppressWarnings("unchecked")
    public JsonMessage list(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "typeId",required = false) Long typeId,
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @RequestParam(value = "pageSize",required = false) Integer pageSize){
        Condition condition= Condition.and(
                new NumberCondition("typeId",typeId, NumberCondition.Handler.EQUAL),
                new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL)
        );
        SimplePropertyPreFilter [] filters= FilterUtil.getOneDeepJsonFilter(EnumItemBean.class);
        if(pageNum==null || pageSize==null){
            return JsonMessage.successed(JsonUtil.toJSONResult(enumItemService.findAll(condition), filters));
        }else{
            return JsonMessage.successed(JsonUtil.toJSONResult(enumItemService.findAll(condition,new PageRequest(pageNum-1,pageSize)), filters));
        }
    }

    /**
     * 保存枚举项
     * @param enumItemDTO
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存枚举项",notes = "保存枚举项")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "enumItemDTO",value = "枚举项实体",dataType = "EnumItemDTO",paramType = "body",required = true),
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存枚举项")})
    public JsonMessage save(@RequestBody EnumItemBean enumItemDTO){
        enumItemService.save(enumItemDTO);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除枚举项
     * @param idArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除枚举项",notes = "删除枚举项")
    @ApiImplicitParam(name = "idArr",value = "枚举项id数组",paramType = "query",required = true)
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除枚举项")})
    public JsonMessage delete(@RequestParam Long[] idArr){
        enumItemService.delete(idArr);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
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
        boolean flag = enumItemService.isUnique(fieldName, val);
        if (flag==false){
            return ErrorDefine.ERROR_FIELD_VALUE_EXISTED.toJsonMessage();
        }else {
            return JsonMessage.successed();
        }
    }

}
