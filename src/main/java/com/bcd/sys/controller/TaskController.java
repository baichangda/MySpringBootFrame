package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.support_shiro.anno.RequiresNotePermissions;
import com.bcd.base.support_shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.TaskService;
import com.bcd.sys.task.TaskUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;


    /**
     * 查询系统任务列表
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.sysTask_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description = "查询系统任务列表")
    @ApiResponse(responseCode = "200", description = "任务列表")
    public JsonMessage<List<TaskBean>> list(
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "关联机构编码") @RequestParam(required = false) String orgCode,
            @Parameter(description = "任务名称") @RequestParam(required = false) String name,
            @Parameter(description = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)") @RequestParam(required = false) Integer status,
            @Parameter(description = "任务类型(1:普通任务;2:文件类型任务)") @RequestParam(required = false) Byte type,
            @Parameter(description = "任务信息(失败时记录失败原因)") @RequestParam(required = false) String message,
            @Parameter(description = "任务开始时间开始") @RequestParam(required = false) Date startTimeBegin,
            @Parameter(description = "任务开始时间结束") @RequestParam(required = false) Date startTimeEnd,
            @Parameter(description = "任务完成时间开始") @RequestParam(required = false) Date finishTimeBegin,
            @Parameter(description = "任务完成时间结束") @RequestParam(required = false) Date finishTimeEnd,
            @Parameter(description = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)") @RequestParam(required = false) String filePaths
    ) {
        Condition condition = Condition.and(
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("orgCode", orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status", status, NumberCondition.Handler.EQUAL),
                new NumberCondition("type", type, NumberCondition.Handler.EQUAL),
                new StringCondition("message", message, StringCondition.Handler.ALL_LIKE),
                new DateCondition("startTime", startTimeBegin, DateCondition.Handler.GE),
                new DateCondition("startTime", startTimeEnd, DateCondition.Handler.LE),
                new DateCondition("finishTime", finishTimeBegin, DateCondition.Handler.GE),
                new DateCondition("finishTime", finishTimeEnd, DateCondition.Handler.LE),
                new StringCondition("filePaths", filePaths, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(taskService.findAll(condition));
    }

    /**
     * 查询系统任务分页
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.sysTask_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description = "查询系统任务列表")
    @ApiResponse(responseCode = "200", description = "任务分页结果集")
    public JsonMessage<Page<TaskBean>> page(
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "关联机构编码") @RequestParam(required = false) String orgCode,
            @Parameter(description = "任务名称") @RequestParam(required = false) String name,
            @Parameter(description = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)") @RequestParam(required = false) Integer status,
            @Parameter(description = "任务类型(1:普通任务;2:文件类型任务)") @RequestParam(required = false) Byte type,
            @Parameter(description = "任务信息(失败时记录失败原因)") @RequestParam(required = false) String message,
            @Parameter(description = "任务开始时间开始") @RequestParam(required = false) Date startTimeBegin,
            @Parameter(description = "任务开始时间结束") @RequestParam(required = false) Date startTimeEnd,
            @Parameter(description = "任务完成时间开始") @RequestParam(required = false) Date finishTimeBegin,
            @Parameter(description = "任务完成时间结束") @RequestParam(required = false) Date finishTimeEnd,
            @Parameter(description = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)") @RequestParam(required = false) String filePaths,
            @Parameter(description = "分页参数(页数)") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "分页参数(页大小)") @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        Condition condition = Condition.and(
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("orgCode", orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status", status, NumberCondition.Handler.EQUAL),
                new NumberCondition("type", type, NumberCondition.Handler.EQUAL),
                new StringCondition("message", message, StringCondition.Handler.ALL_LIKE),
                new DateCondition("startTime", startTimeBegin, DateCondition.Handler.GE),
                new DateCondition("startTime", startTimeEnd, DateCondition.Handler.LE),
                new DateCondition("finishTime", finishTimeBegin, DateCondition.Handler.GE),
                new DateCondition("finishTime", finishTimeEnd, DateCondition.Handler.LE),
                new StringCondition("filePaths", filePaths, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(taskService.findAll(condition, PageRequest.of(pageNum - 1, pageSize)));
    }


    /**
     * 停止系统任务
     *
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.sysTask_stop)
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    @Operation(description = "停止系统任务")
    @ApiResponse(responseCode = "200", description = "停止系统任务结果")
    public JsonMessage stop(@Parameter(description = "系统任务id数组") @RequestParam Long[] ids) {
        if (ids != null && ids.length > 0) {
            TaskUtil.stopTask_single(Arrays.stream(ids).map(e -> (Serializable) e).toArray(len -> new Serializable[len]));
        }
        return JsonMessage.success().message("停止成功");
    }

}
