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

import io.lavagna.common.QueryFactory;
import io.lavagna.common.QueryRepositoryScanner;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ErrorHandler;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Datasource configuration.
 */
@EnableScheduling
@EnableWebSocketMessageBroker
@EnableTransactionManagement
@ComponentScan(basePackages = { "io.lavagna.service", "io.lavagna.config.dbmanager" })
public class PersistenceAndServiceConfig extends AbstractWebSocketMessageBrokerConfigurer implements
		SchedulingConfigurer {

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
	public NamedParameterJdbcTemplate simpleJdbcTemplate(Environment env, DataSource dataSource) {
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
	public QueryFactory queryFactory(Environment env, NamedParameterJdbcTemplate jdbc) {
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
		scheduler.setErrorHandler(new ErrorHandler() {
			@Override
			public void handleError(Throwable t) {
				LogManager.getLogger().error("error while handling job", t);
			}
		});
		scheduler.initialize();
		return scheduler;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/api/socket").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/event");
	}
}
