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

import lombok.Getter;
import io.lavagna.common.ConstructorAnnotationRowMapper.Column;
import io.lavagna.model.BoardColumn.BoardColumnLocation;

@Getter
public class BoardColumnInfo {

	private final int columnId;
	private final String columnName;
	private final ColumnDefinition columnDefinition;
	private final BoardColumnLocation columnLocation;
	private final int columnColor;
	private final int boardId;
	private final String boardName;
	private final String boardShortName;
	private final int projectId;
	private final String projectName;

	public BoardColumnInfo(@Column("BOARD_COLUMN_ID") int columnId, @Column("BOARD_COLUMN_NAME") String columnName,
			@Column("BOARD_COLUMN_LOCATION") BoardColumnLocation columnLocation, @Column("BOARD_ID") int boardId,
			@Column("BOARD_NAME") String boardName, @Column("BOARD_SHORT_NAME") String boardShortName,
			@Column("PROJECT_ID") int projectId, @Column("PROJECT_NAME") String projectName,
			@Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition columnDefininition,
			@Column("BOARD_COLUMN_DEFINITION_COLOR") int columnColor) {
		this.columnId = columnId;
		this.columnName = columnName;
		this.columnDefinition = columnDefininition;
		this.columnColor = columnColor;
		this.columnLocation = columnLocation;

		this.boardId = boardId;
		this.boardName = boardName;
		this.boardShortName = boardShortName;

		this.projectId = projectId;
		this.projectName = projectName;
	}
}
