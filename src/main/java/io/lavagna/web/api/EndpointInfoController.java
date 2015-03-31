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
package io.lavagna.web.api;

import io.lavagna.model.Permission;
import io.lavagna.web.helper.AbstractPermissionUrlPath;
import io.lavagna.web.helper.ExpectPermission;
import io.lavagna.web.helper.PermissionMethodInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@ExpectPermission(Permission.ADMINISTRATION)
@RestController
public class EndpointInfoController {

	private final RequestMappingHandlerMapping handlerMapping;

	@Autowired
	public EndpointInfoController(RequestMappingHandlerMapping handlerMapping) {
		this.handlerMapping = handlerMapping;
	}

	@RequestMapping(value = "/api/admin/endpoint-info", method = RequestMethod.GET)
	public EndpointsInfo getAllEndpoints() {
		List<EndpointInfo> res = new ArrayList<>();
		Set<String> pathVariables = new TreeSet<>();
		for (Entry<RequestMappingInfo, HandlerMethod> kv : handlerMapping.getHandlerMethods().entrySet()) {

			for (String p : kv.getKey().getPatternsCondition().getPatterns()) {
				pathVariables.addAll(extractPathVariables(p));
			}
			res.add(new EndpointInfo(kv));
		}

		Collections.sort(res);

		return new EndpointsInfo(pathVariables, res);
	}

	private static Set<String> extractPathVariables(String pattern) {

		Set<String> identifierAndPathVariable = new HashSet<>();
		// match stuff like "/project/{projectShortName}" or /{projectShortName}/value
		Pattern p = Pattern.compile("(/[^/]+/\\{[^\\}]+\\})|(\\{[^\\}]+\\}/[^/]+/)");
		Matcher m = p.matcher(pattern);
		while (m.find()) {
			identifierAndPathVariable.add(m.group());
		}

		return identifierAndPathVariable;
	}

	@Getter
	public static class EndpointsInfo {
		private final Set<String> idsPathVariable;
		private final List<EndpointInfo> endpointsInfo;
		private final Map<String, String> matchedUrlPath = new HashMap<>();

		public EndpointsInfo(Set<String> idsPathVariable, List<EndpointInfo> endpointsInfo) {
			this.idsPathVariable = idsPathVariable;
			this.endpointsInfo = endpointsInfo;
			for (AbstractPermissionUrlPath p : PermissionMethodInterceptor.URL_PATTERNS_TO_CHECK) {
				matchedUrlPath.put(p.getPath(), p.getPath());
			}
		}
	}

	@Getter
	public static class EndpointInfo implements Comparable<EndpointInfo> {
		private final Set<String> patterns;

		/** can be null */
		private final Permission permission;

		private final Set<RequestMethod> methods;

		private final String handler;

		public EndpointInfo(Entry<RequestMappingInfo, HandlerMethod> kv) {
			patterns = kv.getKey().getPatternsCondition().getPatterns();
			ExpectPermission annotation = ExpectPermission.Helper.getAnnotation(kv.getValue());
			permission = annotation != null ? annotation.value() : null;
			methods = kv.getKey().getMethodsCondition().getMethods();
			handler = kv.getValue().getBeanType().getCanonicalName() + "." + kv.getValue().getMethod().getName();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof EndpointInfo)) {
				return false;
			}
			return compareTo((EndpointInfo) obj) == 0;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(patterns.isEmpty() ? null : patterns.iterator().next())
					.append(methods.isEmpty() ? null : methods.iterator().next()).toHashCode();
		}

		@Override
		public int compareTo(EndpointInfo o) {
			return new CompareToBuilder()
					.append(patterns.isEmpty() ? null : patterns.iterator().next(),
							o.patterns.isEmpty() ? null : o.patterns.iterator().next())
					.append(methods.isEmpty() ? null : methods.iterator().next(),
							o.methods.isEmpty() ? null : o.methods.iterator().next()).toComparison();
		}
	}
}
