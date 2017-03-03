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
import io.lavagna.web.api.model.MilestoneDetail;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;
import static io.lavagna.service.SearchFilter.filter;

@RestController
public class MilestoneController {

    private final CardLabelRepository cardLabelRepository;
    private final ProjectService projectService;
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final ExcelExportService excelExportService;

    @Autowired
    public MilestoneController(CardLabelRepository cardLabelRepository, ProjectService projectService,
        StatisticsService statisticsService, SearchService searchService,
        ExcelExportService excelExportService) {
        this.cardLabelRepository = cardLabelRepository;
        this.projectService = projectService;
        this.statisticsService = statisticsService;
        this.searchService = searchService;
        this.excelExportService = excelExportService;
    }

    private Map<ColumnDefinition, Integer> getStatusColors(int projectId) {
        Map<ColumnDefinition, Integer> statusColors = new EnumMap<>(ColumnDefinition.class);
        for (BoardColumnDefinition cd : projectService.findColumnDefinitionsByProjectId(projectId)) {
            statusColors.put(cd.getValue(), cd.getColor());
        }
        return statusColors;
    }

    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/project/{projectShortName}/cards-by-milestone-detail/{milestoneId}", method = RequestMethod.GET)
    public MilestoneDetail findCardsByMilestoneDetail(@PathVariable("projectShortName") String projectShortName,
        @PathVariable("milestoneId") int milestoneId, UserWithPermission user) {

        int projectId = projectService.findIdByShortName(projectShortName);
        LabelListValueWithMetadata ms = cardLabelRepository.findListValueById(milestoneId);
        if (ms == null) {
            throw new IllegalArgumentException("Milestone not found");
        }

        SearchFilter filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.STRING, ms.getValue());
        List<MilestoneCount> mcs = statisticsService.findCardsCountByMilestone(projectId, ms.getId());
        Map<Long, Pair<Long, Long>> assignedAndClosedCards = statisticsService.getAssignedAndClosedCardsByMilestone(ms,
            DateUtils.addWeeks(DateUtils.truncate(new Date(), Calendar.DATE), -2));

        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.TRASH.toString());

        SearchResults cards = searchService.find(Arrays.asList(filter, notTrashFilter), projectId, null, user);

        Map<ColumnDefinition, Long> cardsCountByStatus = new HashMap<>();
        for (MilestoneCount count : mcs) {
            cardsCountByStatus.put(count.getColumnDefinition(), count.getCount());
        }

        return new MilestoneDetail(cardsCountByStatus, getStatusColors(projectId), cards, assignedAndClosedCards);
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

        return new Milestones(milestones, getStatusColors(project.getId()));
    }

    private void getMilestones(int projectId, Map<Integer, Integer> milestoneToIndex, List<MilestoneInfo> milestones) {
        CardLabel label = cardLabelRepository.findLabelByName(projectId, SYSTEM_LABEL_MILESTONE, CardLabel.LabelDomain.SYSTEM);
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
        @PathVariable("milestone") String milestone, UserWithPermission user, HttpServletResponse resp)
        throws IOException {

        HSSFWorkbook wb = excelExportService.exportMilestoneToExcel(projectShortName, milestone, user);

        resp.setHeader("Content-disposition", "attachment; filename=" + projectShortName + "-" + milestone + ".xls");
        try (OutputStream os = resp.getOutputStream()) {
            wb.write(os);
        }
    }
}
