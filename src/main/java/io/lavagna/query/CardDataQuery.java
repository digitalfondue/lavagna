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
import io.lavagna.model.*;

import java.util.Collection;
import java.util.List;

@QueryRepository
public interface CardDataQuery {

	@Query("SELECT CARD_DATA_ID, CARD_DATA_CONTENT FROM LA_CARD_DATA WHERE CARD_DATA_ID IN (:ids)")
	List<CardIdAndContent> findDataByIds(@Bind("ids") Collection<Integer> ids);

	@Query("SELECT CARD_DATA_ID, CARD_DATA_CARD_ID_FK, CARD_DATA_REFERENCE_ID, CARD_DATA_DELETED, CARD_DATA_TYPE, CARD_DATA_ORDER FROM LA_CARD_DATA WHERE CARD_DATA_ID = :id")
	CardDataMetadata findMetadataById(@Bind("id") int id);

	@Query("SELECT CARD_DATA_ID, CARD_DATA_CONTENT FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_REFERENCE_ID = :refId AND CARD_DATA_TYPE = :type AND CARD_DATA_ORDER = :order")
	List<CardIdAndContent> findContentWith(@Bind("cardId") int cardId, @Bind("refId") int refId,
			@Bind("type") String type, @Bind("order") int order);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_ID = :id AND CARD_DATA_DELETED = FALSE")
	CardData getUndeletedDataLightById(@Bind("id") int id);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_ID = :id")
	CardData getDataLightById(@Bind("id") int id);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC, CARD_DATA_ORDER ASC")
	@QueriesOverride(@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC NULLS FIRST, CARD_DATA_ORDER ASC"))
	List<CardData> findAllLightByCardId(@Bind("cardId") int cardId);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId and CARD_DATA_TYPE = :type AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC, CARD_DATA_ORDER ASC")
	@QueriesOverride(@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId and CARD_DATA_TYPE = :type AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC NULLS FIRST, CARD_DATA_ORDER ASC"))
	List<CardData> findAllLightByCardIdAndType(@Bind("cardId") int cardId, @Bind("type") String type);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId and CARD_DATA_TYPE IN (:types) AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC, CARD_DATA_ORDER ASC")
	@QueriesOverride(@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId and CARD_DATA_TYPE IN (:types) AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC NULLS FIRST, CARD_DATA_ORDER ASC"))
	List<CardData> findAllLightByCardIdAndTypes(@Bind("cardId") int cardId, @Bind("types") List<String> types);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_REFERENCE_ID = :referenceId AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_ORDER ASC")
	List<CardData> findAllLightByReferenceId(@Bind("referenceId") int referenceId);

	@Query("SELECT CARD_DATA_ID, CARD_DATA_ORDER FROM LA_CARD_DATA WHERE CARD_DATA_TYPE IN (:types)")
	List<CardDataIdAndOrder> findAllCardDataIdAndOrderByType(@Bind("types") List<String> types);

	/**
	 * Will return the deleted one too.
	 */
	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_REFERENCE_ID = :referenceId AND CARD_DATA_TYPE = :type ORDER BY CARD_DATA_ORDER ASC")
	List<CardData> findAllLightByReferenceIdAndType(@Bind("referenceId") int referenceId, @Bind("type") String type);

	@Query("INSERT INTO LA_CARD_DATA(CARD_DATA_CARD_ID_FK,CARD_DATA_TYPE,CARD_DATA_CONTENT,CARD_DATA_ORDER) "
			+ " VALUES (:cardId, :type, :body, (SELECT * FROM (SELECT COALESCE(MAX(CARD_DATA_ORDER),0) + 1 FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_TYPE = :type) AS MAX_CARD_DATA_ORDER))")
	int create(@Bind("cardId") int cardId, @Bind("type") String type, @Bind("body") String content);

	@Query("INSERT INTO LA_CARD_DATA(CARD_DATA_CARD_ID_FK,CARD_DATA_REFERENCE_ID,CARD_DATA_TYPE,CARD_DATA_CONTENT,CARD_DATA_ORDER) "
			+ " VALUES (:cardId, :referenceId, :type, :body, (SELECT * FROM (SELECT COALESCE(MAX(CARD_DATA_ORDER),0) + 1 FROM LA_CARD_DATA WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_REFERENCE_ID = :referenceId) AS MAX_CARD_DATA_ORDER))")
	int createWithReferenceOrder(@Bind("cardId") int cardId, @Bind("referenceId") Integer referenceId,
			@Bind("type") String type, @Bind("body") String content);

	@Query("SELECT CARD_DATA_ID, CARD_DATA_CARD_ID_FK, CARD_DATA_REFERENCE_ID, CARD_DATA_TYPE, CARD_DATA_CONTENT, CARD_DATA_ORDER, EVENT_TIME, EVENT_TYPE, EVENT_PREV_CARD_DATA_ID_FK, EVENT_USER_ID_FK "
			+ " FROM LA_CARD_DATA_FULL WHERE CARD_DATA_CARD_ID_FK = :cardId and CARD_DATA_TYPE = :type AND CARD_DATA_DELETED = FALSE ORDER BY CARD_DATA_REFERENCE_ID ASC, CARD_DATA_ORDER ASC")
	List<CardDataFull> findAllByCardIdAndType(@Bind("cardId") int cardId, @Bind("type") String type);

	@Query("SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_DATA WHERE CARD_DATA_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_card_data','card_data_id')))") })
	CardData findLastCreatedLight();

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_TYPE = :type WHERE CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)")
	int updateType(@Bind("type") String type, @Bind("id") int id, @Bind("types") List<String> types);

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_CARD_DATA SET CARD_DATA_ORDER = :order WHERE CARD_DATA_ID = :id AND CARD_DATA_CARD_ID_FK = :cardId")
	String updateOrder();

	@Query("SELECT CARD_DATA_ID FROM LA_CARD_DATA WHERE CARD_DATA_ID IN (:ids) AND CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_TYPE = :cardDataType")
	List<Integer> findAllCardDataIdsBy(@Bind("ids") List<Integer> ids, @Bind("cardId") int cardId,
			@Bind("cardDataType") String cardDataType);

	@Query("SELECT CARD_DATA_ID FROM LA_CARD_DATA WHERE CARD_DATA_ID IN (:ids) AND CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_TYPE IN (:cardDataTypes) AND CARD_DATA_REFERENCE_ID = :referenceId")
	List<Integer> findAllCardDataIdsBy(@Bind("ids") List<Integer> ids, @Bind("cardId") int cardId,
			@Bind("referenceId") int referenceId, @Bind("cardDataTypes") List<String> cardDataTypes);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_ORDER = :order WHERE CARD_DATA_ID = :id")
	int updateOrderById(@Bind("id") int id, @Bind("order") int order);

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_CARD_DATA SET CARD_DATA_ORDER = :order WHERE CARD_DATA_ID = :id AND CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_REFERENCE_ID = :referenceId")
	String updateOrderByCardAndReferenceId();

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_REFERENCE_ID = :referenceId WHERE CARD_DATA_ID = :id AND CARD_DATA_CARD_ID_FK = :cardId")
	int updateReferenceId(@Bind("referenceId") Integer referenceId, @Bind("id") int id, @Bind("cardId") int cardId);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_CONTENT = :body WHERE CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)")
	int updateContent(@Bind("body") String content, @Bind("id") int id, @Bind("types") List<String> types);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_DELETED = TRUE WHERE CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)")
	int softDelete(@Bind("id") int id, @Bind("types") List<String> types);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_DELETED = FALSE WHERE CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)")
	int undoSoftDelete(@Bind("id") int id, @Bind("types") List<String> types);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_DELETED = TRUE WHERE (CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)) OR (CARD_DATA_REFERENCE_ID = :id)")
	int softDeleteOnCascade(@Bind("id") int id, @Bind("types") List<String> types);

	@Query("UPDATE LA_CARD_DATA SET CARD_DATA_DELETED = FALSE WHERE "
			+ " ((CARD_DATA_ID = :id AND CARD_DATA_TYPE IN (:types)) OR "
			+ " (CARD_DATA_REFERENCE_ID = :id)) "
			+ " AND (CARD_DATA_ID NOT IN (SELECT * FROM (SELECT CARD_DATA_ID FROM LA_CARD_DATA INNER JOIN LA_EVENT ON CARD_DATA_ID = EVENT_CARD_DATA_ID_FK WHERE CARD_DATA_REFERENCE_ID = :id AND EVENT_TYPE IN (:filteredEvents)) AS CDATA_WITH_REF))")
	int undoSoftDeleteOnCascade(@Bind("id") int id, @Bind("types") List<String> types,
			@Bind("filteredEvents") List<String> filteredEvents);

	@Query("SELECT CARD_ID, CARD_DATA_TYPE, CARD_DATA_TYPE_COUNT FROM LA_CARD_DATA_COUNT"
			+ " INNER JOIN LA_BOARD_COLUMN ON BOARD_COLUMN_ID = CARD_BOARD_COLUMN_ID_FK"
			+ " WHERE BOARD_ID = :boardId AND BOARD_COLUMN_LOCATION = :location")
	List<CardDataCount> findCountsByBoardIdAndLocation(@Bind("boardId") int boardId, @Bind("location") String location);

	@Query("SELECT CARD_DATA_CARD_ID_FK AS CARD_ID, CARD_DATA_TYPE,  COUNT(CARD_DATA_TYPE) AS CARD_DATA_TYPE_COUNT FROM LA_CARD_DATA WHERE CARD_DATA_DELETED = FALSE AND CARD_DATA_CARD_ID_FK IN (:ids) GROUP BY CARD_DATA_CARD_ID_FK, CARD_DATA_TYPE")
	List<CardDataCount> findCountsByCardIds(@Bind("ids") List<Integer> ids);

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_CARD_DATA_UPLOAD_CONTENT(DIGEST,SIZE,CONTENT,CONTENT_TYPE) VALUES (?, ?, ?, ?)")
	String addUploadContent();

	@Query("SELECT COUNT(1) FROM LA_CARD_DATA_UPLOAD_CONTENT WHERE DIGEST = :digest")
	Integer findDigest(@Bind("digest") String digest);

	@Query("SELECT COUNT(1) FROM LA_CARD_DATA_UPLOAD_CONTENT_LIGHT WHERE CARD_DATA_CARD_ID_FK = :cardId AND CARD_DATA_CONTENT = :digest")
	Integer isFileAvailableByCard(@Bind("cardId") int cardId, @Bind("digest") String digest);

	@Query("INSERT INTO LA_CARD_DATA_UPLOAD(CARD_DATA_ID_FK,CARD_DATA_UPLOAD_CONTENT_DIGEST_FK,ORIGINAL_NAME,DISPLAYED_NAME) VALUES (:cardData, :digest, :name, :displayName)")
	int mapUploadContent(@Bind("cardData") int cardData, @Bind("digest") String digest, @Bind("name") String name,
			@Bind("displayName") String displayName);

	@Query("SELECT * FROM LA_CARD_DATA_UPLOAD_CONTENT_LIGHT WHERE CARD_DATA_CARD_ID_FK = :cardId")
	List<FileDataLight> findAllFilesByCardId(@Bind("cardId") int cardId);

	@Query("SELECT * FROM LA_CARD_DATA_UPLOAD_CONTENT_LIGHT WHERE CARD_DATA_ID = :cardDataId")
	FileDataLight getUndeletedFileByCardDataId(@Bind("cardDataId") int cardDataId);

	@Query("SELECT DIGEST,SIZE,CONTENT_TYPE  FROM LA_CARD_DATA_UPLOAD_CONTENT")
	List<CardDataUploadContentInfo> findAllDataUploadContentInfo();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CONTENT, CONTENT_TYPE FROM LA_CARD_DATA_UPLOAD_CONTENT WHERE DIGEST = :digest")
	String fileContent();

}
