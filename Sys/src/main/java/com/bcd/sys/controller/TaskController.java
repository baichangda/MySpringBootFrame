package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.TaskService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;



    /**
     * 查询系统任务列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询系统任务列表",notes = "查询系统任务列表")
    public JsonMessage<List<TaskBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务状态(1:等待中;2:执行中;2:任务被终止;2:已完成;3:执行失败;)",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "任务类型(1:普通任务;2:文件类型任务)",example="1")
            @RequestParam(value = "type",required = false) Byte type,
            @ApiParam(value = "备注(失败时记录失败原因)")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "任务完成时间开始")
            @RequestParam(value = "finishTimeBegin",required = false) Date finishTimeBegin,
            @ApiParam(value = "任务完成时间截止")
            @RequestParam(value = "finishTimeEnd",required = false) Date finishTimeEnd,
            @ApiParam(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)")
            @RequestParam(value = "filePaths",required = false) String filePaths
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
            new NumberCondition("type",type, NumberCondition.Handler.EQUAL),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE),
            new DateCondition("finishTime",finishTimeBegin, DateCondition.Handler.GE),
            new DateCondition("finishTime",finishTimeEnd, DateCondition.Handler.LE),
            new StringCondition("filePaths",filePaths, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(taskService.findAll(condition));
    }

    /**
     * 查询系统任务分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询系统任务列表",notes = "查询系统任务分页")
    public JsonMessage<Page<TaskBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务状态(1:等待中;2:执行中;2:任务被终止;2:已完成;3:执行失败;)",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "任务类型(1:普通任务;2:文件类型任务)",example="1")
            @RequestParam(value = "type",required = false) Byte type,
            @ApiParam(value = "备注(失败时记录失败原因)")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "任务完成时间开始")
            @RequestParam(value = "finishTimeBegin",required = false) Date finishTimeBegin,
            @ApiParam(value = "任务完成时间截止")
            @RequestParam(value = "finishTimeEnd",required = false) Date finishTimeEnd,
            @ApiParam(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)")
            @RequestParam(value = "filePaths",required = false) String filePaths,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
            new NumberCondition("type",type, NumberCondition.Handler.EQUAL),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE),
            new DateCondition("finishTime",finishTimeBegin, DateCondition.Handler.GE),
            new DateCondition("finishTime",finishTimeEnd, DateCondition.Handler.LE),
            new StringCondition("filePaths",filePaths, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(taskService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存系统任务
     * @param task
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存系统任务",notes = "保存系统任务")
    public JsonMessage save(@ApiParam(value = "系统任务实体") @RequestBody TaskBean task){
        taskService.save(task);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除系统任务
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除系统任务",notes = "删除系统任务")
    public JsonMessage delete(@ApiParam(value = "系统任务id数组") @RequestParam Long[] ids){
        taskService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
