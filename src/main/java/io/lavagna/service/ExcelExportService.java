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

import io.lavagna.model.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.lavagna.common.Constants.*;
import static io.lavagna.service.SearchFilter.*;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Service
@Transactional(readOnly = true)
public class ExcelExportService {

    private final CardRepository cardRepository;
    private final CardDataService cardDataService;
    private final CardLabelRepository cardLabelRepository;
    private final ProjectService projectService;
    private final SearchService searchService;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExcelExportService(CardRepository cardRepository,
        CardDataService cardDataService,
        CardLabelRepository cardLabelRepository,
        ProjectService projectService,
        SearchService searchService,
        BoardColumnRepository boardColumnRepository,
        UserRepository userRepository) {

        this.cardRepository = cardRepository;
        this.cardDataService = cardDataService;
        this.cardLabelRepository = cardLabelRepository;
        this.projectService = projectService;
        this.searchService = searchService;
        this.boardColumnRepository = boardColumnRepository;
        this.userRepository = userRepository;

    }

    public LabelListValueWithMetadata getMilestone(int projectId, String milestone) {
        CardLabel label = cardLabelRepository
            .findLabelByName(projectId, SYSTEM_LABEL_MILESTONE, CardLabel.LabelDomain.SYSTEM);
        List<LabelListValueWithMetadata> listValues = cardLabelRepository
            .findListValuesByLabelIdAndValue(label.getId(), milestone);
        return listValues.size() > 0 ? listValues.get(0) : null;
    }

    private String getUserDescription(Map<Integer, String> userCache, Integer userId) {
        if (!userCache.containsKey(userId)) {
            User user = userRepository.findById(userId);
            userCache.put(userId, firstNonNull(user.getDisplayName(), user.getUsername()));
        }
        return userCache.get(userId);
    }

    private void fillLabelValues(Row row, int colPos, List<CardLabel> labels, Map<CardLabel,
        List<CardLabelValue>> lValues, Map<Integer, String> userCache, Map<Integer, String> cardCache,
        Map<Integer, String> listValueCache) {

        for (CardLabel cl : labels) {
            if (lValues.containsKey(cl)) {
                StringBuilder sb = new StringBuilder();
                for (CardLabelValue lav : lValues.get(cl)) {
                    CardLabelValue.LabelValue lv = lav.getValue();
                    switch (cl.getType()) {
                    case NULL:
                        sb.append("X");
                        break;
                    case STRING:
                        sb.append(lv.getValueString());
                        break;
                    case TIMESTAMP:
                        sb.append(new SimpleDateFormat("yyyy.MM.dd").format(lv.getValueTimestamp()));
                        break;
                    case INT:
                        sb.append(lv.getValueInt());
                        break;
                    case CARD:
                        if (!cardCache.containsKey(lv.getValueCard())) {
                            CardFull card = cardRepository.findFullBy(lv.getValueCard());
                            cardCache.put(lv.getValueCard(), String
                                .format("%s-%s %s", card.getBoardShortName(), card.getSequence(), card.getName()));
                        }
                        sb.append(cardCache.get(lv.getValueCard()));
                        break;
                    case USER:
                        sb.append(getUserDescription(userCache, lv.getValueUser()));
                        break;
                    case LIST:
                        if (!listValueCache.containsKey(lv.getValueList())) {
                            listValueCache.put(lv.getValueList(),
                                cardLabelRepository.findListValueById(lv.getValueList()).getValue());
                        }
                        sb.append(listValueCache.get(lv.getValueList()));
                        break;
                    }

                    sb.append(", ");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 2);
                }
                row.createCell(colPos).setCellValue(sb.toString());
            }
            colPos++;
        }
    }

    private HSSFWorkbook getWorkbookFromSearchFilters(int projectId, String sheetName, List<SearchFilter> filters,
        UserWithPermission user) {

        List<CardLabel> labels = cardLabelRepository.findLabelsByProject(projectId);
        CollectionUtils.filter(labels, new Predicate<CardLabel>() {
            @Override
            public boolean evaluate(CardLabel cl) {
                if (cl.getDomain().equals(CardLabel.LabelDomain.SYSTEM)) {
                    if (cl.getName().equals(SYSTEM_LABEL_ASSIGNED) ||
                        cl.getName().equals(SYSTEM_LABEL_DUE_DATE) ||
                        cl.getName().equals(SYSTEM_LABEL_MILESTONE)) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
        });
        Collections.sort(labels, new Comparator<CardLabel>() {
            public int compare(CardLabel l1, CardLabel l2) {
                return new CompareToBuilder().append(l1.getDomain(), l2.getDomain())
                    .append(l1.getName(), l2.getName())
                    .toComparison();
            }
        });

        SearchResults cards = searchService.find(filters, projectId, null, user);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName);

        Row header = sheet.createRow(0);

        int headerColPos = 0;
        header.createCell(headerColPos++).setCellValue("Board");
        header.createCell(headerColPos++).setCellValue("ID");
        header.createCell(headerColPos++).setCellValue("Name");
        header.createCell(headerColPos++).setCellValue("Column");
        header.createCell(headerColPos++).setCellValue("Status");
        header.createCell(headerColPos++).setCellValue("Description");
        header.createCell(headerColPos++).setCellValue("Created");
        header.createCell(headerColPos++).setCellValue("Created by");
        for (CardLabel cl : labels) {
            header.createCell(headerColPos++).setCellValue(WordUtils.capitalizeFully(cl.getName().replace("_", " ")));
        }

        Map<Integer, BoardColumnInfo> colCache = new HashMap<>();
        Map<Integer, String> userCache = new HashMap<>();
        Map<Integer, String> cardCache = new HashMap<>();
        Map<Integer, String> listValueCache = new HashMap<>();
        int rowPos = 1;
        for (CardFullWithCounts card : cards.getFound()) {

            int colPos = 0;
            Row row = sheet.createRow(rowPos++);

            // Board
            row.createCell(colPos++).setCellValue(card.getBoardShortName());
            // ID
            row.createCell(colPos++).setCellValue(card.getSequence());
            // Name
            row.createCell(colPos++).setCellValue(card.getName());
            // Column
            if (!colCache.containsKey(card.getColumnId())) {
                colCache.put(card.getColumnId(), boardColumnRepository.getColumnInfoById(card.getColumnId()));
            }
            BoardColumnInfo col = colCache.get(card.getColumnId());
            row.createCell(colPos++).setCellValue(col.getColumnName());
            // ColumnDefinition - status
            row.createCell(colPos++).setCellValue(card.getColumnDefinition().toString());
            // Description
            CardDataHistory desc = cardDataService.findLatestDescriptionByCardId(card.getId());
            row.createCell(colPos++).setCellValue(desc != null ? desc.getContent() : "");
            // Creation date
            row.createCell(colPos++).setCellValue(new SimpleDateFormat("yyyy.MM.dd").format(card.getCreationDate()));
            // Created by
            row.createCell(colPos++).setCellValue(getUserDescription(userCache, card.getCreationUser()));
            // Labels
            fillLabelValues(row, colPos, labels, cardLabelRepository.findCardLabelValuesByCardId(card.getId()),
                userCache, cardCache, listValueCache);
        }

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerColPos - 1));

        // Auto size the columns except for the description
        for (int i = 0; i < headerColPos; i++) {
            if (!header.getCell(i).getStringCellValue().equals("Description")) {
                sheet.autoSizeColumn(i);
            } else {
                sheet.setColumnWidth(i, 30 * 256);
            }
        }

        return wb;
    }

    public HSSFWorkbook exportMilestoneToExcel(String projectShortName, String milestone, UserWithPermission user)
        throws IOException {

        int projectId = projectService.findIdByShortName(projectShortName);
        LabelListValueWithMetadata ms = getMilestone(projectId, milestone);
        if (ms == null)
            throw new IllegalArgumentException();

        SearchFilter filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.STRING, milestone);
        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.TRASH.toString());

        return getWorkbookFromSearchFilters(projectId, milestone, Arrays.asList(filter, notTrashFilter), user);

    }

    public HSSFWorkbook exportProjectToExcel(String projectShortName, UserWithPermission user)
        throws IOException {

        Project project = projectService.findByShortName(projectShortName);

        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.TRASH.toString());

        return getWorkbookFromSearchFilters(project.getId(), project.getName(), Arrays.asList(notTrashFilter), user);

    }

}
