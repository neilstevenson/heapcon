package neil.demo.heapcon.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;
import neil.demo.heapcon.MyConstants;

/**
 * <p>A controller to prove things are working.
 * This makes available counts of the HTTP Sessions stored in
 * Hazelcast, plus the connection information that this
 * Hazelcast client currently has to the Hazelcast servers.
 * The application will continue to work as the servers
 * come and go (so long as some remain) so we use this
 * controller to see the current cluster members.
 * </p>
 */
@Controller
@Slf4j
public class DebugController {

    @Autowired
    private HazelcastInstance hazelcastClient;
    
    @GetMapping("/debug")
    public ModelAndView debug(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        log.info("debug(), session={}", httpSession.getId());
        
        ModelAndView modelAndView = new ModelAndView("debug");
        
        // Capture the user agent (ie. Chrome, Safari, Firefox)
        if (httpSession.getAttribute(HttpHeaders.USER_AGENT) == null) {
            String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
            httpSession.setAttribute(HttpHeaders.USER_AGENT, (userAgent == null ? "null" : userAgent));
        }
        
        // size() is an unefficient operation, sums across all members, don't use in Production
        long totalSessions = this.hazelcastClient.getMap(MyConstants.JSESSIONID_MAP_NAME).size();
        modelAndView.addObject("totalSessions", Long.toString(totalSessions));

        // What servers is this client currently connected to ?
        List<String> columns = new ArrayList<>();
        columns.add("Host");
        columns.add("Port");
        columns.add("UUID");
        modelAndView.addObject("columns", columns);
        List<List<String>> data = new ArrayList<>();
        this.hazelcastClient
        .getCluster()
        .getMembers()
        .stream()
        .forEach(member -> {
                List<String> datum = new ArrayList<>();
                datum.add(member.getAddress().getHost());
                datum.add(String.valueOf(member.getAddress().getPort()));
                datum.add(member.getUuid());
                data.add(datum);
        });                

        modelAndView.addObject("data", data);
        
        return modelAndView;
    }

}
