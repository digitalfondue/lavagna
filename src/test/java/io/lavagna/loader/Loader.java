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
package io.lavagna.loader;

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.service.*;
import io.lavagna.service.config.TestServiceConfig;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.lavagna.common.Constants.*;

/**
 * <p>
 * Run this class for loading data in the default database configured in {@link TestServiceConfig}.
 * </p>
 * This will generate around 50 projects, 50 * 10 boards, ~300k cards with comments, action list, labels.
 * <p/>
 * NOTES: for logging the slow queries with mysql:
 * <p/>
 *
 * <pre>
 * [mysqld]
 * slow_query_log = 1
 * slow_query_log_file = "slow.log"
 * long_query_time = 2
 * log-queries-not-using-indexes
 * </pre>
 */
public class Loader {

	private static final int CARDS_PER_COLUMN_IN_BOARD = 30;
	private static final int CARDS_PER_ARCHIVE_BACKLOG_TRASH = 200;
	private static final int COLUMNS_PER_BOARD = 5;
	private static final int PROJECT_NUMBERS = 50;
	private static final int BOARDS_PER_PROJECT = 10;
	private static final int MILESTONES_PER_PROJECT = 10;

	//
	private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime()
			.availableProcessors());

	private static final String[] words = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
			.split(" ");

	private static String commentGen() {
		StringBuilder sb = new StringBuilder();
		int max = RandomUtils.nextInt(10, 70);

		for (int i = 0; i < max; i++) {
			sb.append(words[RandomUtils.nextInt(0, words.length)]).append(" ");
		}

		return sb.toString().trim();
	}

	public static void main(String[] args) throws InterruptedException {

		System.setProperty("datasource.dialect", "MYSQL");

		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestServiceConfig.class,
				PersistenceAndServiceConfig.class);

		ac.register(CreateCardService.class);

		ProjectService ps = ac.getBean(ProjectService.class);
		CreateCardService ccs = ac.getBean(CreateCardService.class);

		UserRepository ur = ac.getBean(UserRepository.class);
		List<User> users = new ArrayList<>();

		System.out.println("creating users");
		for (int i = 0; i < 30; i++) {
			ur.createUser("loader", "user" + i, null,null, null, true);
			users.add(ur.findUserByName("loader", "user" + i));
		}
		System.out.println("end creation");

		CardLabelRepository clr = ac.getBean(CardLabelRepository.class);

		List<Project> projects = new ArrayList<>(PROJECT_NUMBERS);
		List<Integer> milestonesIds = new ArrayList<>();
		System.out.println("creating projects");
		for (int i = 0; i < PROJECT_NUMBERS; i++) {
			String name = "Load test project " + i;
			Project p = ps.create(name, "LDTEST_" + i, name);
			projects.add(p);

			// create user labels
			for (int iLabel = 0; iLabel < 10; iLabel++) {
				clr.addLabel(p.getId(), true, LabelType.NULL, LabelDomain.USER, "label-" + iLabel, 0);
			}

			// update milestone label
			CardLabel milestoneLabel = clr.findLabelByName(p.getId(), SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);
			for (int j = 0; j < MILESTONES_PER_PROJECT; j++) {
				milestonesIds.add(clr.addLabelListValue(milestoneLabel.getId(), Integer.toString(j)).getId());
			}
		}
		System.out.println("end creation");

		System.out.println("creating boards");
		for (Project project : projects) {
			buildBoards(project, ac, users, milestonesIds);
		}
		System.out.println("end creation");

		long processed = ccs.getCreatedCardCounter().get();
		while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
			long current = ccs.getCreatedCardCounter().get();
			System.err.println("processed: " + (current - processed) + " cards in 10s");
			processed = current;
		}

		ac.close();
	}

	/**
	 * 10 boards
	 */
	private static void buildBoards(final Project project, final ApplicationContext ac, final List<User> users,
			final List<Integer> milestonesIds) {
		final BoardRepository br = ac.getBean(BoardRepository.class);
		for (int i = 0; i < BOARDS_PER_PROJECT; i++) {
			final int boardNumber = i;

			Board b = br.createNewBoard("load test board " + boardNumber, "BOARD_" + project.getId() + "_"
					+ boardNumber, "Description", project.getId());
			buildBoard(project, b, ac, users, milestonesIds);

		}
	}

	/**
	 * 5 columns for 1 board. 600 card in archive/trash/backlog (200 each)
	 */
	private static void buildBoard(final Project project, Board board, final ApplicationContext ac,
			final List<User> users, final List<Integer> milestonesIds) {
		BoardColumnRepository columnRepo = ac.getBean(BoardColumnRepository.class);
		final CardService cs = ac.getBean(CardService.class);
		ProjectService ps = ac.getBean(ProjectService.class);

		final CreateCardService ccs = ac.getBean(CreateCardService.class);

		//

		//

		List<BoardColumnDefinition> colDefs = ps.findColumnDefinitionsByProjectId(project.getId());

		for (int i = 0; i < COLUMNS_PER_BOARD; i++) {
			BoardColumnDefinition colDef = colDefs.get(RandomUtils.nextInt(0, colDefs.size()));
			BoardColumn bc = columnRepo.addColumnToBoard("col-" + i, colDef.getId(), BoardColumnLocation.BOARD,
					board.getId());
			buildCards(project, bc, ac, users, milestonesIds);
		}

		// build 200 cards for archive, backlog, trash
		for (BoardColumnLocation bcl : EnumSet.of(BoardColumnLocation.ARCHIVE, BoardColumnLocation.BACKLOG,
				BoardColumnLocation.TRASH)) {
			final BoardColumn defaultColFor = columnRepo.findDefaultColumnFor(board.getId(), bcl);
			for (int i = 0; i < CARDS_PER_ARCHIVE_BACKLOG_TRASH; i++) {
				final int cardCount = i;
				executor.submit(new Runnable() {
					@Override
					public void run() {
						ccs.createCardData(project, defaultColFor, users, cs, cardCount, ac, milestonesIds);
					}
				});

			}
		}
	}

	/**
	 * 30 card for column in board.
	 *
	 * @param bc
	 * @param ac
	 */
	private static void buildCards(final Project p, final BoardColumn bc, final ApplicationContext ac,
			final List<User> users, final List<Integer> milestonesIds) {
		final CardService cs = ac.getBean(CardService.class);
		final CreateCardService ccs = ac.getBean(CreateCardService.class);
		for (int i = 0; i < CARDS_PER_COLUMN_IN_BOARD; i++) {
			final int cardCount = i;
			executor.submit(new Runnable() {
				@Override
				public void run() {
					ccs.createCardData(p, bc, users, cs, cardCount, ac, milestonesIds);
				}
			});
		}
	}

	private static User randomUser(List<User> users) {
		return users.get(RandomUtils.nextInt(0, users.size()));
	}

	@Service
	@Transactional
	public static class CreateCardService {

		public final AtomicLong createdCardCounter = new AtomicLong();

		/**
		 * Add 20 comments
		 */
		public void createCardData(final Project p, final BoardColumn bc, final List<User> users, final CardService cs,
				final int cardCount, final ApplicationContext ac, final List<Integer> milestonesIds) {
			Card c = cs.createCard("card-" + cardCount, bc.getId(), new Date(), randomUser(users));
			CardDataService cds = ac.getBean(CardDataService.class);
			CardLabelRepository clr = ac.getBean(CardLabelRepository.class);

			for (int ic = 0; ic < 20; ic++) {
				cds.createComment(c.getId(), commentGen(), new Date(), randomUser(users).getId());
			}

			// description
			cds.updateDescription(c.getId(), commentGen(), new Date(), randomUser(users).getId());

			// milestone
			CardLabel clMilestone = clr.findLabelByName(p.getId(), SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);
			clr.addLabelValueToCard(
					clMilestone,
					c.getId(),
					new LabelValue(null, null, null, null, null, milestonesIds.get(RandomUtils.nextInt(0,
							MILESTONES_PER_PROJECT))));

			// assigned user
			CardLabel assigned = clr.findLabelByName(p.getId(), SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM);
			int userId1 = randomUser(users).getId();
			clr.addLabelValueToCard(assigned, c.getId(), new LabelValue(null, null, null, null, userId1, null));
			int userId2 = randomUser(users).getId();
			if (userId2 != userId1) {
				clr.addLabelValueToCard(assigned, c.getId(), new LabelValue(null, null, null, null, userId2, null));
			}

			// watched by user
			CardLabel watchedBy = clr.findLabelByName(p.getId(), SYSTEM_LABEL_WATCHED_BY, LabelDomain.SYSTEM);
			clr.addLabelValueToCard(watchedBy, c.getId(), new LabelValue(null, null, null, null, randomUser(users)
					.getId(), null));

			// label 1, 2
			CardLabel l1 = clr.findLabelByName(p.getId(), "label-" + RandomUtils.nextInt(0, 10), LabelDomain.USER);
			clr.addLabelValueToCard(l1, c.getId(), new LabelValue());
			CardLabel l2 = clr.findLabelByName(p.getId(), "label-" + RandomUtils.nextInt(0, 10), LabelDomain.USER);
			if (l1.getId() != l2.getId()) {
				clr.addLabelValueToCard(l2, c.getId(), new LabelValue());
			}

			// action list
			CardData actionList1 = cds.createActionList(c.getId(), "action list 1", randomUser(users).getId(),
                new Date());
			CardData actionList2 = cds.createActionList(c.getId(), "action list 2", randomUser(users).getId(),
                new Date());
			for (int ia = 0; ia < 5; ia++) {
				cds.createActionItem(c.getId(), actionList1.getId(), "item" + ia, randomUser(users).getId(), new Date());
				cds.createActionItem(c.getId(), actionList2.getId(), "item" + ia, randomUser(users).getId(), new Date());
			}

			createdCardCounter.incrementAndGet();

		}

		public AtomicLong getCreatedCardCounter() {
			return createdCardCounter;
		}
	}
}
