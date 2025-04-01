package ai.llm.cook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("ai.llm.cook.repository")
public class CookApplication {
	public static void main(String[] args) {
		SpringApplication.run(CookApplication.class, args);
	}
}

