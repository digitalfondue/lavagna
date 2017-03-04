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

import io.lavagna.model.Permission;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.ProjectService;
import io.lavagna.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Interceptor for enforcing {@link ExpectPermission} annotation. See
 * {@link #preHandle(HttpServletRequest, HttpServletResponse, Object)} code.
 */
@Component
public class PermissionMethodInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private WebApplicationContext context;

	public static final Set<AbstractPermissionUrlPath> URL_PATTERNS_TO_CHECK;

	static {
		Set<AbstractPermissionUrlPath> p = new HashSet<>();

		p.add(new AbstractPermissionUrlPath.BoardShortNameUrlPath("/board/{shortName}", "shortName"));
		p.add(new AbstractPermissionUrlPath.BoardShortNameUrlPath("/card-mapping/{boardShortName:[A-Z0-9_]+}",
				"boardShortName:[A-Z0-9_]+"));
		p.add(new AbstractPermissionUrlPath.BoardShortNameUrlPath("/card-by-seq/{boardShortName:[A-Z0-9_]+}",
				"boardShortName:[A-Z0-9_]+"));

		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/actionitem/{actionItemId}", "actionItemId"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/actionlist/{actionListId}", "actionListId"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/comment/{commentId}", "commentId"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/file/{fileId}", "fileId"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/from-actionlist/{from}", "from"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/move-to-actionlist/{to}", "to"));
		p.add(new AbstractPermissionUrlPath.CardDataIdUrlPath("/activity/{id}", "id"));

		p.add(new AbstractPermissionUrlPath.CardIdUrlPath("/card/{cardId}", "cardId"));

		p.add(new AbstractPermissionUrlPath.ColumnIdUrlPath("/column/{columnId}", "columnId"));
		p.add(new AbstractPermissionUrlPath.ColumnIdUrlPath("/from-column/{previousColumnId}", "previousColumnId"));
		p.add(new AbstractPermissionUrlPath.ColumnIdUrlPath("/to-column/{newColumnId}", "newColumnId"));

		p.add(new AbstractPermissionUrlPath.EventIdUrlPath("/undo/{eventId}", "eventId"));

		p.add(new AbstractPermissionUrlPath.ProjectShortNameUrlPath("/project/{projectShortName}", "projectShortName"));
		p.add(new AbstractPermissionUrlPath.ProjectShortNameUrlPath("{projectShortName:[A-Z0-9_]+}/manage/", "projectShortName:[A-Z0-9_]+"));
		p.add(new AbstractPermissionUrlPath.ProjectShortNameUrlPath("{projectShortName:[A-Z0-9_]+}/milestones/", "projectShortName:[A-Z0-9_]+"));
		p.add(new AbstractPermissionUrlPath.ProjectShortNameUrlPath("{projectShortName:[A-Z0-9_]+}/search/", "projectShortName:[A-Z0-9_]+"));
		p.add(new AbstractPermissionUrlPath.ProjectShortNameUrlPath("/search/{projectShortName:[A-Z0-9_]+}", "projectShortName:[A-Z0-9_]+"));

		p.add(new AbstractPermissionUrlPath.LabelIdUrlPath("/label/{labelId}", "labelId"));
		p.add(new AbstractPermissionUrlPath.LabelValueIdUrlPath("/card-label-value/{labelValueId}", "labelValueId"));

		p.add(new AbstractPermissionUrlPath.LabelListValuedIdPath("/label-list-values/{labelListValueId}", "labelListValueId"));
		p.add(new AbstractPermissionUrlPath.LabelListValuedIdPath("/cards-by-milestone-detail/{milestoneId}", "milestoneId"));

		p.add(new AbstractPermissionUrlPath.ColumnDefinitionIdUrlPath("/redefine/{newDefinitionId}", "newDefinitionId"));

		URL_PATTERNS_TO_CHECK = Collections.unmodifiableSet(p);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		ExpectPermission expectPermission = ExpectPermission.Helper.getAnnotation(handler);
		if (expectPermission == null) {
			return true;
		}

		Class<? extends OwnershipChecker> ownershipChecker = expectPermission.ownershipChecker();

		UserWithPermission user = UserSession.fetchFromRequest(request, userService);

		if(user == null) {
		    response.sendError(HttpStatus.FORBIDDEN.value());
		    return false;
		}

		// check the base permission
		if (user.getBasePermissions().containsKey(expectPermission.value())) {
			return true;
		}

		// check if the resource has a custom ownership checker (the user must
		// have a read permission)
		if (NoOpOwnershipChecker.class != ownershipChecker && user.getBasePermissions().containsKey(Permission.READ)
				&& context.getBean(expectPermission.ownershipChecker()).hasOwnership(request, user)) {
			return true;
		}
		//

		// TODO: in some cases the user pass a list of ids as a _body_. This
		// kind of bulk operation will need to ensure the correctness with the
		// right filter.

		// project level check
		Set<String> projectIds = extractProjectIdsFromRequestUri(request.getRequestURI(), projectService);
		if (allProjectsIdsHavePermission(projectIds, user, expectPermission.value())) {
			return true;
		}

		// project level ownership check
		if (NoOpOwnershipChecker.class != ownershipChecker
				&& allProjectsIdsHavePermission(projectIds, user, Permission.READ)
				&& context.getBean(expectPermission.ownershipChecker()).hasOwnership(request, user)) {
			return true;
		}

		response.sendError(HttpStatus.FORBIDDEN.value());
		return false;
	}

	private static Set<String> extractProjectIdsFromRequestUri(String requestUri, ProjectService projectService) {
		Set<String> projectIds = new HashSet<>();
		for (AbstractPermissionUrlPath p : URL_PATTERNS_TO_CHECK) {
			Set<String> extracted = p.tryToFetchProjectShortNames(requestUri, projectService);
			projectIds.addAll(extracted);
		}
		return projectIds;
	}


	/***
	 * Check that all the related project have the expected permission
	 *
	 * @param projectIds
	 * @param userName
	 * @param expectedPermission
	 * @return
	 */
	private boolean allProjectsIdsHavePermission(Set<String> projectIds, UserWithPermission user,
			Permission expectedPermission) {

		if (projectIds.isEmpty()) {
			return false;
		}

		for (String projectName : projectIds) {
			if (!user.getPermissionsForProject().containsKey(projectName)
					|| !user.getPermissionsForProject().get(projectName).containsKey(expectedPermission)) {
				return false;
			}
		}
		return true;
	}
}
