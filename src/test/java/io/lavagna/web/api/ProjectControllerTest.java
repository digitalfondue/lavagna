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

import io.lavagna.model.*;
import io.lavagna.service.*;
import io.lavagna.web.api.model.CreateRequest;
import io.lavagna.web.api.model.UpdateRequest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

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
	private StatisticsService statisticsService;
	@Mock
	private User user;
	@Mock
	private BoardColumnRepository boardColumnRepository;
	@Mock
	private ExcelExportService excelExportService;
    @Mock
    private MailTicketService mailTicketService;

	private ProjectController projectController;

	private Project project;
    private Board board;
    private BoardColumn col;
    private BoardColumn col2;

	private final String projectShortName = "TEST";

	private ProjectMailTicketConfig mailConfig;

    private ProjectMailTicketConfigData mailTicketConfigData = new ProjectMailTicketConfigData("pop3",
        "inboundserver",
        1,
        "user",
        "password",
        null,
        "",
        "outboundServer",
        2,
        "smtp",
        "noreply@test.com",
        "user",
        "password",
        ""
    );

	@Before
	public void prepare() {
		projectController = new ProjectController(projectService, boardRepository, eventEmitter, statisticsService,
				searchService, boardColumnRepository, excelExportService, mailTicketService);

		project = new Project(0, "test", projectShortName, "Test Project", false);
        board = new Board(0, "test", "TEST", null, project.getId(), false);
        col = new BoardColumn(0, "col1", 0, board.getId(), BoardColumn.BoardColumnLocation.BOARD, 0, ColumnDefinition.OPEN, 0);
        col = new BoardColumn(1, "col2", 1, board.getId(), BoardColumn.BoardColumnLocation.BOARD, 0, ColumnDefinition.OPEN, 0);
        mailConfig = new ProjectMailTicketConfig(0,"config", true, project.getId(), new Date(), mailTicketConfigData.toString(), "subject", "body");
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

    @Test
    public void testCreateMailTicketConfig() {
	    ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col.getId(),
            mailConfig.getId(),
            "{}");

        when(projectService.findIdByShortName(projectShortName)).thenReturn(project.getId());
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);
        when(boardColumnRepository.findById(ticket.getColumnId())).thenReturn(col);
        when(boardRepository.findBoardById(col.getBoardId())).thenReturn(board);
        when(mailTicketService.addTicket(ticket.getName(),
            ticket.getAlias(),
            ticket.getSendByAlias(),
            ticket.getNotificationOverride(),
            ticket.getSubject(),
            ticket.getBody(),
            ticket.getColumnId(),
            ticket.getConfigId(),
            ticket.getMetadata())).thenReturn(ticket);

        projectController.addMailTicketConfig(projectShortName, ticket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMailTicketConfigToAnotherProject() {
        Project project1 = new Project(1, "test1", "TEST1", "Test1 Project", false);

        ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col.getId(),
            mailConfig.getId(),
            "{}");

        when(projectService.findIdByShortName(project1.getShortName())).thenReturn(project1.getId());
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);

        projectController.addMailTicketConfig(project1.getShortName(), ticket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMailTicketConfigToColumnInAnotherProject() {
        Project project1 = new Project(1, "test1", "TEST1", "Test1 Project", false);
        Board board1 = new Board(1, "test1", "TEST1", null, project1.getId(), false);
        BoardColumn col1 = new BoardColumn(2, "col3", 0, board1.getId(), BoardColumn.BoardColumnLocation.BOARD, 1, ColumnDefinition.OPEN, 1);

        ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col1.getId(),
            mailConfig.getId(),
            "{}");



        when(projectService.findIdByShortName(projectShortName)).thenReturn(project.getId());
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);
        when(boardColumnRepository.findById(ticket.getColumnId())).thenReturn(col1);
        when(boardRepository.findBoardById(col1.getBoardId())).thenReturn(board1);

        projectController.addMailTicketConfig(projectShortName, ticket);
    }

    @Test
    public void testUpdateMailTicketConfig() {
        ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col.getId(),
            mailConfig.getId(),
            "{}");

        when(projectService.findIdByShortName(projectShortName)).thenReturn(project.getId());
        when(mailTicketService.findTicket(ticket.getId())).thenReturn(ticket);
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);
        when(boardColumnRepository.findById(ticket.getColumnId())).thenReturn(col);
        when(boardRepository.findBoardById(col.getBoardId())).thenReturn(board);
        when(mailTicketService.updateTicket(
            ticket.getId(),
            ticket.getName(),
            ticket.getEnabled(),
            ticket.getAlias(),
            ticket.getSendByAlias(),
            ticket.getNotificationOverride(),
            ticket.getSubject(),
            ticket.getBody(),
            ticket.getColumnId(),
            ticket.getConfigId(),
            ticket.getMetadata())).thenReturn(1);

        projectController.updateMailTicketConfig(projectShortName, ticket.getId(), ticket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMailTicketConfigToAnotherProject() {
        Project project1 = new Project(1, "test1", "TEST1", "Test1 Project", false);

        ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col.getId(),
            mailConfig.getId(),
            "{}");

        when(projectService.findIdByShortName(project1.getShortName())).thenReturn(project1.getId());
        when(mailTicketService.findTicket(ticket.getId())).thenReturn(ticket);
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);

        projectController.updateMailTicketConfig(project1.getShortName(), ticket.getId(), ticket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMailTicketConfigToColumnInAnotherProject() {
        Project project1 = new Project(1, "test1", "TEST1", "Test1 Project", false);
        Board board1 = new Board(1, "test1", "TEST1", null, project1.getId(), false);
        BoardColumn col1 = new BoardColumn(2, "col3", 0, board1.getId(), BoardColumn.BoardColumnLocation.BOARD, 1, ColumnDefinition.OPEN, 1);

        ProjectMailTicket ticket = new ProjectMailTicket(0,
            "ticketConfig",
            true,
            "alias@example.com",
            false,
            false,
            null,
            null,
            col1.getId(),
            mailConfig.getId(),
            "{}");

        when(projectService.findIdByShortName(projectShortName)).thenReturn(project.getId());
        when(mailTicketService.findTicket(ticket.getId())).thenReturn(ticket);
        when(mailTicketService.findConfig(ticket.getConfigId())).thenReturn(mailConfig);
        when(boardColumnRepository.findById(ticket.getColumnId())).thenReturn(col1);
        when(boardRepository.findBoardById(col1.getBoardId())).thenReturn(board1);

        projectController.updateMailTicketConfig(projectShortName, ticket.getId(), ticket);
    }
}
