package S502VirtualPetApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class S502VirtualPetAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(S502VirtualPetAppApplication.class, args);
	}

//	http://localhost:8080/swagger-ui/index.html#
}
