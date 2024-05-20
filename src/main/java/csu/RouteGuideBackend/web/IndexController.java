package csu.RouteGuideBackend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/join")
    public String join(){
        return "basic/join_form.html";
    }

    @GetMapping("/login")
    public String login(){
        return "basic/login.html";
    }
}
