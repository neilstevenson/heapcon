package neil.demo.heapcon;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;

/**
 * <p>If you want HTTP sessions to be stored in Hazelcast servers
 * include the "{@code @Bean}" below.
 * </p>
 */
@Configuration
public class MyWebConfig {

	@Autowired
	private HazelcastInstance hazelcastClient;

	@Bean
	public WebFilter webFilter() {
		Properties properties = new Properties();

		properties.put("map-name", MyConstants.JSESSIONID_MAP_NAME);
		properties.put("instance-name", this.hazelcastClient.getName());
		properties.put("sticky-session", "false");
		properties.put("use-client", "true");

		return new WebFilter(properties);
	}
}
