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
public class ProjectWithEventCounts {

	private final long events;
	private final Project project;

	public ProjectWithEventCounts(@Column("PROJECT_ID") int projectId, @Column("PROJECT_NAME") String projectName,
			@Column("PROJECT_SHORT_NAME") String projectShortName,
			@Column("PROJECT_DESCRIPTION") String projectDescription,
			@Column("PROJECT_ARCHIVED") boolean projectArchived,//
			@Column("EVENTS") long events) {
		this.project = new Project(projectId, projectName, projectShortName, projectDescription, projectArchived);
		this.events = events;
	}
}
