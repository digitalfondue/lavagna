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
package io.lavagna.query;

import ch.digitalfondue.npjt.*;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.LabelListValue;

import java.util.Date;
import java.util.List;
import java.util.Set;

@QueryRepository
public interface CardLabelQuery {

	@Query("INSERT INTO LA_CARD_LABEL(CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR) VALUES (:projectId, :unique, :type, :domain, :name, :color)")
	int addLabel(@Bind("projectId") int projectId, @Bind("unique") boolean unique, @Bind("type") String type,
			@Bind("domain") String domain, @Bind("name") String name, @Bind("color") int color);

	@Query("INSERT INTO LA_CARD_LABEL_LIST_VALUE(CARD_LABEL_ID_FK, CARD_LABEL_LIST_VALUE_ORDER, CARD_LABEL_LIST_VALUE) VALUES (:cardLabelId, (SELECT * FROM (SELECT COALESCE(MAX(CARD_LABEL_LIST_VALUE_ORDER),0) + 1 FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_ID_FK = :cardLabelId) MAX_ORDER), :value)")
	int addLabelListValue(@Bind("cardLabelId") int cardLabelId, @Bind("value") String value);

	@Query("UPDATE LA_CARD_LABEL_LIST_VALUE SET CARD_LABEL_LIST_VALUE = :value WHERE CARD_LABEL_LIST_VALUE_ID = :id")
	int updateLabelListValue(@Bind("id") int id, @Bind("value") String value);

	@Query("SELECT COUNT(CARD_LABEL_VALUE_ID) FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_ID_FK = :labelId")
	Integer labelUsedCount(@Bind("labelId") int labelId);

	@Query("INSERT INTO LA_CARD_LABEL(CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR) VALUES "
			+ " (:projectId, FALSE, 'USER', 'SYSTEM', 'ASSIGNED', 0), "
			+ " (:projectId, TRUE, 'TIMESTAMP', 'SYSTEM', 'DUE_DATE', 0), "
			+ " (:projectId, TRUE, 'LIST', 'SYSTEM', 'MILESTONE', 0), "
			+ " (:projectId, FALSE, 'USER', 'SYSTEM', 'WATCHED_BY', 0)")
	int addSystemLabels(@Bind("projectId") int projectId);

	@Query("SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_PROJECT_ID_FK = :projectId")
	List<CardLabel> findLabelsByProject(@Bind("projectId") int projectId);

	@Query("SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = :labelId")
	CardLabel findLabelById(@Bind("labelId") int labelId);

	@Query("SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_NAME = :labelName AND CARD_LABEL_DOMAIN = :labelDomain AND CARD_LABEL_PROJECT_ID_FK = :projectId")
	CardLabel findLabelByName(@Bind("projectId") int projectId, @Bind("labelName") String labelName,
			@Bind("labelDomain") String labelDomain);

	@Query("SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_NAME = :labelName AND CARD_LABEL_DOMAIN = :labelDomain AND CARD_LABEL_PROJECT_ID_FK = :projectId")
	List<CardLabel> findLabelsByName(@Bind("projectId") int projectId, @Bind("labelName") String labelName,
			@Bind("labelDomain") String labelDomain);

	@Query("SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = :labelValueId")
	CardLabelValue findLabelValueById(@Bind("labelValueId") int labelValueId);


	@Query("SELECT LA_CARD_LABEL_LIST_VALUE.* FROM LA_CARD_LABEL_LIST_VALUE "
	        + " inner join LA_CARD_LABEL on CARD_LABEL_ID_FK = CARD_LABEL_ID where "
	        + " CARD_LABEL_PROJECT_ID_FK = :projectId ORDER BY CARD_LABEL_ID_FK, CARD_LABEL_LIST_VALUE_ORDER")
	List<LabelListValue> findListValueByProjectId(@Bind("projectId") int projectId);

	@Query("SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_ID_FK = :labelId ORDER BY CARD_LABEL_LIST_VALUE_ORDER")
	List<LabelListValue> findListValuesByLabelId(@Bind("labelId") int labelId);

	@Query("SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_ID_FK = :labelId AND CARD_LABEL_LIST_VALUE = :value ORDER BY CARD_LABEL_LIST_VALUE_ORDER")
	List<LabelListValue> findListValuesByLabelIdAndValue(@Bind("labelId") int labelId, @Bind("value") String value);

	@Query("SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = :labelListValueId")
	LabelListValue findListValueById(@Bind("labelListValueId") int labelListValueId);

	@Query("UPDATE LA_CARD_LABEL SET CARD_LABEL_NAME = :name, CARD_LABEL_COLOR = :color, CARD_LABEL_TYPE = :type WHERE CARD_LABEL_ID = :cardLabelId")
	int updateLabel(@Bind("name") String name, @Bind("color") int color, @Bind("type") String type,
			@Bind("cardLabelId") int cardLabelId);

	@Query("DELETE FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = :labelId")
	int removeLabel(@Bind("labelId") int labelId);

	@Query("DELETE FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_ID_FK = :labelId")
	int removeLabelListValues(@Bind("labelId") int labelId);

	@Query("INSERT INTO LA_CARD_LABEL_VALUE(CARD_ID_FK, CARD_LABEL_VALUE_USE_UNIQUE_INDEX, CARD_LABEL_ID_FK, CARD_LABEL_VALUE_TYPE, CARD_LABEL_VALUE_STRING, CARD_LABEL_VALUE_TIMESTAMP, CARD_LABEL_VALUE_INT, CARD_LABEL_VALUE_CARD_FK, CARD_LABEL_VALUE_USER_FK, CARD_LABEL_VALUE_LIST_VALUE_FK) VALUES (:cardId, :useUniqueIndex, :labelId, :valueType, :valueString, :valueTimestamp, :valueInt, :valueCard, :valueUser, :valueList)")
	int addLabelValueToCard(@Bind("cardId") int cardId, @Bind("useUniqueIndex") Boolean useUniqueIndex,
			@Bind("labelId") int labelId, @Bind("valueType") String valueType, @Bind("valueString") String valueString,
			@Bind("valueTimestamp") Date valueTimestamp, @Bind("valueInt") Integer valueInt,
			@Bind("valueCard") Integer valueCard, @Bind("valueUser") Integer valueUser,
			@Bind("valueList") Integer valueList);

	@Query("DELETE FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = :cardLabelValueId")
	int removeLabelValue(@Bind("cardLabelValueId") int cardLabelValueId);

	@Query("DELETE FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = :labelListValueId")
	int removeLabelListValue(@Bind("labelListValueId") int labelListValueId);

	@Query("SELECT CARD_LABEL_ID, CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR, "
			+ " CARD_LABEL_VALUE_ID, CARD_LABEL_VALUE_USE_UNIQUE_INDEX, CARD_ID_FK, CARD_LABEL_ID_FK, CARD_LABEL_VALUE_TYPE, CARD_LABEL_VALUE_STRING, "
			+ " CARD_LABEL_VALUE_TIMESTAMP, CARD_LABEL_VALUE_INT, CARD_LABEL_VALUE_CARD_FK, CARD_LABEL_VALUE_USER_FK, CARD_LABEL_VALUE_LIST_VALUE_FK "
			+ " FROM LA_CARD_LABEL_VALUE INNER JOIN LA_CARD_LABEL ON LA_CARD_LABEL.CARD_LABEL_ID = LA_CARD_LABEL_VALUE.CARD_LABEL_ID_FK WHERE CARD_ID_FK = :cardId AND CARD_LABEL_VALUE_DELETED = FALSE")
	List<LabelAndValue> findCardLabelValuesByCardId(@Bind("cardId") int cardId);

	@Query("SELECT CARD_LABEL_ID, CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR, "
			+ "CARD_LABEL_VALUE_ID, CARD_LABEL_VALUE_USE_UNIQUE_INDEX, CARD_ID_FK, CARD_LABEL_ID_FK, CARD_LABEL_VALUE_TYPE, CARD_LABEL_VALUE_STRING, "
			+ "CARD_LABEL_VALUE_TIMESTAMP, CARD_LABEL_VALUE_INT, CARD_LABEL_VALUE_CARD_FK, CARD_LABEL_VALUE_USER_FK, CARD_LABEL_VALUE_LIST_VALUE_FK "
			+ "FROM LA_CARD_LABEL_VALUE INNER JOIN LA_CARD_LABEL ON LA_CARD_LABEL.CARD_LABEL_ID = LA_CARD_LABEL_VALUE.CARD_LABEL_ID_FK WHERE CARD_ID_FK in (:ids) AND CARD_LABEL_VALUE_DELETED = FALSE")
	List<LabelAndValue> findCardLabelValuesByCardIds(@Bind("ids") List<Integer> ids);

	@Query("SELECT  CARD_LABEL_ID, CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR, "
			+ "CARD_LABEL_VALUE_ID, CARD_LABEL_VALUE_USE_UNIQUE_INDEX, CARD_ID_FK, CARD_LABEL_ID_FK, CARD_LABEL_VALUE_TYPE, CARD_LABEL_VALUE_STRING, "
			+ "CARD_LABEL_VALUE_TIMESTAMP, CARD_LABEL_VALUE_INT, CARD_LABEL_VALUE_CARD_FK, CARD_LABEL_VALUE_USER_FK, CARD_LABEL_VALUE_LIST_VALUE_FK "
			+ "FROM LA_CARD_LABEL_VALUE INNER JOIN LA_CARD_LABEL ON LA_CARD_LABEL.CARD_LABEL_ID = LA_CARD_LABEL_VALUE.CARD_LABEL_ID_FK "
			+ "INNER JOIN LA_CARD ON LA_CARD_LABEL_VALUE.CARD_ID_FK = LA_CARD.CARD_ID  "
			+ "INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
			+ "WHERE BOARD_COLUMN_BOARD_ID_FK = :boardId AND CARD_LABEL_VALUE_DELETED = FALSE AND BOARD_COLUMN_LOCATION = :location")
	List<LabelAndValue> findCardLabelValuesByBoardId(@Bind("boardId") int boardId, @Bind("location") String location);

	@Query("SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_card_label','card_label_id')))") })
	CardLabel findLastCreatedLabel();

	@Query("SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_card_label_value','card_label_value_id')))") })
	CardLabelValue findLastCreatedLabelValue();

	@Query("SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_card_label_list_value','card_label_list_value_id')))") })
	LabelListValue findLastCreatedLabelListValue();

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_CARD_LABEL_LIST_VALUE SET CARD_LABEL_LIST_VALUE_ORDER = :order WHERE CARD_LABEL_LIST_VALUE_ID = :id")
	String updateLabelListValueOrder();

	@Query("SELECT DISTINCT CARD_LABEL_LIST_VALUE FROM LA_CARD_LABEL_LIST_VALUE INNER JOIN LA_CARD_LABEL ON CARD_LABEL_ID_FK = CARD_LABEL_ID "
			+ " WHERE CARD_LABEL_DOMAIN = :domain AND CARD_LABEL_NAME = :labelName AND CARD_LABEL_LIST_VALUE LIKE CONCAT(:term, '%') ORDER BY CARD_LABEL_LIST_VALUE ASC LIMIT 20")
	List<String> findListValuesBy(@Bind("domain") String domain, @Bind("labelName") String labelName, @Bind("term") String term);

	@Query("SELECT DISTINCT CARD_LABEL_LIST_VALUE FROM LA_CARD_LABEL_LIST_VALUE INNER JOIN LA_CARD_LABEL ON CARD_LABEL_ID_FK = CARD_LABEL_ID "
			+ " WHERE CARD_LABEL_PROJECT_ID_FK IN (:projectIdFilter) AND CARD_LABEL_DOMAIN = :domain AND CARD_LABEL_NAME = :labelName AND CARD_LABEL_LIST_VALUE LIKE CONCAT(:term, '%') ORDER BY CARD_LABEL_LIST_VALUE ASC LIMIT 20")
	List<String> findListValuesBy(@Bind("domain") String domain, @Bind("labelName") String labelName, @Bind("term") String term, @Bind("projectIdFilter") Set<Integer> projectIdFilter);

	@Query("SELECT DISTINCT CARD_LABEL_ID, CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR FROM LA_CARD_LABEL "
			+ " WHERE CARD_LABEL_DOMAIN = 'USER' AND LOWER(CARD_LABEL_NAME) LIKE CONCAT(LOWER(:term), '%') ORDER BY CARD_LABEL_NAME ASC LIMIT 20")
	List<CardLabel> findUserLabelNameBy(@Bind("term") String term);

	@Query("SELECT DISTINCT CARD_LABEL_ID, CARD_LABEL_PROJECT_ID_FK, CARD_LABEL_UNIQUE, CARD_LABEL_TYPE, CARD_LABEL_DOMAIN, CARD_LABEL_NAME, CARD_LABEL_COLOR FROM LA_CARD_LABEL "
			+ " WHERE CARD_LABEL_PROJECT_ID_FK IN (:projectIdFilter) AND CARD_LABEL_DOMAIN = 'USER' AND LOWER(CARD_LABEL_NAME) LIKE CONCAT(LOWER(:term), '%') ORDER BY CARD_LABEL_NAME ASC LIMIT 20")
	List<CardLabel> findUserLabelNameBy(@Bind("term") String term, @Bind("projectIdFilter") Set<Integer> projectIdFilter);

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_LABEL_LIST_VALUE, CARD_LABEL_ID_FK, CARD_LABEL_LIST_VALUE_ID FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE IN (:values)")
	String findLabelListValueMapping();

	@Query("SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_ID_FK = :cardId AND CARD_LABEL_ID_FK = :labelId AND "
			+ " (CARD_LABEL_VALUE_STRING = :valueString OR (CARD_LABEL_VALUE_STRING IS NULL AND :valueString IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_TIMESTAMP = :valueTimestamp OR (CARD_LABEL_VALUE_TIMESTAMP IS NULL AND :valueTimestamp IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_INT = :valueInt OR (CARD_LABEL_VALUE_INT IS NULL AND :valueInt IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_CARD_FK = :valueCard OR (CARD_LABEL_VALUE_CARD_FK IS NULL AND :valueCard IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_USER_FK = :valueUser OR (CARD_LABEL_VALUE_USER_FK IS NULL AND :valueUser IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_LIST_VALUE_FK = :valueList OR (CARD_LABEL_VALUE_LIST_VALUE_FK IS NULL AND :valueList IS NULL))")
	// pgsql need to have a typecast...
	@QueriesOverride({ @QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_LABEL_VALUE WHERE CARD_ID_FK = :cardId AND CARD_LABEL_ID_FK = :labelId AND "
			+ " (CARD_LABEL_VALUE_STRING = :valueString OR (CARD_LABEL_VALUE_STRING IS NULL AND :valueString IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_TIMESTAMP = :valueTimestamp OR (CARD_LABEL_VALUE_TIMESTAMP IS NULL AND :valueTimestamp::timestamp IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_INT = :valueInt OR (CARD_LABEL_VALUE_INT IS NULL AND :valueInt IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_CARD_FK = :valueCard OR (CARD_LABEL_VALUE_CARD_FK IS NULL AND :valueCard IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_USER_FK = :valueUser OR (CARD_LABEL_VALUE_USER_FK IS NULL AND :valueUser IS NULL)) AND "
			+ " (CARD_LABEL_VALUE_LIST_VALUE_FK = :valueList OR (CARD_LABEL_VALUE_LIST_VALUE_FK IS NULL AND :valueList IS NULL))") })
	List<CardLabelValue> findLabelValueByLabelAndValue(@Bind("cardId") int cardId, @Bind("labelId") int labelId,
			@Bind("valueString") String valueString, @Bind("valueTimestamp") Date valueTimestamp,
			@Bind("valueInt") Integer valueInt, @Bind("valueCard") Integer valueCard,
			@Bind("valueUser") Integer valueUser, @Bind("valueList") Integer valueList);
}
