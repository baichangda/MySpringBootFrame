package com.sys.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.base.db.rdb.controller.BaseController;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import com.base.util.JsonUtil;
import com.sys.bean.OrgBean;
import com.sys.service.OrgService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author acemma
 * Created by Administrator on 2017/5/10.
 */
@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/org")
public class OrgController extends BaseController{

    @Autowired
    private OrgService orgService;

    /**
     * 保存机构
     * @param org
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存机构",notes="保存机构")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "org",value = "机构实体",dataType = "SysOrgDTO",paramType = "body")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存机构")})
    public JsonMessage<String> save(@RequestBody OrgBean org){
        orgService.save(org);
        return JsonMessage.successed(null,I18nUtil.getMessage("COMMON.SAVE_SUCCESSED"));
    }

    /**
     * 删除机构
     * @param orgIdArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除机构",notes="删除机构")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgIdArr",value = "机构id数组",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除机构")})
    public JsonMessage delete(@RequestParam Long[] orgIdArr){
        orgService.deleteWithNoReferred(orgIdArr);
        return JsonMessage.successed(null,I18nUtil.getMessage("COMMON.DELETE_SUCCESSED"));
    }

    /**
     *查询机构
     * @param orgId
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiOperation(value = "查询机构",notes="查询机构")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgId",value = "机构id",dataType = "Long",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "机构列表")})
    public JsonMessage<String> list(@RequestParam(value = "orgId",required = false) Long orgId){
        SimplePropertyPreFilter[] filters= JsonUtil.getOneDeepJsonFilter(OrgBean.class);
        return JsonMessage.successed(JsonUtil.toDefaultJSONString(orgService.findOne(orgId),filters));
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
    public JsonMessage<Object> isUniqueCheck(
            @RequestParam(value = "fieldName",required = true) String fieldName,
            @RequestParam(value = "fieldValue",required = true) String val){
        boolean flag = orgService.isUnique(fieldName, val);
        if (flag==false){
            return JsonMessage.failed(I18nUtil.getMessage("IsAvailable_FALSE"));
        }else {
            return JsonMessage.successed(null);
        }
    }

}
