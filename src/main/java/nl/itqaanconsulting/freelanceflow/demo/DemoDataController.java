package nl.itqaanconsulting.freelanceflow.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
class DemoDataController {

    private final DemoDataService demoDataService;

    DemoDataController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @PostMapping("/reset")
    DemoResetResponse reset() {
        return demoDataService.reset();
    }
}
