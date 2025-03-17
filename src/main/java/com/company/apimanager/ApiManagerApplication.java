package com.company.apimanager;

import com.google.common.base.Strings;
import io.jmix.core.repository.EnableJmixDataRepositories;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.JmxUtils;

import javax.sql.DataSource;

@SpringBootApplication
@EnableJmixDataRepositories
public class ApiManagerApplication extends SpringBootServletInitializer {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(ApiManagerApplication.class, args);
    }

    @Profile("!prod")
    @Bean
    @Primary
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }
    @Profile("!prod")
    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
    @Profile("prod")
    @Bean(name = "dataSource")
    @Primary
    DataSource prodDataSource(ApplicationContext context) {
        JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        DataSource dataSource = lookup.getDataSource("java:comp/env/jdbc/api-manager");

        // to avoid org.springframework.jmx.export.UnableToRegisterMBeanException:
        for (MBeanExporter mbeanExporter : context.getBeansOfType(MBeanExporter.class).values()) {
            if (JmxUtils.isMBean(((Object) dataSource).getClass())) {
                mbeanExporter.addExcludedBean("dataSource");
            }
        }
        return dataSource;
    }
    @EventListener
    public void printApplicationUrl(ApplicationStartedEvent event) {
        LoggerFactory.getLogger(ApiManagerApplication.class).info("Application started at "
                + "http://localhost:"
                + environment.getProperty("local.server.port")
                + Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
    }
}
