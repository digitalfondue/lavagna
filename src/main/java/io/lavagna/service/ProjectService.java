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
package io.lavagna.service;

import io.lavagna.model.*;
import io.lavagna.query.ProjectQuery;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * {@link Project} related service.
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

	private final NamedParameterJdbcTemplate jdbc;
	private final CardLabelRepository cardLabelRepository;
	private final PermissionService permissionService;
	private final ProjectQuery queries;


	public ProjectService(NamedParameterJdbcTemplate jdbc, ProjectQuery queries,
			CardLabelRepository cardLabelRepository, PermissionService permissionService) {
		this.jdbc = jdbc;
		this.queries = queries;
		this.cardLabelRepository = cardLabelRepository;
		this.permissionService = permissionService;
	}

	private static <T> T firstOrNull(List<T> t) {
		return t.isEmpty() ? null : t.get(0);
	}

	@Transactional(readOnly = false)
	public Project create(String name, String shortName, String description) {
		queries.createProject(trimToNull(name), trimToNull(shortName.toUpperCase(Locale.ENGLISH)),
				trimToNull(description));
		Project project = queries.findLastCreatedProject();

		// Add default labels to the Project
		cardLabelRepository.addSystemLabels(project.getId());

		// create default column definitions
		createDefaultColumnDefinitions(project.getId());

		//
		Role anonymousRole = new Role("ANONYMOUS");
		permissionService.createFullRoleInProjectId(anonymousRole, project.getId(), false, true, true);
		permissionService.updatePermissionsToRole(anonymousRole, EnumSet.of(Permission.READ));
		//

		return project;
	}

	private void createDefaultColumnDefinitions(int projectId) {
		List<SqlParameterSource> params = new ArrayList<>(ColumnDefinition.values().length);
		for (ColumnDefinition definition : ColumnDefinition.values()) {
			SqlParameterSource param = new MapSqlParameterSource("value", definition.toString()).addValue("color",
					definition.getDefaultColor()).addValue("projectId", projectId);
			params.add(param);
		}
		jdbc.batchUpdate(queries.createColumnDefinition(), params.toArray(new SqlParameterSource[params.size()]));
	}

	@Transactional(readOnly = false)
	public int updateColumnDefinition(int projectId, int columnDefinitionId, int color) {
		return queries.updateColumnDefinition(color, projectId, columnDefinitionId);
	}

	@Transactional(readOnly = false)
	public Project updateProject(int projectId, String name, String description, boolean archived) {
		queries.updateProject(projectId, name, description, archived);
		return queries.findById(projectId);
	}

	/**
	 * Bulk creation of projects. Will skip the project that already exists.
	 *
	 * @param projects
	 */
	@Transactional(readOnly = false)
	public ImmutablePair<List<Project>, List<Project>> createMissing(List<Project> projects) {
		List<Project> created = new ArrayList<>();
		List<Project> skipped = new ArrayList<>();
		Set<String> usedShortNames = new HashSet<>();
		for (Project pi : findAll()) {
			usedShortNames.add(pi.getShortName());
		}

		for (Project p : projects) {
			if (!usedShortNames.contains(p.getShortName())) {
				Project createdProject = create(p.getName(), p.getShortName(), p.getDescription());
				updateProject(createdProject.getId(), createdProject.getName(), createdProject.getDescription(), p.getArchived());
				created.add(createdProject);
			} else {
				skipped.add(p);
			}
		}

		return ImmutablePair.of(Collections.unmodifiableList(created), Collections.unmodifiableList(skipped));
	}

	public Project findById(int projectId) {
		return queries.findById(projectId);
	}

	public Project findByShortName(String shortName) {
		return queries.findByShortName(shortName);
	}

	public int findIdByShortName(String shortName) {
	    return queries.findIdByShortName(shortName);
	}


    public List<Project> findAllProjects(UserWithPermission user) {
        if (user.getBasePermissions().containsKey(Permission.READ)) {
            return findAll();
        }
        return findAllForUserWithPermissionInProject(user);
    }

	public List<Project> findAll() {
		return queries.findAll();
	}

	/**
	 * Find the projects that a user has a specific READ permission present as a _project_ level permission.
	 *
	 * @param user
	 * @return
	 */
    public List<Project> findAllForUserWithPermissionInProject(User user) {
		return queries.findAllForUser(user.getId(), Permission.READ.toString());
	}

	// ---------

	public String findRelatedProjectShortNameByBoardShortname(String shortName) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByBoardShortname(shortName)));
	}

	// FIXME: fetch directly the project short name?
	private String fromProjectIdToShortName(Integer projectId) {
		return projectId != null ? findById(projectId).getShortName() : null;
	}

	public String findRelatedProjectShortNameByCardId(int cardId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByCardId(cardId)));
	}

	public String findRelatedProjectShortNameByEventId(int eventId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByEventId(eventId)));
	}

	public String findRelatedProjectShortNameByColumnId(int columnId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByColumnId(columnId)));
	}

	public String findRelatedProjectShortNameByCardDataId(int cardDataId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByCardDataId(cardDataId)));
	}

	public String findRelatedProjectShortNameByLabelId(Integer labelId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByLabelId(labelId)));
	}

	public String findRelatedProjectShortNameByLabelListValudIdPath(Integer labelListValueIdPath) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByLabelListValudIdPath(labelListValueIdPath)));
	}

	public String findRelatedProjectShortNameByColumnDefinitionId(int columnDefinitionId) {
		return fromProjectIdToShortName(firstOrNull(queries
				.findRelatedProjectIdByColumnDefinitionId(columnDefinitionId)));
	}

	public String findRelatedProjectShortNameByLabelValueId(Integer labelValueId) {
		return fromProjectIdToShortName(firstOrNull(queries.findRelatedProjectIdByLabelValueId(labelValueId)));
	}

	public List<BoardColumnDefinition> findColumnDefinitionsByProjectId(int projectId) {
		return queries.findColumnDefinitionsByProjectId(projectId);
	}

	public Map<ColumnDefinition, BoardColumnDefinition> findMappedColumnDefinitionsByProjectId(int projectId) {
		Map<ColumnDefinition, BoardColumnDefinition> mappedDefinitions = new EnumMap<>(ColumnDefinition.class);
		for (BoardColumnDefinition definition : findColumnDefinitionsByProjectId(projectId)) {
			mappedDefinitions.put(definition.getValue(), definition);
		}
		return mappedDefinitions;
	}

	public boolean existsWithShortName(String shortName) {
		return Integer.valueOf(1).equals(queries.existsWithShortName(shortName));
	}

	public List<ProjectWithEventCounts> findProjectsActivityByUserInProjects(int userId, Collection<Integer> projectIds) {
		if (projectIds.isEmpty()) {
			return Collections.emptyList();
		} else {
			return queries.findProjectsByUserActivityInProjects(userId, projectIds);
		}
	}

	public List<ProjectWithEventCounts> findProjectsActivityByUser(int userId) {
		return queries.findProjectsByUserActivity(userId);
	}

    public ProjectMetadata getMetadata(String shortName) {
        SortedMap<Integer, CardLabel> res = new TreeMap<>();
        int projectId = findIdByShortName(shortName);
        for (CardLabel cl : cardLabelRepository.findLabelsByProject(projectId)) {
            res.put(cl.getId(), cl);
        }

        SortedMap<Integer, LabelListValueWithMetadata> labelListValues = cardLabelRepository.findLabeListValueAggregatedByCardLabelId(projectId);

        Map<ColumnDefinition, BoardColumnDefinition> columnsDefinition = findMappedColumnDefinitionsByProjectId(projectId);

        return new ProjectMetadata(shortName, res, labelListValues, columnsDefinition);
    }
}
