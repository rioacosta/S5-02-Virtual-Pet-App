package S502VirtualPetApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S502MeditationBuddysAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(S502MeditationBuddysAppApplication.class, args);

		System.out.println(
					"╔══════════════════════════════════════════════════════════════════════════╗\n" +
					"║                       MEDITATION BUDDYS API                              ║\n" +
					"║                                                                          ║\n" +
					"║   Application started successfully!                                      ║\n" +
					"║                                                                          ║\n" +
					"║   Swagger UI: http://localhost:8080/swagger-ui/index.html#               ║\n" +
					"║   API Docs:   http://localhost:8080/v3/api-docs                          ║\n" +
					"║                                                                          ║\n" +
					"║   Your virtual sanctuary is ready for mindful journeys.                  ║\n" +
					"║   				Breathe in, breathe out...                 ║\n" +
					"╚══════════════════════════════════════════════════════════════════════════╝\n"
				);
		}
}