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
package io.lavagna.service;

import io.lavagna.model.*;
import io.lavagna.query.StatisticsQuery;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

	private final StatisticsQuery queries;


	public StatisticsService(StatisticsQuery queries) {
		this.queries = queries;
	}

	@Transactional(readOnly = false)
	public void snapshotCardsStatus() {

		queries.snapshotCardsStatus(new Date());

		queries.cleanOldCardsStatusSnapshots();
	}

	private Map<Long, Map<ColumnDefinition, Long>> toStatusByDay(List<StatisticsResult> results) {
		Map<Long, Map<ColumnDefinition, Long>> statusByDay = new HashMap<>();
		for (StatisticsResult result : results) {
			if (!statusByDay.containsKey(result.getDay())) {
				statusByDay.put(result.getDay(), new EnumMap<ColumnDefinition, Long>(ColumnDefinition.class));
			}
			Map<ColumnDefinition, Long> day = statusByDay.get(result.getDay());
			day.put(result.getColumnDefinition(), result.getCount());
		}
		return statusByDay;
	}

	public Map<Long, Map<ColumnDefinition, Long>> getCardsStatusByBoard(int boardId, Date fromDate) {
		return toStatusByDay(queries.getCardsStatusByBoard(boardId, fromDate));
	}

	public Map<Long, Map<ColumnDefinition, Long>> getCardsStatusByProject(int projectId, Date fromDate) {
		return toStatusByDay(queries.getCardsStatusByProject(projectId, fromDate));
	}

	public Integer getActiveUsersOnBoard(int boardId, Date fromDate) {
		return queries.getActiveUsersOnBoard(boardId, fromDate);
	}

	public Integer getActiveUsersOnProject(int projectId, Date fromDate) {
		return queries.getActiveUsersOnProject(projectId, fromDate);
	}

	// Average users per card

	public double getAverageUsersPerCardOnBoard(int boardId) {
		return ObjectUtils.<Double>firstNonNull(queries.getAverageUsersPerCardOnBoard(boardId), 0d);
	}

	public double getAverageUsersPerCardOnProject(int projectId) {
		return ObjectUtils.<Double>firstNonNull(queries.getAverageUsersPerCardOnProject(projectId), 0d);
	}

	// Average cards per user

	public double getAverageCardsPerUserOnBoard(int boardId) {
		return ObjectUtils.<Double>firstNonNull(queries.getAverageCardsPerUserOnBoard(boardId), 0d);
	}

	public double getAverageCardsPerUserOnProject(int projectId) {
		return ObjectUtils.<Double>firstNonNull(queries.getAverageCardsPerUserOnProject(projectId), 0d);
	}

	// Cards by label

	public List<LabelAndValueWithCount> getCardsByLabelOnBoard(int boardId) {
		return queries.getCardsByLabelOnBoard(boardId);
	}

	public List<LabelAndValueWithCount> getCardsByLabelOnProject(int projectId) {
		return queries.getCardsByLabelOnProject(projectId);
	}

	// Created / closed cards

	private Map<Long, Pair<Long, Long>> mergeCounts(List<EventsCount> createdCards, List<EventsCount> closedCards) {
		Map<Long, Pair<Long, Long>> counts = new HashMap<>();
		for (EventsCount count : createdCards) {
			counts.put(count.getDate(), new Pair<>(count.getCount(), 0L));
		}
		for (EventsCount count : closedCards) {
			long created = 0;
			if (counts.containsKey(count.getDate())) {
				created = counts.get(count.getDate()).getFirst();
				counts.remove(count.getDate());
			}
			counts.put(count.getDate(), new Pair<>(created, count.getCount()));
		}
		return counts;
	}

	public Map<Long, Pair<Long, Long>> getCreatedAndClosedCardsByBoard(int boardId, Date fromDate) {
		return mergeCounts(queries.getCreatedCardsByBoard(boardId, fromDate),
				queries.getClosedCardsByBoard(boardId, fromDate));
	}

	public Map<Long, Pair<Long, Long>> getCreatedAndClosedCardsByProject(int projectId, Date fromDate) {
		return mergeCounts(queries.getCreatedCardsByProject(projectId, fromDate),
				queries.getClosedCardsByProject(projectId, fromDate));
	}

	// Most active card

	public CardFull getMostActiveCardByBoard(int boardId, Date fromDate) {
		try {
			return queries.getMostActiveCardByBoard(boardId, fromDate);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	public CardFull getMostActiveCardByProject(int projectId, Date fromDate) {
		try {
			return queries.getMostActiveCardByProject(projectId, fromDate);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	// Milestones

	public Map<Long, Pair<Long, Long>> getAssignedAndClosedCardsByMilestone(LabelListValue milestone, Date fromDate) {
		return mergeCounts(queries.getAssignedCardsByMilestone(milestone.getValue(), fromDate),
				queries.getClosedCardsByMilestone(milestone.getId(), fromDate));
	}

	public List<MilestoneCount> findCardsCountByMilestone(int projectId) {
		return queries.findCardsCountByMilestone(projectId);
	}

    public List<MilestoneCount> findUnassignedCardsCountByMilestone(int projectId) {
        return queries.findUnassignedCardsCountByMilestone(projectId);
    }

    public List<MilestoneCount> findCardsCountByMilestone(int projectId, int milestoneId) {
        return queries.findCardsCountByMilestone(projectId, milestoneId);
    }
}
