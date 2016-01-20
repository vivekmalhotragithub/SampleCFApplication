package com.springboot;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableAutoConfiguration
@ComponentScan
@Configuration
public class Application extends SpringBootServletInitializer implements
		CommandLineRunner {

	private static final Logger log = LoggerFactory
			.getLogger(Application.class);

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		log.info("STARTING APPLICATION :: "+ctx.getApplicationName());
		String[] beanNames = ctx.getBeanDefinitionNames();
		
		log.info("Inspecting  the beans provided by Spring Boot:");
		
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
        	log.info(beanName);
        }
	}

	public void run(String... arg0) throws Exception {
		log.info("Creating tables");

		StringBuilder createTable = new StringBuilder("CREATE TABLE ");
		createTable.append("`Employee`");
		createTable.append("(");
		createTable.append("`id` int NOT NULL AUTO_INCREMENT,");
		createTable.append("`name` varchar(20) DEFAULT NULL,");
		createTable.append("`role` varchar(20) DEFAULT NULL,");
		createTable.append(" PRIMARY KEY (`id`)");
		createTable.append(")");
		createTable.append("ENGINE=InnoDB DEFAULT CHARSET=utf8;");

		jdbcTemplate.execute("DROP TABLE IF EXISTS `Employee`;");
		System.out.println("Executing query " + createTable.toString());
		jdbcTemplate.execute(createTable.toString());

	}

}
