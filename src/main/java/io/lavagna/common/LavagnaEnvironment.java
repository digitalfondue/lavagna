/**
 * This file is part of lavagna.
 *
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class LavagnaEnvironment {

    private final ConfigurableEnvironment environment;

    private static final String LAVAGNA_CONFIG_LOCATION = "lavagna.config.location";

    private static final Logger LOG = LogManager.getLogger();

    public LavagnaEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;

        if (environment.containsProperty(LAVAGNA_CONFIG_LOCATION) && StringUtils.isNotBlank(environment.getProperty(LAVAGNA_CONFIG_LOCATION))) {

            String configLocation = environment.getProperty(LAVAGNA_CONFIG_LOCATION);

            LOG.info("Detected config file {}, loading it", configLocation);
            try {
                environment.getPropertySources().addFirst(new ResourcePropertySource(new UrlResource(configLocation)));
            } catch (IOException ioe) {
                throw new IllegalStateException("error while loading external configuration file at " + configLocation, ioe);
            }
        }

        setSystemPropertyIfNull(environment, "datasource.dialect", "HSQLDB");
        setSystemPropertyIfNull(environment, "datasource.url", "jdbc:hsqldb:mem:lavagna");
        setSystemPropertyIfNull(environment, "datasource.username", "sa");
        setSystemPropertyIfNull(environment, "datasource.password", "");
        setSystemPropertyIfNull(environment, "spring.profiles.active", "dev");

        logUse("datasource.dialect");
        logUse("datasource.url");
        logUse("datasource.username");
        logUse("spring.profiles.active");

    }

    private void logUse(String name) {
        LOG.info("For property {}, the value is: {}", name, environment.getProperty(name));
    }


    private static void setSystemPropertyIfNull(ConfigurableEnvironment env, String name, String value) {
        if(!env.containsProperty(name) || StringUtils.isBlank(env.getProperty(name))) {
            LOG.warn("Property {} is not set, using default value: {}", name, value);
            Map<String, Object> source = Collections.singletonMap(name, (Object) value);
            env.getPropertySources().addFirst(new MapPropertySource(name, source));
        }
    }

    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    public boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    public String getRequiredProperty(String key) {
        return environment.getRequiredProperty(key);
    }

    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }
}
