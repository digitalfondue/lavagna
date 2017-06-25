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

import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.Card;
import io.lavagna.model.CardFull;
import io.lavagna.model.Event;
import io.lavagna.model.User;
import io.lavagna.query.CardQuery;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Repository
@Transactional(readOnly = true)
public class CardRepository {

	private static final Logger LOG = LogManager.getLogger();

	private final NamedParameterJdbcTemplate jdbc;
	private final CardQuery queries;

	public CardRepository(NamedParameterJdbcTemplate jdbc, CardQuery queries) {
		this.jdbc = jdbc;
		this.queries = queries;
	}

	// prepare a {:cardOrder, :cardId, :columnId} list
	private static List<SqlParameterSource> prepareOrderParameter(List<Integer> cardIds, int columnId) {
		List<SqlParameterSource> params = new ArrayList<>(cardIds.size());
		for (int i = 0; i < cardIds.size(); i++) {
			SqlParameterSource p = new MapSqlParameterSource("cardOrder", i + 1)//
					.addValue("cardId", cardIds.get(i))//
					.addValue("columnId", columnId);
			params.add(p);
		}
		return params;
	}

	public Integer findColumnIdById(int cardId) {
		return queries.findColumnIdById(cardId);
	}

	public List<CardFull> findAllByBoardShortName(String boardShortName) {
		return queries.findAllByBoardShortName(boardShortName);
	}

	public List<Card> findAllByBoardIdAndLocation(int boardId, BoardColumnLocation location) {
		return queries.findAllByBoardIdAndLocation(boardId, location.toString());
	}

	public List<CardFull> findAllByColumnId(int columnId) {
		return queries.findAllFullByColumnId(columnId);
	}

	public List<CardFull> findAllByIds(Collection<Integer> ids) {
		return ids.isEmpty() ? Collections.<CardFull> emptyList() : queries.findAllByIds(ids);
	}

	public List<Card> findCards(int boardId, String criteria) {
		return queries.findCards(boardId, criteria);
	}

	public List<Event> fetchAllActivityByCardId(int cardId) {
		return queries.fetchAllActivityByCardId(cardId);
	}

	/**
	 * 10 element per page. Return 11 elements for signaling if there are more pages
	 *
	 * @param boardId
	 * @param location
	 * @param page
	 * @return
	 */
	public List<Integer> fetchPaginatedByBoardIdAndLocation(int boardId, BoardColumnLocation location, int page) {
		return queries.fetchPaginatedByBoardIdAndLocation(boardId, location.toString(), 11, page * 10);
	}

	public Card findBy(int cardId) {
		return queries.findBy(cardId);
	}

	public CardFull findFullBy(int cardId) {
		return queries.findFullBy(cardId);
	}

	public List<CardFull> findFullBy(Collection<Integer> ids) {
	    return queries.findAllByIds(ids);
    }

	public CardFull findFullBy(String boardShortName, int seqNumber) {
		return queries.findFullBy(boardShortName, seqNumber);
	}

	public Integer findCardIdByBoardNameAndSeq(String boardShortName, int seqNumber) {
		return queries.findCardIdByBoardNameAndSeq(boardShortName, seqNumber);
	}

	public boolean existCardWith(String boardShortName, int seqNumber) {
		return Integer.valueOf(1).equals(queries.countCardIdByBoardNameAndSeq(boardShortName, seqNumber));
	}

	public Card updateCard(int cardId, String name) {
		queries.updateCard(trimToNull(name), cardId);
		return findBy(cardId);
	}

	/**
	 * Returns the new Card
	 *
	 * @param name
	 * @param columnId
	 * @return
	 */
	@Transactional(readOnly = false)
	public Card createCard(String name, int columnId, User user) {

		LOG.debug("createCard: {name: {}, columnId: {}, userId: {}}", name, columnId, user.getId());

		int sequence = fetchAndLockSequence(columnId);
		queries.createCard(trimToNull(name), columnId, user.getId(), sequence);
		incrementSequence(columnId, sequence);
		return queries.findLastCreatedCard();
	}

	@Transactional(readOnly = false)
	public Card createCardFromTop(String name, int columnId, User user) {
		Card createdCard = createCard(name, columnId, user);
		moveLastCardAtTop(createdCard.getId(), columnId);
		return createdCard;
	}

	@Transactional(readOnly = false)
	private void moveLastCardAtTop(int lastCardId, int columnId) {
		queries.incrementCardsOrder(columnId);
		SqlParameterSource updateParam = new MapSqlParameterSource("columnId", columnId).addValue("cardId", lastCardId)
				.addValue("cardOrder", 0);
		jdbc.update(queries.updateCardOrder(), updateParam);
	}

	/**
	 * Fetch the ticket number from the counter and lock the row.
	 *
	 * @param columnId
	 * @return
	 */
	private int fetchAndLockSequence(int columnId) {
		return queries.fetchAndLockCardSequence(columnId);
	}

	/**
	 * Increment the counter
	 *
	 * @param columnId
	 */
	@Transactional(readOnly = false)
	private void incrementSequence(int columnId, int sequence) {
		int affected = queries.incrementSequence(sequence, columnId);
		Validate.isTrue(affected == 1, "during the update sequence, " + affected
				+ " were affected for a card inserted in the columnId " + columnId);
	}

	/**
	 * move a card and update the order of the new column. The ids are filtered.
	 *
	 * @param id
	 * @param prevColumnId
	 * @param newColumnId
	 * @param newOrderForNewColumn
	 */
	@Transactional(readOnly = false)
	public void moveCardToColumnAndReorder(int id, int prevColumnId, int newColumnId, List<Integer> newOrderForNewColumn) {
		moveCardToColumn(id, prevColumnId, newColumnId);
		updateCardOrder(newOrderForNewColumn, newColumnId);
	}

	@Transactional(readOnly = false)
	public void moveCardToColumn(int cardId, int previousColumnId, int columnId) {

		SqlParameterSource param = new MapSqlParameterSource("cardId", cardId).addValue("columnId", columnId).addValue(
				"previousColumnId", previousColumnId);
		int affected = jdbc.update(queries.moveCardToColumn(), param);
		Validate.isTrue(1 == affected, "moveCardToColumn: must affect exactly one row");
	}

	@Transactional(readOnly = false)
	public List<Integer> moveCardsToColumn(List<Integer> cardIds, int previousColumnId, int columnId, int userId) {

		List<Integer> filteredCardIds = Utils.filter(cardIds, queries.findCardIdsInColumnId(cardIds, previousColumnId));

		List<SqlParameterSource> params = new ArrayList<>(filteredCardIds.size());
		for (int cardId : filteredCardIds) {
			SqlParameterSource p = new MapSqlParameterSource("cardId", cardId)//
					.addValue("previousColumnId", previousColumnId)//
					.addValue("columnId", columnId);
			params.add(p);
		}

		int[] updateResult = jdbc.batchUpdate(queries.moveCardToColumn(),
				params.toArray(new SqlParameterSource[params.size()]));

		List<Integer> updated = new ArrayList<>();
		for (int i = 0; i < updateResult.length; i++) {
			if (updateResult[i] > 0) {
				updated.add(filteredCardIds.get(i));
			}
		}

		return updated;
	}

	/**
	 * Update card order in a given column id. The cardIds are filtered.
	 *
	 * @param cardIds
	 * @param columnId
	 */
	@Transactional(readOnly = false)
	public void updateCardOrder(List<Integer> cardIds, int columnId) {

		if (cardIds.isEmpty()) {
			return;
		}

		List<Integer> filteredCardIds = Utils.filter(cardIds, queries.findCardIdsInColumnId(cardIds, columnId));

		List<SqlParameterSource> params = prepareOrderParameter(filteredCardIds, columnId);
		jdbc.batchUpdate(queries.updateCardOrder(), params.toArray(new SqlParameterSource[params.size()]));
	}

	public Map<String, Integer> findCardsIds(List<String> cards) {

		List<Object[]> param = new ArrayList<>(cards.size());
		for (String card : cards) {
			String[] splitted = StringUtils.split(card, '-');
			if (splitted.length > 1) {
				try {
					Integer cardSequenceNumber = Integer.valueOf(splitted[splitted.length - 1], 10);
					String boardShortName = StringUtils
							.join(ArrayUtils.subarray(splitted, 0, splitted.length - 1), '-');
					param.add(new Object[] { boardShortName, cardSequenceNumber });

				} catch (NumberFormatException nfe) {
					// skip
				}
			}
		}

		if (param.isEmpty()) {
			return Collections.emptyMap();
		}

		final Map<String, Integer> res = new HashMap<>();
		MapSqlParameterSource paramS = new MapSqlParameterSource("projShortNameAndCardSeq", param);
		jdbc.query(queries.findCardsIs(), paramS, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				res.put(rs.getString("CARD_IDENTIFIER"), rs.getInt("CARD_ID"));
			}
		});

		return res;
	}

	//TODO: not happy about the interface of this one...
	public List<CardFull> findCardBy(String term, Set<Integer> projectIds) {

		String maybeBoardShortName = null;
		Integer maybeSequenceNumber = null;

		if (term != null) {
			String[] splitted = term.split("-", 2);
			maybeBoardShortName = splitted[0].toUpperCase(Locale.ENGLISH);
			if (splitted.length > 1) {
				try {
					maybeSequenceNumber = Integer.valueOf(splitted[1]);
				} catch (NumberFormatException ignore) {
					// ignore
				}
			}
		}
		if(projectIds == null) {
		    return queries.findCardBy(term, maybeBoardShortName, maybeSequenceNumber);
		} else if (projectIds.isEmpty()){
		    return Collections.emptyList();
		} else {
		    return queries.findCardBy(term, maybeBoardShortName, maybeSequenceNumber, projectIds);
		}
	}

	public int updateCardOrder(int cardId, int order) {
		return queries.updateCardOrder(cardId, order);
	}

    public List<Integer> findCardIdsByColumnId(int columnId) {
	    return queries.findCardIdsByColumnId(columnId);
    }
}
