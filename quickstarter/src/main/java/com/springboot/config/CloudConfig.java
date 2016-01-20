package com.springboot.config;

import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ServiceScan
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {
	
	@Bean
    public ApplicationInstanceInfo applicationInfo() {
        return cloud().getApplicationInstanceInfo();
    }
	
	 /*@Bean 
	  public ServletContextTemplateResolver templateResolver(){ 
	        ServletContextTemplateResolver resolver=new ServletContextTemplateResolver(); 
	        resolver.setSuffix(".jsp"); 
	        resolver.setPrefix("/resources/templates/"); 
	        resolver.setTemplateMode("HTML5"); 
	        return resolver; 
	    } */

}
