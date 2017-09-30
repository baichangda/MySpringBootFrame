package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.sys.bean.EnumTypeBean;
import com.bcd.rdb.define.ErrorDefine;
import com.bcd.sys.service.EnumItemService;
import com.bcd.sys.service.EnumTypeService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2017/5/18.
 */
@RestController
@RequestMapping("/api/sys/enumType")
public class EnumTypeController extends BaseController{
    @Autowired
    private EnumTypeService enumTypeService;

    @Autowired
    private EnumItemService enumItemService;
    /**
     * 查询所有枚举类型
     * @param id
     * @param name
     * @param code
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询所有枚举类型",notes = "查询所有枚举类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "主键", dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "name",value = "枚举类型name", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "枚举类型code", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "pageNum",value = "当前页数(分页参数)",dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页显示记录数(分页参数)",dataType = "int",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "所有枚举类型(根据是否传入分页参数来决定返回值的数据类型)")})
    @SuppressWarnings("unchecked")
    public JsonMessage list(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @RequestParam(value = "pageSize",required = false) Integer pageSize){
        Condition condition = Condition.and(
                new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL)
        );
        if(pageNum==null || pageSize==null){
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumTypeService.findAll(condition), EnumTypeBean.getOneDeepJsonFilter()));
        }else{
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumTypeService.findAll(condition,new PageRequest(pageNum-1,pageSize)), EnumTypeBean.getOneDeepJsonFilter()));
        }
    }

    /**
     * 保存枚举类型
     * @param enumTypeDTO
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存枚举类型",notes = "保存枚举类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "enumTypeDTO",value = "枚举类型实体",dataType = "EnumTypeDTO",paramType = "body",required = true),
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存枚举类型")})
    public JsonMessage save(@RequestBody EnumTypeBean enumTypeDTO){
        enumTypeService.save(enumTypeDTO);
        //清空无关系的枚举项
        enumItemService.delete(new NullCondition("typeId"));
        return SuccessDefine.SUCCESS_SAVE_SUCCESSED.toJsonMessage();
    }


    /**
     * 删除枚举类型
     * @param idArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除枚举类型",notes = "删除枚举类型")
    @ApiImplicitParam(name = "idArr",value = "枚举类型id数组",paramType = "query",required = true)
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除枚举类型")})
    public JsonMessage delete(@RequestParam Long[] idArr){
        enumTypeService.delete(idArr);
        return SuccessDefine.SUCCESS_DELETE_SUCCESSED.toJsonMessage();
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
        boolean flag = enumTypeService.isUnique(fieldName, val);
        if (flag==false){
            return ErrorDefine.ERROR_FIELD_VALUE_EXISTED.toJsonMessage();
        }else {
            return JsonMessage.successed();
        }
    }
}
