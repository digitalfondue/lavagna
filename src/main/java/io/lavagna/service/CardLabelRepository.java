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
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Label;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.LabelListValue;
import io.lavagna.model.UserWithPermission;
import io.lavagna.query.CardLabelQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CardLabelRepository {

	private final NamedParameterJdbcTemplate jdbc;
	private final CardLabelQuery queries;

	@Autowired
	public CardLabelRepository(NamedParameterJdbcTemplate jdbc, CardLabelQuery queries) {
		this.jdbc = jdbc;
		this.queries = queries;
	}

	@Transactional(readOnly = false)
	public void addSystemLabels(int projectId) {
		queries.addSystemLabels(projectId);
	}

	@Transactional(readOnly = false)
	public CardLabel addLabel(int projectId, boolean unique, LabelType labelType, LabelDomain labelDomain, String name,
			int color) {
		final boolean reservedName = CardLabel.RESERVED_SYSTEM_LABELS_NAME.contains(name);
		Validate.isTrue((labelDomain == LabelDomain.SYSTEM && reservedName)
				|| (labelDomain == LabelDomain.USER && !reservedName), name + " is a reserved system label name");

		queries.addLabel(projectId, unique, labelType.toString(), labelDomain.toString(), name, color);

		return queries.findLastCreatedLabel();
	}

	@Transactional(readOnly = false)
	public void removeLabel(int labelId) {
		queries.removeLabelListValues(labelId);
		queries.removeLabel(labelId);
	}

	public List<CardLabel> findLabelsByProject(int projectId) {
		return queries.findLabelsByProject(projectId);
	}

	public CardLabel findLabelById(int labelId) {
		return queries.findLabelById(labelId);
	}

	public CardLabel findLabelByName(int projectId, String labelName, LabelDomain labelDomain) {
		return queries.findLabelByName(projectId, labelName, labelDomain.toString());
	}

	public List<CardLabel> findLabelsByName(int projectId, String labelName, LabelDomain labelDomain) {
		return queries.findLabelsByName(projectId, labelName, labelDomain.toString());
	}

	public CardLabelValue findLabelValueById(int labelValueId) {
		return queries.findLabelValueById(labelValueId);
	}

	public List<CardLabelValue> findLabelValueByLabelAndValue(int cardId, CardLabel cl, LabelValue lv) {
		return queries.findLabelValueByLabelAndValue(cardId, cl.getId(), lv.getValueString(), lv.getValueTimestamp(),
				lv.getValueInt(), lv.getValueCard(), lv.getValueUser(), lv.getValueList());
	}

	/**
	 * Return a map of label by {cardId => {CardLabel => [CardLabelValue]}}
	 * 
	 * @return
	 */
	public Map<Integer, Map<CardLabel, List<CardLabelValue>>> findCardLabelValuesByBoardId(int boardId,
			BoardColumnLocation location) {

		Map<Integer, Map<CardLabel, List<CardLabelValue>>> res = new HashMap<>();
		for (LabelAndValue lv : queries.findCardLabelValuesByBoardId(boardId, location.toString())) {
			if (!res.containsKey(lv.getLabelValueCardId())) {
				res.put(lv.getLabelValueCardId(), new HashMap<CardLabel, List<CardLabelValue>>());
			}
			CardLabel cl = lv.label();
			if (!res.get(lv.getLabelValueCardId()).containsKey(cl)) {
				res.get(lv.getLabelValueCardId()).put(cl, new ArrayList<CardLabelValue>());
			}
			res.get(lv.getLabelValueCardId()).get(cl).add(lv.labelValue());
		}
		return res;
	}

	public Map<Integer, List<LabelAndValue>> findCardLabelValuesByCardIds(List<Integer> ids) {
		Map<Integer, List<LabelAndValue>> res = new HashMap<>();
		for (LabelAndValue lv : queries.findCardLabelValuesByCardIds(ids)) {
			if (!res.containsKey(lv.getLabelValueCardId())) {
				res.put(lv.getLabelValueCardId(), new ArrayList<LabelAndValue>());
			}
			res.get(lv.getLabelValueCardId()).add(lv);
		}
		return res;
	}

	public Map<CardLabel, List<CardLabelValue>> findCardLabelValuesByCardId(int cardId) {

		Map<CardLabel, List<CardLabelValue>> res = new HashMap<>();

		for (LabelAndValue lv : queries.findCardLabelValuesByCardId(cardId)) {
			CardLabel cl = lv.label();
			if (!res.containsKey(cl)) {
				res.put(cl, new ArrayList<CardLabelValue>());
			}
			res.get(cl).add(lv.labelValue());
		}

		return res;
	}

	@Transactional(readOnly = false)
	public CardLabel updateLabel(int labelId, Label label) {
		CardLabel cl = findLabelById(labelId);
		Validate.isTrue(cl.getDomain() == LabelDomain.USER, "Cannot update values in SYSTEM label for label with id "
				+ labelId);
		return updateLabel(label, cl);
	}

	@Transactional(readOnly = false)
	public CardLabel updateSystemLabel(int labelId, Label label) {
		CardLabel cl = findLabelById(labelId);
		Validate.isTrue(cl.getDomain() == LabelDomain.SYSTEM, "Cannot update values in USER label for label with id "
				+ labelId);
		return updateLabel(label, cl);
	}

	public List<String> findUserLabelNameBy(String term, Integer projectId, UserWithPermission userWithPermission) {
		Set<Integer> projectIdFilter = userWithPermission.toProjectIdsFilter(projectId);
		return projectIdFilter.isEmpty() ? queries.findUserLabelNameBy(term) :  queries.findUserLabelNameBy(term, projectIdFilter);
	}

	public List<String> findListValuesBy(LabelDomain domain, String labelName, String term, Integer projectId, UserWithPermission userWithPermission) {
		Set<Integer> projectIdFilter = userWithPermission.toProjectIdsFilter(projectId);
		return projectIdFilter.isEmpty() ? queries.findListValuesBy(domain.toString(), labelName, term) : queries.findListValuesBy(domain.toString(), labelName, term, projectIdFilter);
	}
	

	@Transactional(readOnly = false)
	private CardLabel updateLabel(Label label, CardLabel cl) {
		// type cannot be changed!
		Validate.isTrue(cl.getType() == label.getType());

		CardLabel toUpdate = cl.set(label.getName(), label.getType(), label.getColor());

		queries.updateLabel(toUpdate.getName(), toUpdate.getColor(), toUpdate.getType().toString(), toUpdate.getId());

		return toUpdate;
	}

	@Transactional(readOnly = false)
	public CardLabelValue addLabelValueToCard(CardLabel label, int cardId, LabelValue val) {

		queries.addLabelValueToCard(cardId, label.isUnique() ? true : null, label.getId(), label.getType().toString(),
				val.getValueString(), val.getValueTimestamp(), val.getValueInt(), val.getValueCard(),
				val.getValueUser(), val.getValueList());

		return queries.findLastCreatedLabelValue();
	}

	@Transactional(readOnly = false)
	public int removeLabelValue(CardLabelValue cardLabelValue) {
		return queries.removeLabelValue(cardLabelValue.getCardLabelValueId());
	}

	// Label list values

	@Transactional(readOnly = false)
	public LabelListValue addLabelListValue(int labelId, String value) {
		queries.addLabelListValue(labelId, value);
		return queries.findLastCreatedLabelListValue();
	}

	@Transactional(readOnly = false)
	public void removeLabelListValue(int labelListValueId) {
		queries.removeLabelListValue(labelListValueId);
	}

	@Transactional(readOnly = false)
	public void updateLabelListValue(LabelListValue lvl) {
		queries.updateLabelListValue(lvl.getId(), lvl.getValue());
	}

	public List<LabelListValue> findListValuesByLabelId(int labelId) {
		return queries.findListValuesByLabelId(labelId);
	}

	public List<LabelListValue> findListValuesByLabelIdAndValue(int labelId, String value) {
		return queries.findListValuesByLabelIdAndValue(labelId, value);
	}

	public LabelListValue findListValueById(int labelListValueId) {
		return queries.findListValueById(labelListValueId);
	}

	@Transactional(readOnly = false)
	public void swapLabelListValues(int first, int second) {
		LabelListValue firstValue = findListValueById(first);
		LabelListValue secondValue = findListValueById(second);
		SqlParameterSource p1 = new MapSqlParameterSource("id", firstValue.getId()).addValue("order",
				secondValue.getOrder());
		SqlParameterSource p2 = new MapSqlParameterSource("id", secondValue.getId()).addValue("order",
				firstValue.getOrder());
		jdbc.batchUpdate(queries.swapLabelListValues(), new SqlParameterSource[] { p1, p2 });
	}

	public Map<String, Map<Integer, Integer>> findLabelListValueMapping(List<String> labelValues) {
		final Map<String, Map<Integer, Integer>> res = new HashMap<>();
		jdbc.query(queries.findLabelListValueMapping(), new MapSqlParameterSource("values", labelValues),
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String name = rs.getString("CARD_LABEL_LIST_VALUE");
						if (!res.containsKey(name)) {
							res.put(name, new HashMap<Integer, Integer>());
						}
						res.get(name).put(rs.getInt("CARD_LABEL_ID_FK"), rs.getInt("CARD_LABEL_LIST_VALUE_ID"));
					}
				});
		return res;
	}

	public int labelUsedCount(int labelId) {
		return queries.labelUsedCount(labelId);
	}

}
