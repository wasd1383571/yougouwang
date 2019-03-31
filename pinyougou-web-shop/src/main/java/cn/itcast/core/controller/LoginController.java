package cn.itcast.core.controller;


import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /*
     * 页面显示当前登录人
     * */
    @RequestMapping("showName")
    public Map<String,Object> showName(HttpServletRequest request){

        //获取当前登录人用户名
        SecurityContext attribute = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");

        User user = (User) attribute.getAuthentication().getPrincipal();

        String username = user.getUsername();


        HashMap<String, Object> map = new HashMap<>();

        map.put("username",username);

        map.put("curTime",new Date());

        return map;
    }
}
