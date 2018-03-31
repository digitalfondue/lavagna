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

import io.lavagna.common.CookieNames;
import io.lavagna.web.security.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.tuckey.web.filters.urlrewrite.gzip.GzipFilter;

import javax.servlet.*;
import javax.servlet.ServletRegistration.Dynamic;
import java.util.Collections;

public class DispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { EnableWebSocketMessageBrocker.class,
                DataSourceConfig.class,//
				PersistenceAndServiceConfig.class,//
				SchedulingServiceConfig.class,//
				WebSecurityConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { WebConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		// initialize cookie
		if(StringUtils.isNotEmpty(System.getProperty(CookieNames.PROPERTY_NAME))) {
			CookieNames.updatePrefix(System.getProperty(CookieNames.PROPERTY_NAME));
		}
		//

		//definition order = execution order, the first executed filter is CSFRFilter

		addFilter(servletContext, "CSFRFilter", CSFRFilter.class, "/*");

		addFilter(servletContext, "RememberMeFilter", RememberMeFilter.class, "/*");

		addFilter(servletContext, "AnonymousUserFilter", AnonymousUserFilter.class, "/*");

		addFilter(servletContext, "SecurityFilter", SecurityFilter.class, "/*");

		addFilter(servletContext, "ETagFilter", ShallowEtagHeaderFilter.class, "*.js", "*.css",//
                "/", "/project/*", "/admin/*", "/me/",//
                "*.html", "*.woff", "*.eot", "*.svg", "*.ttf");

		addFilter(servletContext, "GzipFilter", GzipFilter.class, "*.js", "*.css",//
                "/", "/project/*", "/admin/*", "/me/",//
                "/api/self", "/api/board/*", "/api/project/*");


		servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
		servletContext.getSessionCookieConfig().setHttpOnly(true);
		servletContext.getSessionCookieConfig().setName(CookieNames.getSessionCookieName());
	}

    private static void addFilter(ServletContext context, String filterName, Class<? extends Filter> filterClass, String... urlPatterns) {
	    javax.servlet.FilterRegistration.Dynamic hstsFilter = context.addFilter(filterName, filterClass);
        hstsFilter.setAsyncSupported(true);
        hstsFilter.addMappingForUrlPatterns(null, false, urlPatterns);
	}

	@Override
	protected void customizeRegistration(Dynamic registration) {

		MultipartConfigElement multipartConfigElement = new MultipartConfigElement("");

		registration.setMultipartConfig(multipartConfigElement);
		registration.setInitParameter("dispatchOptionsRequest", "true");
		registration.setAsyncSupported(true);
	}
}
