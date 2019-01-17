package com.bcd.sys.rdb.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.rdb.bean.TaskBean;
import com.bcd.sys.task.TaskUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.bcd.sys.rdb.service.TaskService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;



    /**
     * 查询系统任务列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询系统任务列表",notes = "查询系统任务列表")
    @ApiResponse(code = 200,message = "任务列表")
    public JsonMessage<List<TaskBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败;)",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "任务类型(1:普通任务;2:文件类型任务)",example="1")
            @RequestParam(value = "type",required = false) Byte type,
            @ApiParam(value = "任务信息(失败时记录失败原因)")
            @RequestParam(value = "message",required = false) String message,
            @ApiParam(value = "任务开始时间开始")
            @RequestParam(value = "startTimeBegin",required = false) Date startTimeBegin,
            @ApiParam(value = "任务开始时间截止")
            @RequestParam(value = "startTimeEnd",required = false) Date startTimeEnd,
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
            new StringCondition("message",message, StringCondition.Handler.ALL_LIKE),
            new DateCondition("startTime",startTimeBegin, DateCondition.Handler.GE),
            new DateCondition("startTime",startTimeEnd, DateCondition.Handler.LE),
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
    @ApiResponse(code = 200,message = "任务分页结果集")
    public JsonMessage<Page<TaskBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "任务名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败;)",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "任务类型(1:普通任务;2:文件类型任务)",example="1")
            @RequestParam(value = "type",required = false) Byte type,
            @ApiParam(value = "任务信息(失败时记录失败原因)")
            @RequestParam(value = "message",required = false) String message,
            @ApiParam(value = "任务开始时间开始")
            @RequestParam(value = "startTimeBegin",required = false) Date startTimeBegin,
            @ApiParam(value = "任务开始时间截止")
            @RequestParam(value = "startTimeEnd",required = false) Date startTimeEnd,
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
            new StringCondition("message",message, StringCondition.Handler.ALL_LIKE),
            new DateCondition("startTime",startTimeBegin, DateCondition.Handler.GE),
            new DateCondition("startTime",startTimeEnd, DateCondition.Handler.LE),
            new DateCondition("finishTime",finishTimeBegin, DateCondition.Handler.GE),
            new DateCondition("finishTime",finishTimeEnd, DateCondition.Handler.LE),
            new StringCondition("filePaths",filePaths, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(taskService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }


    /**
     * 停止系统任务
     * @param ids
     * @return
     */
    @RequestMapping(value = "/stop",method = RequestMethod.POST)
    @ApiOperation(value = "停止系统任务",notes = "停止系统任务")
    @ApiResponse(code = 200,message = "停止系统任务结果")
    public JsonMessage stop(@ApiParam(value = "系统任务id数组") @RequestParam Long[] ids){
        if(ids!=null&&ids.length>0){
            TaskUtil.stopTask(true,Arrays.stream(ids).map(e->(Serializable)e).toArray(len->new Serializable[len]));
        }
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
