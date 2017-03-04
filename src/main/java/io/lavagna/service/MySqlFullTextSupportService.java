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

import io.lavagna.query.MySqlFullTextSupportQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling the asynchronous copy of text data from INNODB tables to MYSAM ones.
 * <p>
 * As we support older version of MySQL, the fulltext search engine is not present in the INNODB tables.
 */
@Service
@Transactional(propagation = Propagation.NESTED)
public class MySqlFullTextSupportService {

	private static final Logger LOG = LogManager.getLogger();

	private final MySqlFullTextSupportQuery queries;


	public MySqlFullTextSupportService(MySqlFullTextSupportQuery queries) {
		this.queries = queries;
	}

	public void syncNewCards() {
		int rowAffected = queries.syncNewCards();
		LOG.debug("syncNewCards : updated {} row", rowAffected);
	}

	public void syncUpdatedCards() {
		int rowAffected = queries.syncUpdatedCards();
		LOG.debug("syncUpdatedCards : updated {} row", rowAffected);
	}

	public void syncNewCardData() {
		int rowAffected = queries.syncNewCardData();
		LOG.debug("syncNewCardData : updated {} row", rowAffected);
	}

	public void syncUpdatedCardData() {
		int rowAffected = queries.syncUpdatedCardData();
		LOG.debug("syncUpdatedCardData : updated {} row", rowAffected);
	}

}
