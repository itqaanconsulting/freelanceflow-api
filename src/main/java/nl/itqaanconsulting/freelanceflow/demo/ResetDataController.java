package nl.itqaanconsulting.freelanceflow.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
class ResetDataController {

    private final ResetDataService resetDataService;

    ResetDataController(ResetDataService resetDataService) {
        this.resetDataService = resetDataService;
    }

    @PostMapping("/reset")
    ResetDataResponse reset() {
        return resetDataService.reset();
    }
}
