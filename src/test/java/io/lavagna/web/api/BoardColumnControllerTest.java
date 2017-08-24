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
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.User;
import io.lavagna.service.*;
import io.lavagna.web.api.BoardColumnController.BoardColumnToCreate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BoardColumnControllerTest {

	@Mock
	private BoardColumnRepository boardColumnRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private EventEmitter eventEmitter;
	@Mock
	private BoardColumnController boardColumnController;
	@Mock
	private ProjectService projectService;
	@Mock
    private CardRepository cardRepository;

	@Mock
	private User user;

	private final String shortName = "SHORT";
	private final BoardColumnLocation location = BoardColumnLocation.BOARD;
	private final BoardColumn column = new BoardColumn(42, "test", 0, 42, location, 0, ColumnDefinition.OPEN,
			ColumnDefinition.OPEN.getDefaultColor());

	@Before
	public void prepare() {
		boardColumnController = new BoardColumnController(boardColumnRepository, boardRepository, cardRepository, projectService,
				eventEmitter);
	}

	@Test
	public void fetchAll() {
		List<BoardColumn> bc = Arrays.asList(new BoardColumn(42, "test", 0, 42, location, 0, ColumnDefinition.OPEN,
				ColumnDefinition.OPEN.getDefaultColor()));
		when(boardRepository.findBoardIdByShortName(shortName)).thenReturn(42);
		when(boardColumnRepository.findAllColumnsFor(42, location)).thenReturn(bc);

		Assert.assertEquals(bc, boardColumnController.fetchAll(shortName, location));

		verify(boardRepository).findBoardIdByShortName(shortName);
		verify(boardColumnRepository).findAllColumnsFor(42, location);
	}

	@Test
	public void create() {
		BoardColumnToCreate toCreate = new BoardColumnController.BoardColumnToCreate();
		toCreate.setName("column");
		toCreate.setDefinition(0);

		when(projectService.findRelatedProjectShortNameByColumnDefinitionId(0)).thenReturn("PROJECT_SHORT_NAME");
		when(projectService.findRelatedProjectShortNameByBoardShortname(shortName)).thenReturn("PROJECT_SHORT_NAME");

		when(boardRepository.findBoardIdByShortName(shortName)).thenReturn(0);
		when(boardColumnRepository.addColumnToBoard(toCreate.getName(), 0, BoardColumnLocation.BOARD, 0)).thenReturn(
				new BoardColumn(0, toCreate.getName(), 0, 0, BoardColumnLocation.BOARD, 0, ColumnDefinition.OPEN,
						ColumnDefinition.OPEN.getDefaultColor()));

		boardColumnController.create(shortName, toCreate, user);

		verify(boardRepository).findBoardIdByShortName(shortName);
		verify(boardColumnRepository).addColumnToBoard(toCreate.getName(), 0, BoardColumnLocation.BOARD, 0);
		verify(eventEmitter).emitCreateColumn(shortName, location, toCreate.getName(), user);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithMismatchingDefinitionId() {
		BoardColumnToCreate toCreate = new BoardColumnController.BoardColumnToCreate();
		toCreate.setName("column");
		toCreate.setDefinition(0);

		when(projectService.findRelatedProjectShortNameByColumnDefinitionId(0)).thenReturn("ANOTHER_PROJECT_SHORT_NAME");
		when(projectService.findRelatedProjectShortNameByBoardShortname(shortName)).thenReturn("PROJECT_SHORT_NAME");

		when(boardRepository.findBoardIdByShortName(shortName)).thenReturn(0);

		boardColumnController.create(shortName, toCreate, user);

	}

	@Test
	public void rename() {

		when(boardColumnRepository.findById(42)).thenReturn(column);

		Board b = new Board(0, "name", shortName, null, 84, false);

		when(boardRepository.findBoardById(column.getBoardId())).thenReturn(b);

		boardColumnController.rename(42, "column2", user);

		verify(boardColumnRepository).renameColumn(42, "column2", 42);
		verify(eventEmitter).emitUpdateColumn(eq(shortName), eq(column.getLocation()), eq(42), any(BoardColumn.class), any(BoardColumn.class), eq(user));
	}

	@Test
	public void reorder() {
		when(boardRepository.findBoardIdByShortName(shortName)).thenReturn(0);
		List<Integer> conv = Arrays.asList(1, 2, 3);
		List<Number> order = Arrays.<Number> asList(1, 2, 3);

		boardColumnController.reorder(shortName, location, order);

		verify(boardRepository).findBoardIdByShortName(shortName);
		verify(boardColumnRepository).updateColumnOrder(conv, 0, location);
		verify(eventEmitter).emitUpdateColumnPosition(shortName, location);
	}

	@Test
	public void testGetColumnInfo() {
		boardColumnController.getColumnInfo(42);
	}

	@Test
	public void testMoveColumnWithoutReorder() {
		when(boardColumnRepository.findById(42)).thenReturn(column);
		when(boardRepository.findBoardById(42)).thenReturn(new Board(42, shortName, shortName, "", 0, false));

		boardColumnController.moveColumnWithoutReorder(42, BoardColumnLocation.ARCHIVE, eq(user));
	}
}
