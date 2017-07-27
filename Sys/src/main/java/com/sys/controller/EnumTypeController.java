package com.sys.controller;

import com.base.condition.BaseCondition;
import com.base.condition.impl.NullCondition;
import com.base.condition.impl.NumberCondition;
import com.base.condition.impl.StringCondition;
import com.base.controller.BaseController;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import com.base.util.JsonUtil;
import com.sys.bean.EnumTypeBean;
import com.sys.service.EnumItemService;
import com.sys.service.EnumTypeService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Administrator on 2017/5/18.
 */
@RestController
@RequestMapping("/api/enumType")
public class EnumTypeController extends BaseController{
    @Autowired
    private EnumTypeService enumTypeBO;

    @Autowired
    private EnumItemService enumItemBO;
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
    public JsonMessage<Object> list(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @RequestParam(value = "pageSize",required = false) Integer pageSize){
        List<BaseCondition> conditionList = Stream.of(
                new StringCondition("name",name,StringCondition.Handler.ALL_LIKE),
                new StringCondition("code",code,StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id,NumberCondition.Handler.EQUAL)
        ).collect(Collectors.toList());
        if(pageNum==null || pageSize==null){
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumTypeBO.findAll(conditionList), EnumTypeBean.getOneDeepJsonFilter()));
        }else{
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumTypeBO.findAll(conditionList,new PageRequest(pageNum-1,pageSize)), EnumTypeBean.getOneDeepJsonFilter()));
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
    public JsonMessage<Object> save(@RequestBody EnumTypeBean enumTypeDTO){
        enumTypeBO.save(enumTypeDTO);
        //清空无关系的枚举项
        enumItemBO.delete(new NullCondition("typeId"));
        return new JsonMessage<>(true, I18nUtil.getMessage("COMMON.SAVE_SUCCESSED"));
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
    public JsonMessage<Object> delete(@RequestParam Long[] idArr){
        enumTypeBO.delete(idArr);
        return new JsonMessage<>(true, I18nUtil.getMessage("COMMON.DELETE_SUCCESSED"));
    }


    /**
     * 验证枚举类型名称是否可用
     * @param name
     * @return
     */
    @RequestMapping(value = "/checkNameIsAvailable", method = RequestMethod.GET)
    @ApiOperation(value="验证枚举类型名称",notes = "验证枚举类型名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "枚举类型名称", dataType = "String",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "true(可用) false(不可用)")})
    public JsonMessage checkNameIsAvailable(
            @RequestParam(value = "name") String name
    ){
        List<EnumTypeBean> list= enumTypeBO.findAll(new StringCondition("name",name, StringCondition.Handler.EQUAL));
        if(list==null||list.size()==0){
            return JsonMessage.successed(null);
        }else{
            return JsonMessage.failed(I18nUtil.getMessage("EnumTypeController.checkNameIsAvailable.exists"));
        }
    }

    /**
     * 验证枚举类型编码是否可用
     * @param code
     * @return
     */
    @RequestMapping(value = "/checkCodeIsAvailable", method = RequestMethod.GET)
    @ApiOperation(value="验证枚举类型编码",notes = "验证枚举类型编码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code",value = "枚举类型编码", dataType = "String",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "true(可用) false(不可用)")})
    public JsonMessage checkCodeIsAvailable(
            @RequestParam(value = "code") String code
    ){
        List<EnumTypeBean> list= enumTypeBO.findAll(new StringCondition("code",code, StringCondition.Handler.EQUAL));
        if(list==null||list.size()==0){
            return JsonMessage.successed(null);
        }else{
            return JsonMessage.failed(I18nUtil.getMessage("EnumTypeController.checkCodeIsAvailable.exists"));
        }
    }
}
