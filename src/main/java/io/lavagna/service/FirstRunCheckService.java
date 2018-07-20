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
package io.lavagna.service;

import io.lavagna.common.LavagnaEnvironment;
import io.lavagna.model.ConfigurationKeyValue;
import io.lavagna.model.Key;
import io.lavagna.model.UserToCreate;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class FirstRunCheckService {

    private final LavagnaEnvironment environment;
    private final ConfigurationRepository configurationRepository;
    private final SetupService setupService;

    public FirstRunCheckService(LavagnaEnvironment environment,
                                ConfigurationRepository configurationRepository,
                                SetupService setupService) {
        this.environment = environment;
        this.configurationRepository = configurationRepository;
        this.setupService = setupService;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if( !"true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE)) && "true".equals(environment.getProperty("setup.fromData"))) {

            List<ConfigurationKeyValue> configurations = new ArrayList<>();
            addConfIfPresent(Key.BASE_APPLICATION_URL, configurations, true);
            configurations.add(new ConfigurationKeyValue(Key.AUTHENTICATION_METHOD, "['"+environment.getRequiredProperty("setup.AUTHENTICATION_METHOD")+"']"));
            configurations.add(new ConfigurationKeyValue(Key.SETUP_COMPLETE, "true"));

            for(Key conf : Arrays.asList(Key.LDAP_SERVER_URL,
                Key.LDAP_MANAGER_DN,
                Key.LDAP_MANAGER_PASSWORD,
                Key.LDAP_USER_SEARCH_BASE,
                Key.LDAP_USER_SEARCH_FILTER,
                Key.LDAP_AUTOCREATE_MISSING_ACCOUNT,
                Key.OAUTH_CONFIGURATION,
                Key.ENABLE_ANON_USER,
                Key.SMTP_ENABLED,
                Key.SMTP_CONFIG,
                Key.EMAIL_NOTIFICATION_TIMESPAN,
                Key.TRELLO_API_KEY,
                Key.MAX_UPLOAD_FILE_SIZE
                )) {
                addConfIfPresent(conf, configurations, false);
            }



            UserToCreate user = new UserToCreate(environment.getRequiredProperty("setup.admin.user.provider"),
                environment.getRequiredProperty("setup.admin.user.name"));
            user.setPassword(environment.getProperty("setup.admin.user.password"));
            user.setEnabled(true);
            user.setRoles(Collections.singletonList("ADMIN"));
            setupService.updateConfiguration(configurations, user);
        }
    }

    private void addConfIfPresent(Key key, List<ConfigurationKeyValue> configurations, boolean required) {
        String keyName = "setup." + key.name();
        if (required) {
            environment.getRequiredProperty(keyName);
        }
        if (environment.containsProperty(keyName)) {
            configurations.add(new ConfigurationKeyValue(key, environment.getProperty(keyName)));
        }
    }
}
