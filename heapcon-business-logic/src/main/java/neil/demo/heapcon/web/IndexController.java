package neil.demo.heapcon.web;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>A controller for the landing page. URL "{@code /}".
 * results in the "{@code index}" template file, with
 * logging of the page hit.
 * </p>
 */
@Controller
@Slf4j
public class IndexController {
	
    @GetMapping("/")
    public String index(HttpSession httpSession) {
        log.info("index(), session={}", httpSession.getId());
        
        return "index";
    }
    
}
