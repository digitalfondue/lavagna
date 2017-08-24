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
package io.lavagna.web.support;

import io.lavagna.common.LavagnaEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


//TODO add check
@RunWith(MockitoJUnitRunner.class)
public class ResourceControllerTest {

	@Mock
	private LavagnaEnvironment env;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private ServletContext context;
	@Mock
	private HttpSession session;

	Set<String> s = new HashSet<>();

	@Before
	public void prepare() throws IOException {
		when(env.getActiveProfiles()).thenReturn(new String[] { "dev" });
		s.add("file.js");
		s.add("file.css");
		s.add("file.html");

		when(context.getResourcePaths(anyString())).thenReturn(s);
		when(context.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream(new byte[] { 42, 42, 42 }));
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		when(request.getServletContext()).thenReturn(context);
		when(request.getSession()).thenReturn(session);
	}

	@Test
	public void testIndex() throws IOException {
		new ResourceController(env).handleIndex(request, response);
	}

	@Test
	public void testJs() throws IOException {
		new ResourceController(env).handleJs(request, response);
	}

	@Test
	public void testCss() throws IOException {
		new ResourceController(env).handleCss(request, response);
	}
}
