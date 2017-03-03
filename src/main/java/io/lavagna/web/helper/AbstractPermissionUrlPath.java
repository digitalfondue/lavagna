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
package io.lavagna.web.helper;

import io.lavagna.service.ProjectService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPermissionUrlPath {

	private static final Logger LOG = LogManager.getLogger();

	private final String path;
	private final Pattern pattern;

	AbstractPermissionUrlPath(String path, String paramName) {
		this.path = path;

		// we have an internal regex
		if (paramName.contains(":")) {
			this.pattern = Pattern.compile(path.replace("{" + paramName + "}", "(" + (paramName.split(":")[1]) + ")"));
		} else {
			this.pattern = Pattern.compile(path.replace("{" + paramName + "}", "([^/]+)"));
		}
	}

	Set<String> tryToFetchProjectShortNames(String requestUri, ProjectService projectService) {
		LOG.trace("tryToFetchProjectShortNames : uri : {}, pattern: {}", requestUri, pattern);
		Set<String> e = extractAll(requestUri);
		if (e.isEmpty()) {
			return Collections.emptySet();
		}
		return tryToFetchProjectShortName(e, projectService);
	}

	/**
	 * Most of the cases the url has a single id, should not be a performance issue
	 *
	 * @param ids
	 * @param projectService
	 * @return
	 */
	protected abstract Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService);

	private Set<String> extractAll(String uri) {
		Matcher m = pattern.matcher(uri);
		Set<String> groups = new HashSet<>();
		while (m.find()) {
			groups.add(m.group(1));
		}

		LOG.trace("extract all : uri : {}, pattern: {}, groups: {}", uri, pattern, groups);
		return groups;
	}

	private static void addIfNotNull(Set<String> s, String i) {
		if (i != null) {
			s.add(i);
		}
	}

	private static Set<Integer> from(Set<String> ids) {
		Set<Integer> res = new HashSet<>();
		for (String id : ids) {
			try {
				res.add(Integer.parseInt(id));
			} catch (NumberFormatException e) {
				// nope
			}
		}
		return res;
	}

	static class ProjectShortNameUrlPath extends AbstractPermissionUrlPath {
		ProjectShortNameUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> shortName, ProjectService projectService) {
			return shortName;
		}
	}

	static class BoardShortNameUrlPath extends AbstractPermissionUrlPath {
		BoardShortNameUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (String shortName : ids) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByBoardShortname(shortName));
			}
			return res;
		}
	}

	static class CardIdUrlPath extends AbstractPermissionUrlPath {
		CardIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (int cardId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByCardId(cardId));
			}
			return res;
		}
	}

	static class EventIdUrlPath extends AbstractPermissionUrlPath {
		public EventIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		protected Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (int eventId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByEventId(eventId));
			}
			return res;
		}
	}

	static class CardDataIdUrlPath extends AbstractPermissionUrlPath {
		CardDataIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (int cardDataId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByCardDataId(cardDataId));
			}
			return res;
		}
	}

	static class ColumnIdUrlPath extends AbstractPermissionUrlPath {
		ColumnIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (Integer columnId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByColumnId(columnId));
			}
			return res;
		}
	}

	static class LabelIdUrlPath extends AbstractPermissionUrlPath {
		LabelIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (Integer labelId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByLabelId(labelId));
			}
			return res;
		}
	}

	static class LabelValueIdUrlPath extends AbstractPermissionUrlPath {
		LabelValueIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (Integer labelValueId : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByLabelValueId(labelValueId));
			}
			return res;
		}
	}

	static class LabelListValuedIdPath extends AbstractPermissionUrlPath {
		LabelListValuedIdPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (Integer labelListValueIdPath : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByLabelListValudIdPath(labelListValueIdPath));
			}
			return res;
		}
	}

	static class ColumnDefinitionIdUrlPath extends AbstractPermissionUrlPath {
		ColumnDefinitionIdUrlPath(String path, String paramName) {
			super(path, paramName);
		}

		@Override
		public Set<String> tryToFetchProjectShortName(Set<String> ids, ProjectService projectService) {
			Set<String> res = new HashSet<>();
			for (Integer id : from(ids)) {
				addIfNotNull(res, projectService.findRelatedProjectShortNameByColumnDefinitionId(id));
			}
			return res;
		}
	}

	public String getPath() {
		return path;
	}
}
