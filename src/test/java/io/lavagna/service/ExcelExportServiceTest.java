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
import io.lavagna.service.config.TestServiceConfig;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class ExcelExportServiceTest {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CardLabelRepository cardLabelRepository;

    Project project;
    UserWithPermission user;

    @Before
    public void prepare() {
        Helper.createUser(userRepository, "test", "test-user");
        User u = userRepository.findUserByName("test", "test-user");
        Role r = new Role("TEST");
        permissionService.createRole(r);
        permissionService.updatePermissionsToRole(r, EnumSet.of(Permission.READ));
        permissionService.assignRolesToUsers(Collections.singletonMap(r, Collections.singleton(u.getId())));

        user = new UserWithPermission(u, permissionService.findBasePermissionByUserId(u.getId()),
            Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

        project = projectService.create("test", "TEST", "desc");

        CardLabel lnull = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.NULL, CardLabel.LabelDomain.USER, "null", 0);

        CardLabel lstring = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.STRING, CardLabel.LabelDomain.USER, "string", 0);

        CardLabel ltimestamp = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.TIMESTAMP, CardLabel.LabelDomain.USER, "date", 0);

        CardLabel luser = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.USER, CardLabel.LabelDomain.USER, "user", 0);

        CardLabel lcard = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.CARD, CardLabel.LabelDomain.USER, "card", 0);

        CardLabel lint = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.INT, CardLabel.LabelDomain.USER, "int", 0);

        CardLabel llist = cardLabelRepository
            .addLabel(project.getId(), false, CardLabel.LabelType.LIST, CardLabel.LabelDomain.USER, "list", 0);
        LabelListValue llistVal = cardLabelRepository.addLabelListValue(llist.getId(), "abcd");

        // Init new card
        Board board = boardRepository
            .createNewBoard("test-label", "LABEL", "label", projectService.findByShortName("TEST").getId());
        List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
        BoardColumn column = boardColumnRepository.addColumnToBoard("label-column", definitions.get(0).getId(),
            BoardColumn.BoardColumnLocation.BOARD, board.getId());
        Card card = cardService.createCard("card", column.getId(), new Date(), user);

        // Init milestone
        CardLabel l = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_MILESTONE, CardLabel.LabelDomain.SYSTEM);
        LabelListValue mlv = cardLabelRepository.addLabelListValue(l.getId(), "1.0");

        // Add labels
        cardLabelRepository.addLabelValueToCard(l, card.getId(), new CardLabelValue.LabelValue(null, null, null, null,
            null, mlv.getId()));

        cardLabelRepository.addLabelValueToCard(lnull, card.getId(), new CardLabelValue.LabelValue(null, null, null,
            null, null, null));
        cardLabelRepository.addLabelValueToCard(lstring, card.getId(), new CardLabelValue.LabelValue("ABC"));
        cardLabelRepository.addLabelValueToCard(ltimestamp, card.getId(), new CardLabelValue.LabelValue(new Date()));
        cardLabelRepository.addLabelValueToCard(luser, card.getId(), new CardLabelValue.LabelValue(null, null, null,
            null, u.getId(), null));
        cardLabelRepository.addLabelValueToCard(lcard, card.getId(), new CardLabelValue.LabelValue(null, null, null,
            card.getId(), null, null));
        cardLabelRepository.addLabelValueToCard(lint, card.getId(), new CardLabelValue.LabelValue(null, null, 999,
            null, null, null));
        cardLabelRepository.addLabelValueToCard(llist, card.getId(), new CardLabelValue.LabelValue(null, null, null,
            null, null, llistVal.getId()));
    }

    @Test
    public void testGetWrongMilestone() {
        LabelListValueWithMetadata m = excelExportService.getMilestone(project.getId(), "AAAA");
        Assert.assertNull(m);
    }

    @Test
    public void testGetMilestone() {
        LabelListValueWithMetadata m = excelExportService.getMilestone(project.getId(), "1.0");
        Assert.assertNotNull(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportMilestoneToExcelWrongValue() throws IOException {

        excelExportService.exportMilestoneToExcel(project.getShortName(), "AAA", user);

    }

    @Test
    public void testExportMilestoneToExcel() throws IOException {

        HSSFWorkbook w = excelExportService.exportMilestoneToExcel(project.getShortName(), "1.0", user);

        Assert.assertNotNull(w);
        Assert.assertEquals(1, w.getSheet("1.0").getLastRowNum()); // 0 based -> 1 means 2 rows (header + 1 card)

    }

    @Test
    public void testExportProjectToExcel() throws IOException {

        HSSFWorkbook w = excelExportService.exportProjectToExcel(project.getShortName(), user);

        Assert.assertNotNull(w);
        Assert.assertEquals(1, w.getSheet(project.getName()).getLastRowNum()); // 0 based -> 1 means 2 rows (header + 1 card)

    }
}
