package com.colak.springtutorial.config;

import com.colak.springtutorial.employee.dto.EmployeeDTO;
import com.hazelcast.client.cache.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfiguration {

    // Use a different cache name from hazelcast server. This is just for testing to see that the Cache is created
    public static final String EMPLOYEE_CACHE_NAME = "my-employees";

    @Bean
    public ClientConfig clientConfig() throws IOException {
        XmlClientConfigBuilder xmlClientConfigBuilder = new XmlClientConfigBuilder("client.xml");
        return xmlClientConfigBuilder.build();
    }

    @Bean
    public Cache<Long, EmployeeDTO> employeesCache() {
        // Get the default cache service.
        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastClientCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<Long, EmployeeDTO> mutableConfiguration = getMutableConfiguration();

        // Now create the cache.
        Cache<Long, EmployeeDTO> employeesCache = cacheManager.createCache(EMPLOYEE_CACHE_NAME, mutableConfiguration);
        log.info("employeesCache is constructor : {}", employeesCache);
        return employeesCache;
    }

    private static MutableConfiguration<Long, EmployeeDTO> getMutableConfiguration() {
        MutableConfiguration<Long, EmployeeDTO> mutableConfiguration = new MutableConfiguration<>();
        mutableConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 1)));
        mutableConfiguration.setTypes(Long.class, EmployeeDTO.class);

        mutableConfiguration.setStatisticsEnabled(true);
        mutableConfiguration.setManagementEnabled(true);

        return mutableConfiguration;
    }
}