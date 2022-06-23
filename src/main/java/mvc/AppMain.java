package mvc;


import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
@ComponentScan({"springboot.services"})
@EntityScan("mvc.model")
@EnableJpaRepositories("springboot.repositories")
public class AppMain{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        LOGGER.info("before launch");
        //SpringApplication.run(AppMain.class, args);
        //launch(args);
        Application.launch(JavaFXApp.class, args);
        LOGGER.info("after launch");
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                // we only care about our particular service controllers
                if (beanName.substring(0, 3).equalsIgnoreCase("person"))
                    System.out.println(beanName);
            }
        };
    }
}
