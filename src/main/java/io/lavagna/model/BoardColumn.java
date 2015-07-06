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
package io.lavagna.model;

import io.lavagna.model.Event.EventType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class BoardColumn {

	public enum BoardColumnLocation {

		BOARD, BACKLOG, ARCHIVE, TRASH;

		public static final Map<BoardColumnLocation, EventType> MAPPING;

		static {
			Map<BoardColumnLocation, EventType> mapping = new EnumMap<>(BoardColumnLocation.class);
			mapping.put(BoardColumnLocation.ARCHIVE, EventType.CARD_ARCHIVE);
			mapping.put(BoardColumnLocation.BACKLOG, EventType.CARD_BACKLOG);
			mapping.put(BoardColumnLocation.TRASH, EventType.CARD_TRASH);
			mapping.put(BoardColumnLocation.BOARD, EventType.CARD_CREATE);
			MAPPING = Collections.unmodifiableMap(mapping);
		}
	}

	private final int id;
	private final String name;
	private final int order;
	private final int boardId;
	private final BoardColumnLocation location;
	private final int definitionId;
	private final ColumnDefinition status;
	private final int color;

	public BoardColumn(
			@Column("BOARD_COLUMN_ID") int id,//
			@Column("BOARD_COLUMN_NAME") String name,//
			@Column("BOARD_COLUMN_ORDER") int order,//
			@Column("BOARD_COLUMN_BOARD_ID_FK") int boardId,//
			@Column("BOARD_COLUMN_LOCATION") BoardColumnLocation location,
			@Column("BOARD_COLUMN_DEFINITION_ID") int definitionId,
			@Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition status,
			@Column("BOARD_COLUMN_DEFINITION_COLOR") int color) {
		this.id = id;
		this.name = name;
		this.order = order;
		this.boardId = boardId;
		this.location = location;

		this.definitionId = definitionId;
		this.status = status;
		this.color = color;
	}
}