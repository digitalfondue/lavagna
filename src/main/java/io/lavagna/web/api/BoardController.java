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
import io.lavagna.web.api.model.Suggestion;
import io.lavagna.web.api.model.TaskStatistics;
import io.lavagna.web.api.model.TaskStatisticsAndHistory;
import io.lavagna.web.api.model.UpdateRequest;
import io.lavagna.web.helper.ExpectPermission;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
public class BoardController {

	private final BoardRepository boardRepository;
	private final ProjectService projectService;
	private final SearchService searchService;
	private final StatisticsService statisticsService;
	private final EventEmitter eventEmitter;


	public BoardController(BoardRepository boardRepository, ProjectService projectService, SearchService searchService,
			EventEmitter eventEmitter, StatisticsService statisticsService) {
		this.boardRepository = boardRepository;
		this.projectService = projectService;
		this.searchService = searchService;
		this.eventEmitter = eventEmitter;
		this.statisticsService = statisticsService;
	}

	@RequestMapping(value = "/api/suggest-board-short-name", method = RequestMethod.GET)
	public Suggestion suggestBoardShortName(@RequestParam("name") String name) {
		return new Suggestion(ShortNameGenerator.generateShortNameFrom(name));
	}

	@RequestMapping(value = "/api/check-board-short-name", method = RequestMethod.GET)
	public boolean checkBoardShortName(@RequestParam("name") String name) {
		return ShortNameGenerator.isShortNameValid(name) && !boardRepository.existsWithShortName(name);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/board/{shortName}", method = RequestMethod.POST)
	public Board updateBoard(@PathVariable("shortName") String shortName, @RequestBody UpdateRequest updatedBoard, User user) {
		Board board = boardRepository.findBoardByShortName(shortName);
		board = boardRepository.updateBoard(board.getId(), updatedBoard.getName(), updatedBoard.getDescription(),
				updatedBoard.isArchived());
		eventEmitter.emitUpdateBoard(shortName, user);
		return board;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/board/{shortName}", method = RequestMethod.GET)
	public Board findByShortName(@PathVariable("shortName") String shortName) {
		return boardRepository.findBoardByShortName(shortName);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/board/{shortName}/task-statistics", method = RequestMethod.GET)
	public TaskStatistics boardTaskStatistics(@PathVariable("shortName") String shortName, UserWithPermission user) {
		Board board = boardRepository.findBoardByShortName(shortName);

		Map<ColumnDefinition, Integer> tasks = searchService
				.findTaksByColumnDefinition(board.getProjectId(), board.getId(), false, user);

		Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions = projectService
				.findMappedColumnDefinitionsByProjectId(board.getProjectId());

		return new TaskStatistics(tasks, columnDefinitions);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/board/{shortName}/statistics/{fromDate}", method = RequestMethod.GET)
	public TaskStatisticsAndHistory boardStatistics(@PathVariable("shortName") String shortName,
			@PathVariable("fromDate") Date fromDate, UserWithPermission user) {
		Board board = boardRepository.findBoardByShortName(shortName);

		Map<ColumnDefinition, Integer> tasks = searchService
				.findTaksByColumnDefinition(board.getProjectId(), board.getId(), false, user);

		Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions = projectService
				.findMappedColumnDefinitionsByProjectId(board.getProjectId());

		Integer activeUsers = statisticsService.getActiveUsersOnBoard(board.getId(), fromDate);

		return new TaskStatisticsAndHistory(tasks, columnDefinitions,
				statisticsService.getCardsStatusByBoard(board.getId(), fromDate),
				statisticsService.getCreatedAndClosedCardsByBoard(board.getId(), fromDate),
				activeUsers,
				statisticsService.getAverageUsersPerCardOnBoard(board.getId()),
				statisticsService.getAverageCardsPerUserOnBoard(board.getId()),
				statisticsService.getCardsByLabelOnBoard(board.getId()),
				statisticsService.getMostActiveCardByBoard(board.getId(), fromDate));
	}
}
