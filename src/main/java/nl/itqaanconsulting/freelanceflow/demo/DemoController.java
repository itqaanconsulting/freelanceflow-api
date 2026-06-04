package nl.itqaanconsulting.freelanceflow.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class DemoController {

    @GetMapping({"/", "/demo", "/demo/"})
    String demo() {
        return "redirect:/demo/index.html";
    }
}
