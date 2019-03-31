package cn.itcast.core.controller;




import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
* 主界面显示用户名
* */
@RestController
@SuppressWarnings("all")
@RequestMapping("/login")
public class LoginController {


    @RequestMapping("name")
    public Map<String,Object> showName(HttpServletRequest request){
        //获取session
        HttpSession session = request.getSession();
        SecurityContext attribute = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        //用户对象
        User user = (User) attribute.getAuthentication().getPrincipal();
        //获取用户名
        String username = user.getUsername();

        Map<String, Object> map = new HashMap<>();
        map.put("loginName",username);

        return map;

       /* 2:使用SecurityContextHolder 工具类 获取用户名或是用户名对象 当前线程
        String username2 = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("从当前线程中获取的用户名：" + username2);
        3:jsp页面  ${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.username}
        4:jsp页面  <security:authentication  name="principal.username" />*/
    }
}
