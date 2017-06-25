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
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.service.*;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BoardColumnController {

	private static final Logger LOG = LogManager.getLogger();

	private final BoardColumnRepository boardColumnRepository;
	private final BoardRepository boardRepository;
	private final ProjectService projectService;
	private final EventEmitter eventEmitter;
	private final CardRepository cardRepository;


	public BoardColumnController(BoardColumnRepository boardColumnRepository,
                                 BoardRepository boardRepository,
                                 CardRepository cardRepository,
                                 ProjectService projectService,
                                 EventEmitter eventEmitter) {
		this.boardColumnRepository = boardColumnRepository;
		this.boardRepository = boardRepository;
		this.projectService = projectService;
		this.cardRepository = cardRepository;
		this.eventEmitter = eventEmitter;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/column/{columnId}", method = RequestMethod.GET)
	public BoardColumnInfo getColumnInfo(@PathVariable("columnId") int columnId) {
		return boardColumnRepository.getColumnInfoById(columnId);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/board/{shortName}/columns-in/{location}", method = RequestMethod.GET)
	public List<BoardColumn> fetchAll(@PathVariable("shortName") String shortName,
			@PathVariable("location") BoardColumnLocation location) {
		int boardId = boardRepository.findBoardIdByShortName(shortName);
		return boardColumnRepository.findAllColumnsFor(boardId, location);
	}

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/board/{shortName}/columns-in", method = RequestMethod.GET)
    public List<BoardColumn> fetchAll(@PathVariable("shortName") String shortName) {
        int boardId = boardRepository.findBoardIdByShortName(shortName);
        return boardColumnRepository.findAllColumnsFor(boardId);
    }

	@ExpectPermission(Permission.CREATE_COLUMN)
	@RequestMapping(value = "/api/board/{shortName}/column", method = RequestMethod.POST)
	public void create(@PathVariable("shortName") String shortName, @RequestBody BoardColumnToCreate column, User user) {
		int boardId = boardRepository.findBoardIdByShortName(shortName);
		LOG.debug("column: {}, definition: {}", column.name, column.definition);

		Validate.isTrue(projectService.findRelatedProjectShortNameByBoardShortname(shortName).equals(
				projectService.findRelatedProjectShortNameByColumnDefinitionId(column.definition)));

		boardColumnRepository.addColumnToBoard(column.name, column.definition, BoardColumnLocation.BOARD, boardId);

		eventEmitter.emitCreateColumn(shortName, BoardColumnLocation.BOARD, column.name, user);
	}

	@ExpectPermission(Permission.RENAME_COLUMN)
	@RequestMapping(value = "/api/column/{columnId}/rename/{newName}", method = RequestMethod.POST)
	public int rename(@PathVariable("columnId") int columnId, @PathVariable("newName") String newName, User user) {
		BoardColumn column = boardColumnRepository.findById(columnId);
		Board board = boardRepository.findBoardById(column.getBoardId());
		int res = boardColumnRepository.renameColumn(columnId, newName, column.getBoardId());
		BoardColumn updatedColumn = boardColumnRepository.findById(columnId);

		eventEmitter.emitUpdateColumn(board.getShortName(), boardColumnRepository.findById(columnId).getLocation(),
				columnId, column, updatedColumn, user);

		return res;
	}

	@ExpectPermission(Permission.RENAME_COLUMN)
	@RequestMapping(value = "/api/column/{columnId}/redefine/{newDefinitionId}", method = RequestMethod.POST)
	public int redefine(@PathVariable("columnId") int columnId, @PathVariable("newDefinitionId") int definitionId, User user) {

		Validate.isTrue(projectService.findRelatedProjectShortNameByColumnId(columnId).equals(
				projectService.findRelatedProjectShortNameByColumnDefinitionId(definitionId)));

		BoardColumn column = boardColumnRepository.findById(columnId);
		Board board = boardRepository.findBoardById(column.getBoardId());
		int res = boardColumnRepository.redefineColumn(columnId, definitionId, column.getBoardId());
		BoardColumn updatedColumn = boardColumnRepository.findById(columnId);

		eventEmitter.emitUpdateColumn(board.getShortName(), boardColumnRepository.findById(columnId).getLocation(),
				columnId, column, updatedColumn, user);

		return res;
	}

	@ExpectPermission(Permission.MOVE_COLUMN)
	@RequestMapping(value = "/api/board/{shortName}/columns-in/{location}/column/order", method = RequestMethod.POST)
	public boolean reorder(@PathVariable("shortName") String shortName,
			@PathVariable("location") BoardColumnLocation location, @RequestBody List<Number> columnIdOrdered) {
		int boardId = boardRepository.findBoardIdByShortName(shortName);
		boardColumnRepository.updateColumnOrder(Utils.from(columnIdOrdered), boardId, location);

		eventEmitter.emitUpdateColumnPosition(shortName, location);

		return true;
	}

	/**
	 * Move the column in the given {location}
	 */
	@ExpectPermission(Permission.MOVE_COLUMN)
	@RequestMapping(value = "/api/column/{columnId}/to-location/{location}", method = RequestMethod.POST)
	@ResponseBody
	public void moveColumnWithoutReorder(@PathVariable("columnId") int columnId,
			@PathVariable("location") BoardColumnLocation location, User user) {
		Validate.isTrue(location != BoardColumnLocation.BOARD);
		BoardColumn col = boardColumnRepository.findById(columnId);

		Validate.isTrue(col.getLocation() == BoardColumnLocation.BOARD);

		boardColumnRepository.moveToLocation(col.getId(), location, user);

        BoardColumn destination = boardColumnRepository.findById(columnId);

        List<Integer> cardIds = cardRepository.findCardIdsByColumnId(columnId);

		String boardShortName = boardRepository.findBoardById(col.getBoardId()).getShortName();
		eventEmitter.emitUpdateColumnPosition(boardShortName, BoardColumnLocation.BOARD);
		eventEmitter.emitMoveCardOutsideOfBoard(boardShortName, location);

        eventEmitter.emitCardHasMoved(projectService.findRelatedProjectShortNameByBoardShortname(boardShortName),
            boardShortName, cardIds, col, destination, user);

	}

	public static class BoardColumnToCreate {
		private String name;
		private Integer definition;

        public String getName() {
            return this.name;
        }

        public Integer getDefinition() {
            return this.definition;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDefinition(Integer definition) {
            this.definition = definition;
        }
    }
}
