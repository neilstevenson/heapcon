package neil.demo.heapcon;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.GroupProperty;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Configuration for a Hazelcast server.
 * </p>
 * <p>Spring Boot will use the "{@code Config @Bean}" to
 * create a Hazelcast server instance for us.
 * </p>
 * <p>If we are in Kubernetes, we adjust the configuration
 * to use the Kubernetes discovery mechanism.
 * </p>
 * <p>Note we could specify all the Kubernetes configuration in
 * the "{@code hazelcast.xml} file but then only use it with
 * Kubernetes. Amending the configuration conditionally allows
 * us to use the one module in Kubernetes and elsewhere.
 * </p>
 */
@Configuration
@Slf4j
public class MyConfig {

	/**
	 * <p>Load Hazelcast configuration from a file, and possibly
	 * amend what has been loaded.
	 * </p>
	 *
	 * @return Configuration to build a {@code HazelcastInstance}
	 */
	@Bean
	public Config config() {
		Config config = new ClasspathXmlConfig("hazelcast.xml");

		boolean kubernetes = System.getProperty("kubernetes", "false").equalsIgnoreCase("true");
		log.info("kubernetes=={}", kubernetes);

		if (kubernetes) {
			this.changeConfigForKubernetes(config);
		}

		return config;
	}

	
	/**
	 * <p>If we are in Kubernetes, we need to adjust the Hazelcast
	 * configuration from the original "{@code hazelcast.xml}" file
	 * loaded from the classpath.
	 * </p>
	 * <p>There are four parts to this:
	 * </p>
	 * <ol>
	 * <li><p><b>Remove old discopvery : </b>The XML file specifies the
	 * TCP/IP addresses to try to find other services. These addresses
	 * will be wrong now, so turn them off so we don't bother trying them.
	 * <p></li>
	 * <li><p><b>Configure new discovery : </b>Make a Kubernetes discovery
	 * object, since we wish to discover via Kubernetes, and provide the
	 * name of the Kubernetes service (a String) that we will be discovering.
	 * <p></li>
	 * <li><p><b>Turn on new discopvery : </b>Activate the plugin discovery
	 * mechasnim.
	 * <p></li>
	 * <li><p><b>New Management Center location : </b>Amend the Management
	 * Center URL with the Kubernetes load balancer address that will
	 * allow us to connect to it.
	 * <p></li>
	 * </ol>
	 * 
	 * @param config Input object which will be modified
	 */
	private void changeConfigForKubernetes(Config config) {
		// (1) Deactive the existing discovery mechanism
		config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

		// (2) Configure Kubernetes discovery
		HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory = new HazelcastKubernetesDiscoveryStrategyFactory();
		DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(
				hazelcastKubernetesDiscoveryStrategyFactory);
		discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
				"service-hazelcast-server");

		// (3) Activate Kubernetes discovery
		config.setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
		config.getNetworkConfig().getJoin().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);

		// (4) Where to find Mancenter
		config.getManagementCenterConfig().setEnabled(true)
			.setUrl("http://service-hazelcast-management-center:8080/hazelcast-mancenter");

	}
}
