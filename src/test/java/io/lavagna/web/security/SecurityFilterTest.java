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
package io.lavagna.web.security;

import io.lavagna.config.WebSecurityConfig;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.UserRepository;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

//TODO COMPLETE the various path....: auth/not auth user. CSRF check
@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterTest {

	@Mock
	private WebApplicationContext webApplicationContext;

	@Mock
	private ConfigurationRepository configurationRepository;

	@Mock
	private SessionHandler sessionHandler;

	@Mock
	private UserRepository userRepository;

	@Mock
	private FilterConfig filterConfig;

	@Mock
	private ServletContext servletContext;

	@Before
	public void prepare() {
		WebSecurityConfig webSecurityConfig = new WebSecurityConfig();

		//
		Map<String, SecurityConfiguration> paths = new LinkedHashMap<>();
		paths.put("configuredAppPathConf", webSecurityConfig.configuredApp(configurationRepository, sessionHandler, webApplicationContext));
		paths.put("unconfiguredAppPathConf", webSecurityConfig.unconfiguredApp(configurationRepository));
		//

		when(webApplicationContext.getBeansOfType(SecurityConfiguration.class)).thenReturn(paths);
		when(filterConfig.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(webApplicationContext);
	}

	@Test
	public void test() throws IOException, ServletException {

		SecurityFilter sf = new SecurityFilter();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");

		Map<Key, String> conf = new EnumMap<>(Key.class);
		conf.put(Key.SETUP_COMPLETE, "true");
		when(configurationRepository.getValueOrNull(Mockito.eq(Key.SETUP_COMPLETE))).thenReturn("true");

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		sf.init(filterConfig);

		sf.doFilterInternal(request, response, chain);
	}

	@Test
	public void testSetupNotComplete() throws IOException, ServletException {

		SecurityFilter sf = new SecurityFilter();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");

		Map<Key, String> conf = new EnumMap<>(Key.class);
		conf.put(Key.SETUP_COMPLETE, "false");
		when(configurationRepository.getValueOrNull(Mockito.eq(Key.SETUP_COMPLETE))).thenReturn(null);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		sf.init(filterConfig);
		sf.doFilterInternal(request, response, chain);
	}
}
