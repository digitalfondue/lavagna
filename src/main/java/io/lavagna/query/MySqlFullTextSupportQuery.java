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

import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;

@QueryRepository
public interface MySqlFullTextSupportQuery {

	@Query("INSERT INTO LA_CARD_FTS_SUPPORT SELECT CARD_ID,CARD_NAME,CARD_LAST_UPDATED FROM LA_CARD left join LA_CARD_FTS_SUPPORT ON CARD_ID = CARD_FTS_SUPPORT_CARD_ID_FK WHERE CARD_FTS_SUPPORT_CARD_ID_FK IS NULL LIMIT 1000")
	int syncNewCards();

	@Query("REPLACE INTO LA_CARD_FTS_SUPPORT SELECT CARD_ID,CARD_NAME,CARD_LAST_UPDATED FROM LA_CARD left join LA_CARD_FTS_SUPPORT ON CARD_ID = CARD_FTS_SUPPORT_CARD_ID_FK WHERE CARD_LAST_UPDATED <> CARD_FTS_SUPPORT_LAST_UPDATED LIMIT 1000")
	int syncUpdatedCards();

	@Query("INSERT INTO LA_CARD_DATA_FTS_SUPPORT SELECT CARD_DATA_ID,CARD_DATA_CONTENT,CARD_DATA_LAST_UPDATED FROM LA_CARD_DATA left join LA_CARD_DATA_FTS_SUPPORT ON CARD_DATA_ID = CARD_DATA_FTS_SUPPORT_CARD_DATA_ID_FK WHERE CARD_DATA_FTS_SUPPORT_CARD_DATA_ID_FK IS NULL LIMIT 1000")
	int syncNewCardData();

	@Query("REPLACE INTO LA_CARD_DATA_FTS_SUPPORT SELECT CARD_DATA_ID,CARD_DATA_CONTENT,CARD_DATA_LAST_UPDATED FROM LA_CARD_DATA left join LA_CARD_DATA_FTS_SUPPORT ON CARD_DATA_ID = CARD_DATA_FTS_SUPPORT_CARD_DATA_ID_FK WHERE CARD_DATA_LAST_UPDATED <> CARD_DATA_FTS_SUPPORT_LAST_UPDATED LIMIT 1000")
	int syncUpdatedCardData();
}
