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
import io.lavagna.common.LavagnaEnvironment;
import io.lavagna.service.DatabaseMigrator;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.flywaydb.core.api.MigrationVersion;
import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.ConfigurableEnvironment;

public class DataSourceConfig {

	public static final MigrationVersion LATEST_STABLE_VERSION = MigrationVersion.fromVersion("16");

	@Bean
    public LavagnaEnvironment getEnvironment(ConfigurableEnvironment environment) {
	    return new LavagnaEnvironment(environment);
    }

	@Bean(destroyMethod = "close")
	public DataSource getDataSource(LavagnaEnvironment env) throws URISyntaxException {

	    ensureJDBCDrivers(env.getRequiredProperty("datasource.dialect"));

		HikariDataSource dataSource = new HikariDataSource();

		if (env.containsProperty("datasource.url") && //
				env.containsProperty("datasource.username")) {
			urlAndCredentials(dataSource, env);
		} else {
			urlWithCredentials(dataSource, env);
		}

		if (System.getProperty("startDBManager") != null) {
			DatabaseManagerSwing.main(new String[] { "--url", "jdbc:hsqldb:mem:lavagna", "--noexit" });
		}

		return dataSource;
	}

	// tomcat 8 is picky when we include the drivers in the war
    private static void ensureJDBCDrivers(String dialect) {
	    try {
            switch (dialect) {
                case "HSQLDB":
                    DriverManager.registerDriver(new org.hsqldb.jdbcDriver());
                    break;
                case "MYSQL":
                    DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                    break;
                case "PGSQL":
                    DriverManager.registerDriver(new org.postgresql.Driver());
                    break;
                default:
                    throw new IllegalStateException("Unknown dialect " + dialect);
            }
        } catch (SQLException e) {
	        throw new IllegalStateException(e);
        }
    }
    //


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
	private static void urlWithCredentials(HikariDataSource dataSource, LavagnaEnvironment env)
			throws URISyntaxException {
		URI dbUri = new URI(env.getRequiredProperty("datasource.url"));
		dataSource.setUsername(dbUri.getUserInfo().split(":")[0]);
		dataSource.setPassword(dbUri.getUserInfo().split(":")[1]);
		dataSource.setJdbcUrl(String.format("%s://%s:%s%s", scheme(dbUri), dbUri.getHost(), dbUri.getPort(),
				dbUri.getPath()));
	}

	private static String scheme(URI uri) {
		return "postgres".equals(uri.getScheme()) ? "jdbc:postgresql" : uri.getScheme();
	}

	private static void urlAndCredentials(HikariDataSource dataSource, LavagnaEnvironment env) {
		dataSource.setJdbcUrl(env.getRequiredProperty("datasource.url"));
		dataSource.setUsername(env.getRequiredProperty("datasource.username"));
		dataSource.setPassword(env.getProperty("datasource.password") != null ? env.getProperty("datasource.password") : "");
	}

	@Bean
	public DatabaseMigrator migrator(LavagnaEnvironment env, DataSource dataSource, ApplicationEventPublisher publisher) {

		boolean isDev = ArrayUtils.contains(env.getActiveProfiles(),"dev");

		DatabaseMigrator migrator = new DatabaseMigrator(env, dataSource, isDev ? MigrationVersion.LATEST : LATEST_STABLE_VERSION);
		publisher.publishEvent(new DatabaseMigrationDoneEvent(this));
		return migrator;
	}
}
