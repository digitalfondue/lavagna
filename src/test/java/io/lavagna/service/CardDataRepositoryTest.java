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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CardDataRepositoryTest {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardDataRepository cardDataRepo;

	private Card card1;

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		User user = userRepository.findUserByName("test", "test-user");

		Project project = projectService.create("test", "TEST", "desc");
		Board board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		boardColumnRepository.addColumnToBoard("col1", definitions.get(0).getId(),
				BoardColumn.BoardColumnLocation.BOARD,
				board.getId());
		List<BoardColumn> cols = boardColumnRepository
				.findAllColumnsFor(board.getId(), BoardColumn.BoardColumnLocation.BOARD);
		BoardColumn col1 = cols.get(0);
		cardService.createCard("card1", col1.getId(), new Date(), user);
		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		card1 = cards.get(0);
	}

	@Test
	public void testFindMetadataById() {
		cardDataRepo.createData(card1.getId(), CardType.COMMENT, "TEST");
		List<CardData> data = cardDataRepo.findAllDataLightByCardId(card1.getId());
		Assert.assertEquals(1, data.size());

		CardDataMetadata cdm = cardDataRepo.findMetadataById(data.get(0).getId());

		Assert.assertEquals(card1.getId(), cdm.getCardId());
		Assert.assertEquals(CardType.COMMENT, cdm.getType());
	}

	@Test
	public void testFindDataByIdsWithEmptyCollection() {
		Assert.assertEquals(0, cardDataRepo.findDataByIds(new ArrayList<Integer>()).size());
	}
}
