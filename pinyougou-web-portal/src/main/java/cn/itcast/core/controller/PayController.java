package cn.itcast.core.controller;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
* 微信支付页
* */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    /*
    * 生成二维码
    * */
    @RequestMapping("createNative")
    public Map<String,String> createNative(){
        //获取当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return payService.createNative(name);
    }
    /*
    * 微信支付
    * */
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        try {
            int i = 0;
            //死循环
            while (true) {
                Map<String,String> map = payService.queryPayStatus(out_trade_no);
                if ("NOTPAY".equals(map.get("trade_state"))) {

                    //间隔一段时间在询问
                    Thread.sleep(5000);
                    i++;
                    if (i >= 60){
                        return new Result(false,"支付超时");
                    }
                }else{

                     //payService.updatePayLog();
                    return new Result(true,"支付成功");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }

}
