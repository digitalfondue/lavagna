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

import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import static java.util.EnumSet.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HSTSFilterTest {


    @Mock
    private FilterConfig filterConfig;

    @Mock
    private ServletContext servletContext;

    @Mock
    private WebApplicationContext webApplicationContext;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private SessionCookieConfig sessionCookieConfig;

    @Before
    public void prepare() {
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ConfigurationRepository.class)).thenReturn(configurationRepository);
        when(servletContext.getSessionCookieConfig()).thenReturn(sessionCookieConfig);
    }

    @Test
    public void testHttpsCallWithHttpsEnabled() throws ServletException, IOException {
        HSTSFilter filter = new HSTSFilter();

        Map<Key, String> conf = new EnumMap<>(Key.class);
        conf.put(Key.USE_HTTPS, "true");
        conf.put(Key.BASE_APPLICATION_URL, "https://example.com");

        when(configurationRepository.getValueOrNull(Key.USE_HTTPS)).thenReturn("true");
        when(configurationRepository.findConfigurationFor(of(Key.USE_HTTPS, Key.BASE_APPLICATION_URL))).thenReturn(conf);

        filter.init(filterConfig);

        verify(filterConfig.getServletContext().getSessionCookieConfig()).setSecure(true);


        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSecure(true);
        request.getServletContext().getSessionCookieConfig().setSecure(true);

        filter.doFilterInternal(request, response, chain);

        Assert.assertTrue(response.containsHeader("Strict-Transport-Security"));
    }

    @Test
    public void testHttpCallWithHttpsEnabled() throws ServletException, IOException {
        HSTSFilter filter = new HSTSFilter();

        Map<Key, String> conf = new EnumMap<>(Key.class);
        conf.put(Key.USE_HTTPS, "true");
        conf.put(Key.BASE_APPLICATION_URL, "https://example.com");

        when(configurationRepository.getValueOrNull(Key.USE_HTTPS)).thenReturn("true");
        when(configurationRepository.findConfigurationFor(of(Key.USE_HTTPS, Key.BASE_APPLICATION_URL))).thenReturn(conf);

        filter.init(filterConfig);

        verify(filterConfig.getServletContext().getSessionCookieConfig()).setSecure(true);


        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setSecure(false);
        request.getServletContext().getSessionCookieConfig().setSecure(true);

        filter.doFilterInternal(request, response, chain);

        Assert.assertEquals("https://example.com", response.getRedirectedUrl());
    }

}
