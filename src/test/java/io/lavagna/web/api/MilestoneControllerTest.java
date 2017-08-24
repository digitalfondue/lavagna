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
package io.lavagna.web.api;

import io.lavagna.model.*;
import io.lavagna.service.*;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MilestoneControllerTest {

    @Mock
    private BoardColumnRepository boardColumnRepository;
    @Mock
    private CardLabelRepository cardLabelRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private StatisticsService statisticsService;
    @Mock
    private SearchService searchService;
    @Mock
    private ExcelExportService excelExportService;
    @Mock
    private Card card;
    @Mock
    private BoardColumn boardColumn;
    @Mock
    private Project project;
    @Mock
    private Board board;

    private MilestoneController milestoneController;
    @Mock
    private UserWithPermission user;

    @Before
    public void prepare() {
        milestoneController = new MilestoneController(cardLabelRepository, projectService,
            statisticsService, searchService, excelExportService);
    }

    @Test
    public void testFindCardsByMilestone() {
        when(cardLabelRepository.findLabelByName(1, SYSTEM_LABEL_MILESTONE, CardLabel.LabelDomain.SYSTEM)).thenReturn(
            new CardLabel(1, 1, true, CardLabel.LabelType.STRING, CardLabel.LabelDomain.SYSTEM, SYSTEM_LABEL_MILESTONE, 0));
        when(projectService.findByShortName("TEST")).thenReturn(new Project(1, "test", "TEST", "test project", false));

        List<MilestoneCount> counts = new ArrayList<>();
        MilestoneCount count = new MilestoneCount(null, ColumnDefinition.OPEN, 1);
        counts.add(count);
        when(statisticsService.findCardsCountByMilestone(1)).thenReturn(counts);

        List<LabelListValueWithMetadata> listValues = new ArrayList<>();
        when(cardLabelRepository.findListValuesByLabelId(1)).thenReturn(listValues);

        Milestones cardsByMilestone = milestoneController.findCardsByMilestone("TEST");

        Assert.assertEquals(1, cardsByMilestone.getMilestones().size());
        MilestoneInfo md = cardsByMilestone.getMilestones().get(0);
        Assert.assertEquals("Unassigned", md.getLabelListValue().getValue());
        Assert.assertEquals(0, cardsByMilestone.getStatusColors().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindCardsByMilestoneDetailWrongValue() {
        milestoneController.findCardsByMilestoneDetail("TEST", 1, user);
    }

    @Test
    public void testFindCardsByMilestoneDetail() {

        when(projectService.findIdByShortName("TEST")).thenReturn(1);

        LabelListValue llv = new LabelListValue(1, 6, 0, "1.0");
        LabelListValueWithMetadata llvm = new LabelListValueWithMetadata(llv, new HashMap<String, String>());
        when(cardLabelRepository.findListValueById(1)).thenReturn(llvm);


        milestoneController.findCardsByMilestoneDetail("TEST", 1, user);

        verify(cardLabelRepository).findListValueById(eq(1));

    }

    @Test
    public void testExportMilestoneToExcel() throws IOException {

        MockHttpServletResponse mockResp = new MockHttpServletResponse();

        when(excelExportService.exportMilestoneToExcel("TEST", "1.0", user)).thenReturn(new HSSFWorkbook());

        milestoneController.exportMilestoneToExcel("TEST", "1.0", user, mockResp);

        verify(excelExportService).exportMilestoneToExcel(eq("TEST"), eq("1.0"), eq(user));
    }
}
