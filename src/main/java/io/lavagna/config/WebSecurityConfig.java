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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.lavagna.common.Json;
import io.lavagna.common.Version;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.GsonHttpMessageConverter;
import io.lavagna.web.helper.UserSession;
import io.lavagna.web.security.LoginHandler;
import io.lavagna.web.security.SecurityConfiguration;
import io.lavagna.web.security.SecurityConfiguration.LoginHandlerFinder;
import io.lavagna.web.security.SecurityConfiguration.LoginPageGenerator;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.User;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.DemoLogin;
import io.lavagna.web.security.login.LdapLogin;
import io.lavagna.web.security.login.LdapLogin.LdapAuthenticator;
import io.lavagna.web.security.login.OAuthLogin;
import io.lavagna.web.security.login.OAuthLogin.OAuthConfiguration;
import io.lavagna.web.security.login.OAuthLogin.OauthConfigurationFetcher;
import io.lavagna.web.security.login.PersonaLogin.AudienceFetcher;
import io.lavagna.web.security.login.PersonaLogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.builder.ServiceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.samskivert.mustache.Mustache;

public class WebSecurityConfig {

    @Bean
    public SecurityConfiguration configuredApp(ConfigurationRepository configurationRepository, UserRepository userRepository, SessionHandler sessionHandler, ApplicationContext context) {
        
        return new SecurityConfiguration().requestMatcher(onlyWhenSetupComplete(configurationRepository))
                .loginHandlerFinder(loginHandlerFinder(configurationRepository, context))
                .sessionHandler(sessionHandler)
                .request("/favicon.ico").permitAll()
                .request("/css/**").permitAll()
                .request("/setup/**").denyAll()
                .request("/api/calendar/**").permitAll()
                .request("/api/**").requireAuthenticated(false)
                .request("/**").requireAuthenticated()
                .login("/login/**", "/login", loginPageGenerator())
                .logout("/logout/**", "/logout");
    }
    
    @Bean
    public SecurityConfiguration unconfiguredApp(ConfigurationRepository configurationRepository) {
        
        return new SecurityConfiguration().requestMatcher(onlyWhenSetupIsNotComplete(configurationRepository))
                .request("/setup/**").permitAll()
                .request("/bootstrap-3.0/**").permitAll()
                .request("/css/**").permitAll()
                .request("/js/**").permitAll()
                .request("/help/**").permitAll()
                .request("/about/**").permitAll().request("/**")
                .redirectTo("/setup/").disableLogin();
    }
    
    private LoginPageGenerator loginPageGenerator() {
        return new LoginPageGenerator() {
            
            @Override
            public void generate(HttpServletRequest req, HttpServletResponse resp, Map<String, LoginHandler> handlers) throws IOException {
                Map<String, Object> model = new HashMap<>();
                model.put("version", Version.version());
                for (LoginHandler lh : handlers.values()) {
                    model.putAll(lh.modelForLoginPage(req));
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/html");
                try (InputStream is = req.getServletContext().getResourceAsStream("/WEB-INF/views/login.html")) {
                    Mustache.compiler().defaultValue("").compile(new InputStreamReader(is, StandardCharsets.UTF_8)).execute(model, resp.getWriter());
                }
            }
        };
    }
    
    @Bean
    public SessionHandler sessionHandler(final UserRepository userRepository) {
        return new SessionHandler() {
            @Override
            public void invalidate(HttpServletRequest req, HttpServletResponse resp) {
                UserSession.invalidate(req, resp, userRepository);
            }

            @Override
            public boolean isUserAuthenticated(HttpServletRequest req) {
                return UserSession.isUserAuthenticated(req);
            }
            
            @Override
            public boolean isUserAnonymous(HttpServletRequest req) {
                return UserSession.isUserAnonymous(req);
            }

            @Override
            public void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp) {
                UserSession.setUser(userId, isUserAnonymous, req, resp, userRepository);
            }

            @Override
            public void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp, boolean addRememberMeCookie) {
                UserSession.setUser(userId, isUserAnonymous, req, resp, userRepository, addRememberMeCookie);
            }
        };
    }
    
    private static class WebSecurityUser implements User {
        
        private final int id;
        private final boolean anonymous;
        
        private WebSecurityUser(io.lavagna.model.User user) {
            this.id = user.getId();
            this.anonymous = user.isAnonymous();
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public boolean isAnonymous() {
            return anonymous;
        }
        
    }
    
    @Bean
    private Users users(final UserRepository userRepository) {
        return new Users() {
            @Override
            public boolean userExistsAndEnabled(String provider, String name) {
                return userRepository.userExistsAndEnabled(provider, name);
            }
            @Override
            public User findUserByName(String provider, String name) {
                return new WebSecurityUser(userRepository.findUserByName(provider, name));
            }
        };
    }
    
    private LoginHandlerFinder loginHandlerFinder(final ConfigurationRepository configurationRepository, final ApplicationContext context) {
        return new LoginHandlerFinder() {
            @Override
            public Map<String, LoginHandler> find() {
                LoginHandlerType[] authMethods = Json.GSON.fromJson(configurationRepository.getValue(Key.AUTHENTICATION_METHOD), LoginHandlerType[].class);
                Map<String, LoginHandler> res = new HashMap<String, LoginHandler>();
                for (LoginHandlerType m : authMethods) {
                    res.put(m.pathAfterLogin, context.getBean(m.classHandler));
                }
                return res;
            }
        };
    }

    public enum LoginHandlerType {

        DEMO(DemoLogin.class, "demo"), LDAP(LdapLogin.class, "ldap"), OAUTH(OAuthLogin.class, "oauth"), PERSONA(PersonaLogin.class, "persona");

        LoginHandlerType(Class<? extends LoginHandler> classHandler, String pathAfterLogin) {
            this.classHandler = classHandler;
            this.pathAfterLogin = pathAfterLogin;
        }

        private final Class<? extends LoginHandler> classHandler;
        private final String pathAfterLogin;
    }

    
    private static SecurityConfiguration.RequestMatcher onlyWhenSetupComplete(final ConfigurationRepository configurationRepository) {
        return new SecurityConfiguration.RequestMatcher() {
            @Override
            public boolean match(HttpServletRequest request) {
                return "true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
            }
        };
    }
    
    private static SecurityConfiguration.RequestMatcher onlyWhenSetupIsNotComplete(final ConfigurationRepository configurationRepository) {
        return new SecurityConfiguration.RequestMatcher() {
            @Override
            public boolean match(HttpServletRequest request) {
                return !"true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
            }
        };
    }

    @Lazy
    @Bean
    public DemoLogin demoLogin(Users users, SessionHandler sessionHandler) {
        return new DemoLogin(users, sessionHandler, "/login?error-demo");
    }

    @Lazy
    @Bean
    public OAuthLogin oauthLogin(Users users, SessionHandler sessionHandler, final ConfigurationRepository configurationRepository) {
        OauthConfigurationFetcher configurationFetcher = new OauthConfigurationFetcher() {
            @Override
            public OAuthConfiguration fetch() {
                return Json.GSON.fromJson(configurationRepository.getValueOrNull(Key.OAUTH_CONFIGURATION), OAuthConfiguration.class);
            }
        };
        return new OAuthLogin(users, sessionHandler, configurationFetcher, new ServiceBuilder(), "/login?error-oauth");
    }

    @Lazy
    @Bean
    public LdapLogin ldapLogin(Users users, SessionHandler sessionHandler, final Ldap ldap) {
        
        LdapAuthenticator authenticator = new LdapAuthenticator() {
            @Override
            public boolean authenticate(String username, String password) {
                return ldap.authenticate(username, password);
            }
        };
        
        return new LdapLogin(users, sessionHandler, authenticator, "/login?error-ldap");
    }

    @Lazy
    @Bean
    public PersonaLogin personaLogin(Users users, SessionHandler sessionHandler, final ConfigurationRepository configurationRepository, RestTemplate restTemplate) {
        
        AudienceFetcher audienceFetcher = new AudienceFetcher() {
            @Override
            public String fetch() {
                return configurationRepository.getValue(Key.PERSONA_AUDIENCE);
            }
        };
        
        return new PersonaLogin(users, sessionHandler, audienceFetcher, restTemplate, "/WEB-INF/views/logout-persona.html");
    }

    @Lazy
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(asList(new FormHttpMessageConverter(), new GsonHttpMessageConverter()));
        return restTemplate;
    }

}
