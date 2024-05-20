package csu.RouteGuideBackend.web;

import csu.RouteGuideBackend.domain.member.entity.Member;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class IndexController {
    @GetMapping()
    public String index(@ModelAttribute Member member){
        return "basic/index.html";
    }
    @GetMapping("/join")
    public String join(){
        return "basic/join_form.html";
    }

    @GetMapping("/login")
    public String login(){
        return "basic/login.html";
    }
}
