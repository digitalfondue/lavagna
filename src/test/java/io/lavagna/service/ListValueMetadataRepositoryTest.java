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
import io.lavagna.query.ListValueMetadataQuery;
import io.lavagna.service.config.TestServiceConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class ListValueMetadataRepositoryTest {

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private CardLabelRepository cardLabelRepository;

	@Autowired
	private ListValueMetadataQuery listValueMetadataQuery;

	private Project project;

	private Board board;

	private BoardColumn column;

	private User user;

	private Card card;

	@Before
	public void setUpBoard() {
		Helper.createUser(userRepository, "test", "label");
		user = userRepository.findUserByName("test", "label");
		project = projectService.create("test", "TEST", "desc");
		board = boardRepository.createNewBoard("test-label", "LABEL", "label", projectService.findByShortName("TEST")
				.getId());
		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		column = boardColumnRepository.addColumnToBoard("label-column", definitions.get(0).getId(),
				BoardColumn.BoardColumnLocation.BOARD, board.getId());
		card = cardService.createCard("card", column.getId(), new Date(), user);
	}

	private Pair<LabelListValue, LabelListValue> createLabelListValue() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);
		Assert.assertEquals(0, cardLabelRepository.findListValuesByLabelId(label.getId()).size());
		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "1");
		cardLabelRepository.addLabelValueToCard(label, card.getId(), new CardLabelValue.LabelValue(null, null, null,
				null, null, llv.getId()));


		LabelListValue llv2 = cardLabelRepository.addLabelListValue(label.getId(), "2");
		cardLabelRepository.addLabelValueToCard(label, card.getId(), new CardLabelValue.LabelValue(null, null, null,
				null, null, llv2.getId()));


		return Pair.of(llv, llv2);
	}

	@Test
	public void listValueMetadataLifecycleTest() {

		LabelListValue llv = createLabelListValue().getLeft();


		Assert.assertTrue(listValueMetadataQuery.findByLabelListValueId(llv.getId()).isEmpty());

		listValueMetadataQuery.insert(llv.getId(), "KEY", "VALUE");
		Assert.assertFalse(listValueMetadataQuery.findByLabelListValueId(llv.getId()).isEmpty());
		ListValueMetadata lvm = listValueMetadataQuery.findByLabelListValueId(llv.getId()).get(0);
		Assert.assertEquals("KEY", lvm.getKey());
		Assert.assertEquals("VALUE", lvm.getValue());
		Assert.assertEquals(lvm, listValueMetadataQuery.findByLabelListValueIdAndKey(lvm.getLabelListValueId(), lvm.getKey()));

		listValueMetadataQuery.update(lvm.getLabelListValueId(), lvm.getKey(), "VALUE2");

		ListValueMetadata lvm2 = listValueMetadataQuery.findByLabelListValueIdAndKey(lvm.getLabelListValueId(), lvm.getKey());
		Assert.assertEquals("VALUE2", lvm2.getValue());

		listValueMetadataQuery.delete(lvm.getLabelListValueId(), lvm.getKey());

		Assert.assertTrue(listValueMetadataQuery.findByLabelListValueId(llv.getId()).isEmpty());

	}

	@Test
	public void deleteAllWithLabelIdTest() {

		LabelListValue llv = createLabelListValue().getLeft();
		Assert.assertTrue(listValueMetadataQuery.findByLabelListValueId(llv.getId()).isEmpty());

		listValueMetadataQuery.insert(llv.getId(), "KEY", "VALUE");
		listValueMetadataQuery.insert(llv.getId(), "KEY2", "VALUE");

		Assert.assertEquals(2, listValueMetadataQuery.findByLabelListValueId(llv.getId()).size());

		listValueMetadataQuery.deleteAllWithLabelListValueId(llv.getId());

		Assert.assertTrue(listValueMetadataQuery.findByLabelListValueId(llv.getId()).isEmpty());
	}

	@Test
	public void findAll() {

		Pair<LabelListValue, LabelListValue> llvp = createLabelListValue();

		List<Integer> labelListValueIds = Arrays.asList(llvp.getLeft().getId(), llvp.getRight().getId());

		Assert.assertEquals(0, listValueMetadataQuery.findByLabelListValueIds(labelListValueIds).size());

		listValueMetadataQuery.insert(llvp.getLeft().getId(), "KEY", "VALUE");

		Assert.assertEquals(1, listValueMetadataQuery.findByLabelListValueIds(labelListValueIds).size());

		listValueMetadataQuery.insert(llvp.getRight().getId(), "KEY", "VALUE");

		Assert.assertEquals(2, listValueMetadataQuery.findByLabelListValueIds(labelListValueIds).size());
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testDeleteCascade() {
		LabelListValue llv = createLabelListValue().getLeft();
		listValueMetadataQuery.insert(llv.getId(), "KEY", "VALUE");
		listValueMetadataQuery.insert(llv.getId(), "KEY2", "VALUE");

		Assert.assertEquals(2, listValueMetadataQuery.findByLabelListValueId(llv.getId()).size());

		cardLabelRepository.removeLabelListValue(llv.getId());
	}

}
