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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ExcelExportService;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchService;
import io.lavagna.service.StatisticsService;
import io.lavagna.web.api.model.CreateRequest;
import io.lavagna.web.api.model.UpdateRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerTest {

	@Mock
	private ProjectService projectService;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private EventEmitter eventEmitter;
	@Mock
	private SearchService searchService;
	@Mock
	StatisticsService statisticsService;
	@Mock
	private User user;
	@Mock
	BoardColumnRepository boardColumnRepository;
	@Mock
	private ExcelExportService excelExportService;

	private ProjectController projectController;

	private Project project;

	private final String projectShortName = "TEST";

	@Before
	public void prepare() {
		projectController = new ProjectController(projectService, boardRepository, eventEmitter, statisticsService,
				searchService, boardColumnRepository, excelExportService);

		project = new Project(0, "test", projectShortName, "Test Project", false);
	}

	@Test
	public void create() {

		CreateRequest create = new CreateRequest();
		create.setDescription("desc");
		create.setShortName("NAME");
		create.setName("name");

		projectController.create(create, user);

		verify(projectService).create("name", "NAME", "desc");
		verify(eventEmitter).emitCreateProject("NAME", user);
	}

	@Test
	public void update() {
		UpdateRequest updatedProject = new UpdateRequest();
		updatedProject.setName("New name");
		updatedProject.setDescription("Updated desc");

		projectController.updateProject(projectShortName, updatedProject, user);

		verify(projectService).updateProject(eq(project.getId()), eq("New name"), eq("Updated desc"), eq(false));
	}

	@Test
	public void updateColumnDefinition() {
		ProjectController.UpdateColumnDefinition updatedColumnDefinition = new ProjectController.UpdateColumnDefinition();
		updatedColumnDefinition.setDefinition(10);
		updatedColumnDefinition.setColor(5);

		projectController.updateColumnDefinition(project.getShortName(), updatedColumnDefinition, user);

		verify(projectService).updateColumnDefinition(project.getId(), updatedColumnDefinition.getDefinition(),
				updatedColumnDefinition.getColor());
	}

	@Test
	public void createBoard() {
		CreateRequest cb = new CreateRequest();
		cb.setDescription("desc");
		cb.setName("name");
		cb.setShortName("NAME");

		projectController.createBoard("TEST", cb, user);

		verify(boardRepository).createNewBoard("name", "NAME", "desc", project.getId());
		verify(eventEmitter).emitCreateBoard("TEST", "NAME", user);
	}

	@Test
	public void findBoards() {
		projectController.findBoards(projectShortName);
		verify(boardRepository).findBoardInfo(0);
	}

	@Test
	public void findByShortName() {
		projectController.findByShortName(projectShortName);
		verify(projectService).findByShortName(projectShortName);
	}

	@Test
	public void findProjects() {

		UserWithPermission readProject = new UserWithPermission(user, EnumSet.of(Permission.READ),
				Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());
		projectController.findProjects(readProject);
		verify(projectService).findAllProjects(readProject);

		reset(projectService);

		UserWithPermission noReadProject = new UserWithPermission(user, Collections.<Permission>emptySet(),
				Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

		projectController.findProjects(noReadProject);

		verify(projectService, never()).findAll();
        verify(projectService).findAllProjects(readProject);
	}

	@Test
	public void projectStatistics() {
		UserWithPermission readProject = new UserWithPermission(user, EnumSet.of(Permission.READ),
				Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

		Map<ColumnDefinition, BoardColumnDefinition> defs = new EnumMap<>(ColumnDefinition.class);
		defs.put(ColumnDefinition.OPEN, new BoardColumnDefinition(1, project.getId(), ColumnDefinition.OPEN, 0));
		defs.put(ColumnDefinition.CLOSED, new BoardColumnDefinition(2, project.getId(), ColumnDefinition.CLOSED, 0));
		defs.put(ColumnDefinition.BACKLOG, new BoardColumnDefinition(3, project.getId(), ColumnDefinition.BACKLOG, 0));
		defs.put(ColumnDefinition.DEFERRED,
				new BoardColumnDefinition(4, project.getId(), ColumnDefinition.DEFERRED, 0));

		when(projectService.findMappedColumnDefinitionsByProjectId(project.getId())).thenReturn(defs);

		Map<ColumnDefinition, Integer> tasks = new EnumMap<>(ColumnDefinition.class);
		tasks.put(ColumnDefinition.OPEN, 0);
		tasks.put(ColumnDefinition.CLOSED, 0);
		tasks.put(ColumnDefinition.BACKLOG, 0);
		tasks.put(ColumnDefinition.DEFERRED, 0);
		when(searchService.findTaksByColumnDefinition(eq(project.getId()), isNull(Integer.class), any(Boolean.class),
				eq(readProject))).thenReturn(tasks);

		projectController.projectStatistics(projectShortName, new Date(), readProject);

	}

    @Test
    public void testExportMilestoneToExcel() throws IOException {
        UserWithPermission readProject = new UserWithPermission(user, EnumSet.of(Permission.READ), Collections.<String, Set<Permission>>emptyMap(),
                Collections.<Integer, Set<Permission>>emptyMap());
        MockHttpServletResponse mockResp = new MockHttpServletResponse();
        when(excelExportService.exportProjectToExcel("TEST", readProject)).thenReturn(new HSSFWorkbook());
        projectController.exportMilestoneToExcel("TEST", readProject, mockResp);
        verify(excelExportService).exportProjectToExcel(eq("TEST"), eq(readProject));
    }
}
