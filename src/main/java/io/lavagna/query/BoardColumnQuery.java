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
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumnInfo;

import java.util.List;
import java.util.Set;

@QueryRepository
public interface BoardColumnQuery {

	@Query("SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = :columnId")
	BoardColumn findById(@Bind("columnId") int columnId);

	@Query("SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID IN (:ids)")
	List<BoardColumn> findByIds(@Bind("ids") Set<Integer> ids);

	@Query("SELECT BOARD_COLUMN_ID FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID IN (:ids) AND BOARD_COLUMN_LOCATION = :location AND BOARD_COLUMN_BOARD_ID_FK = :boardId")
	List<Integer> findColumnIdsInBoard(@Bind("ids") List<Integer> ids, @Bind("location") String location, @Bind("boardId") int boardId);

	@Query("SELECT CARD_ID FROM LA_CARD WHERE CARD_BOARD_COLUMN_ID_FK = :columnId")
	List<Integer> findCardsInColumnId(@Bind("columnId") int columnId);

	@Query("INSERT INTO LA_BOARD_COLUMN(BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_LOCATION, BOARD_COLUMN_DEFINITION_ID_FK) VALUES "
			+ "(:name,  (SELECT * FROM (SELECT COALESCE(MAX(BOARD_COLUMN_ORDER),0) + 1 FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_BOARD_ID_FK = :boardId AND BOARD_COLUMN_LOCATION = :location) AS MAX_BOARD_COLUMN_ORDER), "
			+ ":boardId, :location, :definitionId)")
	int addColumnToBoard(@Bind("name") String name, @Bind("boardId") int boardId, @Bind("location") String location,
			@Bind("definitionId") int definitionId);

    @Query("INSERT INTO LA_BOARD_COLUMN(BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_LOCATION, BOARD_COLUMN_DEFINITION_ID_FK) VALUES "
        + "(:name, :order, :boardId, :location, :definitionId)")
    int addColumnToBoard(@Bind("name") String name, @Bind("boardId") int boardId, @Bind("location") String location,
                         @Bind("order") int order, @Bind("definitionId") int definitionId);

	@Query("SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE,"
			+ " BOARD_COLUMN_DEFINITION_COLOR FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_BOARD_ID_FK = :boardId AND BOARD_COLUMN_LOCATION = :location "
			+ "ORDER BY BOARD_COLUMN_ORDER ASC, BOARD_COLUMN_NAME ASC")
	List<BoardColumn> findAllColumnFor(@Bind("boardId") int boardId, @Bind("location") String location);

	@Query("SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE,"
			+ " BOARD_COLUMN_DEFINITION_COLOR FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_BOARD_ID_FK = :boardId "
			+ "ORDER BY BOARD_COLUMN_ORDER ASC, BOARD_COLUMN_NAME ASC")
	List<BoardColumn> findAllColumnFor(@Bind("boardId") int boardId);

	@Query("SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE, BOARD_COLUMN_DEFINITION_COLOR FROM LA_BOARD_COLUMN_FULL "
			+ "WHERE BOARD_COLUMN_BOARD_ID_FK = :boardId AND BOARD_COLUMN_LOCATION = :location AND BOARD_COLUMN_NAME = :location")
	BoardColumn findDefaultColumnFor(@Bind("boardId") int boardId, @Bind("location") String location);

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_BOARD_COLUMN SET BOARD_COLUMN_ORDER = :order WHERE BOARD_COLUMN_ID = :columnId AND BOARD_COLUMN_BOARD_ID_FK = :boardId AND BOARD_COLUMN_LOCATION = :location")
	String updateColumnOrder();

	@Query("UPDATE LA_BOARD_COLUMN SET BOARD_COLUMN_LOCATION = :location, BOARD_COLUMN_DEFINITION_ID_FK = :columnDefinitionId WHERE BOARD_COLUMN_ID = :columnId")
	int moveToLocation(@Bind("columnId") int columnId, @Bind("location") String location,
			@Bind("columnDefinitionId") int columnDefinitionId);

	@Query("UPDATE LA_BOARD_COLUMN SET BOARD_COLUMN_ORDER = :order WHERE BOARD_COLUMN_ID = :columnId")
	int updateOrder(@Bind("columnId") int columnId, @Bind("order") int order);

	@Query("UPDATE LA_BOARD_COLUMN SET BOARD_COLUMN_NAME = :newName WHERE BOARD_COLUMN_ID = :columnId AND BOARD_COLUMN_BOARD_ID_FK =  :boardId")
	int renameColumn(@Bind("newName") String newName, @Bind("columnId") int columnId, @Bind("boardId") int boardId);

	@Query("SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE, BOARD_COLUMN_DEFINITION_COLOR "
			+ " FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE, BOARD_COLUMN_DEFINITION_COLOR "
					+ " FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = LAST_INSERT_ID()"),//
			@QueryOverride(db = DB.PGSQL, value = "SELECT BOARD_COLUMN_ID, BOARD_COLUMN_NAME, BOARD_COLUMN_ORDER, BOARD_COLUMN_LOCATION, BOARD_COLUMN_BOARD_ID_FK, BOARD_COLUMN_DEFINITION_ID, BOARD_COLUMN_DEFINITION_VALUE, BOARD_COLUMN_DEFINITION_COLOR "
					+ " FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_board_column','board_column_id')))") })
	BoardColumn findLastCreatedColumn();

	@Query("SELECT * FROM LA_BOARD_COLUMN_INFO WHERE BOARD_COLUMN_ID = :columnId")
	BoardColumnInfo getColumnInfoById(@Bind("columnId") int columnId);

	@Query("UPDATE LA_BOARD_COLUMN SET BOARD_COLUMN_DEFINITION_ID_FK = :definitionId WHERE BOARD_COLUMN_ID = :columnId AND BOARD_COLUMN_BOARD_ID_FK =  :boardId")
	int redefineColumn(@Bind("definitionId") int definitionId, @Bind("columnId") int columnId,
			@Bind("boardId") int boardId);

}
