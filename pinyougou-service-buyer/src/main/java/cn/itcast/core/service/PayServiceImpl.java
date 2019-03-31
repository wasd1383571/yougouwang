package cn.itcast.core.service;

import cn.itcast.common.utils.HttpClient;
import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.pojo.log.PayLog;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*
 * 微信支付页
 * */
@Service
@Transactional
@SuppressWarnings("all")
public class PayServiceImpl implements PayService {

    //公众账号id
    @Value("${appid}")
    private String appid;
    //商户账号
    @Value("${partner}")
    private String partner;
    //商户密钥
    @Value("${partnerkey}")
    private String partnerkey;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;

     /*
     * 生成二维码
     * */
    //连接微信服务器 Http协议 远程调用
    @Override
    public Map<String, String> createNative(String name){

          //取出缓存中的日志信息 流水表
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(name);


        //String out_trade_no = String.valueOf(idWorker.nextId());

          //浏览器 发出Http请求  响应
          //java代码完成一次 （浏览器发出请求 响应）
          //Apache HttpClient  对象

        //URL  统一下单地址
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpClient httpClient = new HttpClient(url);
        //设置是否https
        httpClient.setHttps(true);

        //入参：
        Map<String, String> param = new HashMap<>();
            //公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        param.put("appid",appid);
            //商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        param.put("mch_id",partner);
            //随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
        //param.put("nonce_str", UUID.randomUUID().toString().replaceAll("-",""));
        param.put("nonce_str", WXPayUtil.generateNonceStr());

            //商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值
            //商品简单描述，该字段请按照规范传递，具体请见参数规定
        param.put("body","瞅啥，掏钱就完了");
            //商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。详见商户订单号
        param.put("out_trade_no",payLog.getOutTradeNo());
            //标价币种	fee_type	否	String(16)	CNY	符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
            //标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
        //param.put("total_fee",String.valueOf(payLog.getTotalFee()));
        param.put("total_fee","1");
            //终端IP	spbill_create_ip	是	String(16)	123.12.12.123	APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
        param.put("spbill_create_ip","127.0.0.1");
            //通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        param.put("notify_url","http://itcast.cn");
            //交易类型	trade_type	是	String(16)	JSAPI	取值如下：JSAPI（扫码 或 关注 公众号），NATIVE（扫码支付），APP（APP支付）等，说明详见参数规定
        param.put("trade_type","NATIVE");

        try {
            //签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
            //签名类型	sign_type	否	String(32)	MD5	签名类型，默认为MD5，支持HMAC-SHA256和MD5。
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);

            //设置参数
            httpClient.setXmlParam(signedXml);
            //提交请求
            httpClient.post();
            //返回响应
            String content = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            //判断是否成功
            if ("SUCCESS".equals(map.get("return_code"))){

                //页面需要显示订单号和金额
                //成功 返回响应中，没有订单号 和 金额
                map.put("total_fee",String.valueOf(payLog.getTotalFee()));
                map.put("out_trade_no",payLog.getOutTradeNo());

                return map;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回失败信息
        return null;
    }


    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {


        //查询有没有支付
        //查询 接口连接
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        HttpClient httpClient = new HttpClient(url);
        //设置https
        httpClient.setHttps(true);
        //入参
        HashMap<String, String> param = new HashMap<>();
        //参数:
            //公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        param.put("appid",appid);
            //商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        param.put("mch_id",partner);
            //微信订单号	transaction_id	二选一	String(32)	1009660380201506130728806387	微信的订单号，建议优先使用
            //商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        param.put("out_trade_no",out_trade_no);
            //随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        param.put("nonce_str",WXPayUtil.generateNonceStr());
            //签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	通过签名算法计算得出的签名值，详见签名生成算法
        try {
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
            //设置参数
            httpClient.setXmlParam(xml);
            //提交响应
            httpClient.post();
            //返回
            String content = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
   /* @Autowired
    private PayLogDao payLogDao;*/

    /*@Override
    public void updatePayLog() {
        PayLog payLog = new PayLog();
        //支付完成时间
        payLog.setPayTime(new Date());
        //订单交易流水号
        payLog.setTransactionId(String.valueOf(idWorker.nextId()));
        //交易状态
        payLog.setTradeState("1");

        payLogDao.updateByPrimaryKeySelective(payLog);

    }*/
}
