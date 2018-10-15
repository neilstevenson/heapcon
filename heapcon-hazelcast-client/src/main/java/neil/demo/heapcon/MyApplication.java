package neil.demo.heapcon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Start <a href="http://spring.io/projects/spring-boot">Spring Boot</a> and allow
 * it to do the rest.
 * </p>
 * <p>If we didn't have the {link MyConfig} object creatinng a "{@code ClientConfig}"
 * {@code @Bean}, then Spring would find the "{@code hazelcast-client.xml}" file on the
 * classpath, deduce we want a Hazelcast server, and build one for us. But since we
 * provide a customised {@code ClientConfig}" {@code @Bean} Spring will use that to
 * configure the Hazelcast client it builds.
 * </p>
 * <p>Note in this demo we chose not to do any server-side processing,
 * so the build for this module does not include the business logic
 * module.
 * </p>
 */
@SpringBootApplication
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
