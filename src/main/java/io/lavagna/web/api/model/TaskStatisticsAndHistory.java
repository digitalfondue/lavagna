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
import io.lavagna.model.CardFull;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.LabelAndValueWithCount;
import io.lavagna.model.Pair;

import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class TaskStatisticsAndHistory extends TaskStatistics {

	private final Map<Long, Map<ColumnDefinition, Long>> statusHistory;
	private final Map<Long, Pair<Long, Long>> createdAndClosedCards;
	private final List<LabelAndValueWithCount> cardsByLabel;
	private final Integer activeUsers;
	private final double averageUsersPerCard;
	private final double averageCardsPerUser;
	private final CardFull mostActiveCard;

	public TaskStatisticsAndHistory(Map<ColumnDefinition, Integer> tasks,
			Map<ColumnDefinition, BoardColumnDefinition> columnDefinitions,
			Map<Long, Map<ColumnDefinition, Long>> statusHistory, Map<Long, Pair<Long, Long>> createdAndClosedCards,
			Integer activeUsers, double averageUsersPerCard, double averageCardsPerUser,
			List<LabelAndValueWithCount> cardsByLabel, CardFull mostActiveCard) {
		super(tasks, columnDefinitions);
		this.statusHistory = statusHistory;
		this.createdAndClosedCards = createdAndClosedCards;
		this.activeUsers = activeUsers;
		this.averageUsersPerCard = averageUsersPerCard;
		this.averageCardsPerUser = averageCardsPerUser;
		this.cardsByLabel = cardsByLabel;
		this.mostActiveCard = mostActiveCard;
	}
}
