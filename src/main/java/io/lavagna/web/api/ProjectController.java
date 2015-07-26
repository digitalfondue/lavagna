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

import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.BoardInfo;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.UserWithPermission;
import io.lavagna.model.util.ShortNameGenerator;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.StatisticsService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchService;
import io.lavagna.web.api.model.CreateRequest;
import io.lavagna.web.api.model.Suggestion;
import io.lavagna.web.api.model.TaskStatistics;
import io.lavagna.web.api.model.TaskStatisticsAndHistory;
import io.lavagna.web.api.model.UpdateRequest;
import io.lavagna.web.api.model.ValidationException;
import io.lavagna.web.helper.ExpectPermission;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectController {

	private final ProjectService projectService;
	private final BoardRepository boardRepository;
	private final EventEmitter eventEmitter;
	private final StatisticsService statisticsService;
	private final SearchService searchService;

	@Autowired
	public ProjectController(ProjectService projectService, BoardRepository boardRepository, EventEmitter eventEmitter,
			StatisticsService statisticsService, SearchService searchService) {
		this.projectService = projectService;
		this.boardRepository = boardRepository;
		this.eventEmitter = eventEmitter;
		this.statisticsService = statisticsService;
		this.searchService = searchService;
	}

	@RequestMapping(value = "/api/project", method = RequestMethod.GET)
	public List<Project> findProjects(UserWithPermission user) {
		if (user.getBasePermissions().containsKey(Permission.READ)) {
			return projectService.findAll();
		}
		return projectService.findAllForUserWithPermissionInProject(user);
	}

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/project", method = RequestMethod.POST)
	public void create(@RequestBody CreateRequest project) {
		checkShortName(project.getShortName());
		projectService.create(project.getName(), project.getShortName(), project.getDescription());
		eventEmitter.emitCreateProject(project.getShortName());
	}

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/suggest-project-short-name", method = RequestMethod.GET)
	public Suggestion suggestProjectShortName(@RequestParam("name") String name) {
		return new Suggestion(ShortNameGenerator.generateShortNameFrom(name));
	}

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/check-project-short-name", method = RequestMethod.GET)
	public boolean checkProjectShortName(@RequestParam("name") String name) {
		return ShortNameGenerator.isShortNameValid(name) && !projectService.existsWithShortName(name);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}", method = RequestMethod.GET)
	public Project findByShortName(@PathVariable("projectShortName") String shortName) {
		return projectService.findByShortName(shortName);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/definitions", method = RequestMethod.GET)
	public List<BoardColumnDefinition> findColumnDefinitions(@PathVariable("projectShortName") String shortName) {
		Project project = projectService.findByShortName(shortName);
		return projectService.findColumnDefinitionsByProjectId(project.getId());
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/board", method = RequestMethod.GET)
	public List<BoardInfo> findBoards(@PathVariable("projectShortName") String shortName) {
		Project project = projectService.findByShortName(shortName);
		return boardRepository.findBoardInfo(project.getId());
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/board", method = RequestMethod.POST)
	public void createBoard(@PathVariable("projectShortName") String shortName, @RequestBody CreateRequest board) {
		checkShortName(board.getShortName());
		Project project = projectService.findByShortName(shortName);
		boardRepository.createNewBoard(board.getName(), board.getShortName(), board.getDescription(), project.getId());
		eventEmitter.emitCreateBoard(project.getShortName());
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/definition", method = RequestMethod.PUT)
	public int updateColumnDefinition(@PathVariable("projectShortName") String shortName,
			@RequestBody UpdateColumnDefinition columnDefinition) {
		Project project = projectService.findByShortName(shortName);
		return projectService.updateColumnDefinition(project.getId(), columnDefinition.getDefinition(),
				columnDefinition.getColor());
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}", method = RequestMethod.POST)
	public Project updateProject(@PathVariable("projectShortName") String shortName,
			@RequestBody UpdateRequest updatedProject) {
		Project project = projectService.findByShortName(shortName);
		project = projectService.updateProject(project.getId(), updatedProject.getName(),
				updatedProject.getDescription(), updatedProject.isArchived());
		eventEmitter.emitUpdateProject(shortName);
		return project;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/task-statistics", method = RequestMethod.GET)
	public TaskStatistics projectTaskStatistics(@PathVariable("projectShortName") String projectShortName,
			UserWithPermission user) {
		int projectId = projectService.findByShortName(projectShortName).getId();

		Map<ColumnDefinition, Integer> tasks = searchService
				.findTaksByColumnDefinition(projectId, null, true, user);

		Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions = projectService
				.findMappedColumnDefinitionsByProjectId(projectId);

		return new TaskStatistics(tasks, columnDefinitions);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/statistics/{fromDate}", method = RequestMethod.GET)
	public TaskStatisticsAndHistory projectStatistics(@PathVariable("projectShortName") String projectShortName,
			@PathVariable("fromDate") Date fromDate, UserWithPermission user) {
		int projectId = projectService.findByShortName(projectShortName).getId();

		Map<ColumnDefinition, Integer> tasks = searchService
				.findTaksByColumnDefinition(projectId, null, true, user);

		Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions = projectService
				.findMappedColumnDefinitionsByProjectId(projectId);

		Map<Long, Map<ColumnDefinition, Long>> cardStatus = statisticsService.getCardsStatusByProject(projectId,
				fromDate);


		Integer activeUsers = statisticsService.getActiveUsersOnProject(projectId, fromDate);

		return new TaskStatisticsAndHistory(tasks, columnDefinitions, cardStatus,
				statisticsService.getCreatedAndClosedCardsByProject(projectId, fromDate),
				activeUsers,
				statisticsService.getAverageUsersPerCardOnProject(projectId),
				statisticsService.getAverageCardsPerUserOnProject(projectId),
				statisticsService.getCardsByLabelOnProject(projectId),
				activeUsers > 0 ? statisticsService.getMostActiveCardByProject(projectId, fromDate) : null);
	}

	/**
	 * Check the format of a short name.
	 * <p/>
	 * It must be composed only with uppercase alpha numeric ASCII characters and "_".
	 *
	 * @param shortName
	 */
	private static void checkShortName(String shortName) {
		if (!ShortNameGenerator.isShortNameValid(shortName)) {
			throw new ValidationException();
		}
	}

	@Getter
	@Setter
	public static class UpdateColumnDefinition {
		private int definition;
		private int color;
	}
}
