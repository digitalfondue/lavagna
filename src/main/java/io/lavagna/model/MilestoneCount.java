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
import io.lavagna.common.ConstructorAnnotationRowMapper;

@Getter
public class MilestoneCount {

	private final Integer milestoneId;
	private final ColumnDefinition columnDefinition;
	private final long count;

	public MilestoneCount(@ConstructorAnnotationRowMapper.Column("CARD_LABEL_VALUE_LIST_VALUE_FK") Integer milestoneId,
			@ConstructorAnnotationRowMapper.Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition columnDefinition,
			@ConstructorAnnotationRowMapper.Column("MILESTONE_COUNT") long count) {
		this.milestoneId = milestoneId;
		this.columnDefinition = columnDefinition;
		this.count = count;
	}
}
