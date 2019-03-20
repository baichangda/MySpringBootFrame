package com.bcd.vco.controller;

import com.bcd.base.message.JsonMessage;
import com.incarcloud.vco.data.HostData;
import com.incarcloud.vco.data.VinHostData;
import com.incarcloud.vco.visitor.HostVisitor;
import com.incarcloud.vco.visitor.VCOVisitor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/vco")
@SuppressWarnings("unchecked")
public class VCOController {

    @RequestMapping(value = "/host/list",method = RequestMethod.GET)
    @ApiOperation(value = "查询host列表",notes = "查询host列表")
    @ApiResponse(code = 200,message = "查询host列表")
    public JsonMessage<List<HostData>> list(){
        HostVisitor hostVisitor=new HostVisitor();
        return JsonMessage.success(hostVisitor.list());
    }

    @RequestMapping(value = "/host/vin/packet/list",method = RequestMethod.GET)
    @ApiOperation(value = "查询host下vin的报文数量列表",notes = "查询host下vin的报文数量列表")
    @ApiResponse(code = 200,message = "查询host下vin的报文数量列表")
    public JsonMessage<List<Map<String,Object>>> getHostVinPacketList(){
        HostVisitor hostVisitor=new HostVisitor();
        return JsonMessage.success(hostVisitor.getHostVinPacketList());
    }


    @RequestMapping(value = "/vin/getHost",method = RequestMethod.GET)
    @ApiOperation(value = "查询vin所属的host",notes = "查询vin所属的host")
    @ApiResponse(code = 200,message = "vin所属的host")
    public JsonMessage<VinHostData> getHost(@ApiParam(value = "车辆vin")
                         @RequestParam(value = "vin",required = true) String vin){
        VCOVisitor vcoVisitor=new VCOVisitor(vin);
        return JsonMessage.success(vcoVisitor.getHost());
    }

    @RequestMapping(value = "/vin/getPacketNum",method = RequestMethod.GET)
    @ApiOperation(value = "查询vin的报文数量",notes = "查询vin的报文数量")
    @ApiResponse(code = 200,message = "vin的报文数量")
    public JsonMessage<Integer> getPacketNum(@ApiParam(value = "车辆vin")
                                 @RequestParam(value = "vin",required = true) String vin){
        VCOVisitor vcoVisitor=new VCOVisitor(vin);
        return JsonMessage.success(vcoVisitor.getPacketNum());
    }
}
