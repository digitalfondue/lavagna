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

@Getter
public class BoardColumnDefinition {

	private final int id;
	private final int projectId;
	private final ColumnDefinition value;
	private final int color;

	public BoardColumnDefinition(@Column("BOARD_COLUMN_DEFINITION_ID") int id,
			@Column("BOARD_COLUMN_DEFINITION_PROJECT_ID_FK") int projectId,
			@Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition value,
			@Column("BOARD_COLUMN_DEFINITION_COLOR") int color) {
		this.id = id;
		this.projectId = projectId;
		this.value = value;
		this.color = color;
	}
}
