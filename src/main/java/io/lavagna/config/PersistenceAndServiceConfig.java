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


import ch.digitalfondue.npjt.QueryFactory;
import ch.digitalfondue.npjt.QueryRepositoryScanner;
import io.lavagna.common.LavagnaEnvironment;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Datasource configuration.
 */
@EnableTransactionManagement
@ComponentScan(basePackages = { "io.lavagna.service", "io.lavagna.config.dbmanager" })
public class PersistenceAndServiceConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}

	@Bean
	public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	private static class CustomSQLErrorCodesTranslator extends SQLErrorCodeSQLExceptionTranslator {
		protected DataAccessException customTranslate(String task, String sql, SQLException sqlex) {
			// because mysql don't support check constraints :(
			if (sqlex.getMessage().contains("RAISE_CHECK_ERROR")) {
				return new DataIntegrityViolationException(task, sqlex);
			}
			return null;
		}
	}

	@Bean
	public NamedParameterJdbcTemplate simpleJdbcTemplate(LavagnaEnvironment env, DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		// mysql does not support check constraints
		if ("MYSQL".equals(env.getProperty("datasource.dialect"))) {
			CustomSQLErrorCodesTranslator tr = new CustomSQLErrorCodesTranslator();
			tr.setDataSource(dataSource);
			jdbcTemplate.setExceptionTranslator(tr);
		}
		return new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	@Bean
	public QueryRepositoryScanner queryRepoScanner(QueryFactory queryFactory) {
		return new QueryRepositoryScanner(queryFactory, "io.lavagna.query");
	}

	@Bean
	public QueryFactory queryFactory(LavagnaEnvironment env, NamedParameterJdbcTemplate jdbc) {
		return new QueryFactory(env.getProperty("datasource.dialect"), jdbc);
	}

	@Bean
	public LobHandler lobHander() {
		return new DefaultLobHandler();
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasename("classpath:/io/lavagna/i18n/messages");
		source.setUseCodeAsDefaultMessage(true);
		source.setFallbackToSystemLocale(false);
		return source;
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setErrorHandler(t -> LogManager.getLogger().error("error while handling job", t));
		scheduler.initialize();
		return scheduler;
	}
}
