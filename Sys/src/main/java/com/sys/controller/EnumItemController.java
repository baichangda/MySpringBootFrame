package com.sys.controller;

import com.base.condition.BaseCondition;
import com.base.condition.impl.NumberCondition;
import com.base.condition.impl.StringCondition;
import com.base.controller.BaseController;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import com.base.util.JsonUtil;
import com.sys.bean.EnumItemBean;
import com.sys.service.EnumItemService;
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
@RequestMapping("/api/sys/enumItem")
public class EnumItemController extends BaseController{
    @Autowired
    private EnumItemService enumItemBO;
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
    public JsonMessage<Object> list(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "typeId",required = false) Long typeId,
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @RequestParam(value = "pageSize",required = false) Integer pageSize){
        List<BaseCondition> conditionList = Stream.of(
                new NumberCondition("typeId",typeId,NumberCondition.Handler.EQUAL),
                new StringCondition("name",name,StringCondition.Handler.ALL_LIKE),
                new StringCondition("code",code,StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id,NumberCondition.Handler.EQUAL)
        ).collect(Collectors.toList());
        if(pageNum==null || pageSize==null){
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumItemBO.findAll(conditionList), EnumItemBean.getOneDeepJsonFilter()));
        }else{
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(enumItemBO.findAll(conditionList,new PageRequest(pageNum-1,pageSize)), EnumItemBean.getOneDeepJsonFilter()));
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
    public JsonMessage<Object> save(@RequestBody EnumItemBean enumItemDTO){
        enumItemBO.save(enumItemDTO);
        return new JsonMessage<>(true, I18nUtil.getMessage("COMMON.SAVE_SUCCESSED"));
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
    public JsonMessage<Object> delete(@RequestParam Long[] idArr){
        enumItemBO.delete(idArr);
        return new JsonMessage<>(true, I18nUtil.getMessage("COMMON.DELETE_SUCCESSED"));
    }


    /**
     * 验证枚举项名称是否可用
     * @param name
     * @return
     */
    @RequestMapping(value = "/checkNameIsAvailable", method = RequestMethod.GET)
    @ApiOperation(value="验证枚举项名称",notes = "验证枚举项名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "枚举项名称", dataType = "String",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "true(可用) false(不可用)")})
    public JsonMessage checkNameIsAvailable(
            @RequestParam(value = "name") String name
    ){
        List<EnumItemBean> list= enumItemBO.findAll(new StringCondition("name",name, StringCondition.Handler.EQUAL));
        if(list==null||list.size()==0){
            return JsonMessage.successed(null);
        }else{
            return JsonMessage.failed(I18nUtil.getMessage("EnumItemController.checkNameIsAvailable.exists"));
        }
    }

    /**
     * 验证枚举项编码是否可用
     * @param code
     * @return
     */
    @RequestMapping(value = "/checkCodeIsAvailable", method = RequestMethod.GET)
    @ApiOperation(value="验证枚举项编码",notes = "验证枚举项编码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code",value = "枚举项编码", dataType = "String",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "true(可用) false(不可用)")})
    public JsonMessage checkCodeIsAvailable(
            @RequestParam(value = "code") String code
    ){
        List<EnumItemBean> list= enumItemBO.findAll(new StringCondition("code",code, StringCondition.Handler.EQUAL));
        if(list==null||list.size()==0){
            return JsonMessage.successed(null);
        }else{
            return JsonMessage.failed(I18nUtil.getMessage("EnumItemController.checkCodeIsAvailable.exists"));
        }
    }

}
