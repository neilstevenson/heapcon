package neil.demo.heapcon;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.GroupProperty;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Client configuration for Kubernetes, Cloud Foundry or standalone.
 * Having multiple choices makes the configuration slightly have more
 * alternative paths to produce configuration for the Hazelcast client.
 * </p>
 */
@Configuration
@Slf4j
public class MyConfig {

	@Autowired
	private String vcapServices;
	
	/**
	 * <p>
	 * Load the Hazelcast client configuration from an XML file
	 * on the classpath, and possibly adjust it for Kubernetes
	 * or Pivotal Cloud Foundry.
	 * </p>
	 * <p>It is possible to use both Kubernetes and PCF, but
	 * the code here isn't set up to handle this.
	 * </p>
	 * 
	 * @param kubernetes true/false for Kubernetes
	 * @param pcf true/false for PCF
	 * @return Config to use to build a Hazelcast client
	 * @throws IOException
	 */
	@Bean
	public ClientConfig clientConfig(
			Boolean kubernetes,
			Boolean pcf 
			) throws IOException {
		ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();

		if (kubernetes) {
			this.changeConfigForKubernetes(clientConfig);
		}
		if (pcf) {
			this.changeConfigForPCF(clientConfig);
		}

		return clientConfig;
	}


	/**
	 * <p>Q: Are we in Kubernetes ?
	 * </p>
	 * <p>A: If the environment variable <i>we set</i> is present.
	 * Kubernetes doesn't seem to provide one.
	 * </p>
	 *
	 * @return
	 */
	@Bean
	public Boolean kubernetes() {
		boolean kubernetes = System.getProperty("kubernetes", "false").equalsIgnoreCase("true");
		log.info("kubernetes=={}", kubernetes);

		return kubernetes;
	}
	
	/**
	 * <p>Q: Are we in Pivotal Cloud Foundry ?
	 * </p>
	 * <p>A: If the environment variable <i>PCF sets</i> is present.
	 * </p>
	 *
	 * @return
	 */
	@Bean
	public Boolean pcf() {
		boolean pcf = this.vcapServices.length() > 2;
		log.info("pcf=={}", pcf);

		return pcf;
	}

	/**
	 * <p>Create a bean to capture the "{@code VCAP_SERVICES}"
	 * environment variable as we need it more than once.
	 * </p>
	 *
	 * @param environment
	 * @return Default JSON if not found
	 */
    @Bean
    public String vcapServices(Environment environment) {
    	return environment.getProperty("VCAP_SERVICES", "{}");
    }
	
	/**
	 * <p>If we are in Kubernetes, we need to adjust the Hazelcast
	 * configuration from the original "{@code hazelcast-client.xml}" file
	 * loaded from the classpath.
	 * </p>
	 * <p>There are three parts to this:
	 * </p>
	 * <ol>
	 * <li><p><b>Remove old discopvery : </b>The XML file specifies the
	 * TCP/IP addresses to try to find the servers. These addresses
	 * will be wrong now, so turn them off so we don't bother trying them.
	 * <p></li>
	 * <li><p><b>Configure new discovery : </b>Make a Kubernetes discovery
	 * object, since we wish to discover via Kubernetes, and provide the
	 * name of the Kubernetes service (a String) that we will be discovering.
	 * <p></li>
	 * <li><p><b>Turn on new discovery : </b>Activate the plugin discovery
	 * mechanism.
	 * <p></li>
	 * </ol>
	 * 
	 * @param clientConfig Input object which will be modified
	 */
	private void changeConfigForKubernetes(ClientConfig clientConfig) {
		// (1) Deactive the existing discovery mechanism
		clientConfig.getNetworkConfig().getAddresses().clear();

		// (2) Configure Kubernetes discovery
		HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory = new HazelcastKubernetesDiscoveryStrategyFactory();
		DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(
				hazelcastKubernetesDiscoveryStrategyFactory);
		discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
				"service-hazelcast-server");

		// (3) Activate Kubernetes discovery
		clientConfig.setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
		clientConfig.getNetworkConfig().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
	}
	
	/**
	 * <p>PCF provides the bound services via a JSON environment
	 * variable. We need to find the "{@code hazelcast.credentials.members}"
	 * and we get the IP addresses of the servers.
	 * </p>
	 *
	 * @param clientConfig
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void changeConfigForPCF(ClientConfig clientConfig) {
	    BasicJsonParser basicJsonParser = new BasicJsonParser();
	    Map<String, Object> json = basicJsonParser.parseMap(this.vcapServices);

		log.info("VCAP_SERVICES='{}'", json);

	    List hazelcast = (List) json.get("hazelcast");
	    Map credentials = (Map) ((Map) hazelcast.get(0)).get("credentials");
	    List<String> members = (List<String>) credentials.get("members");

		List<String> addresses = clientConfig.getNetworkConfig().getAddresses();
		addresses.clear();

		members
		.stream()
		.forEach(member -> {
			addresses.add(member.replace('"',' ').trim());
		});	}
}
