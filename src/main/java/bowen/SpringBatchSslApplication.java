package bowen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBatchSslApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchSslApplication.class, args);
	}

}
