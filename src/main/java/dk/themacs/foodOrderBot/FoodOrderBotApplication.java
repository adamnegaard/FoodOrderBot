package dk.themacs.foodOrderBot;

import com.slack.api.bolt.App;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ServletComponentScan
public class FoodOrderBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodOrderBotApplication.class, args);
	}

}
