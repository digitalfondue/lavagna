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

import io.lavagna.model.Board;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.*;
import io.lavagna.web.api.model.Suggestion;
import io.lavagna.web.api.model.UpdateRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BoardControllerTest {

	@Mock
	BoardRepository boardRepository;
	@Mock
	ProjectService projectService;
	@Mock
	SearchService searchService;
	@Mock
	EventEmitter eventEmitter;
	@Mock
	StatisticsService statisticsService;
	@Mock
	UserWithPermission user;

	private BoardController boardController;

	private final String shortName = "SHORT";

	@Before
	public void prepare() {
		boardController = new BoardController(boardRepository, projectService, searchService, eventEmitter,
				statisticsService);
	}

	@Test
	public void testFindByShortName() {
		boardController.findByShortName(shortName);
		verify(boardRepository).findBoardByShortName(shortName);
	}

	@Test
	public void testSuggestShortBoardName() {
		Suggestion suggestion = boardController.suggestBoardShortName("test");
		Assert.assertEquals("TEST", suggestion.getSuggestion());
	}

	@Test
	public void testFindBoardByShortName() {
		Board b = new Board(1, "NAME", "SHORT", "desc", 5, false);
		when(boardRepository.findBoardByShortName("TEST")).thenReturn(b);

		Map<ColumnDefinition, BoardColumnDefinition> defs = new EnumMap<>(ColumnDefinition.class);
		defs.put(ColumnDefinition.OPEN, new BoardColumnDefinition(1, b.getProjectId(), ColumnDefinition.OPEN, 0));
		defs.put(ColumnDefinition.CLOSED, new BoardColumnDefinition(2, b.getProjectId(), ColumnDefinition.CLOSED, 0));
		defs.put(ColumnDefinition.BACKLOG, new BoardColumnDefinition(3, b.getProjectId(), ColumnDefinition.BACKLOG, 0));
		defs.put(ColumnDefinition.DEFERRED,
				new BoardColumnDefinition(4, b.getProjectId(), ColumnDefinition.DEFERRED, 0));

		when(projectService.findMappedColumnDefinitionsByProjectId(b.getProjectId())).thenReturn(defs);

		Map<ColumnDefinition, Integer> tasks = new EnumMap<>(ColumnDefinition.class);
		tasks.put(ColumnDefinition.OPEN, 0);
		tasks.put(ColumnDefinition.CLOSED, 0);
		tasks.put(ColumnDefinition.BACKLOG, 0);
		tasks.put(ColumnDefinition.DEFERRED, 0);
		when(searchService
				.findTaksByColumnDefinition(eq(b.getProjectId()), eq(b.getId()), any(Boolean.class), eq(user)))
				.thenReturn(tasks);

		boardController.boardStatistics("TEST", new Date(), user);
		verify(boardRepository).findBoardByShortName(eq("TEST"));
	}

	@Test
	public void update() {
		Board b = mock(Board.class);
		when(boardRepository.findBoardByShortName(shortName)).thenReturn(b);

		UpdateRequest updatedBoard = new UpdateRequest();
		updatedBoard.setName("New name");
		updatedBoard.setDescription("Updated desc");

		boardController.updateBoard(shortName, updatedBoard, user);

		verify(boardRepository).updateBoard(eq(b.getId()), eq("New name"), eq("Updated desc"), eq(false));
	}
}
