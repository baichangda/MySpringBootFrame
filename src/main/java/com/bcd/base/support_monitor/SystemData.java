package com.bcd.base.support_monitor;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 系统监控信息表
 */
@Getter
@Setter
public class SystemData implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    //field
    //cpu物理核心
    private int physicalProcessorNum;

    //cpu逻辑核心
    private int logicalProcessorNum;

    //cpu使用百分比
    private double cpuUsePercent;

    //内存使用百分比
    private double memoryUsePercent;

    //最大内存(GB)
    private double memoryMax;

    //已使用内存(GB)
    private double memoryUse;

    //磁盘最大容量(GB)
    private double diskMax;

    //磁盘使用容量(GB)
    private double diskUse;

    //磁盘使用百分比
    private double diskUsePercent;

    //磁盘读取速度(KB/s)
    private double diskReadSpeed;

    //磁盘写入速度(KB/s)
    private double diskWriteSpeed;

    //网络流入速度(KB/s)
    private double netRecvSpeed;

    //网络流出速度(KB/s)
    private double netSentSpeed;

    //method

}
