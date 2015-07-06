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

import io.lavagna.model.BoardColumn.BoardColumnLocation;

import java.util.Date;

import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class StatisticForExport {

	private final Date date;
	private final ColumnDefinition columnDefinition;
	private final BoardColumnLocation location;
	private final long count;

	public StatisticForExport(@Column("BOARD_STATISTICS_TIME") Date date,
			@Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition columnDefinition,
			@Column("BOARD_STATISTICS_LOCATION") BoardColumnLocation location,
			@Column("BOARD_STATISTICS_COUNT") long count) {
		this.date = date;
		this.columnDefinition = columnDefinition;
		this.location = location;
		this.count = count;
	}
}
