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

import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.BoardColumnInfo;
import io.lavagna.model.User;
import io.lavagna.query.BoardColumnQuery;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Repository
@Transactional(readOnly = true)
public class BoardColumnRepository {

	private final NamedParameterJdbcTemplate jdbc;
	private final EventRepository eventRepository;
	private final BoardColumnQuery queries;

	public BoardColumnRepository(NamedParameterJdbcTemplate jdbc, EventRepository eventRepository,
			BoardColumnQuery queries) {
		this.jdbc = jdbc;
		this.eventRepository = eventRepository;
		this.queries = queries;
	}

	public BoardColumnInfo getColumnInfoById(int columnId) {
		return queries.getColumnInfoById(columnId);
	}

	public BoardColumn findById(int columnId) {
		return queries.findById(columnId);
	}

	public BoardColumn findDefaultColumnFor(int boardId, BoardColumnLocation location) {
		return queries.findDefaultColumnFor(boardId, location.toString());
	}

	public List<BoardColumn> findAllColumnsFor(int boardId) {
		return queries.findAllColumnFor(boardId);
	}

	public int updateOrder(int columnId, int order) {
		return queries.updateOrder(columnId, order);
	}

	public List<BoardColumn> findAllColumnsFor(int boardId, BoardColumnLocation location) {
		return queries.findAllColumnFor(boardId, location.toString());
	}

	public List<BoardColumn> findByIds(Set<Integer> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		return queries.findByIds(ids);
	}

	/**
	 * Returns the new Column
	 *
	 * @param name
	 * @param boardId
	 * @return
	 */
	@Transactional(readOnly = false)
	public BoardColumn addColumnToBoard(String name, int definitionId, BoardColumnLocation location, int boardId) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(location);

		queries.addColumnToBoard(trimToNull(name), boardId, location.toString(), definitionId);

		return queries.findLastCreatedColumn();
	}

	@Transactional(readOnly = false)
    public BoardColumn addColumnToBoardPosition(String name, int definitionId, BoardColumnLocation location, int order, int boardId) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(location);

        queries.addColumnToBoard(trimToNull(name), boardId, location.toString(), order, definitionId);

        return queries.findLastCreatedColumn();
    }

	@Transactional(readOnly = false)
	public int renameColumn(int columnId, String newName, int boardId) {
		return queries.renameColumn(trimToNull(newName), columnId, boardId);
	}

	/**
	 * update column order in a given board/location. The column ids are filtered.
	 *
	 * @param columns
	 * @param boardId
	 * @param location
	 */
	@Transactional(readOnly = false)
	public void updateColumnOrder(List<Integer> columns, int boardId, BoardColumnLocation location) {
		Objects.requireNonNull(columns);
		Objects.requireNonNull(location);

		// keep only the columns that are inside the boardId and have location=board
		List<Integer> filteredColumns = Utils.filter(columns,
				queries.findColumnIdsInBoard(columns, location.toString(), boardId));
		//

		SqlParameterSource[] params = new SqlParameterSource[filteredColumns.size()];
		for (int i = 0; i < filteredColumns.size(); i++) {
			params[i] = new MapSqlParameterSource("order", i + 1).addValue("columnId", filteredColumns.get(i))
					.addValue("boardId", boardId).addValue("location", location.toString());
		}

		jdbc.batchUpdate(queries.updateColumnOrder(), params);
	}

	@Transactional(readOnly = false)
	public int moveToLocation(int id, BoardColumnLocation location, User user) {
		Validate.isTrue(location != BoardColumnLocation.BOARD);

		// copy the column definition id of the default one
		int columnDefinitionId = findDefaultColumnFor(findById(id).getBoardId(), location).getDefinitionId();
		//

		int res = queries.moveToLocation(id, location.toString(), columnDefinitionId);

		List<Integer> cardIds = queries.findCardsInColumnId(id);
		eventRepository.insertCardEvent(cardIds, id, user.getId(), BoardColumnLocation.Companion.getMAPPING().get(location),
				new Date());

		return res;
	}

	@Transactional(readOnly = false)
	public int redefineColumn(int columnId, int definitionId, int boardId) {
		return queries.redefineColumn(definitionId, columnId, boardId);
	}
}
