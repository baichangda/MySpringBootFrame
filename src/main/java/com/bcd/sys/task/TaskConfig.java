package com.bcd.sys.task;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfig {

    TaskService taskService;

    @Bean
    public TaskBuilder<TaskBean,Long> taskBuilder() {
        return TaskBuilder.newBuilder("common", taskService);
    }
}
