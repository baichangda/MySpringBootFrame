package com.bcd.base.support_monitor;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 系统监控信息表
 */
@Getter
@Setter
public class SystemData implements Serializable {
    private final static long serialVersionUID = 1L;
    //field
    //cpu物理核心
    private int physicalProcessorNum;

    //cpu逻辑核心
    private int logicalProcessorNum;

    //cpu使用百分比
    private BigDecimal cpuUsePercent;

    //内存使用百分比
    private BigDecimal memoryUsePercent;

    //最大内存(GB)
    private BigDecimal memoryMax;

    //已使用内存(GB)
    private BigDecimal memoryUse;

    //磁盘最大容量(GB)
    private BigDecimal diskMax;

    //磁盘使用容量(GB)
    private BigDecimal diskUse;

    //磁盘使用百分比
    private BigDecimal diskUsePercent;

    //磁盘读取速度(KB/s)
    private BigDecimal diskReadSpeed;

    //磁盘写入速度(KB/s)
    private BigDecimal diskWriteSpeed;

    //网络流入速度(KB/s)
    private BigDecimal netRecvSpeed;

    //网络流出速度(KB/s)
    private BigDecimal netSentSpeed;

    //method

}
