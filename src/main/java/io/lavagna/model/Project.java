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
public class Project {

	private final int id;
	private final String name;
	private final String shortName;
	private final String description;
	private final boolean archived;

	public Project(@Column("PROJECT_ID") int id, @Column("PROJECT_NAME") String name,
			@Column("PROJECT_SHORT_NAME") String shortName, @Column("PROJECT_DESCRIPTION") String description,
			@Column("PROJECT_ARCHIVED") boolean archived) {
		this.id = id;
		this.name = name;
		this.shortName = shortName;
		this.description = description;
		this.archived = archived;
	}
}
