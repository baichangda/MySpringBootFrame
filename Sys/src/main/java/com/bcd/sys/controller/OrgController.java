package com.bcd.sys.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.RDBUtil;
import com.bcd.sys.bean.OrgBean;
import com.bcd.rdb.define.ErrorDefine;
import com.bcd.sys.service.OrgService;
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
    public JsonMessage save(@RequestBody OrgBean org){
        orgService.save(org);
        return SuccessDefine.SUCCESS_SAVE_SUCCESSED.toJsonMessage();
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
        return SuccessDefine.SUCCESS_DELETE_SUCCESSED.toJsonMessage();
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
    public JsonMessage list(@RequestParam(value = "orgId",required = false) Long orgId){
        SimplePropertyPreFilter[] filters= RDBUtil.getOneDeepJsonFilter(OrgBean.class);
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
    public JsonMessage isUniqueCheck(
            @RequestParam(value = "fieldName",required = true) String fieldName,
            @RequestParam(value = "fieldValue",required = true) String val){
        boolean flag = orgService.isUnique(fieldName, val);
        if (flag==false){
            return ErrorDefine.ERROR_FIELD_VALUE_EXISTED.toJsonMessage();
        }else {
            return JsonMessage.successed();
        }
    }

}
