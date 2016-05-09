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

import static org.mockito.Mockito.when;
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.Card;
import io.lavagna.model.CardLabel;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.MilestoneCount;
import io.lavagna.model.Project;
import io.lavagna.model.ProjectAndBoard;
import io.lavagna.model.User;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.MilestoneExportService;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchService;
import io.lavagna.service.StatisticsService;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private MilestoneExportService milestoneExportService;
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
    private User user;

    @Before
    public void prepare() {
        milestoneController = new MilestoneController(cardLabelRepository, projectService,
            statisticsService, searchService, milestoneExportService);

        ProjectAndBoard pab = new ProjectAndBoard(project.getId(), project.getShortName(), project.getName(),
            project.getDescription(), project.isArchived(), board.getId(), board.getShortName(), board.getName(),
            board.getDescription(), board.isArchived());
        when(boardRepository.findProjectAndBoardByColumnId(boardColumn.getId())).thenReturn(pab);
    }

    @Test
    public void testFindCardsByMilestone() {
        when(cardLabelRepository.findLabelByName(1, "MILESTONE", CardLabel.LabelDomain.SYSTEM)).thenReturn(
            new CardLabel(1, 1, true, CardLabel.LabelType.STRING, CardLabel.LabelDomain.SYSTEM, "MILESTONE", 0));
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
}
