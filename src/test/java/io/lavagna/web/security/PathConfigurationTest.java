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

import io.lavagna.web.security.PathConfiguration;
import io.lavagna.web.security.PathConfiguration.BasicUrlMatcher;
import io.lavagna.web.security.PathConfiguration.LoginUrlMatcher;
import io.lavagna.web.security.PathConfiguration.LogoutUrlMatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

//TODO COMPLETE
@RunWith(MockitoJUnitRunner.class)
public class PathConfigurationTest {

	@Test
	public void testBuildDefault() {
		PathConfiguration pc = new PathConfiguration()//
				.request("/setup/**").denyAll()//
				.request("/**").requireAuthenticated()//
				.login("/login/**", "/login/", "/WEB-INF/views/login.html").logout("/logout/**", "/logout");
		pc.buildMatcherList();
	}

	@Test
	public void testBasicUrlMatcher() {
		PathConfiguration pc = new PathConfiguration();

		BasicUrlMatcher b = new BasicUrlMatcher(pc, "/setup/**");
		b.denyAll();
		b.permitAll();
		b.requireAuthenticated();
	}

	@Test
	public void testLogoutUrlMatcher() {
		new LogoutUrlMatcher("/logout/**", "/logout");
	}

	@Test
	public void testLoginUrlMatcher() {
		new LoginUrlMatcher("/login/**", "/login/", "/WEB-INF/views/login.html");
	}
}
