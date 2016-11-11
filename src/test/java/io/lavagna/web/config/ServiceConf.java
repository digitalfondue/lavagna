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
package io.lavagna.web.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.lavagna.common.Json;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.model.UserMetadata;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.BulkOperationService;
import io.lavagna.service.CalendarService;
import io.lavagna.service.CardDataRepository;
import io.lavagna.service.CardDataService;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.CardRepository;
import io.lavagna.service.CardService;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.EventRepository;
import io.lavagna.service.EventService;
import io.lavagna.service.ExcelExportService;
import io.lavagna.service.ExportImportService;
import io.lavagna.service.ImportService;
import io.lavagna.service.LabelService;
import io.lavagna.service.Ldap;
import io.lavagna.service.LdapConnection;
import io.lavagna.service.PermissionService;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchService;
import io.lavagna.service.SetupService;
import io.lavagna.service.StatisticsService;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.security.login.OAuthLogin;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public class ServiceConf {

	@Bean
	public UserRepository getUserRepository() {
		return mock(UserRepository.class);
	}

	@Bean
	public EventService getEventService() {
		return mock(EventService.class);
	}

	@Bean
	public StatisticsService getBoardStatisticsService() {
		return mock(StatisticsService.class);
	}

	@Bean
	public ExportImportService getExportImportService2() {
		return mock(ExportImportService.class);
	}

	@Bean
	public UserService getUserService() {
		UserService u = mock(UserService.class);
		User user = new User(0, "test", "test-user", null, null, true, true, new Date(), false, Json.GSON.toJson(new UserMetadata(false, false)));
		UserWithPermission uwp = new UserWithPermission(user, EnumSet.allOf(Permission.class),
				Collections.<String, Set<Permission>> emptyMap(), Collections.<Integer, Set<Permission>> emptyMap());
		when(u.findUserWithPermission(0)).thenReturn(uwp);
		return u;
	}

	@Bean
	public PermissionService getPermissionService() {
		return mock(PermissionService.class);
	}

	@Bean
	public Ldap ldap() {
		return mock(Ldap.class);
	}

	@Bean
	public LdapConnection ldapConnection() {
		return mock(LdapConnection.class);
	}

	@Bean
	public BoardColumnRepository getBoardColumnRepository() {
		return mock(BoardColumnRepository.class);
	}

	@Bean
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return mock(NamedParameterJdbcTemplate.class);
	}

	@Bean
	public BoardRepository getBoardRepository() {
		return mock(BoardRepository.class);
	}

	@Bean
	public EventRepository getEventRepository() {
		return mock(EventRepository.class);
	}

	@Bean
	public CardService getCardService() {
		return mock(CardService.class);
	}

	@Bean
	public CardRepository getCardRepository() {
		return mock(CardRepository.class);
	}

	@Bean
	public CardDataRepository getCardDataRepository() {
		return mock(CardDataRepository.class);
	}

	@Bean
	public SetupService getSetupService() {
		return mock(SetupService.class);
	}

	@Bean
	public CardDataService getCardDataService() {
		return mock(CardDataService.class);
	}

	@Bean
	public ImportService getImportService() {
		return mock(ImportService.class);
	}

	@Bean
	public ProjectService getProjectService() {
		return mock(ProjectService.class);
	}

	@Bean
	public CardLabelRepository getCardLabelRepository() {
		return mock(CardLabelRepository.class);
	}

	@Bean
	public EventEmitter getEventEmitter() {
		return mock(EventEmitter.class);
	}

	@Bean
	public SimpMessageSendingOperations getSimpMessageSendingOperations() {
		return mock(SimpMessageSendingOperations.class);
	}

	@Bean
	public ConfigurationRepository getConfigurationRepository() {
		return mock(ConfigurationRepository.class);
	}

	@Bean
	public LabelService getLabelService() {
		return mock(LabelService.class);
	}

	@Bean
	public SearchService getSearchService() {
		return mock(SearchService.class);
	}

	@Bean
	public BulkOperationService getBulkOperationService() {
		return mock(BulkOperationService.class);
	}

	@Bean
	public CalendarService getCalendarService() {
		return mock(CalendarService.class);
	}

	@Bean
	public OAuthLogin getOAuthLogin() {
	    return mock(OAuthLogin.class);
	}

	@Bean
	public ExcelExportService getMilestoneExportService() {
	    return mock(ExcelExportService.class);
	}
}
