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
package io.lavagna.web.api.model;

import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.ColumnDefinition;

import java.util.Map;

import lombok.Getter;

@Getter
public class TaskStatistics {

	private final int openTaskColor;
	private final int closedTaskColor;
	private final int backlogTaskColor;
	private final int deferredTaskColor;

	private final int openTaskCount;
	private final int closedTaskCount;
	private final int backlogTaskCount;
	private final int deferredTaskCount;

	public TaskStatistics(Map<ColumnDefinition, Integer> tasks,
			Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions) {

		this.openTaskColor = columnDefinitions.get(ColumnDefinition.OPEN).getColor();
		this.closedTaskColor = columnDefinitions.get(ColumnDefinition.CLOSED).getColor();
		this.backlogTaskColor = columnDefinitions.get(ColumnDefinition.BACKLOG).getColor();
		this.deferredTaskColor = columnDefinitions.get(ColumnDefinition.DEFERRED).getColor();

		this.openTaskCount = tasks.get(ColumnDefinition.OPEN);
		this.closedTaskCount = tasks.get(ColumnDefinition.CLOSED);
		this.backlogTaskCount = tasks.get(ColumnDefinition.BACKLOG);
		this.deferredTaskCount = tasks.get(ColumnDefinition.DEFERRED);
	}
}
