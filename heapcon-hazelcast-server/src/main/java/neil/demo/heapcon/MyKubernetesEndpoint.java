package neil.demo.heapcon;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Kubernetes needs a REST endpoint to test if the application
 * is happy, so provide it. The Hazelcast client has a similar
 * counterpart with different logic.
 * </p>
 */
@RestController
@Slf4j
public class MyKubernetesEndpoint {

	@Autowired
    private HazelcastInstance hazelcastServer;
	
	/**
	 * <p>Kubernetes needs two URLs. The "{@code readiness}" is called repeatedly
	 * to check the server is up, until an HTTP 200 is received. Once it is up
	 * the "{@code liveness}" URL is called periodically to try to detect a
	 * broken JVM.
	 * </p>
	 * 
	 * @param httpServletRequest
	 * @return
	 */
	@GetMapping({"/liveness", "readiness"})
	public String kubernetesEndpoint(HttpServletRequest httpServletRequest) {
		log.info("kubernetesEndpoint() URL: {}", httpServletRequest.getRequestURI());

        ClusterState clusterState = this.hazelcastServer.getCluster().getClusterState();

        if (clusterState == ClusterState.ACTIVE) {
            return Boolean.TRUE.toString();
        } else {
            throw new RuntimeException("ClusterState==" + clusterState);
        }
    }
	
}
