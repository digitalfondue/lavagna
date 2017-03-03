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
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_ASSIGNED;
import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
public class StatisticsServiceTest {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private CardLabelRepository cardLabelRepository;

	@Autowired
	private LabelService labelService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StatisticsService statisticsService;

	private Date today;
	private Date yesterday;
	private Date oneMonthAgo;
	private User user;
	private BoardColumn openCol;
	private Board board;
	private Card card;
	private CardLabel assigned;
	private CardLabel customLabel;
	private CardLabelValue.LabelValue labelValueToUser;

	private Date lastSnapshot;

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");
		Project p = projectService.create("test", "TEST", "desc");
		board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, p.getId());
		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(projectService
				.findByShortName("TEST").getId());

		for (BoardColumnDefinition def : definitions) {
			if (def.getValue() == ColumnDefinition.OPEN) {
				openCol = boardColumnRepository.addColumnToBoard("col1", def.getId(),
						BoardColumn.BoardColumnLocation.BOARD,
						board.getId());
			}
		}

		today = DateUtils.truncate(new Date(), Calendar.DATE);
		yesterday = DateUtils.addDays(today, -1);
		oneMonthAgo = DateUtils.addMonths(today, -1);

		lastSnapshot = DateUtils.addYears(today, -10);

		card = cardService.createCard("card1", openCol.getId(), today, user);

		assigned = cardLabelRepository.findLabelByName(p.getId(), SYSTEM_LABEL_ASSIGNED, CardLabel.LabelDomain.SYSTEM);
		customLabel = cardLabelRepository.addLabel(p.getId(), true, CardLabel.LabelType.USER,
				CardLabel.LabelDomain.USER, "Custom label", 0);
		labelValueToUser = new CardLabelValue.LabelValue(null, null, null, null, user.getId(), null);
	}

	private void createArchivedCard() {
		BoardColumn destination = boardColumnRepository.findDefaultColumnFor(board.getId(),
				BoardColumn.BoardColumnLocation.ARCHIVE);
		Card archivedCard = cardService.createCard("Archived card", openCol.getId(), today, user);
		cardService.moveCardToColumn(archivedCard.getId(), archivedCard.getColumnId(), destination.getId(),
				user.getId(), new Date());
	}

	private void snapshotStatistics() throws InterruptedException {
		long diffInSeconds;
		do {
			diffInSeconds = (new Date().getTime() - lastSnapshot.getTime()) / 1000;
			Thread.sleep(250);
		} while (diffInSeconds < 1);
		lastSnapshot = new Date();
		statisticsService.snapshotCardsStatus();
	}

	private void verifyResults(Map<Long, Map<ColumnDefinition, Long>> results, int resultsSize, Date date,
			ColumnDefinition def, long expectedValue) {
		Assert.assertEquals(resultsSize, results.size());
		long dateTime = DateUtils.truncate(date, Calendar.DATE).getTime();
		if (!results.containsKey(dateTime)) {
			Assert.fail();
		}

		Map<ColumnDefinition, Long> day = results.get(dateTime);
		if (!day.containsKey(def)) {
			Assert.fail();
		}

		Assert.assertEquals(expectedValue, day.get(def).longValue());
	}

	@Test
	public void getCardsStatusByBoardTest() throws InterruptedException {
		Map<Long, Map<ColumnDefinition, Long>> results = statisticsService.getCardsStatusByBoard(board.getId(),
				oneMonthAgo);

		Assert.assertEquals(0, results.size());

		snapshotStatistics();

		results = statisticsService.getCardsStatusByBoard(board.getId(), oneMonthAgo);

		verifyResults(results, 1, today, ColumnDefinition.OPEN, 1);
	}

	@Test
	public void getCardsStatusByProjectTest() throws InterruptedException {
		Map<Long, Map<ColumnDefinition, Long>> results = statisticsService
				.getCardsStatusByProject(board.getProjectId(), oneMonthAgo);

		Assert.assertEquals(0, results.size());

		snapshotStatistics();

		results = statisticsService.getCardsStatusByProject(board.getProjectId(), oneMonthAgo);

		verifyResults(results, 1, today, ColumnDefinition.OPEN, 1);
	}

	@Test
	public void getCardsStatusByBoardOnDoubleSnapshotTest() throws InterruptedException {
		Map<Long, Map<ColumnDefinition, Long>> results = statisticsService.getCardsStatusByBoard(board.getId(),
				oneMonthAgo);

		Assert.assertEquals(0, results.size());

		snapshotStatistics();

		snapshotStatistics();

		results = statisticsService.getCardsStatusByBoard(board.getId(), oneMonthAgo);

		verifyResults(results, 1, today, ColumnDefinition.OPEN, 1);
	}

	@Test
	public void getCardsStatusByBoardOnDoubleCreateTest() throws InterruptedException {
		Map<Long, Map<ColumnDefinition, Long>> results = statisticsService.getCardsStatusByBoard(board.getId(),
				oneMonthAgo);

		Assert.assertEquals(0, results.size());

		snapshotStatistics();

		cardService.createCard("card2", openCol.getId(), today, user);

		snapshotStatistics();

		results = statisticsService.getCardsStatusByBoard(board.getId(), oneMonthAgo);

		verifyResults(results, 1, today, ColumnDefinition.OPEN, 2);
	}

	@Test
	public void getActiveUsersOnBoardTest() {
		Integer activeUsers = statisticsService.getActiveUsersOnBoard(board.getId(), oneMonthAgo);

		Assert.assertEquals(1, activeUsers.intValue());
	}

	@Test
	public void getActiveUsersOnProjectTest() {
		Integer activeUsers = statisticsService.getActiveUsersOnProject(board.getProjectId(), oneMonthAgo);

		Assert.assertEquals(1, activeUsers.intValue());
	}

	@Test
	public void getAverageUsersPerCardOnBoardTest() {
		double averageUsers = statisticsService.getAverageUsersPerCardOnBoard(board.getId());
		Assert.assertEquals(0, averageUsers, Double.MIN_VALUE);

		labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());

		averageUsers = statisticsService.getAverageUsersPerCardOnBoard(board.getId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);

		createArchivedCard();

		averageUsers = statisticsService.getAverageUsersPerCardOnBoard(board.getId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);
	}

	@Test
	public void getAverageUsersPerCardOnProjectTest() {
		double averageUsers = statisticsService.getAverageUsersPerCardOnProject(board.getProjectId());
		Assert.assertEquals(0, averageUsers, Double.MIN_VALUE);

		labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());

		averageUsers = statisticsService.getAverageUsersPerCardOnProject(board.getProjectId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);

		createArchivedCard();

		averageUsers = statisticsService.getAverageUsersPerCardOnProject(board.getProjectId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);
	}

	// Average cards per user

	@Test
	public void getAverageCardsPerUserOnBoardTest() {
		double averageUsers = statisticsService.getAverageCardsPerUserOnBoard(board.getId());
		Assert.assertEquals(0, averageUsers, Double.MIN_VALUE);

		labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());

		averageUsers = statisticsService.getAverageCardsPerUserOnBoard(board.getId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);

		createArchivedCard();

		averageUsers = statisticsService.getAverageCardsPerUserOnBoard(board.getId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);
	}

	@Test
	public void getAverageCardsPerUserOnProjectTest() {
		double averageUsers = statisticsService.getAverageCardsPerUserOnProject(board.getProjectId());
		Assert.assertEquals(0, averageUsers, Double.MIN_VALUE);

		labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());

		averageUsers = statisticsService.getAverageCardsPerUserOnProject(board.getProjectId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);

		createArchivedCard();

		averageUsers = statisticsService.getAverageCardsPerUserOnProject(board.getProjectId());
		Assert.assertEquals(1, averageUsers, Double.MIN_VALUE);
	}

	// Cards by label

	@Test
	public void getCardsByLabelOnBoardTest() {
		List<LabelAndValueWithCount> labels = statisticsService.getCardsByLabelOnBoard(board.getId());
		Assert.assertEquals(0, labels.size());

		labelService.addLabelValueToCard(customLabel.getId(), card.getId(), labelValueToUser, user, new Date());

		labels = statisticsService.getCardsByLabelOnBoard(board.getId());
		Assert.assertEquals(1, labels.size());
	}

	@Test
	public void getCardsByLabelOnProjectTest() {
		List<LabelAndValueWithCount> labels = statisticsService.getCardsByLabelOnProject(board.getProjectId());
		Assert.assertEquals(0, labels.size());

		labelService.addLabelValueToCard(customLabel.getId(), card.getId(), labelValueToUser, user, new Date());

		labels = statisticsService.getCardsByLabelOnProject(board.getProjectId());
		Assert.assertEquals(1, labels.size());
	}

	// Created / closed cards

	@Test
	public void getCreatedAndClosedCardsByBoardTest() {
		Map<Long, Pair<Long, Long>> events = statisticsService.getCreatedAndClosedCardsByBoard(board.getId(),
				oneMonthAgo);
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(1, events.get(today.getTime()).getFirst().longValue());
		Assert.assertEquals(0, events.get(today.getTime()).getSecond().longValue());

		cardService.createCard("card2", openCol.getId(), yesterday, user);
		cardService.createCard("card3", openCol.getId(), yesterday, user);

		BoardColumn destination = boardColumnRepository.findDefaultColumnFor(board.getId(),
				BoardColumn.BoardColumnLocation.ARCHIVE);
		cardService.moveCardToColumn(card.getId(), card.getColumnId(), destination.getId(), user.getId(), yesterday);

		events = statisticsService.getCreatedAndClosedCardsByBoard(board.getId(), oneMonthAgo);

		Assert.assertEquals(2, events.size());
		Assert.assertEquals(1L, events.get(today.getTime()).getFirst().longValue());
		Assert.assertEquals(0L, events.get(today.getTime()).getSecond().longValue());
		Assert.assertEquals(2L, events.get(yesterday.getTime()).getFirst().longValue());
		Assert.assertEquals(1L, events.get(yesterday.getTime()).getSecond().longValue());
	}

	@Test
	public void getCreatedAndClosedCardsByProjectTest() {
		Map<Long, Pair<Long, Long>> events = statisticsService.getCreatedAndClosedCardsByProject(
				board.getProjectId(), oneMonthAgo);
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(1, events.get(today.getTime()).getFirst().longValue());
		Assert.assertEquals(0, events.get(today.getTime()).getSecond().longValue());

		cardService.createCard("card2", openCol.getId(), yesterday, user);
		cardService.createCard("card3", openCol.getId(), yesterday, user);

		BoardColumn destination = boardColumnRepository.findDefaultColumnFor(board.getId(),
				BoardColumn.BoardColumnLocation.ARCHIVE);
		cardService.moveCardToColumn(card.getId(), card.getColumnId(), destination.getId(), user.getId(), yesterday);

		events = statisticsService.getCreatedAndClosedCardsByProject(board.getProjectId(), oneMonthAgo);

		Assert.assertEquals(2, events.size());
		Assert.assertEquals(1L, events.get(today.getTime()).getFirst().longValue());
		Assert.assertEquals(0L, events.get(today.getTime()).getSecond().longValue());
		Assert.assertEquals(2L, events.get(yesterday.getTime()).getFirst().longValue());
		Assert.assertEquals(1L, events.get(yesterday.getTime()).getSecond().longValue());
	}

	// Most active card

	@Test
	public void getMostActiveCardByBoardTest() {
		Card resultCard = statisticsService.getMostActiveCardByBoard(board.getId(), oneMonthAgo);
		Assert.assertEquals(card.getId(), resultCard.getId());
	}

	@Test
	public void getTodayMostActiveCardByBoardTest() {
		Card resultCard = statisticsService.getMostActiveCardByBoard(board.getId(), today);
		Assert.assertEquals(card.getId(), resultCard.getId());
	}

	@Test
	public void getTomorrowMostActiveCardByBoardTest() {
		Card resultCard = statisticsService.getMostActiveCardByBoard(board.getId(), DateUtils.addDays(today, 1));
		Assert.assertNull(resultCard);
	}

	@Test
	public void getMostActiveCardByProjectTest() {
		Card resultCard = statisticsService.getMostActiveCardByProject(board.getProjectId(), oneMonthAgo);
		Assert.assertEquals(card.getId(), resultCard.getId());
	}

	@Test
	public void getTodayMostActiveCardByProjectTest() {
		Card resultCard = statisticsService.getMostActiveCardByProject(board.getProjectId(), today);
		Assert.assertEquals(card.getId(), resultCard.getId());
	}

	@Test
	public void getTomorrowMostActiveCardByProjectTest() {
		Card resultCard = statisticsService.getMostActiveCardByProject(board.getProjectId(), DateUtils.addDays(today, 1));
		Assert.assertNull(resultCard);
	}

	// Milestones

	@Test
	public void getCreatedAndClosedCardsByMilestoneTest() {
		Card card2 = cardService.createCard("card2", openCol.getId(), yesterday, user);

		CardLabel milestoneLabel = cardLabelRepository.findLabelByName(board.getProjectId(), SYSTEM_LABEL_MILESTONE,
				CardLabel.LabelDomain.SYSTEM);

		LabelListValue milestone = cardLabelRepository.addLabelListValue(milestoneLabel.getId(), "Dummy milestone");

		Map<Long, Pair<Long, Long>> results = statisticsService.getAssignedAndClosedCardsByMilestone(milestone,
				oneMonthAgo);
		Assert.assertEquals(results.size(), 0);

		labelService.addLabelValueToCard(milestoneLabel.getId(), card2.getId(),
				new CardLabelValue.LabelValue(null, null, null, null, null, milestone.getId()), user, new Date());

		results = statisticsService.getAssignedAndClosedCardsByMilestone(milestone, oneMonthAgo);
		Assert.assertEquals(results.size(), 1);
	}

	@Test
	public void testFindCardsByMilestoneOnEmptyProject() {
		Project p = projectService.create("test", "TEST2", "desc");
		List<MilestoneCount> res = statisticsService.findCardsCountByMilestone(p.getId());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void testFindCardsByMilestoneOnMultipleProjects() {
		cardService.createCard("card1", openCol.getId(), new Date(), user);
		cardService.createCard("card2", openCol.getId(), new Date(), user);
		Project p = projectService.create("test", "TEST2", "desc");

		Assert.assertEquals(0, statisticsService.findCardsCountByMilestone(p.getId()).size());
		Assert.assertEquals(1, statisticsService.findCardsCountByMilestone(board.getProjectId()).size());
	}

	@Test
	public void testFindCardsByMilestone() {
		cardService.createCard("card1", openCol.getId(), new Date(), user);
		cardService.createCard("card2", openCol.getId(), new Date(), user);
		cardService.createCard("card3", openCol.getId(), new Date(), user);

		List<MilestoneCount> res = statisticsService.findCardsCountByMilestone(board.getProjectId());
		Assert.assertEquals(1, res.size());
	}

	@Test
	public void testFindCardsByMilestoneOnTrashedCards() {
		cardService.createCard("card1", openCol.getId(), new Date(), user);
		cardService.createCard("card2", openCol.getId(), new Date(), user);

		Card card3 = cardService.createCard("card3", openCol.getId(), new Date(), user);
		List<Integer> trashedCardIds = new ArrayList<>();
		trashedCardIds.add(card3.getId());
		BoardColumn trashColumn = boardColumnRepository.findDefaultColumnFor(openCol.getBoardId(),
				BoardColumn.BoardColumnLocation.TRASH);
		cardService.moveCardsToColumn(trashedCardIds, openCol.getId(), trashColumn.getId(), user.getId(),
				BoardColumn.BoardColumnLocation.Companion.getMAPPING().get(BoardColumn.BoardColumnLocation.TRASH), new Date());

		Card card4 = cardService.createCard("card4", openCol.getId(), new Date(), user);
		List<Integer> archivedCardIds = new ArrayList<>();
		archivedCardIds.add(card4.getId());
		BoardColumn archiveColumn = boardColumnRepository.findDefaultColumnFor(openCol.getBoardId(),
				BoardColumn.BoardColumnLocation.ARCHIVE);
		cardService.moveCardsToColumn(archivedCardIds, openCol.getId(), archiveColumn.getId(), user.getId(),
				BoardColumn.BoardColumnLocation.Companion.getMAPPING().get(BoardColumn.BoardColumnLocation.ARCHIVE), new Date());

		// Double check that everything is in the right place
		Assert.assertEquals(3, cardRepository.findAllByColumnId(openCol.getId()).size());
		Assert.assertEquals(1, cardRepository.findAllByColumnId(trashColumn.getId()).size());
		Assert.assertEquals(1, cardRepository.findAllByColumnId(archiveColumn.getId()).size());

		List<MilestoneCount> res = statisticsService.findCardsCountByMilestone(board.getProjectId());

		Assert.assertEquals(2, res.size());
	}

	private void checkMilestone(List<MilestoneCount> result, Integer milestoneId, int count) {
		for (MilestoneCount mc : result) {
			if (Objects.equals(mc.getMilestoneId(), milestoneId)) {
				Assert.assertEquals(count, mc.getCount());
				return;
			}
		}
		Assert.fail();
	}

	@Test
	public void testFindCardsByMilestoneWithLabels() {
		cardService.createCard("card1", openCol.getId(), new Date(), user);
		cardService.createCard("card2", openCol.getId(), new Date(), user);
		Card c3 = cardService.createCard("card3", openCol.getId(), new Date(), user);

		LabelListValue llv = null;
		for (CardLabel cardLabel : cardLabelRepository.findLabelsByProject(board.getProjectId())) {
			if (cardLabel.getDomain() == CardLabel.LabelDomain.SYSTEM && cardLabel.getName().equals(SYSTEM_LABEL_MILESTONE)) {
				llv = cardLabelRepository.addLabelListValue(cardLabel.getId(), "v1.0");
				cardLabelRepository.addLabelValueToCard(cardLabel, c3.getId(), new CardLabelValue.LabelValue(null,
						null, null, null, null, llv.getId()));
				break;
			}
		}
		List<MilestoneCount> res = statisticsService.findCardsCountByMilestone(board.getProjectId());
		Assert.assertEquals(2, res.size());

		checkMilestone(res, null, 3);
		checkMilestone(res, llv.getId(), 1);
	}
}
