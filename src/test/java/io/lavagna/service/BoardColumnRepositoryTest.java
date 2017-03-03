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

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class BoardColumnRepositoryTest {

	private static final String BOARD_SHORT_NAME = "TESTBRD";

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private BoardRepository boardRepository;

	private Board board;

	private Map<ColumnDefinition, BoardColumnDefinition> definitions;

	private User user;

	@Before
	public void createBoard() {
		projectService.create("test", "TEST", "desc");
		boardRepository
				.createNewBoard(BOARD_SHORT_NAME, BOARD_SHORT_NAME, null, projectService.findByShortName("TEST").getId());
		board = boardRepository.findBoardByShortName(BOARD_SHORT_NAME);
		definitions = projectService.findMappedColumnDefinitionsByProjectId(projectService.findByShortName("TEST")
				.getId());
		Helper.createUser(userRepository, "test", "label");
		user = userRepository.findUserByName("test", "label");
	}

	private BoardColumnDefinition closedDefinition() {
		return definitions.get(ColumnDefinition.CLOSED);
	}

	private BoardColumnDefinition openDefinition() {
		return definitions.get(ColumnDefinition.OPEN);
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testCantCreateArchiveInBoard() {
		boardColumnRepository.addColumnToBoard(BoardColumnLocation.ARCHIVE.toString(), closedDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId());
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testCantCreateBacklogInBoard() {
		boardColumnRepository.addColumnToBoard(BoardColumnLocation.BACKLOG.toString(),
				definitions.get(ColumnDefinition.BACKLOG).getId(), BoardColumnLocation.BOARD, board.getId());
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testCantCreateTrashInBoard() {
		boardColumnRepository.addColumnToBoard(BoardColumnLocation.TRASH.toString(), closedDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId());
	}

	@Test
	public void testFindColumnById() {
		int colId = boardColumnRepository.addColumnToBoard("test", openDefinition().getId(), BoardColumnLocation.BOARD,
				board.getId()).getId();
		Assert.assertNotNull(boardColumnRepository.findById(colId));
	}

	@Test
	public void testFindColumnByIds() {
		int colId1 = boardColumnRepository.addColumnToBoard("test", openDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId()).getId();
		int colId2 = boardColumnRepository.addColumnToBoard("test", openDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId()).getId();
		Assert.assertEquals(2, boardColumnRepository.findByIds(new HashSet<>(Arrays.asList(colId1, colId2))).size());

		Assert.assertTrue(boardColumnRepository.findByIds(Collections.<Integer>emptySet()).isEmpty());
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testFindColumnByIdNothingFound() {
		boardColumnRepository.findById(Integer.MAX_VALUE);
	}

	@Test
	public void testDefaultColumnsOnRepoCreation() {
		Assert.assertEquals(1, boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.ARCHIVE)
				.size());
		Assert.assertEquals(1, boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BACKLOG)
				.size());
		Assert.assertEquals(0, boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD).size());
	}

	@Test
	public void testAddColumn() {
		Assert.assertEquals(
				"test",
				boardColumnRepository.addColumnToBoard("test", openDefinition().getId(), BoardColumnLocation.BOARD,
						board.getId()).getName());
	}

	@Test
	public void testFindAllColumn() {
		Assert.assertEquals(0, boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD).size());
		boardColumnRepository.addColumnToBoard("test", openDefinition().getId(), BoardColumnLocation.BOARD,
				board.getId());
		Assert.assertEquals(1, boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD).size());
	}

	@Test
	public void testRenameColumn() {
		Assert.assertEquals(
				"test",
				boardColumnRepository.addColumnToBoard("test", openDefinition().getId(), BoardColumnLocation.BOARD,
						board.getId()).getName());
		BoardColumn bCol = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD).get(0);
		Assert.assertEquals("test", bCol.getName());

		boardColumnRepository.renameColumn(bCol.getId(), "renameTest", board.getId());

		BoardColumn bColRenamed = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD)
				.get(0);
		Assert.assertEquals("renameTest", bColRenamed.getName());
	}

	@Test
	public void testUpateOrder() {
		boardColumnRepository.addColumnToBoard("test-1", openDefinition().getId(), BoardColumnLocation.BOARD,
				board.getId());
		boardColumnRepository.addColumnToBoard("test-2", openDefinition().getId(), BoardColumnLocation.BOARD,
				board.getId());
		List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		checkOrder(cols.get(0), 1, "test-1");
		checkOrder(cols.get(1), 2, "test-2");

		boardColumnRepository.updateColumnOrder(Arrays.asList(cols.get(1).getId(), cols.get(0).getId()), board.getId(),
				BoardColumnLocation.BOARD);

		List<BoardColumn> colsOrder = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		checkOrder(colsOrder.get(0), 1, "test-2");
		checkOrder(colsOrder.get(1), 2, "test-1");
	}

	@Test
	public void changeColumnDefinition() {
		boardColumnRepository.addColumnToBoard("test-1", openDefinition().getId(), BoardColumnLocation.BOARD,
				board.getId());
		List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		Assert.assertEquals(1, cols.size());
		boardColumnRepository.redefineColumn(cols.get(0).getId(), closedDefinition().getId(), cols.get(0).getBoardId());
		cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		Assert.assertEquals(1, cols.size());
		Assert.assertEquals(closedDefinition().getId(), cols.get(0).getDefinitionId());

	}

	private static void checkOrder(BoardColumn col, int expectedOrder, String expectedName) {
		Assert.assertEquals(expectedOrder, col.getOrder());
		Assert.assertEquals(expectedName, col.getName());
	}

	@Test
	public void testGetColumnInfoById() {
		BoardColumn bc = boardColumnRepository.addColumnToBoard("test-1", openDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId());

		BoardColumnInfo boardColumnInfo = boardColumnRepository.getColumnInfoById(bc.getId());

		Assert.assertEquals(bc.getBoardId(), boardColumnInfo.getBoardId());
		Assert.assertEquals(bc.getId(), boardColumnInfo.getColumnId());
	}

	@Test
	public void testMoveToLocation() {
		BoardColumn bc = boardColumnRepository.addColumnToBoard("test-1", openDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId());

		boardColumnRepository.moveToLocation(bc.getId(), BoardColumnLocation.ARCHIVE, user);

		Assert.assertEquals(BoardColumnLocation.ARCHIVE, boardColumnRepository.findById(bc.getId()).getLocation());
		Assert.assertEquals(closedDefinition().getId(), boardColumnRepository.findById(bc.getId()).getDefinitionId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMoveToLocationNotBoard() {
		BoardColumn bc = boardColumnRepository.addColumnToBoard("test-1", openDefinition().getId(),
				BoardColumnLocation.BOARD, board.getId());
		boardColumnRepository.moveToLocation(bc.getId(), BoardColumnLocation.BOARD, user);
	}

}
