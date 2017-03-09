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

import io.lavagna.common.LavagnaEnvironment;
import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.service.PermissionService.ProjectRoleAndPermissionFullHolder;
import io.lavagna.service.SearchFilter.FilterType;
import io.lavagna.service.SearchFilter.SearchFilterValue;
import io.lavagna.service.SearchFilter.ValueType;
import io.lavagna.service.config.TestServiceConfig;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Collections.singletonList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class SearchServiceTest {

	@Autowired
	private LavagnaEnvironment env;
	@Autowired
	private SearchService searchService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardColumnRepository boardColumnRepository;
	@Autowired
	private CardService cardService;
	@Autowired
	private MySqlFullTextSupportService mySqlFullTextSupportService;

	private User user;
	private User userWithNoAccess;

	private UserWithPermission userWithPermissions;
	private UserWithPermission userWithNoAccessPermission;

	private final SearchFilter createdByMe = new SearchFilter(FilterType.CREATED_BY, null, new SearchFilterValue(
			ValueType.CURRENT_USER, "me"));

	private Project project;
	private Board board;
	private BoardColumn column;
	private BoardColumn closedColumn;

	@Before
	public void prepare() {
		userRepository.createUser("test", "test", null,null, null, true);
		userRepository.createUser("test", "test-no-access", null,null, null, true);
		user = userRepository.findUserByName("test", "test");
		userWithNoAccess = userRepository.findUserByName("test", "test-no-access");
		Role r = new Role("TEST");
		permissionService.createRole(r);
		permissionService.updatePermissionsToRole(r, EnumSet.of(Permission.READ));
		permissionService.assignRolesToUsers(Collections.singletonMap(r, Collections.singleton(user.getId())));

		userWithPermissions = new UserWithPermission(user, permissionService.findBasePermissionByUserId(user.getId()),
				Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

		userWithNoAccessPermission = new UserWithPermission(userWithNoAccess,
				permissionService.findBasePermissionByUserId(userWithNoAccess.getId()),
				Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

		project = projectService.create("test search", "TEST-SRC", "desc");
		board = boardRepository.createNewBoard("TEST-SEARCH", "TEST-SRC", "desc", project.getId());

		List<BoardColumnDefinition> columnDefinitions = projectService
				.findColumnDefinitionsByProjectId(project.getId());
		for (BoardColumnDefinition bcd : columnDefinitions) {
			if (bcd.getValue() == ColumnDefinition.OPEN) {
				column = boardColumnRepository.addColumnToBoard("test", bcd.getId(), BoardColumnLocation.BOARD,
						board.getId());
			} else if (bcd.getValue() == ColumnDefinition.CLOSED) {
				closedColumn = boardColumnRepository.addColumnToBoard("test", bcd.getId(), BoardColumnLocation.BOARD,
						board.getId());
			}
		}
	}

	@Test
	public void testNoReadPermission() {
		SearchResults find = searchService.find(singletonList(createdByMe), null, null, userWithNoAccessPermission, 0);
		Assert.assertEquals(0, find.getCount());

		SearchResults find2 = searchService.find(singletonList(createdByMe), project.getId(), null,
				userWithNoAccessPermission, 0);
		Assert.assertEquals(0, find2.getCount());
	}

	@Test
	public void testEmpty() {
		SearchResults find = searchService.find(singletonList(createdByMe), null, null, userWithPermissions, 0);
		Assert.assertEquals(0, find.getCount());
	}

	@Test
	public void testFindCreatedByMe() {
		cardService.createCard("test", column.getId(), new Date(), user);
		SearchResults find = searchService.find(singletonList(createdByMe), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testFindByStatus() {
		SearchFilter status = new SearchFilter(FilterType.STATUS, null,
				new SearchFilterValue(ValueType.STRING, "OPEN"));
		cardService.createCard("test", column.getId(), new Date(), user);
		SearchResults find = searchService.find(singletonList(status), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testCreatedToday() {
		SearchFilter today = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "today"));
		SearchFilter thisWeek = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "this week"));
		SearchFilter thisMonth = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "this month"));
		SearchFilter lastWeek = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "last week"));
		SearchFilter lastMonth = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "last month"));
		Date now = new Date();
		cardService.createCard("test", column.getId(), now, user);
		SearchResults find = searchService.find(singletonList(today), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());

		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		SearchFilter todayString = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(ValueType.STRING,
				formatter.format(now)));

		Assert.assertEquals(1,
				searchService.find(singletonList(todayString), null, null, userWithPermissions, 0).getCount());
		Assert.assertEquals(1,
				searchService.find(singletonList(todayString), null, null, userWithPermissions, 0).getCount());
		Assert.assertEquals(1,
				searchService.find(singletonList(thisWeek), null, null, userWithPermissions, 0).getCount());
		Assert.assertEquals(1,
				searchService.find(singletonList(thisMonth), null, null, userWithPermissions, 0).getCount());
		Assert.assertEquals(1,
				searchService.find(singletonList(lastWeek), null, null, userWithPermissions, 0).getCount());
		Assert.assertEquals(1,
				searchService.find(singletonList(lastMonth), null, null, userWithPermissions, 0).getCount());

	}

	@Test
	public void testSearchBoard() {
		Date now = new Date();
		cardService.createCard("test", column.getId(), now, user);
		Board board2 = boardRepository.createNewBoard("TEST-SEARCH2", "TEST-SR2", "desc", project.getId());

		SearchFilter thisWeek = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(
				ValueType.DATE_IDENTIFIER, "this week"));

		Assert.assertEquals(0,
				searchService.find(singletonList(thisWeek), null, board.getId(), userWithPermissions, 0).getCount());

		Assert.assertEquals(0,
				searchService.find(singletonList(thisWeek), -5, board.getId(), userWithPermissions, 0).getCount());

		Assert.assertEquals(1,
				searchService.find(singletonList(thisWeek), project.getId(), board.getId(), userWithPermissions, 0)
						.getCount());

		Assert.assertEquals(0,
				searchService.find(singletonList(thisWeek), project.getId(), board2.getId(), userWithPermissions, 0)
						.getCount());
	}

	@Test
	public void testUnassigned() {
		SearchFilter unassigned = new SearchFilter(FilterType.ASSIGNED, null, new SearchFilterValue(
				ValueType.UNASSIGNED, "unassigned"));
		cardService.createCard("test", column.getId(), new Date(), user);
		SearchResults find = searchService.find(singletonList(unassigned), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testCreatedThisYear() throws ParseException {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		SearchFilter yearFilter = new SearchFilter(FilterType.CREATED, null, new SearchFilterValue(ValueType.STRING,
				"01.01." + year + "..31.12." + year));
		cardService.createCard("test", column.getId(), new Date(), user);
		cardService.createCard("test", column.getId(), DateUtils.parseDate("01.01." + year, "dd.MM.yyyy"), user);
		cardService.createCard("test", column.getId(), DateUtils.parseDate("31.12." + year, "dd.MM.yyyy"), user);
		SearchResults find = searchService.find(singletonList(yearFilter), null, null, userWithPermissions, 0);
		Assert.assertEquals(3, find.getCount());
	}

	@Test
	public void testFTS() {
		SearchFilter fts = new SearchFilter(FilterType.FREETEXT, null, new SearchFilterValue(ValueType.STRING, "test"));

		syncMYSQLFTS();

		SearchResults find = searchService.find(singletonList(fts), null, null, userWithPermissions, 0);
		Assert.assertEquals(0, find.getCount());

		cardService.createCard("test", column.getId(), new Date(), user);

		syncMYSQLFTS();

		SearchResults find2 = searchService.find(singletonList(fts), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find2.getCount());
	}

	private void syncMYSQLFTS() {
		if ("MYSQL".equals(env.getProperty("datasource.dialect"))) {
			mySqlFullTextSupportService.syncNewCards();
			mySqlFullTextSupportService.syncNewCardData();
			mySqlFullTextSupportService.syncUpdatedCards();
			mySqlFullTextSupportService.syncUpdatedCardData();
		}
	}

	@Test
	public void testInProject() {
		cardService.createCard("test", column.getId(), new Date(), user);
		SearchResults find = searchService
				.find(Arrays.asList(createdByMe), project.getId(), null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testWithoutPagination() {
		for (int i = 0; i < 52; i++) {
			cardService.createCard("test", column.getId(), new Date(), user);
		}

		SearchResults find = searchService.find(Arrays.asList(createdByMe), project.getId(), null, userWithPermissions);
		Assert.assertEquals(52, find.getFound().size());
		Assert.assertEquals(52, find.getCount());
		Assert.assertEquals(0, find.getCurrentPage());
		Assert.assertEquals(1, find.getTotalPages());
	}

	@Test
	public void testPagination() {
		for (int i = 0; i < 52; i++) {
			cardService.createCard("test", column.getId(), new Date(), user);
		}

		SearchResults find = searchService
				.find(Arrays.asList(createdByMe), project.getId(), null, userWithPermissions, 0);
		Assert.assertEquals(51, find.getFound().size());
		Assert.assertEquals(52, find.getCount());
		Assert.assertEquals(0, find.getCurrentPage());
		Assert.assertEquals(2, find.getTotalPages());

		SearchResults find2 = searchService
				.find(Arrays.asList(createdByMe), project.getId(), null, userWithPermissions, 1);

		Assert.assertEquals(2, find2.getFound().size());
		Assert.assertEquals(52, find2.getCount());
		Assert.assertEquals(1, find2.getCurrentPage());
		Assert.assertEquals(2, find2.getTotalPages());
	}

	@Test
	public void testUserWithReadPermissionInProject() {
		Role r = new Role("READ");
		permissionService.createRoleInProjectId(r, project.getId());
		permissionService.assignRoleToUsersInProjectId(r, Collections.singleton(userWithNoAccess.getId()),
				project.getId());
		permissionService.updatePermissionsToRoleInProjectId(r, EnumSet.of(Permission.READ), project.getId());

		ProjectRoleAndPermissionFullHolder permissionsHolder = permissionService
				.findPermissionsGroupedByProjectForUserId(userWithNoAccess.getId());
		UserWithPermission uwp = new UserWithPermission(userWithNoAccess, EnumSet.noneOf(Permission.class),
				permissionsHolder.getPermissionsByProject(), permissionsHolder.getPermissionsByProjectId());

		cardService.createCard("test", column.getId(), new Date(), uwp);

		SearchResults find = searchService.find(Arrays.asList(createdByMe), null, null, uwp, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testFindByStatusAndCreatedByMe() {
		SearchFilter status = new SearchFilter(FilterType.STATUS, null,
				new SearchFilterValue(ValueType.STRING, "OPEN"));
		cardService.createCard("test", column.getId(), new Date(), user);
		SearchResults find = searchService.find(Arrays.asList(createdByMe, status), null, null, userWithPermissions, 0);
		Assert.assertEquals(1, find.getCount());
	}

	@Test
	public void testFindTaksByColumnDefinition() {

		cardService.createCard("test", column.getId(), new Date(), user);

		Map<ColumnDefinition, Integer> tasks = searchService.findTaksByColumnDefinition(project.getId(), null, false,
				userWithPermissions);

		Assert.assertEquals(1, tasks.get(ColumnDefinition.OPEN).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.CLOSED).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.BACKLOG).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.DEFERRED).intValue());
	}

	@Test
	public void testFindTaksByColumnDefinitionOnManyCards() {

		int cardsToCreate = 100;
		for (int i = 0; i < cardsToCreate; i++) {
			cardService.createCard("test" + i, closedColumn.getId(), new Date(), user);
		}
		Map<ColumnDefinition, Integer> tasks = searchService.findTaksByColumnDefinition(project.getId(), null, false,
				userWithPermissions);

		Assert.assertEquals(0, tasks.get(ColumnDefinition.OPEN).intValue());
		Assert.assertEquals(cardsToCreate, tasks.get(ColumnDefinition.CLOSED).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.BACKLOG).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.DEFERRED).intValue());
	}

	@Test
	public void testFindTaksByColumnDefinitionAfterMove() {

		Card card = cardService.createCard("test", column.getId(), new Date(), user);
		cardService.moveCardToColumn(card.getId(), column.getId(), closedColumn.getId(), user.getId(), new Date());

		Map<ColumnDefinition, Integer> tasks = searchService.findTaksByColumnDefinition(project.getId(), null, false,
				userWithPermissions);

		Assert.assertEquals(0, tasks.get(ColumnDefinition.OPEN).intValue());
		Assert.assertEquals(1, tasks.get(ColumnDefinition.CLOSED).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.BACKLOG).intValue());
		Assert.assertEquals(0, tasks.get(ColumnDefinition.DEFERRED).intValue());
	}
}
