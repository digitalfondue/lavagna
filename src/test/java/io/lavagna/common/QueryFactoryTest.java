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
package io.lavagna.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class QueryFactoryTest {

	@Mock
	NamedParameterJdbcTemplate jdbc;

	public interface QueryTest {
		@Query(type = QueryType.TEMPLATE, value = "SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = :columnId")
		String findById();

		@Query(type = QueryType.TEMPLATE, value = "SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = :columnId")
		@QueriesOverride(@QueryOverride(db = "MYSQL", value = "SELECT * FROM LA_BOARD_COLUMN_FULL_MYSQL WHERE BOARD_COLUMN_ID = :columnId"))
		String overrideQuery();
	}

	@Test
	public void testSimpleAnnotationQuery() {
		QueryFactory qf = new QueryFactory("HSQLDB", jdbc);

		QueryTest qt = qf.from(QueryTest.class);

		Assert.assertEquals("SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = :columnId", qt.findById());
	}

	@Test
	public void testOverrideAnnotation() {
		QueryFactory qf = new QueryFactory("HSQLDB", jdbc);
		QueryTest qt = qf.from(QueryTest.class);
		Assert.assertEquals("SELECT * FROM LA_BOARD_COLUMN_FULL WHERE BOARD_COLUMN_ID = :columnId", qt.overrideQuery());

		QueryFactory qfMysql = new QueryFactory("MYSQL", jdbc);
		QueryTest qtMysql = qfMysql.from(QueryTest.class);
		Assert.assertEquals("SELECT * FROM LA_BOARD_COLUMN_FULL_MYSQL WHERE BOARD_COLUMN_ID = :columnId",
				qtMysql.overrideQuery());
	}

}
