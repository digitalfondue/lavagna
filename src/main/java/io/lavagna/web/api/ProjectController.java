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
import io.lavagna.model.util.ShortNameGenerator;
import io.lavagna.service.*;
import io.lavagna.web.api.model.*;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.Validate;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class ProjectController {

    private final ProjectService projectService;
    private final BoardRepository boardRepository;
    private final EventEmitter eventEmitter;
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final BoardColumnRepository boardColumnRepository;
    private final ExcelExportService excelExportService;
    private final MailTicketService mailTicketService;

    @Autowired
    public ProjectController(ProjectService projectService, BoardRepository boardRepository, EventEmitter eventEmitter,
        StatisticsService statisticsService, SearchService searchService, BoardColumnRepository boardColumnRepository,
        ExcelExportService excelExportService, MailTicketService mailTicketService) {
        this.projectService = projectService;
        this.boardRepository = boardRepository;
        this.eventEmitter = eventEmitter;
        this.statisticsService = statisticsService;
        this.searchService = searchService;
        this.boardColumnRepository = boardColumnRepository;
        this.excelExportService = excelExportService;
        this.mailTicketService = mailTicketService;
    }

    @RequestMapping(value = "/api/project", method = RequestMethod.GET)
    public List<Project> findProjects(UserWithPermission user) {
        return projectService.findAllProjects(user);
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/project", method = RequestMethod.POST)
    public void create(@RequestBody CreateRequest project, User user) {
        checkShortName(project.getShortName());
        projectService.create(project.getName(), project.getShortName(), project.getDescription());
        eventEmitter.emitCreateProject(project.getShortName(), user);
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
        int projectId = projectService.findIdByShortName(shortName);
        return projectService.findColumnDefinitionsByProjectId(projectId);
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/board", method = RequestMethod.GET)
    public List<BoardInfo> findBoards(@PathVariable("projectShortName") String shortName) {
        int projectId = projectService.findIdByShortName(shortName);
        return boardRepository.findBoardInfo(projectId);
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/metadata", method = RequestMethod.GET)
    public ProjectMetadata getMetadata(@PathVariable("projectShortName") String shortName) {
        return projectService.getMetadata(shortName);
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/board", method = RequestMethod.POST)
    public void createBoard(@PathVariable("projectShortName") String shortName, @RequestBody CreateRequest board, User user) {
        checkShortName(board.getShortName());
        int projectId = projectService.findIdByShortName(shortName);
        boardRepository.createNewBoard(board.getName(), board.getShortName(), board.getDescription(), projectId);
        eventEmitter.emitCreateBoard(shortName, board.getShortName(), user);
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/definition", method = RequestMethod.PUT)
    public int updateColumnDefinition(@PathVariable("projectShortName") String shortName,
        @RequestBody UpdateColumnDefinition columnDefinition, User user) {
        int projectId = projectService.findIdByShortName(shortName);
        int res = projectService.updateColumnDefinition(projectId, columnDefinition.getDefinition(),
                columnDefinition.getColor());
        eventEmitter.emitUpdateColumnDefinition(shortName, user);
        return res;
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}", method = RequestMethod.POST)
    public Project updateProject(@PathVariable("projectShortName") String shortName,
        @RequestBody UpdateRequest updatedProject, User user) {
        int projectId = projectService.findIdByShortName(shortName);
        Project project = projectService.updateProject(projectId, updatedProject.getName(),
            updatedProject.getDescription(), updatedProject.isArchived());
        eventEmitter.emitUpdateProject(shortName, user);
        return project;
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/mailConfigs", method = RequestMethod.GET)
    public List<ProjectMailTicketConfig> getProjectMailConfigs(@PathVariable("projectShortName") String projectShortName) {
        int projectId = projectService.findIdByShortName(projectShortName);

        return mailTicketService.findAllByProject(projectId);
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/mailConfig", method = RequestMethod.POST)
    public ProjectMailTicketConfig addMailConfig(@PathVariable("projectShortName") String projectShortName,
                                                       @RequestBody ProjectMailTicketConfig config) {
        int projectId = projectService.findIdByShortName(projectShortName);

        return mailTicketService.addConfig(config.getName(),
            projectId,
            config.getConfig(),
            config.getSubject(),
            config.getBody());
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/mailConfig/{id}", method = RequestMethod.POST)
    public int updateMailConfig(@PathVariable("projectShortName") String projectShortName,
                                                       @PathVariable("id") int configId,
                                                       @RequestBody ProjectMailTicketConfig updatedConfig) {
        int projectId = projectService.findIdByShortName(projectShortName);

        return mailTicketService.updateConfig(configId,
            updatedConfig.getName(),
            updatedConfig.getEnabled(),
            updatedConfig.getConfig(),
            updatedConfig.getSubject(),
            updatedConfig.getBody(),
            projectId);
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/mailConfig/{id}", method = RequestMethod.DELETE)
    public int deleteMailConfig(@PathVariable("projectShortName") String projectShortName,
                                @PathVariable("id") int configId) {
        int projectId = projectService.findIdByShortName(projectShortName);

        return mailTicketService.deleteConfig(configId, projectId);
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/ticketConfig", method = RequestMethod.POST)
    public ProjectMailTicket addMailTicketConfig(@PathVariable("projectShortName") String projectShortName,
                                                 @RequestBody ProjectMailTicket ticket) {
        int projectId = projectService.findIdByShortName(projectShortName);
        ProjectMailTicketConfig config = mailTicketService.findConfig(ticket.getConfigId());

        Validate.isTrue(config.getProjectId() == projectId);

        BoardColumn column = boardColumnRepository.findById(ticket.getColumnId());
        Board board = boardRepository.findBoardById(column.getBoardId());

        Validate.isTrue(board.getProjectId() == projectId);

        return mailTicketService.addTicket(ticket.getName(),
            ticket.getAlias(),
            ticket.getSendByAlias(),
            ticket.getNotificationOverride(),
            ticket.getSubject(),
            ticket.getBody(),
            ticket.getColumnId(),
            ticket.getConfigId(),
            ticket.getMetadata());
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/ticketConfig/{id}", method = RequestMethod.POST)
    public int updateMailTicketConfig(@PathVariable("projectShortName") String projectShortName,
                                                       @PathVariable("id") int id,
                                                       @RequestBody ProjectMailTicket updatedTicket) {
        int projectId = projectService.findIdByShortName(projectShortName);
        ProjectMailTicket ticket = mailTicketService.findTicket(id);
        ProjectMailTicketConfig config = mailTicketService.findConfig(ticket.getConfigId());

        Validate.isTrue(config.getProjectId() == projectId);

        BoardColumn column = boardColumnRepository.findById(updatedTicket.getColumnId());
        Board board = boardRepository.findBoardById(column.getBoardId());

        Validate.isTrue(board.getProjectId() == projectId);

        return mailTicketService.updateTicket(id,
            updatedTicket.getName(),
            updatedTicket.getEnabled(),
            updatedTicket.getAlias(),
            updatedTicket.getSendByAlias(),
            updatedTicket.getNotificationOverride(),
            updatedTicket.getSubject(),
            updatedTicket.getBody(),
            updatedTicket.getColumnId(),
            updatedTicket.getConfigId(),
            updatedTicket.getMetadata());
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/ticketConfig/{id}", method = RequestMethod.DELETE)
    public int deleteMailTicket(@PathVariable("projectShortName") String projectShortName,
                                @PathVariable("id") int ticketId) {
        int projectId = projectService.findIdByShortName(projectShortName);
        ProjectMailTicket ticket = mailTicketService.findTicket(ticketId);
        ProjectMailTicketConfig config = mailTicketService.findConfig(ticket.getConfigId());

        Validate.isTrue(config.getProjectId() == projectId);

        return mailTicketService.deleteTicket(ticketId);
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/task-statistics", method = RequestMethod.GET)
    public TaskStatistics projectTaskStatistics(@PathVariable("projectShortName") String projectShortName,
        UserWithPermission user) {
        int projectId = projectService.findIdByShortName(projectShortName);

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
        int projectId = projectService.findIdByShortName(projectShortName);

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
            statisticsService.getMostActiveCardByProject(projectId, fromDate));
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/export/", method = RequestMethod.GET)
    public void exportMilestoneToExcel(@PathVariable("projectShortName") String projectShortName,
        UserWithPermission user, HttpServletResponse resp)
        throws IOException {

        HSSFWorkbook wb = excelExportService.exportProjectToExcel(projectShortName, user);

        resp.setHeader("Content-disposition", "attachment; filename=" + projectShortName + ".xls");
        wb.write(resp.getOutputStream());
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

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/columns-in/", method = RequestMethod.GET)
    public List<ProjectColumn> fetchAllColumns(@PathVariable("projectShortName") String shortName) {

        List<ProjectColumn> list = new ArrayList<>();

        int projectId = projectService.findIdByShortName(shortName);
        for (BoardInfo bInfo : boardRepository.findBoardInfo(projectId)) {
            int boardId = boardRepository.findBoardIdByShortName(bInfo.getShortName());
            for (BoardColumn bc : boardColumnRepository.findAllColumnsFor(boardId)) {
                if (bc.getLocation().equals(BoardColumn.BoardColumnLocation.BOARD)) {
                    list.add(new ProjectColumn(bInfo.getName(), bc.getId(), bc.getName()));
                }
            }
        }
        return list;
    }

    public static class UpdateColumnDefinition {
        private int definition;
        private int color;

        public int getDefinition() {
            return this.definition;
        }

        public int getColor() {
            return this.color;
        }

        public void setDefinition(int definition) {
            this.definition = definition;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    public static class ProjectColumn {
        private String board;
        private int columnId;
        private String columnName;

        @java.beans.ConstructorProperties({ "board", "columnId", "columnName" }) public ProjectColumn(String board,
            int columnId, String columnName) {
            this.board = board;
            this.columnId = columnId;
            this.columnName = columnName;
        }

        public String getBoard() {
            return this.board;
        }

        public int getColumnId() {
            return this.columnId;
        }

        public String getColumnName() {
            return this.columnName;
        }

        public void setBoard(String board) {
            this.board = board;
        }

        public void setColumnId(int columnId) {
            this.columnId = columnId;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }
}
