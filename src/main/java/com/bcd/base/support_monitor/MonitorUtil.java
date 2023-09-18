package com.bcd.base.support_monitor;

import com.bcd.base.util.FloatUtil;
import com.bcd.base.util.JsonUtil;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 监控信息采集工具类
 */
public class MonitorUtil {
    private final static double GB = 1024 * 1024 * 1024;
    private final static double MB = 1024 * 1024;
    private final static double KB = 1024;

    private static BigDecimal half_up(BigDecimal data, int scale) {
        return data.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 此方法会阻塞1秒
     *
     * @return
     */
    public static SystemData collect() {
        SystemData systemData = new SystemData();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        //cpu
        CentralProcessor processor = hal.getProcessor();
        systemData.setPhysicalProcessorNum(processor.getPhysicalProcessorCount());
        systemData.setLogicalProcessorNum(processor.getLogicalProcessorCount());
        long[] cpu_oldTicks = processor.getSystemCpuLoadTicks();

        //内存
        GlobalMemory memory = hal.getMemory();
        long memory_total = memory.getTotal();
        long memory_available = memory.getAvailable();
        systemData.setMemoryMax(FloatUtil.format(memory_total / GB, 2));
        systemData.setMemoryUse(FloatUtil.format((memory_total - memory_available) / GB, 2));
        systemData.setMemoryUsePercent(FloatUtil.format((memory_total - memory_available) * 100d / memory_total, 2));

        //磁盘
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> osFileStores = fileSystem.getFileStores();
        double disk_free = 0d;
        double disk_total = 0d;
        for (OSFileStore fs : osFileStores) {
            disk_free += fs.getUsableSpace();
            disk_total += fs.getTotalSpace();
        }
        systemData.setDiskMax(FloatUtil.format(disk_total / GB, 2));
        systemData.setDiskUse(FloatUtil.format((disk_total - disk_free) / GB, 2));
        systemData.setDiskUsePercent(FloatUtil.format((disk_total - disk_free) * 100d / disk_total, 2));

        //磁盘io
        long prev_disk_io_read = 0;
        long prev_disk_io_write = 0;
        for (HWDiskStore disk : hal.getDiskStores()) {
            prev_disk_io_read += disk.getReadBytes();
            prev_disk_io_write += disk.getWriteBytes();
        }

        //网络io
        long prev_net_recv = 0;
        long prev_net_sent = 0;
        for (NetworkIF net : hal.getNetworkIFs()) {
            prev_net_recv += net.getBytesRecv();
            prev_net_sent += net.getBytesSent();
        }


        Util.sleep(1000);

        //cpu
        systemData.setCpuUsePercent(FloatUtil.format(processor.getSystemCpuLoadBetweenTicks(cpu_oldTicks) * 100, 2));

        //磁盘io
        long cur_disk_io_read = 0;
        long cur_disk_io_write = 0;
        for (HWDiskStore disk : hal.getDiskStores()) {
            cur_disk_io_read += disk.getReadBytes();
            cur_disk_io_write += disk.getWriteBytes();
        }
        systemData.setDiskReadSpeed(FloatUtil.format((cur_disk_io_read - prev_disk_io_read) / KB, 2));
        systemData.setDiskWriteSpeed(FloatUtil.format((cur_disk_io_write - prev_disk_io_write) / KB, 2));
        //网络io
        long cur_net_recv = 0;
        long cur_net_sent = 0;
        for (NetworkIF net : hal.getNetworkIFs()) {
            net.updateAttributes();
            cur_net_recv += net.getBytesRecv();
            cur_net_sent += net.getBytesSent();
        }
        systemData.setNetRecvSpeed(FloatUtil.format((cur_net_recv - prev_net_recv) / KB, 2));
        systemData.setNetSentSpeed(FloatUtil.format((cur_net_sent - prev_net_sent) / KB, 2));
        return systemData;
    }


    public static void main(String[] args) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            SystemData systemData = collect();
            System.out.println(JsonUtil.toJson(systemData));
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);
    }
}
