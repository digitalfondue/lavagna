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

import io.lavagna.common.Json;
import io.lavagna.model.*;
import io.lavagna.query.StatisticsQuery;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
class LavagnaExporter {

	private final ConfigurationRepository configurationRepository;
	private final UserRepository userRepository;
	private final PermissionService permissionService;
	private final ProjectService projectService;
	private final CardLabelRepository cardLabelRepository;
	private final BoardRepository boardRepository;
	private final BoardColumnRepository boardColumnRepository;
	private final EventRepository eventRepository;
	private final CardRepository cardRepository;
	private final CardDataRepository cardDataRepository;
	private final StatisticsQuery statisticsQuery;


	public LavagnaExporter(ConfigurationRepository configurationRepository, UserRepository userRepository,
			PermissionService permissionService, ProjectService projectService,
			CardLabelRepository cardLabelRepository, BoardRepository boardRepository,
			BoardColumnRepository boardColumnRepository, EventRepository eventRepository,
			CardRepository cardRepository, CardDataRepository cardDataRepository, StatisticsQuery statisticsQuery) {
		this.configurationRepository = configurationRepository;
		this.userRepository = userRepository;
		this.permissionService = permissionService;
		this.projectService = projectService;
		this.cardLabelRepository = cardLabelRepository;
		this.boardRepository = boardRepository;
		this.boardColumnRepository = boardColumnRepository;
		this.eventRepository = eventRepository;
		this.cardRepository = cardRepository;
		this.cardDataRepository = cardDataRepository;
		this.statisticsQuery = statisticsQuery;
	}

	public void exportData(OutputStream os) throws IOException {
		try (ZipOutputStream zf = new ZipOutputStream(os);
				OutputStreamWriter osw = new OutputStreamWriter(zf, StandardCharsets.UTF_8)) {
			writeEntry("config.json", configurationRepository.findAll(), zf, osw);
			writeEntry("users.json", userRepository.findAll(), zf, osw);
			writeEntry("permissions.json", permissionService.findAllRolesAndRelatedPermissionWithUsers(), zf, osw);

			writeEntry("users-password-hash.json", userRepository.findUsersWithPasswords(), zf, osw);

			exportFiles(zf, osw);

			for (Project p : projectService.findAll()) {
				exportProject(zf, osw, p);
			}

			//
			final int amountPerPage = 100;
			int pages = (eventRepository.count() + amountPerPage - 1) / amountPerPage;

			writeEntry("events-page-count.json", pages, zf, osw);
			for (int i = 0; i < pages; i++) {
				writeEntry("events-" + i + ".json", toEventFull(eventRepository.find(i * 100, 100)), zf, osw);
			}
			//
			writeEntry("card-data-types-order.json", cardDataRepository.findAllByTypes(EnumSet.of(CardType.ACTION_LIST,
					CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED)), zf, osw);
		}
	}

	private void exportFiles(ZipOutputStream zf, OutputStreamWriter osw) throws IOException {
		osw.flush();
		for (CardDataUploadContentInfo fileData : cardDataRepository.findAllDataUploadContentInfo()) {
			zf.putNextEntry(new ZipEntry("files/" + fileData.getDigest()));
			cardDataRepository.outputFileContent(fileData.getDigest(), zf);
			writeEntry("files/" + fileData.getDigest() + ".json", fileData, zf, osw);
		}
	}

	private List<EventFull> toEventFull(List<Event> events) {
		List<EventFull> res = new ArrayList<>(events.size());
		for (Event e : events) {
			// TODO not optimal

			User u = userRepository.findById(e.getUserId());
			ImmutablePair<Board, Card> bc = findByCardId(e.getCardId());
			//
			String content = handleContent(e);

			User labelUser = e.getValueUser() != null ? userRepository.findById(e.getValueUser()) : null;
			ImmutablePair<Board, Card> labelCard = e.getValueCard() != null ? findByCardId(e.getValueCard()) : null;

			//
			res.add(new EventFull(e, u, bc, content, labelCard, labelUser));
		}
		return res;
	}

	private ImmutablePair<Board, Card> findByCardId(int id) {
		Card c = cardRepository.findBy(id);
		Board b = boardRepository.findBoardById(boardColumnRepository.findById(c.getColumnId()).getBoardId());
		return ImmutablePair.of(b, c);
	}

	// TODO CLEANUP
	private String handleContent(Event e) {
		if (e.getDataId() == null) {
			return null;
		}

		switch (e.getEvent()) {
		case COMMENT_CREATE:
			return extractFirstContent(e, CardType.COMMENT_HISTORY);
		case DESCRIPTION_CREATE:
			return extractFirstContent(e, CardType.DESCRIPTION_HISTORY);
		case DESCRIPTION_UPDATE:
		case COMMENT_UPDATE:
			List<Event> nextEvent = eventRepository.findNextEventFor(e);
			return cardDataRepository.getDataLightById(
					nextEvent.isEmpty() ? e.getDataId() : nextEvent.get(0).getPreviousDataId()).getContent();
		case ACTION_ITEM_CREATE:
		case ACTION_LIST_CREATE:
			return cardDataRepository.getDataLightById(e.getDataId()).getContent();
		case FILE_UPLOAD:
		case FILE_DELETE:
			return cardDataRepository.getDataLightById(e.getDataId()).getContent();
		default:
			return null;
		}

	}

	private String extractFirstContent(Event e, CardType type) {
		CardData cd = cardDataRepository.getDataLightById(e.getDataId());
		List<CardData> history = cardDataRepository.findAllDataLightByReferenceIdAndType(cd.getId(), type);
		return (history.isEmpty() ? cd : history.get(0)).getContent();
	}

	private static void writeEntry(String entryName, Object toSerialize, ZipOutputStream zf, OutputStreamWriter osw) {
		try {
			zf.putNextEntry(new ZipEntry(entryName));
			Json.GSON.toJson(toSerialize, osw);
			osw.flush();
			zf.flush();
			zf.closeEntry();
		} catch (IOException ioe) {
			throw new IllegalStateException("error while serializing entry " + entryName, ioe);
		}
	}

	private void exportProject(ZipOutputStream zf, OutputStreamWriter osw, Project p) {

		String projectNameDir = "projects/" + p.getShortName();
		writeEntry(projectNameDir + ".json", p, zf, osw);
		writeEntry(projectNameDir + "/permissions.json",
				permissionService.findAllRolesAndRelatedPermissionWithUsersInProjectId(p.getId()), zf, osw);

		List<Pair<CardLabel, List<LabelListValueWithMetadata>>> labels = new ArrayList<>();
		for (CardLabel cl : cardLabelRepository.findLabelsByProject(p.getId())) {
		    Pair<CardLabel, List<LabelListValueWithMetadata>> toAdd = Pair.Companion.of(cl, cardLabelRepository.findListValuesByLabelId(cl.getId()));
			labels.add(toAdd);
		}
		writeEntry(projectNameDir + "/labels.json", labels, zf, osw);
		writeEntry(projectNameDir + "/column-definitions.json",
				projectService.findMappedColumnDefinitionsByProjectId(p.getId()), zf, osw);

		for (BoardInfo boardInfo : boardRepository.findBoardInfo(p.getId())) {
			exportBoard(boardInfo, p, zf, osw);
		}
	}

	private void exportBoard(BoardInfo boardInfo, Project p, ZipOutputStream zf, OutputStreamWriter osw) {
		String boardNameDir = "boards/" + boardInfo.getShortName();
		writeEntry(boardNameDir + ".json", Pair.Companion.of(p.getShortName(), boardInfo), zf, osw);
		int boardId = boardRepository.findBoardIdByShortName(boardInfo.getShortName());

		writeEntry(boardNameDir + "/columns.json", boardColumnRepository.findAllColumnsFor(boardId), zf, osw);
		writeEntry(boardNameDir + "/cards.json", cardRepository.findAllByBoardShortName(boardInfo.getShortName()), zf,
				osw);
		writeEntry(boardNameDir + "/statistics.json", statisticsQuery.findForBoard(boardId), zf, osw);
	}
}
