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

import com.samskivert.mustache.Mustache;
import io.lavagna.common.Json;
import io.lavagna.common.Version;
import io.lavagna.model.Key;
import io.lavagna.model.Role;
import io.lavagna.model.UserToCreate;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.helper.GsonHttpMessageConverter;
import io.lavagna.web.helper.UserSession;
import io.lavagna.web.security.LoginHandler;
import io.lavagna.web.security.SecurityConfiguration;
import io.lavagna.web.security.SecurityConfiguration.*;
import io.lavagna.web.security.login.DemoLogin;
import io.lavagna.web.security.login.LdapLogin;
import io.lavagna.web.security.login.PasswordLogin;
import io.lavagna.web.security.login.LdapLogin.LdapAuthenticator;
import io.lavagna.web.security.login.OAuthLogin;
import io.lavagna.web.security.login.OAuthLogin.OAuthConfiguration;
import io.lavagna.web.security.login.OAuthLogin.OauthConfigurationFetcher;
import io.lavagna.web.security.login.oauth.OAuthServiceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class WebSecurityConfig {

    @Bean
    public SecurityConfiguration configuredApp(ConfigurationRepository configurationRepository, SessionHandler sessionHandler, ApplicationContext context) {

        return new SecurityConfiguration().requestMatcher(onlyWhenSetupComplete(configurationRepository))
                .loginHandlerFinder(loginHandlerFinder(configurationRepository, context))
                .sessionHandler(sessionHandler)
                .request("/favicon.ico").permitAll()
                .request("/favicon/**").permitAll()
                .request("/css/**").permitAll()
                .request("/fonts/**").permitAll()
                .request("/resource-login/**").permitAll()
                .request("/setup/**").denyAll()
                .request("/api/calendar/**").permitAll()
                .request("/api/api-hook/**").permitAll()
                .request("/api/**").requireAuthenticated(false)
                .request("/**").requireAuthenticated()
                .login("/login/**", "/login", loginPageGenerator())
                .logout("/logout/**", "/logout");
    }

    @Bean
    public SecurityConfiguration unconfiguredApp(ConfigurationRepository configurationRepository) {

        return new SecurityConfiguration().requestMatcher(onlyWhenSetupIsNotComplete(configurationRepository))
                .request("/setup/**").permitAll()
                .request("/css/**").permitAll()
                .request("/js/**").permitAll()
                .request("/fonts/**").permitAll()
                .request("/help/**").permitAll()
                .request("/about/**").permitAll()
                .request("/favicon/**").permitAll()
                .request("/**").redirectTo("/setup/")
                .disableLogin();
    }

    private LoginPageGenerator loginPageGenerator() {
        return (req, resp, handlers) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("version", Version.version());
            for (LoginHandler lh : handlers.values()) {
                model.putAll(lh.modelForLoginPage(req));
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            model.put("json", Json.GSON.toJson(model));
            try (InputStream is = req.getServletContext().getResourceAsStream("/WEB-INF/views/login.html")) {
                Mustache.compiler().escapeHTML(false).defaultValue("").compile(new InputStreamReader(is, StandardCharsets.UTF_8)).execute(model, resp.getWriter());
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
            this.anonymous = user.getAnonymous();
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

    public static class AccountCreatorIfMissing {

        private final UserRepository userRepository;
        private final ConfigurationRepository configurationRepository;
        private final UserService userService;


        public AccountCreatorIfMissing(UserRepository userRepository,
                                       ConfigurationRepository configurationRepository,
                                       UserService userService) {
            this.userRepository = userRepository;
            this.configurationRepository = configurationRepository;
            this.userService = userService;
        }

        private void createDefaultUser(String provider, String name) {
            UserToCreate userToCreate = new UserToCreate(provider, name);
            userToCreate.setRoles(Collections.singletonList(Role.Companion.getDEFAULT_ROLE().getName()));
            userService.createUser(userToCreate);
        }

        private boolean canLdap(String provider, String name) {
            return "ldap".equals(provider) &&
                !userRepository.userExists(provider, name) &&
                "true".equals(configurationRepository.getValueOrNull(Key.LDAP_AUTOCREATE_MISSING_ACCOUNT));
        }

        private boolean canCreateUserForOauthProvider(String provider) {
            OAuthConfiguration conf = Json.GSON.fromJson(configurationRepository.getValueOrNull(Key.OAUTH_CONFIGURATION), OAuthConfiguration.class);
            return conf != null && conf.hasProvider(provider) && conf.getProviderWithName(provider).getAutoCreateMissingAccount();
        }

        private boolean canOauth(String provider, String name) {
            return provider.startsWith("oauth.") && !userRepository.userExists(provider, name) && canCreateUserForOauthProvider(provider);
        }

        public void createIfConfiguredAndMissing(String provider, String name) {

            if (canLdap(provider, name) || canOauth(provider, name)) {
                createDefaultUser(provider, name);
            }
        }
    }

    @Bean
    private AccountCreatorIfMissing accountCreatorIfMissing(UserRepository userRepository,
                                                            ConfigurationRepository configurationRepository,
                                                            UserService userService) {
        return new AccountCreatorIfMissing(userRepository, configurationRepository, userService);
    }

    @Bean
    private Users users(final UserRepository userRepository, final AccountCreatorIfMissing accountCreatorIfMissing) {
        return new Users() {
            @Override
            public boolean userExistsAndEnabled(String provider, String name) {
                accountCreatorIfMissing.createIfConfiguredAndMissing(provider, name);
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
        DEMO(DemoLogin.class, "demo"), LDAP(LdapLogin.class, "ldap"), OAUTH(OAuthLogin.class, "oauth"), PASSWORD(PasswordLogin.class, "password");

        LoginHandlerType(Class<? extends LoginHandler> classHandler, String pathAfterLogin) {
            this.classHandler = classHandler;
            this.pathAfterLogin = pathAfterLogin;
        }

        private final Class<? extends LoginHandler> classHandler;
        private final String pathAfterLogin;
    }


    private static SecurityConfiguration.RequestMatcher onlyWhenSetupComplete(final ConfigurationRepository configurationRepository) {
        return request -> "true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
    }

    private static SecurityConfiguration.RequestMatcher onlyWhenSetupIsNotComplete(final ConfigurationRepository configurationRepository) {
        return request -> !"true".equals(configurationRepository.getValueOrNull(Key.SETUP_COMPLETE));
    }

    @Lazy
    @Bean
    public DemoLogin demoLogin(Users users, SessionHandler sessionHandler) {
        return new DemoLogin(users, sessionHandler, "/login?error-demo");
    }

    @Lazy
    @Bean
    public OAuthLogin oauthLogin(Users users, SessionHandler sessionHandler, final ConfigurationRepository configurationRepository) {
        OauthConfigurationFetcher configurationFetcher = () -> Json.GSON.fromJson(configurationRepository.getValueOrNull(Key.OAUTH_CONFIGURATION), OAuthConfiguration.class);
        return new OAuthLogin(users, sessionHandler, configurationFetcher, new OAuthServiceBuilder(), "/login?error-oauth");
    }

    @Lazy
    @Bean
    public LdapLogin ldapLogin(Users users, SessionHandler sessionHandler, final Ldap ldap) {

        LdapAuthenticator authenticator = new LdapAuthenticator() {
            @Override
            public boolean authenticate(String username, String password) {
                return ldap.authenticate(username, password);
            }
            @Override
            public boolean checkUserAvailability(String username) {
                return ldap.checkUserAvailability(username);
            }
        };

        return new LdapLogin(users, sessionHandler, authenticator, "/login?error-ldap");
    }

    @Lazy
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(asList(new FormHttpMessageConverter(), new GsonHttpMessageConverter()));
        return restTemplate;
    }

    @Lazy
    @Bean
    public PasswordLogin passwordLogin(Users users, SessionHandler sessionHandler, UserRepository userRepository) {
        return new PasswordLogin(users, sessionHandler, userRepository, "/login?error-password");
    }

}
