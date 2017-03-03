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
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.Project;
import io.lavagna.model.ProjectWithEventCounts;

import java.util.Collection;
import java.util.List;

@QueryRepository
public interface ProjectQuery {

	@Query("INSERT INTO LA_PROJECT(PROJECT_NAME, PROJECT_SHORT_NAME, PROJECT_DESCRIPTION) VALUES (:name, :shortName, :description)")
	int createProject(@Bind("name") String name, @Bind("shortName") String shortName,
			@Bind("description") String description);

	@Query("UPDATE LA_PROJECT SET PROJECT_NAME = :name, PROJECT_DESCRIPTION = :description, PROJECT_ARCHIVED = :archived WHERE PROJECT_ID = :projectId")
	int updateProject(@Bind("projectId") int projectId, @Bind("name") String name,
			@Bind("description") String description, @Bind("archived") boolean archived);

	@Query("SELECT * FROM LA_PROJECT WHERE PROJECT_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_PROJECT WHERE PROJECT_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_PROJECT WHERE PROJECT_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_project','project_id')))") })
	Project findLastCreatedProject();

	@Query("SELECT * FROM LA_PROJECT WHERE PROJECT_ID = :projectId")
	Project findById(@Bind("projectId") int projectId);

	@Query("SELECT PROJECT_ID FROM LA_PROJECT WHERE PROJECT_SHORT_NAME = :shortName")
	int findIdByShortName(@Bind("shortName") String shortName);

	@Query("SELECT * FROM LA_PROJECT WHERE PROJECT_SHORT_NAME = :shortName")
	Project findByShortName(@Bind("shortName") String shortName);

	@Query("SELECT * FROM LA_PROJECT ORDER BY PROJECT_SHORT_NAME")
	List<Project> findAll();

	@Query("SELECT DISTINCT PROJECT_ID, PROJECT_NAME, PROJECT_SHORT_NAME, PROJECT_DESCRIPTION, PROJECT_ARCHIVED FROM LA_PROJECT "//
			+ " INNER JOIN LA_PROJECT_USER_ROLE ON LA_PROJECT_USER_ROLE.PROJECT_ID_FK = PROJECT_ID "//
			+ " INNER JOIN LA_PROJECT_ROLE ON LA_PROJECT_ROLE.PROJECT_ID_FK = PROJECT_ID "//
			+ " INNER JOIN LA_PROJECT_ROLE_PERMISSION ON LA_PROJECT_ROLE_PERMISSION.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "//
			+ " WHERE "//
			+ " USER_ID_FK = :userId AND PERMISSION = :permission")
	List<Project> findAllForUser(@Bind("userId") int userId, @Bind("permission") String permission);

	@Query("SELECT BOARD_PROJECT_ID_FK FROM LA_BOARD WHERE BOARD_SHORT_NAME = :shortName")
	List<Integer> findRelatedProjectIdByBoardShortname(@Bind("shortName") String shortName);

	@Query("SELECT BOARD_PROJECT_ID_FK FROM LA_BOARD WHERE BOARD_ID = "//
			+ " (SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = "//
			+ " (SELECT CARD_BOARD_COLUMN_ID_FK FROM LA_CARD WHERE CARD_ID = :cardId))")
	List<Integer> findRelatedProjectIdByCardId(@Bind("cardId") int cardId);

	@Query("SELECT BOARD_PROJECT_ID_FK FROM LA_BOARD WHERE BOARD_ID = (SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = :columnId)")
	List<Integer> findRelatedProjectIdByColumnId(@Bind("columnId") int columnId);

	@Query("SELECT BOARD_PROJECT_ID_FK FROM LA_BOARD WHERE BOARD_ID = "//
			+ "(SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = "//
			+ "(SELECT CARD_BOARD_COLUMN_ID_FK FROM LA_CARD WHERE CARD_ID = "//
			+ "(SELECT CARD_DATA_CARD_ID_FK FROM LA_CARD_DATA WHERE CARD_DATA_ID = :cardDataId )))")
	List<Integer> findRelatedProjectIdByCardDataId(@Bind("cardDataId") int cardDataId);

	@Query("SELECT CARD_LABEL_PROJECT_ID_FK FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = :labelId")
	List<Integer> findRelatedProjectIdByLabelId(@Bind("labelId") int labelId);

	@Query("SELECT BOARD_COLUMN_DEFINITION_PROJECT_ID_FK FROM LA_BOARD_COLUMN_DEFINITION WHERE BOARD_COLUMN_DEFINITION_ID = :id")
	List<Integer> findRelatedProjectIdByColumnDefinitionId(@Bind("id") int id);

	@Query("SELECT CARD_LABEL_PROJECT_ID_FK FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = (SELECT CARD_LABEL_ID_FK FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_ID = :labelValueId)")
	List<Integer> findRelatedProjectIdByLabelValueId(@Bind("labelValueId") int labelValueId);

	@Query("SELECT CARD_LABEL_PROJECT_ID_FK FROM LA_CARD_LABEL WHERE CARD_LABEL_ID = (SELECT CARD_LABEL_ID_FK FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE_ID = :labelListValueIdPath)")
	List<Integer> findRelatedProjectIdByLabelListValudIdPath(@Bind("labelListValueIdPath") int labelListValueIdPath);

	@Query("SELECT BOARD_PROJECT_ID_FK FROM LA_BOARD WHERE BOARD_ID = "//
			+ "(SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = "//
			+ "(SELECT CARD_BOARD_COLUMN_ID_FK FROM LA_CARD WHERE CARD_ID = "//
			+ "(SELECT EVENT_CARD_ID_FK FROM LA_EVENT WHERE EVENT_ID = :eventId )))")
	List<Integer> findRelatedProjectIdByEventId(@Bind("eventId") int eventId);

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_BOARD_COLUMN_DEFINITION (BOARD_COLUMN_DEFINITION_PROJECT_ID_FK, BOARD_COLUMN_DEFINITION_VALUE, BOARD_COLUMN_DEFINITION_COLOR) VALUES (:projectId, :value, :color)")
	String createColumnDefinition();

	@Query("UPDATE LA_BOARD_COLUMN_DEFINITION SET BOARD_COLUMN_DEFINITION_COLOR = :color WHERE BOARD_COLUMN_DEFINITION_PROJECT_ID_FK = :projectId AND BOARD_COLUMN_DEFINITION_ID = :definitionId")
	int updateColumnDefinition(@Bind("color") int color, @Bind("projectId") int projectId,
			@Bind("definitionId") int definitionId);

	@Query("SELECT * FROM LA_BOARD_COLUMN_DEFINITION WHERE BOARD_COLUMN_DEFINITION_PROJECT_ID_FK = :projectId")
	List<BoardColumnDefinition> findColumnDefinitionsByProjectId(@Bind("projectId") int projectId);

	@Query("SELECT COUNT(PROJECT_SHORT_NAME) FROM LA_PROJECT WHERE PROJECT_SHORT_NAME = :shortName")
	Integer existsWithShortName(@Bind("shortName") String shortName);

	@Query("SELECT LA_PROJECT.PROJECT_ID, LA_PROJECT.PROJECT_NAME, LA_PROJECT.PROJECT_SHORT_NAME, PROJECT_DESCRIPTION, PROJECT_ARCHIVED, "
			+ "COUNT( EVENT_ID ) AS EVENTS FROM LA_PROJECT "
			+ "INNER JOIN LA_CARD_FULL ON LA_CARD_FULL.PROJECT_ID = LA_PROJECT.PROJECT_ID "
			+ "INNER JOIN LA_EVENT ON EVENT_CARD_ID_FK = LA_CARD_FULL.CARD_ID AND EVENT_USER_ID_FK = :userId "
			+ "GROUP BY LA_PROJECT.PROJECT_ID, PROJECT_NAME, LA_PROJECT.PROJECT_SHORT_NAME, PROJECT_DESCRIPTION "
			+ "ORDER BY EVENTS DESC")
	List<ProjectWithEventCounts> findProjectsByUserActivity(@Bind("userId") int userId);

	@Query("SELECT LA_PROJECT.PROJECT_ID, LA_PROJECT.PROJECT_NAME, LA_PROJECT.PROJECT_SHORT_NAME, PROJECT_DESCRIPTION, PROJECT_ARCHIVED, "
			+ "COUNT( EVENT_ID ) AS EVENTS FROM LA_PROJECT "
			+ "INNER JOIN LA_CARD_FULL ON LA_CARD_FULL.PROJECT_ID = LA_PROJECT.PROJECT_ID "
			+ "INNER JOIN LA_EVENT ON EVENT_CARD_ID_FK = LA_CARD_FULL.CARD_ID AND EVENT_USER_ID_FK = :userId "
			+ "WHERE LA_CARD_FULL.PROJECT_ID IN (:projects) "
			+ "GROUP BY LA_PROJECT.PROJECT_ID, PROJECT_NAME, LA_PROJECT.PROJECT_SHORT_NAME, PROJECT_DESCRIPTION "
			+ "ORDER BY EVENTS DESC")
	List<ProjectWithEventCounts> findProjectsByUserActivityInProjects(@Bind("userId") int userId,
			@Bind("projects") Collection<Integer> projectIds);

}
