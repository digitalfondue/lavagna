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

import static io.lavagna.service.SearchFilter.filter;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumnInfo;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.SearchResults;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MilestoneExportService {

    private final CardRepository cardRepository;
    private final CardLabelRepository cardLabelRepository;
    private final ProjectService projectService;
    private final SearchService searchService;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;

    @Autowired
    public MilestoneExportService(CardRepository cardRepository, CardLabelRepository cardLabelRepository,
        ProjectService projectService, SearchService searchService, BoardColumnRepository boardColumnRepository,
        UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.cardLabelRepository = cardLabelRepository;
        this.projectService = projectService;
        this.searchService = searchService;
        this.boardColumnRepository = boardColumnRepository;
        this.userRepository = userRepository;
    }

    public LabelListValueWithMetadata getMilestone(int projectId, String milestone) {
        CardLabel label = cardLabelRepository.findLabelByName(projectId, "MILESTONE", CardLabel.LabelDomain.SYSTEM);
        List<LabelListValueWithMetadata> listValues = cardLabelRepository
            .findListValuesByLabelIdAndValue(label.getId(), milestone);
        return listValues.size() > 0 ? listValues.get(0) : null;
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
                        if (!userCache.containsKey(lv.getValueUser())) {
                            User user = userRepository.findById(lv.getValueUser());
                            userCache.put(lv.getValueUser(),
                                firstNonNull(user.getDisplayName(), user.getUsername()));
                        }
                        sb.append(userCache.get(lv.getValueUser()));
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

    public HSSFWorkbook exportMilestoneToExcel(String projectShortName, String milestone, UserWithPermission user)
        throws IOException {

        int projectId = projectService.findByShortName(projectShortName).getId();
        LabelListValueWithMetadata ms = getMilestone(projectId, milestone);
        if (ms == null)
            throw new IllegalArgumentException();

        List<CardLabel> labels = cardLabelRepository.findLabelsByProject(projectId);
        labels.removeIf(new Predicate<CardLabel>() {
            @Override public boolean test(CardLabel cl) {
                if (cl.getDomain().equals(CardLabel.LabelDomain.SYSTEM)) {
                    if (cl.getName().equals("ASSIGNED") ||
                        cl.getName().equals("DUE_DATE")) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        labels.sort(new Comparator<CardLabel>() {
            public int compare(CardLabel l1, CardLabel l2) {
                int domains = l1.getDomain().compareTo(l2.getDomain());
                if (domains != 0) {
                    return domains;
                }
                return l1.getName().compareTo(l2.getName());
            }
        });

        SearchFilter filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.STRING, milestone);
        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.TRASH.toString());
        SearchResults cards = searchService.find(Arrays.asList(filter, notTrashFilter), projectId, null, user);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(milestone);

        Row header = sheet.createRow(0);

        int colPos = 0;
        header.createCell(colPos++).setCellValue("ID");
        header.createCell(colPos++).setCellValue("Name");
        header.createCell(colPos++).setCellValue("Column");
        header.createCell(colPos++).setCellValue("Status");
        for (CardLabel cl : labels) {
            header.createCell(colPos++).setCellValue(WordUtils.capitalizeFully(cl.getName().replace("_", " ")));
        }

        Map<Integer, BoardColumnInfo> colCache = new HashMap<>();
        Map<Integer, String> userCache = new HashMap<>();
        Map<Integer, String> cardCache = new HashMap<>();
        Map<Integer, String> listValueCache = new HashMap<>();
        int rowPos = 1;
        for (CardFullWithCounts card : cards.getFound()) {

            colPos = 0;
            Row row = sheet.createRow(rowPos++);

            // ID
            row.createCell(colPos++).setCellValue(String.format("%s-%s", card.getBoardShortName(), card.getSequence()));
            // Name
            row.createCell(colPos++).setCellValue(card.getName());
            // Column
            if (!colCache.containsKey(card.getColumnId())) {
                colCache.put(card.getColumnId(), boardColumnRepository.getColumnInfoById(card.getColumnId()));
            }
            BoardColumnInfo col = colCache.get(card.getColumnId());
            row.createCell(colPos++).setCellValue(col.getColumnName());
            // ColumnDefinition
            row.createCell(colPos++).setCellValue(card.getColumnDefinition().toString());
            // Labels
            fillLabelValues(row, colPos, labels, cardLabelRepository.findCardLabelValuesByCardId(card.getId()),
                userCache, cardCache, listValueCache);
        }

        for (int i = 0; i < colPos; i++) {
            sheet.autoSizeColumn(i);
        }

        return wb;

    }

}
