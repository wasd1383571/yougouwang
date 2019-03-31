package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.Date;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserDao userDao;
    @Override
    public void sendCode(final String phone) {

        //生成验证码
       final String random = RandomStringUtils.randomNumeric(6);
        //将生成的验证码保存到缓存中
        redisTemplate.boundValueOps(phone).set(random);
        //设置有效时间
        //redisTemplate.boundValueOps(phone).expire(1, TimeUnit.MINUTES);
        //发送消息 map
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage map = session.createMapMessage();

                //手机号
                map.setString("iphone",phone);
                //短信签名
                map.setString("SignName","品优购商城");
                //短信模板
                map.setString("TemplateCode","SMS_161365070");
                //验证码
                map.setString("TemplateParam","{'number':'"+random+"'}");

                return map;
            }
        });
    }
    /*
    * 完成注册
    * */
    @Override
    public void add(User user, String smscode){
        //获取到验证码
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        //判断验证码是否失效
        if (null == code){
            throw new RuntimeException("验证码失效");
        }
        //如果验证码一致
        if (smscode.equals(code)){

            //设置创建时间
            user.setCreated(new Date());
            //设置更新时间
            user.setUpdated(new Date());
            //设置密码加密
            /*BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));*/

            userDao.insertSelective(user);
        }else{

            throw new RuntimeException("验证码不一致");
        }
    }
}
