import com.bcd.Application;
import com.bcd.base.util.ExcelUtil;
import com.bcd.base.util.FileUtil;
import com.bcd.mongodb.test.service.TestService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Test {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    TestService testService;

    @org.junit.Test
    public void test(){
        Map<String,Long> result= testService.groupByPostLine();
        List<List> excelList=new ArrayList<>();
        excelList.add(Arrays.asList("班线名称","调度单数量"));
        result.forEach((k,v)->{
            System.out.println(k+"  "+v);
            excelList.add(Arrays.asList(k,v));
        });
        XSSFWorkbook workbook= ExcelUtil.exportExcel_2007(excelList,(cell, o) -> {
            ExcelUtil.inputValue(cell,o);
        });

        Path path= Paths.get("/Users/baichangda/data.xlsx");
        FileUtil.createNewFile(path);
        try(OutputStream os= Files.newOutputStream(path)){
            workbook.write(os);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
