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
package io.lavagna.service.config;

import io.lavagna.common.LavagnaEnvironment;
import io.lavagna.config.DataSourceConfig;
import io.lavagna.service.DatabaseMigrator;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestServiceConfig {

	@Bean(destroyMethod = "close")
	public DataSource getDataSource(LavagnaEnvironment env) throws URISyntaxException {
		return new DataSourceConfig().getDataSource(env);
	}

	@Bean
	public DatabaseMigrator migrator(LavagnaEnvironment env, DataSource dataSource) {
		return new DatabaseMigrator(env, dataSource, DataSourceConfig.LATEST_STABLE_VERSION);
	}

	@Bean
	public LavagnaEnvironment env() {
		MockEnvironment m = new MockEnvironment();
		String dialect = System.getProperty("datasource.dialect", "HSQLDB");
		for (Entry<String, String> kv : datasourceConf().get(dialect).entrySet()) {
			m.setProperty(kv.getKey(), kv.getValue());
		}
		return new LavagnaEnvironment(m);
	}

	@Bean
    public SimpMessageSendingOperations emitter() {
	    return Mockito.mock(SimpMessageSendingOperations.class);
    }

	private static Map<String, Map<String, String>> datasourceConf() {
		Map<String, Map<String, String>> r = new HashMap<>();
		r.put("HSQLDB", c("HSQLDB", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:lavagna", "sa", ""));
		r.put("MYSQL", c("MYSQL", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/lavagna?useUnicode=true&characterEncoding=utf-8", "root", ""));
		r.put("PGSQL", c("PGSQL", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/lavagna", "postgres", "password"));
		r.put("PGSQL-TRAVIS", c("PGSQL", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/lavagna", "postgres", ""));
		return r;
	}

	private static Map<String, String> c(String dialect, String driver, String url, String username, String password) {
		Map<String, String> c = new HashMap<>();
		c.put("datasource.dialect", dialect);
		c.put("datasource.driver", driver);
		c.put("datasource.url", url);
		c.put("datasource.username", username);
		c.put("datasource.password", password);
		return c;
	}

}
