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
package io.lavagna.config;

import static java.util.Arrays.asList;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.GsonHttpMessageConverter;
import io.lavagna.web.security.PathConfiguration;
import io.lavagna.web.security.login.DemoLogin;
import io.lavagna.web.security.login.LdapLogin;
import io.lavagna.web.security.login.OAuthLogin;
import io.lavagna.web.security.login.OAuthLogin.Handler;
import io.lavagna.web.security.login.PersonaLogin;

import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class WebSecurityConfig {

    @Bean(name = "configuredAppPathConf")
    public PathConfiguration configuredApp(ConfigurationRepository configurationRepository) {
        
        return new PathConfiguration().requestMatcher(onlyWhenSetupComplete(configurationRepository))
                .request("/favicon.ico").permitAll()
                .request("/css/all.css").permitAll()
                .request("/setup/**").denyAll()
                .request("/api/calendar/**").permitAll()
                .request("/api/**").requireAuthenticated(false)
                .request("/**").requireAuthenticated()
                .login("/login/**", "/login", "/WEB-INF/views/login.html")
                .logout("/logout/**", "/logout");
    }

    @Bean(name = "unconfiguredAppPathConf")
    public PathConfiguration unconfiguredApp(ConfigurationRepository configurationRepository) {
        
        return new PathConfiguration().requestMatcher(onlyWhenSetupIsNotComplete(configurationRepository))
                .request("/setup/**").permitAll()
                .request("/bootstrap-3.0/**").permitAll()
                .request("/css/**").permitAll()
                .request("/js/**").permitAll()
                .request("/help/**").permitAll()
                .request("/about/**").permitAll().request("/**")
                .redirectTo("/setup/").disableLogin();
    }
    
    private static PathConfiguration.RequestMatcher onlyWhenSetupComplete(final ConfigurationRepository configurationRepository) {
        return new PathConfiguration.RequestMatcher() {
            @Override
            public boolean match(HttpServletRequest request) {
                return "true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
            }
        };
    }
    
    private static PathConfiguration.RequestMatcher onlyWhenSetupIsNotComplete(final ConfigurationRepository configurationRepository) {
        return new PathConfiguration.RequestMatcher() {
            @Override
            public boolean match(HttpServletRequest request) {
                return !"true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
            }
        };
    }

    @Lazy
    @Bean
    public DemoLogin demoLogin(UserRepository userRepository) {
        return new DemoLogin(userRepository, "/login?error-demo");
    }

    @Lazy
    @Bean
    public OAuthLogin oauthLogin(UserRepository userRepository, ConfigurationRepository configurationRepository) {
        return new OAuthLogin(userRepository, configurationRepository, new Handler(new ServiceBuilder()), "/login?error-oauth");
    }

    @Lazy
    @Bean
    public LdapLogin ldapLogin(UserRepository userRepository, ConfigurationRepository configurationRepository, Ldap ldap) {
        return new LdapLogin(userRepository, ldap, "/login?error-ldap");
    }

    @Lazy
    @Bean
    public PersonaLogin personaLogin(UserRepository userRepository, ConfigurationRepository configurationRepository, RestTemplate restTemplate) {
        return new PersonaLogin(userRepository, configurationRepository, restTemplate, "/WEB-INF/views/logout-persona.html");
    }

    @Lazy
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(asList(new FormHttpMessageConverter(), new GsonHttpMessageConverter()));
        return restTemplate;
    }

}
