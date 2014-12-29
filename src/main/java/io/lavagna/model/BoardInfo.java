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

import io.lavagna.common.ConstructorAnnotationRowMapper.Column;
import lombok.Getter;

@Getter
public class BoardInfo {
	private final String shortName;
	private final String name;
	private final String description;
	private final boolean archived;

	public BoardInfo(@Column("BOARD_SHORT_NAME") String shortName, @Column("BOARD_NAME") String name,
			@Column("BOARD_DESCRIPTION") String description, @Column("BOARD_ARCHIVED") boolean archived) {
		this.shortName = shortName;
		this.name = name;
		this.description = description;
		this.archived = archived;
	}
}