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

import static io.lavagna.service.SearchFilter.filter;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.CardLabel;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.LabelListValue;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.MilestoneCount;
import io.lavagna.model.Pair;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.SearchResults;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.MilestoneExportService;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchFilter;
import io.lavagna.service.SearchService;
import io.lavagna.service.StatisticsService;
import io.lavagna.web.api.model.MilestoneDetail;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MilestoneController {

    private final CardLabelRepository cardLabelRepository;
    private final ProjectService projectService;
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final MilestoneExportService milestoneExportService;

    @Autowired
    public MilestoneController(CardLabelRepository cardLabelRepository, ProjectService projectService,
        StatisticsService statisticsService, SearchService searchService,
        MilestoneExportService milestoneExportService) {
        this.cardLabelRepository = cardLabelRepository;
        this.projectService = projectService;
        this.statisticsService = statisticsService;
        this.searchService = searchService;
        this.milestoneExportService = milestoneExportService;
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/cards-by-milestone", method = RequestMethod.GET)
    public Milestones findCardsByMilestone(@PathVariable("projectShortName") String projectShortName) {
        Project project = projectService.findByShortName(projectShortName);
        Map<Integer, Integer> milestoneToIndex = new HashMap<>();
        List<MilestoneInfo> milestones = new ArrayList<>();
        getMilestones(project.getId(), milestoneToIndex, milestones);

        for (MilestoneCount count : statisticsService.findCardsCountByMilestone(project.getId())) {
            MilestoneInfo md = milestones.get(milestoneToIndex.get(count.getMilestoneId()));
            md.getCardsCountByStatus().put(count.getColumnDefinition(), count.getCount());
        }

        Map<ColumnDefinition, Integer> statusColors = new EnumMap<>(ColumnDefinition.class);
        for (BoardColumnDefinition cd : projectService.findColumnDefinitionsByProjectId(project.getId())) {
            statusColors.put(cd.getValue(), cd.getColor());
        }

        return new Milestones(milestones, statusColors);
    }

    private void getMilestones(int projectId, Map<Integer, Integer> milestoneToIndex, List<MilestoneInfo> milestones) {
        CardLabel label = cardLabelRepository.findLabelByName(projectId, "MILESTONE", CardLabel.LabelDomain.SYSTEM);
        List<LabelListValueWithMetadata> listValues = cardLabelRepository.findListValuesByLabelId(label.getId());
        int foundUnassignedIndex = -1;
        int mIndex = 0;
        for (LabelListValue milestone : listValues) {
            milestones.add(new MilestoneInfo(milestone, new EnumMap<ColumnDefinition, Long>(ColumnDefinition.class)));
            milestoneToIndex.put(milestone.getId(), mIndex);
            if ("Unassigned".equals(milestone.getValue())) {
                foundUnassignedIndex = mIndex;
            }
            mIndex++;
        }
        if (foundUnassignedIndex < 0) {
            LabelListValue unassigned = new LabelListValue(-1, 0, Integer.MAX_VALUE, "Unassigned");
            milestones.add(new MilestoneInfo(unassigned, new EnumMap<ColumnDefinition, Long>(ColumnDefinition.class)));
            milestoneToIndex.put(null, milestoneToIndex.size());
        } else {
            milestoneToIndex.put(null, foundUnassignedIndex);
        }
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/export-milestone/{milestone}", method = RequestMethod.GET)
    public void exportMilestoneToExcel(@PathVariable("projectShortName") String projectShortName,
        @PathVariable("milestone") String milestone, UserWithPermission user, HttpServletResponse response)
        throws IOException {

        HSSFWorkbook wb = milestoneExportService.exportMilestoneToExcel(projectShortName, milestone, user);

        response.setHeader("Content-disposition", "attachment; filename=" + milestone + ".xls");
        wb.write(response.getOutputStream());
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/cards-by-milestone-detail/{milestone}", method = RequestMethod.GET)
    public MilestoneDetail findCardsByMilestoneDetail(@PathVariable("projectShortName") String projectShortName,
        @PathVariable("milestone") String milestone, UserWithPermission user) {

        int projectId = projectService.findByShortName(projectShortName).getId();
        LabelListValueWithMetadata ms = milestoneExportService.getMilestone(projectId, milestone);

        SearchFilter filter;
        Map<Long, Pair<Long, Long>> assignedAndClosedCards;

        if (ms != null) {
            filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.STRING, milestone);
            assignedAndClosedCards = statisticsService.getAssignedAndClosedCardsByMilestone(ms,
                DateUtils.addWeeks(DateUtils.truncate(new Date(), Calendar.DATE), -2));
        } else {
            filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.UNASSIGNED, null);
            assignedAndClosedCards = null;
        }

        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.TRASH.toString());

        SearchResults cards = searchService.find(Arrays.asList(filter, notTrashFilter), projectId, null, user);
        return new MilestoneDetail(cards, assignedAndClosedCards);
    }

}
