package neil.demo.heapcon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Start <a href="http://spring.io/projects/spring-boot">Spring Boot</a> and allow
 * it to do the rest. Spring Boot will use the {@link MyConfig} class to construct
 * configuration for a Hazelcast server, deduce we want a Hazelcast server, and
 * use our configuration {@code @Bean} to as input to building it.
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
