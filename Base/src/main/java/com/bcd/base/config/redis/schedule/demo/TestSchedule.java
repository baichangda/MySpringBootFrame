package com.bcd.base.config.redis.schedule.demo;

import com.bcd.base.config.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.base.util.ExcelUtil;
import com.bcd.base.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//@Component
//@Configurable
//@EnableScheduling
public class TestSchedule {

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test1(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test1 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test1 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test2(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test2 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test2 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test3(){
//        String a=null;
//        System.out.println(Thread.currentThread().getId()+" exception===========test3 "+sdf.format(new Date()));
//        a.toString();
        System.out.println(Thread.currentThread().getId()+"===========test3 "+sdf.format(new Date()));
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @ClusterFailedSchedule(lockId = "bcd-test",timeOut = 2000L)
    public void test4(){
        System.out.println(Thread.currentThread().getId()+"===========test4 "+sdf.format(new Date()));
    }

    public static void main(String [] args){
        /**
         * /Users/baichangda/Downloads/con-by-gf18031 补录.xlsx  >> /Users/baichangda/1.txt
         * /Users/baichangda/Downloads/con-by-gf18148 补录.xlsx  >> /Users/baichangda/2.txt
         */
        List<String[]> dbList=new ArrayList<>();
        try (BufferedReader br= Files.newBufferedReader(Paths.get("/Users/baichangda/2.txt"), Charset.forName("GBK"))){
            String line;
            while((line=br.readLine())!=null){
                //id,contract_id,source_name,dest_name,brand_name,model_name,unit_price
                dbList.add(line.split("\t"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dbList.remove(0);
        Map<String,Object[]> dataMap1= dbList.stream().collect(Collectors.toMap(e->{
            return e[1]+","+e[2]+","+e[3]+","+e[4]+","+e[5];
        },e->new Object[]{e[0],e[6]},(e1,e2)->e1));

//        String contractId="con-by-gf18031";
        String contractId="con-by-gf18148";
        List<List> excelList=new ArrayList<>();
        try(InputStream is=Files.newInputStream(Paths.get("/Users/baichangda/Downloads/"+contractId+" 补录.xlsx"))){
            //出发城市	目的地城市	品牌	车系	里程	单价（错误）	单价（修改后）
            excelList= ExcelUtil.readExcel(is,1,2,1,7, row->{
                Cell cell0=row.getCell(0);
                if(cell0==null|| StringUtils.isEmpty(cell0.toString())){
                    return false;
                }else{
                    return true;
                }
            },cell -> {
                return ExcelUtil.readCell(cell);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String,String> updateMap=new LinkedHashMap<>();

        for(int i=0;i<=excelList.size()-1;i++){
            List data1=excelList.get(i);
            String key=contractId+","+data1.get(0)+","+data1.get(1)+","+data1.get(2)+","+data1.get(3);
            String newVal=FormatUtil.formatToString_n_2((Double)data1.get(6));
            Object[] val=dataMap1.get(key);
            String id=val[0].toString();
            String oldVal=FormatUtil.formatToString_n_2(Double.parseDouble(val[1].toString()));
            if(id!=null){
                String sql="update t_contract_price set unit_price="+newVal+" where id="+id+"; -- "+oldVal;
                updateMap.put(key,sql);
                excelList.remove(i);
                i--;
            }
        }

        excelList.forEach(data1->{
            String key=data1.get(0)+","+data1.get(1)+","+data1.get(2)+","+data1.get(3)+","+FormatUtil.formatToString_n_2((Double)data1.get(5))+ ","+FormatUtil.formatToString_n_2((Double)data1.get(6));
            System.out.println(key);
        });

        System.out.println("=========================================================");

        updateMap.forEach((k,v)->{
//            System.out.println(k);
            System.out.println(v);
        });

    }
}
