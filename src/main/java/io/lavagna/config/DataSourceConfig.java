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
package io.lavagna.config;

import io.lavagna.common.DatabaseMigrationDoneEvent;
import io.lavagna.common.QueryFactory;
import io.lavagna.query.ValidationQuery;
import io.lavagna.service.DatabaseMigrator;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.flywaydb.core.api.MigrationVersion;
import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class DataSourceConfig {
	
	public static final MigrationVersion LATEST_STABLE_VERSION = MigrationVersion.fromVersion("2");

	@Bean(destroyMethod = "close")
	public DataSource getDataSource(Environment env) throws URISyntaxException {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		dataSource.setDriverClassName(env.getRequiredProperty("datasource.driver"));

		if (env.containsProperty("datasource.url") && //
				env.containsProperty("datasource.username") && //
				env.containsProperty("datasource.password")) {
			urlAndCredentials(dataSource, env);
		} else {
			urlWithCredentials(dataSource, env);
		}

		dataSource.setValidationQuery(QueryFactory.from(ValidationQuery.class,
				env.getRequiredProperty("datasource.dialect")).validation());
		dataSource.setTestOnBorrow(true);
		dataSource.setTestOnConnect(true);
		dataSource.setTestWhileIdle(true);
		
		
		if (System.getProperty("startDBManager") != null) {
			DatabaseManagerSwing.main(new String[] { "--url", "jdbc:hsqldb:mem:lavagna", "--noexit" });
		}
		
		return dataSource;
	}


	/**
	 * for supporting heroku style url:
	 * 
	 * <pre>
	 * [database type]://[username]:[password]@[host]:[port]/[database name]
	 * </pre>
	 * 
	 * @param dataSource
	 * @param env
	 * @throws URISyntaxException
	 */
	private static void urlWithCredentials(org.apache.tomcat.jdbc.pool.DataSource dataSource, Environment env)
			throws URISyntaxException {
		URI dbUri = new URI(env.getRequiredProperty("datasource.url"));
		dataSource.setUsername(dbUri.getUserInfo().split(":")[0]);
		dataSource.setPassword(dbUri.getUserInfo().split(":")[1]);
		dataSource.setUrl(String.format("%s://%s:%s%s", scheme(dbUri), dbUri.getHost(), dbUri.getPort(),
				dbUri.getPath()));
	}

	private static String scheme(URI uri) {
		return "postgres".equals(uri.getScheme()) ? "jdbc:postgresql" : uri.getScheme();
	}

	private static void urlAndCredentials(org.apache.tomcat.jdbc.pool.DataSource dataSource, Environment env) {
		dataSource.setUrl(env.getRequiredProperty("datasource.url"));
		dataSource.setUsername(env.getRequiredProperty("datasource.username"));
		dataSource.setPassword(env.getRequiredProperty("datasource.password"));
	}

	@Bean
	public DatabaseMigrator migrator(Environment env, DataSource dataSource, ApplicationEventPublisher publisher) {
		
		boolean isDev = ArrayUtils.contains(env.getActiveProfiles(),"dev");
		
		DatabaseMigrator migrator = new DatabaseMigrator(env, dataSource, isDev ? MigrationVersion.LATEST : LATEST_STABLE_VERSION);
		publisher.publishEvent(new DatabaseMigrationDoneEvent(this));
		return migrator;
	}
}
