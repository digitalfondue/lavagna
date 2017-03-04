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

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.service.config.TestServiceConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class ProjectServiceTest {

	private static final String TEST_BOARD = "TEST-BRD";

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CardService cardService;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private CardLabelRepository cardLabelRepository;

	@Autowired
	private NamedParameterJdbcTemplate jdbc;

	@Before
	public void prepare() {
		jdbc.update("DELETE FROM LA_PROJECT_ROLE_PERMISSION", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_PROJECT_ROLE", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_CARD_LABEL_VALUE", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_CARD_LABEL_LIST_VALUE", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_CARD_LABEL", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_BOARD_COLUMN_DEFINITION", new EmptySqlParameterSource());
		jdbc.update("DELETE FROM LA_PROJECT", new EmptySqlParameterSource());
	}

	@Test
	public void createTest() {
		Assert.assertEquals("test", projectService.create("test", "TEST", "desc").getName());
	}

	@Test
	public void updateProjectTest() {
		Project p = projectService.create("test", "TEST", "desc");

		Project newProject = projectService.updateProject(p.getId(), "new name", p.getDescription(), p.getArchived());

		Assert.assertEquals(p.getId(), newProject.getId());
		Assert.assertEquals(p.getDescription(), newProject.getDescription());
		Assert.assertNotEquals(p.getName(), newProject.getName());
		Assert.assertEquals("new name", newProject.getName());
	}

	@Test(expected = DuplicateKeyException.class)
	public void uniquenessTest() {
		Assert.assertEquals("test", projectService.create("test", "TEST", "desc").getName());
		// Throw DuplicateKeyException on double create
		projectService.create("test", "TEST", "desc");
	}

	@Test
	public void findAllTest() {
		Assert.assertEquals(0, projectService.findAll().size());
		Assert.assertEquals("test1", projectService.create("test1", "TEST1", "desc").getName());
		Assert.assertEquals("test2", projectService.create("test2", "TEST2", "desc").getName());
		Assert.assertEquals(2, projectService.findAll().size());
	}

	@Test
	public void findByIdTest() {
		Assert.assertEquals("test1", projectService.create("test1", "TEST1", "desc").getName());

		Project found = projectService.findById(projectService.findAll().get(0).getId());
		Assert.assertEquals("test1", found.getName());
		Assert.assertEquals("desc", found.getDescription());
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void findByIdNotFoundTest() {
		projectService.findById(42);
	}

	@Test
	public void testFindAllForUser() {

		Helper.createUser(userRepository, "test", "user-test");
		User user = userRepository.findUserByName("test", "user-test");

		projectService.create("test", "TEST", "desc");
		Project project = projectService.findByShortName("TEST");

		// empty because the user don't have the correct role READ
		Assert.assertTrue(projectService.findAllForUserWithPermissionInProject(user).isEmpty());

		// empty, because the role is system wide and not attached to the
		// specific project
		Role role = new Role("HAS_READ");
		permissionService.createRole(role);
		permissionService.updatePermissionsToRole(role, EnumSet.of(Permission.READ));
		permissionService.assignRoleToUsers(role, Collections.singleton(user.getId()));

		Assert.assertEquals(EnumSet.of(Permission.READ), permissionService.findBasePermissionByUserId(user.getId()));
		Assert.assertEquals(0, projectService.findAllForUserWithPermissionInProject(user).size());
		//

		Role projectRole = new Role("PROJECT_HAS_READ");
		permissionService.createRoleInProjectId(projectRole, project.getId());
		permissionService.updatePermissionsToRoleInProjectId(projectRole, EnumSet.of(Permission.READ), project.getId());
		permissionService.assignRoleToUsersInProjectId(projectRole, Collections.singleton(user.getId()),
				project.getId());

		Assert.assertEquals(1, projectService.findAllForUserWithPermissionInProject(user).size());
		Assert.assertEquals("test", projectService.findAllForUserWithPermissionInProject(user).get(0).getName());
	}

	@Test
	public void updateColumnDefinition() {
		Project project = projectService.create("test1", "TEST1", "desc");
		Map<ColumnDefinition, BoardColumnDefinition> definitions = projectService
				.findMappedColumnDefinitionsByProjectId(project.getId());
		Assert.assertEquals(ColumnDefinition.OPEN.getDefaultColor(), definitions.get(ColumnDefinition.OPEN).getColor());
		Assert.assertEquals(ColumnDefinition.CLOSED.getDefaultColor(), definitions.get(ColumnDefinition.CLOSED)
				.getColor());
		Assert.assertEquals(ColumnDefinition.BACKLOG.getDefaultColor(), definitions.get(ColumnDefinition.BACKLOG)
				.getColor());
		Assert.assertEquals(ColumnDefinition.DEFERRED.getDefaultColor(), definitions.get(ColumnDefinition.DEFERRED)
				.getColor());
		projectService.updateColumnDefinition(project.getId(), definitions.get(ColumnDefinition.OPEN).getId(), 1234);
		definitions = projectService.findMappedColumnDefinitionsByProjectId(project.getId());
		Assert.assertEquals(1234, definitions.get(ColumnDefinition.OPEN).getColor());
		Assert.assertEquals(ColumnDefinition.CLOSED.getDefaultColor(), definitions.get(ColumnDefinition.CLOSED)
				.getColor());
		Assert.assertEquals(ColumnDefinition.BACKLOG.getDefaultColor(), definitions.get(ColumnDefinition.BACKLOG)
				.getColor());
		Assert.assertEquals(ColumnDefinition.DEFERRED.getDefaultColor(), definitions.get(ColumnDefinition.DEFERRED)
				.getColor());
	}

	private Pair<Project, User> prepareOneActivity() {


		Helper.createUser(userRepository, "test", "test-user");
		User user = userRepository.findUserByName("test", "test-user");

		projectService.create("test", "TEST", "desc");
		Project project = projectService.findByShortName("TEST");
		boardRepository.createNewBoard("TEST", TEST_BOARD, "TEST", project.getId());
		Board board = boardRepository.findBoardByShortName(TEST_BOARD);

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		BoardColumn bc = boardColumnRepository.addColumnToBoard("col1", definitions.get(0).getId(),
				BoardColumn.BoardColumnLocation.BOARD, board.getId());
		cardService.createCard("card1", bc.getId(), new Date(), user);

		return Pair.of(project, user);
	}


	@Test
	public void testFindBoardsByUserActivityWithGlobalPermissions() {
		Pair<Project, User> prep = prepareOneActivity();

		List<ProjectWithEventCounts> projects = projectService.findProjectsActivityByUser(prep.getRight().getId());
		Assert.assertEquals(1, projects.size());
	}

	@Test
	public void testFindBoardsByUserActivityWithPermissionsOnTest() {
		Pair<Project, User> prep = prepareOneActivity();

		List<ProjectWithEventCounts> projects = projectService.findProjectsActivityByUserInProjects(prep.getRight().getId(),
				Arrays.asList(prep.getLeft().getId()));
		Assert.assertEquals(1, projects.size());
	}

	@Test
	public void testFindBoardsByUserActivityWithNoPermissionsOnTest() {
		Helper.createUser(userRepository, "test", "test-user");
		User user = userRepository.findUserByName("test", "test-user");

		List<ProjectWithEventCounts> projects = projectService.findProjectsActivityByUserInProjects(user.getId(),
				Collections.<Integer>emptyList());
		Assert.assertEquals(0, projects.size());
	}

	@Test
	public void testExistsWithShortName() {
	    Assert.assertFalse(projectService.existsWithShortName(null));
	    Assert.assertFalse(projectService.existsWithShortName(""));
	    Assert.assertFalse(projectService.existsWithShortName("PROJECT"));

	    projectService.create("PROJECT", "PROJECT", "desc");
	    Assert.assertTrue(projectService.existsWithShortName("PROJECT"));
	}

	@Test
	public void testProjectMetadata() {
	    projectService.create("test", "TEST", "desc");
	    ProjectMetadata metadata = projectService.getMetadata("TEST");

	    Assert.assertEquals(4, metadata.getColumnsDefinition().size());

	    //4 system labels
	    Assert.assertEquals(4, metadata.getLabels().size());

	    CardLabel milestoneLabel = null;
        for (CardLabel cl : metadata.getLabels().values()) {
            if (SYSTEM_LABEL_MILESTONE.equals(cl.getName()) && cl.getDomain() == LabelDomain.SYSTEM) {
                milestoneLabel = cl;
            }
        }
	    Assert.assertTrue(metadata.getLabelListValues().isEmpty());

	    cardLabelRepository.addLabelListValue(milestoneLabel.getId(), "1.0.0");

	    ProjectMetadata metadata2 = projectService.getMetadata("TEST");

	    Assert.assertEquals(1, metadata2.getLabelListValues().size());


	    LabelListValueWithMetadata llval = metadata2.getLabelListValues().values().iterator().next();
	    Assert.assertEquals("1.0.0", llval.getValue());
	    Assert.assertTrue(llval.getMetadata().isEmpty());


	    cardLabelRepository.createLabelListMetadata(llval.getId(), "muh", "perfs");


	    ProjectMetadata metadata3 = projectService.getMetadata("TEST");

	    LabelListValueWithMetadata llval3 = metadata3.getLabelListValues().get(llval.getId());
        Assert.assertFalse(llval3.getMetadata().isEmpty());
        Assert.assertEquals("perfs", llval3.getMetadata().get("muh"));

	}
}
