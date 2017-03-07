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
import io.lavagna.model.*;
import io.lavagna.model.Event.EventType;
import io.lavagna.query.CardDataQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Repository
@Transactional(readOnly = true)
public class CardDataRepository {

	private static final Logger LOG = LogManager.getLogger();

	private final NamedParameterJdbcTemplate jdbc;
	private final CardDataQuery queries;

	public CardDataRepository(NamedParameterJdbcTemplate jdbc, CardDataQuery queries) {
		this.jdbc = jdbc;
		this.queries = queries;
	}

	private static List<String> toStringList(Set<?> s) {
		List<String> r = new ArrayList<>(s.size());
		for (Object e : s) {
			r.add(e.toString());
		}
		return r;
	}

	// prepare a {:order, :id, :cardId} list
	private static List<SqlParameterSource> prepareOrderParameter(List<Integer> dataIds, int cardId, Integer referenceId) {
		LOG.debug("prepareOrderParameter: {card: {}, data count: {}, reference {}}", cardId, dataIds.size(),
				referenceId);
		List<SqlParameterSource> params = new ArrayList<>(dataIds.size());
		for (int i = 0; i < dataIds.size(); i++) {
			LOG.debug("prepareOrderParameter FOR: {data: {}, order: {}}", dataIds.get(i), i + 1);
			SqlParameterSource p = new MapSqlParameterSource("order", i + 1).addValue("id", dataIds.get(i))
					.addValue("cardId", cardId).addValue("referenceId", referenceId);
			params.add(p);
		}
		return params;
	}

	public CardData getUndeletedDataLightById(int id) {
		return queries.getUndeletedDataLightById(id);
	}

	public CardData getDataLightById(int id) {
		return queries.getDataLightById(id);
	}

	public Map<Integer, String> findDataByIds(Collection<Integer> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, String> res = new HashMap<>();
		for (CardIdAndContent c : queries.findDataByIds(ids)) {
			res.put(c.getId(), c.getContent());
		}
		return res;
	}

	public CardDataMetadata findMetadataById(int id) {
		return queries.findMetadataById(id);
	}

	public String findContentWith(int cardId, int referenceId, CardType type, int order) {
		List<CardIdAndContent> res = queries.findContentWith(cardId, referenceId, type.toString(), order);
		return res.isEmpty() ? null : res.get(0).getContent();
	}

	public List<CardData> findAllDataLightByCardId(int cardId) {
		return queries.findAllLightByCardId(cardId);
	}

	public List<CardData> findAllDataLightByReferenceId(int referenceId) {
		return queries.findAllLightByReferenceId(referenceId);
	}

	public List<CardData> findAllDataLightByCardIdAndTypes(int cardId, Set<CardType> types) {
		return queries.findAllLightByCardIdAndTypes(cardId, toStringList(types));
	}

	public List<CardData> findAllDataLightByCardIdAndType(int cardId, CardType type) {
		return queries.findAllLightByCardIdAndType(cardId, type.toString());
	}

	public List<CardDataIdAndOrder> findAllByTypes(Set<CardType> types) {
		return queries.findAllCardDataIdAndOrderByType(toStringList(types));
	}

	public List<CardData> findAllDataLightByReferenceIdAndType(int referenceId, CardType type) {
		return queries.findAllLightByReferenceIdAndType(referenceId, type.toString());
	}

	public List<CardDataFull> findAllDataByCardIdAndType(int cardId, CardType type) {
		return queries.findAllByCardIdAndType(cardId, type.toString());
	}

	public List<FileDataLight> findAllFilesByCardId(int cardId) {
		return queries.findAllFilesByCardId(cardId);
	}

	public FileDataLight getUndeletedFileByCardDataId(int cardDataId) {
		return queries.getUndeletedFileByCardDataId(cardDataId);
	}

	public List<CardDataUploadContentInfo> findAllDataUploadContentInfo() {
		return queries.findAllDataUploadContentInfo();
	}

	@Transactional(readOnly = false)
	public CardData createData(int cardId, CardType type, String content) {
		LOG.debug("createCardData: {card: {}, type: {}, body: {}}", cardId, type, content);
		queries.create(cardId, type.toString(), requireNonNull(trimToEmpty(content), "body cannot be empty"));
		return queries.findLastCreatedLight();
	}

	@Transactional(readOnly = false)
	public CardData createDataWithReferenceOrder(int cardId, Integer referenceId, CardType type, String content) {
		LOG.debug("createDataWithReferenceOrder: {card: {}, reference: {}, type: {}, body: {}}", cardId,
				referenceId, type, content);

		queries.createWithReferenceOrder(cardId, referenceId, type.toString(),
				requireNonNull(trimToEmpty(content), "body cannot be empty"));

		return queries.findLastCreatedLight();
	}

	/**
	 * Order the action list. Additionally, the ids are filtered.
	 *
	 * @param cardId
	 * @param data
	 */
	@Transactional(readOnly = false)
	public void updateActionListOrder(int cardId, List<Integer> data) {

		// we filter out wrong ids
		List<Integer> filtered = Utils.filter(data,
				queries.findAllCardDataIdsBy(data, cardId, CardType.ACTION_LIST.toString()));
		//

		SqlParameterSource[] params = new SqlParameterSource[filtered.size()];
		for (int i = 0; i < filtered.size(); i++) {
			params[i] = new MapSqlParameterSource("order", i + 1).addValue("id", filtered.get(i)).addValue("cardId",
					cardId);
		}

		jdbc.batchUpdate(queries.updateOrder(), params);
	}

	@Transactional(readOnly = false)
	public int updateOrderById(int id, int order) {
		return queries.updateOrderById(id, order);
	}

	/**
	 * Order the action item inside a action list. Additionally, the ids are filtered.
	 *
	 * @param cardId
	 * @param newReferenceId
	 * @param newDataOrder
	 */
	@Transactional(readOnly = false)
	public void updateOrderByCardAndReferenceId(int cardId, Integer newReferenceId, List<Integer> newDataOrder) {

		List<Integer> filtered = Utils.filter(
				newDataOrder,
				queries.findAllCardDataIdsBy(newDataOrder, cardId, newReferenceId,
						Arrays.asList(CardType.ACTION_CHECKED.toString(), CardType.ACTION_UNCHECKED.toString())));

		List<SqlParameterSource> params = prepareOrderParameter(filtered, cardId, newReferenceId);

		jdbc.batchUpdate(queries.updateOrderByCardAndReferenceId(),
				params.toArray(new SqlParameterSource[params.size()]));
	}

	@Transactional(readOnly = false)
	public int updateReferenceId(int cardId, int dataId, Integer referenceId) {
		LOG.debug("updateReferenceId: {card: {}, data: {}, referenceId: {}}", cardId, dataId, referenceId);
		return queries.updateReferenceId(referenceId, dataId, cardId);
	}

	@Transactional(readOnly = false)
	public int updateContent(int id, Set<CardType> types, String content) {
		return queries.updateContent(requireNonNull(trimToEmpty(content), "body cannot be empty"), id,
				toStringList(types));
	}

	@Transactional(readOnly = false)
	public int updateType(int id, Set<CardType> oldTypes, CardType newType) {
		LOG.debug("updateType: {item: {}, type: {}}", id, newType);
		return queries.updateType(newType.toString(), id, toStringList(oldTypes));
	}

	@Transactional(readOnly = false)
	public int softDelete(int id, Set<CardType> types) {
		LOG.debug("softDelete: {id: {}}", id);
		return queries.softDelete(id, toStringList(types));
	}

	@Transactional(readOnly = false)
	public int undoSoftDelete(int id, Set<CardType> types) {
		LOG.debug("undoSoftDelete: {id: {}}", id);
		return queries.undoSoftDelete(id, toStringList(types));
	}

	@Transactional(readOnly = false)
	public int softDeleteOnCascade(int id, Set<CardType> types) {
		LOG.debug("softDeleteOnCascade: {id: {}}", id);
		return queries.softDeleteOnCascade(id, toStringList(types));
	}

	@Transactional(readOnly = false)
	public int undoSoftDeleteOnCascade(int id, Set<CardType> types, Set<EventType> filteredEvents) {
		LOG.debug("undoSoftDeleteOnCascade: {id: {}}", id);
		return queries.undoSoftDeleteOnCascade(id, toStringList(types), toStringList(filteredEvents));
	}

	public List<CardDataCount> findCountsByBoardIdAndLocation(int boardId, BoardColumnLocation location) {
		return queries.findCountsByBoardIdAndLocation(boardId, location.toString());
	}

	public List<CardDataCount> findCountsByCardIds(List<Integer> ids) {
		return ids.isEmpty() ? Collections.<CardDataCount> emptyList() : queries.findCountsByCardIds(ids);
	}

	@Transactional(readOnly = false)
	public int addUploadContent(final String digest, final long fileSize, final InputStream content,
			final String contentType) {
		LobHandler lobHandler = new DefaultLobHandler();
		return jdbc.getJdbcOperations().execute(queries.addUploadContent(),
				new AbstractLobCreatingPreparedStatementCallback(lobHandler) {

					@Override
					protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
						ps.setString(1, digest);
						ps.setLong(2, fileSize);
						lobCreator.setBlobAsBinaryStream(ps, 3, content, (int) fileSize);
						ps.setString(4, contentType);
					}
				});
	}

	@Transactional(readOnly = false)
	public int createUploadInfo(String digest, String name, String displayName, int cardDataId) {
		return queries.mapUploadContent(cardDataId, digest, name, displayName);
	}

	public boolean fileExists(String digest) {
		return queries.findDigest(digest).equals(1);
	}

	public boolean isFileAvailableByCard(String digest, int cardId) {
		return queries.isFileAvailableByCard(cardId, digest).equals(1);
	}

	public void outputFileContent(String digest, final OutputStream out) throws IOException {
		LOG.debug("get file digest : {} ", digest);
		SqlParameterSource param = new MapSqlParameterSource("digest", digest);

		jdbc.query(queries.fileContent(), param, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				try (InputStream is = rs.getBinaryStream("CONTENT")) {
					StreamUtils.copy(is, out);
				} catch (IOException e) {
					throw new IllegalStateException("Error while copying data", e);
				}
			}
		});
	}
}
