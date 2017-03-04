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
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CardLabelRepositoryTest {

	private final int SYSTEM_LABELS = 4;

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

	@Test
	public void testFindLabelByName() {
		String milestoneName = SYSTEM_LABEL_MILESTONE;
		Assert.assertEquals(milestoneName,
				cardLabelRepository.findLabelByName(project.getId(), milestoneName, CardLabel.LabelDomain.SYSTEM)
						.getName());
	}

	@Test
	public void testFindLabelByNameWith2Projects() {
		projectService.create("test2", "TEST2", "desc");
		String milestoneName = SYSTEM_LABEL_MILESTONE;
		Assert.assertEquals(milestoneName,
				cardLabelRepository.findLabelByName(project.getId(), milestoneName, CardLabel.LabelDomain.SYSTEM)
						.getName());
	}

	@Test(expected = DuplicateKeyException.class)
	public void testAddDuplicateNameLabel() {
		cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING, CardLabel.LabelDomain.USER,
				"label1", 0);
		cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING, CardLabel.LabelDomain.USER,
				"label1", 0);
	}

	@Test
	public void testRemoveLabel() {
		Assert.assertEquals(SYSTEM_LABELS, cardLabelRepository.findLabelsByProject(project.getId()).size());
		CardLabel inserted = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
				CardLabel.LabelDomain.USER, "label1", 0);
		Assert.assertEquals(SYSTEM_LABELS + 1, cardLabelRepository.findLabelsByProject(project.getId()).size());
		cardLabelRepository.removeLabel(inserted.getId());
		Assert.assertEquals(SYSTEM_LABELS, cardLabelRepository.findLabelsByProject(project.getId()).size());
	}

	@Test
	public void testUpdateLabel() {
		CardLabel inserted = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
				CardLabel.LabelDomain.USER, "label1", 0);
		Assert.assertEquals("label1", inserted.getName());
		Assert.assertEquals(0, inserted.getColor());

		Label label = new Label("label-new", false, inserted.getType(), 0xffffff);
		cardLabelRepository.updateLabel(inserted.getId(), label);

		CardLabel cl2 = cardLabelRepository.findLabelById(inserted.getId());

		Assert.assertEquals("label-new", cl2.getName());
		Assert.assertEquals(0xffffff, cl2.getColor());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongUpdateSystemLabel() {
		CardLabel randomSystemLabel = cardLabelRepository.findLabelsByProject(project.getId()).get(0);
		Label label = new Label("label-new", false, randomSystemLabel.getType(), 0xffffff);

		cardLabelRepository.updateLabel(randomSystemLabel.getId(), label);
	}

	@Test
	public void testUpdateSystemLabel() {
		CardLabel randomSystemLabel = cardLabelRepository.findLabelsByProject(project.getId()).get(0);
		Label label = new Label("label-new", false, randomSystemLabel.getType(), 0xffffff);

		cardLabelRepository.updateSystemLabel(randomSystemLabel.getId(), label);

		CardLabel cl2 = cardLabelRepository.findLabelById(randomSystemLabel.getId());
		Assert.assertEquals("label-new", cl2.getName());
		Assert.assertEquals(0xffffff, cl2.getColor());
	}

	/**
	 * Cannot change a label type when already defined
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateLabelWithValuesAndNewType() {
		CardLabel inserted = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
				CardLabel.LabelDomain.USER, "label1", 0);

		Assert.assertEquals(0, cardLabelRepository.findCardLabelValuesByCardId(card.getId()).size());
		Assert.assertEquals(0, cardLabelRepository.labelUsedCount(inserted.getId()));

		cardLabelRepository.addLabelValueToCard(inserted, card.getId(), new CardLabelValue.LabelValue("my string"));

		Assert.assertEquals(1, cardLabelRepository.labelUsedCount(inserted.getId()));

		Assert.assertEquals(1, cardLabelRepository.findCardLabelValuesByCardId(card.getId()).size());

		Label label = new Label("label-new", false, inserted.getType(), inserted.getColor());
		cardLabelRepository.updateLabel(inserted.getId(), label);

		Assert.assertEquals(1, cardLabelRepository.findCardLabelValuesByCardId(card.getId()).size());

		label = new Label("label-new-2", false, CardLabel.LabelType.TIMESTAMP, inserted.getColor());
		cardLabelRepository.updateLabel(inserted.getId(), label);
	}

	@Test
    public void testFindLabelValueByLabelAndValue() {
	    CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
                CardLabel.LabelDomain.USER, "label1", 0);

	    CardLabelValue value = cardLabelRepository.addLabelValueToCard(label, card.getId(), new CardLabelValue.LabelValue("my string"));

	    List<CardLabelValue> found = cardLabelRepository.findLabelValueByLabelAndValue(card.getId(), label, new CardLabelValue.LabelValue("my string"));

	    Assert.assertEquals(1, found.size());

	    Assert.assertEquals(value.getCardLabelValueId(), found.get(0).getCardLabelValueId());
	    Assert.assertEquals(value.getValue(), found.get(0).getValue());
    }

	@Test
	public void testFindUserLabelNameByGlobalRead() {
	    CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
                CardLabel.LabelDomain.USER, "label1", 0);

	    UserWithPermission uwpGlobalRead = new UserWithPermission(user, EnumSet.of(Permission.READ),
	            Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

	    List<CardLabel> foundLabels = cardLabelRepository.findUserLabelNameBy("label1", project.getId(), uwpGlobalRead);
	    Assert.assertEquals(1, foundLabels.size());
	    Assert.assertEquals(label.getName(), foundLabels.get(0).getName());

        List<CardLabel> foundLabels2 = cardLabelRepository.findUserLabelNameBy("label1", null, uwpGlobalRead);
        Assert.assertEquals(1, foundLabels2.size());
        Assert.assertEquals(label.getName(), foundLabels2.get(0).getName());
	}

	@Test
    public void testFindUserLabelNameByProjectRead() {

	    CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING,
                CardLabel.LabelDomain.USER, "label1", 0);

	    UserWithPermission uwpProjectRead = new UserWithPermission(user, EnumSet.noneOf(Permission.class),
                Collections.<String, Set<Permission>>singletonMap(project.getShortName(), EnumSet.of(Permission.READ)),
                Collections.<Integer, Set<Permission>>singletonMap(project.getId(), EnumSet.of(Permission.READ)));

	    List<CardLabel> foundLabels1 = cardLabelRepository.findUserLabelNameBy("label1", project.getId(), uwpProjectRead);
        Assert.assertEquals(1, foundLabels1.size());
        Assert.assertEquals(label.getName(), foundLabels1.get(0).getName());

        List<CardLabel> foundLabels2 = cardLabelRepository.findUserLabelNameBy("label1", null, uwpProjectRead);
        Assert.assertEquals(1, foundLabels2.size());
        Assert.assertEquals(label.getName(), foundLabels2.get(0).getName());
	}

	@Test(expected = IllegalArgumentException.class)
    public void testFindUserLabelNameByProjectNoReadWithProjectId() {

	    cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING, CardLabel.LabelDomain.USER, "label1", 0);

	    UserWithPermission noRead = new UserWithPermission(user, EnumSet.noneOf(Permission.class),
                Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());
	    cardLabelRepository.findUserLabelNameBy("label1", project.getId(), noRead);
    }


	//TODO: check use and correctness for this case
	@Test
    public void testFindUserLabelNameByProjectNoReadWithProjectIdNull() {

	    cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.STRING, LabelDomain.USER, "label1", 0);

        UserWithPermission noRead = new UserWithPermission(user, EnumSet.noneOf(Permission.class),
                Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());
        Assert.assertEquals(1, cardLabelRepository.findUserLabelNameBy("label1", null, noRead).size());
    }

	@Test
	public void testAddLabelListValue() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST, LabelDomain.USER, "listlabel", 0);

		Assert.assertEquals(0, cardLabelRepository.findListValuesByLabelId(label.getId()).size());

		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "123");
		cardLabelRepository.addLabelValueToCard(label, card.getId(), new CardLabelValue.LabelValue(null, null, null,
				null, null, llv.getId()));

		cardLabelRepository.createLabelListMetadata(llv.getId(), "MY_KEY", "MY_METADATA");

		Assert.assertEquals(1, cardLabelRepository.findListValuesByLabelId(label.getId()).size());
		Assert.assertEquals("123", cardLabelRepository.findListValuesByLabelId(label.getId()).get(0).getValue());

		Assert.assertEquals("MY_METADATA", cardLabelRepository.findListValuesByLabelId(label.getId()).get(0).getMetadata().get("MY_KEY"));

		cardLabelRepository.updateLabelListValue(llv.newValue("MY_NEW_VALUE"));

		Assert.assertEquals("MY_NEW_VALUE", cardLabelRepository.findListValuesByLabelId(label.getId()).get(0).getValue());


		//
		UserWithPermission uwpGlobalRead = new UserWithPermission(user, EnumSet.of(Permission.READ),
                Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());
		Assert.assertEquals(1, cardLabelRepository.findListValuesBy(LabelDomain.USER, "listlabel", "MY_NEW_VALUE", project.getId(), uwpGlobalRead).size());
		Assert.assertEquals("MY_NEW_VALUE", cardLabelRepository.findListValuesBy(LabelDomain.USER, "listlabel", "MY_NEW_VALUE", project.getId(), uwpGlobalRead).get(0));


		Assert.assertEquals(1, cardLabelRepository.findListValuesBy(LabelDomain.USER, "listlabel", "MY_NEW_VALUE", null, uwpGlobalRead).size());
        Assert.assertEquals("MY_NEW_VALUE", cardLabelRepository.findListValuesBy(LabelDomain.USER, "listlabel", "MY_NEW_VALUE", null, uwpGlobalRead).get(0));
	}

	@Test
	public void testFindListValueById() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);
		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "1");

		Assert.assertEquals("1", cardLabelRepository.findListValueById(llv.getId()).getValue());
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testFindListValueByIdWithBadId() {
		Assert.assertEquals("1", cardLabelRepository.findListValueById(-3).getValue());
	}

	@Test
	public void testRemoveLabelListValue() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);
		LabelListValue v1 = cardLabelRepository.addLabelListValue(label.getId(), "1");

		Assert.assertEquals(1, cardLabelRepository.findListValuesByLabelId(label.getId()).size());

		cardLabelRepository.removeLabelListValue(v1.getId());

		Assert.assertEquals(0, cardLabelRepository.findListValuesByLabelId(label.getId()).size());
	}

	@Test
	public void testSwapListLabel() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);

		LabelListValue v1 = cardLabelRepository.addLabelListValue(label.getId(), "1");
		LabelListValue v2 = cardLabelRepository.addLabelListValue(label.getId(), "2");
		List<LabelListValueWithMetadata> values = cardLabelRepository.findListValuesByLabelId(label.getId());
		Assert.assertEquals(1, values.get(0).getOrder());
		Assert.assertEquals("1", values.get(0).getValue());
		Assert.assertEquals(2, values.get(1).getOrder());
		Assert.assertEquals("2", values.get(1).getValue());

		cardLabelRepository.swapLabelListValues(v1.getId(), v2.getId());

		values = cardLabelRepository.findListValuesByLabelId(label.getId());
		Assert.assertEquals(1, values.get(0).getOrder());
		Assert.assertEquals("2", values.get(0).getValue());
		Assert.assertEquals(2, values.get(1).getOrder());
		Assert.assertEquals("1", values.get(1).getValue());
	}

    @Test
    public void testMoveLabelListValueToOrder() {
        CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
            CardLabel.LabelDomain.USER, "listlabel", 0);

        LabelListValue v1 = cardLabelRepository.addLabelListValue(label.getId(), "1");
        LabelListValue v2 = cardLabelRepository.addLabelListValue(label.getId(), "2");
        List<LabelListValueWithMetadata> values = cardLabelRepository.findListValuesByLabelId(label.getId());
        Assert.assertEquals(1, values.get(0).getOrder());
        Assert.assertEquals("1", values.get(0).getValue());
        Assert.assertEquals(2, values.get(1).getOrder());
        Assert.assertEquals("2", values.get(1).getValue());

        cardLabelRepository.moveLabelListValueToOrder(v1.getId(), v2.getOrder());

        values = cardLabelRepository.findListValuesByLabelId(label.getId());
        Assert.assertEquals(1, values.get(0).getOrder());
        Assert.assertEquals("2", values.get(0).getValue());
        Assert.assertEquals(2, values.get(1).getOrder());
        Assert.assertEquals("1", values.get(1).getValue());
    }

	@Test
	public void testCountLabelListValueUse() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);
		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "1");

		Assert.assertEquals(0, cardLabelRepository.countLabeListValueUse(llv.getId()));
		cardLabelRepository.addLabelValueToCard(label, card.getId(), new LabelValue(null, null, null, null, null, llv.getId()));
		Assert.assertEquals(1, cardLabelRepository.countLabeListValueUse(llv.getId()));
	}

	@Test
	public void testLabelListValueMapping() {


		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);

		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "listvalue");
		Assert.assertTrue(cardLabelRepository.findLabelListValueMapping(Collections.<String>emptyList()).isEmpty());
		Assert.assertFalse(cardLabelRepository.findLabelListValueMapping(Collections.singletonList("listvalue")).isEmpty());

		cardLabelRepository.addLabelValueToCard(label, card.getId(), new LabelValue(null, null, null, null, null, llv.getId()));

		Map<String, Map<Integer, Integer>> mapping = cardLabelRepository.findLabelListValueMapping(Collections.singletonList("listvalue"));
		Assert.assertTrue(mapping.containsKey("listvalue"));
		Assert.assertTrue(mapping.get("listvalue").containsKey(label.getId()));
		Assert.assertEquals(Integer.valueOf(llv.getId()), mapping.get("listvalue").get(label.getId()));

	}

	@Test
	public void testHandleLabelListValueMetadata() {
		CardLabel label = cardLabelRepository.addLabel(project.getId(), false, CardLabel.LabelType.LIST,
				CardLabel.LabelDomain.USER, "listlabel", 0);
		LabelListValue llv = cardLabelRepository.addLabelListValue(label.getId(), "1");

		Assert.assertTrue(cardLabelRepository.findListValueMetadataByLabelListValueId(llv.getId()).isEmpty());

		//creation
		cardLabelRepository.createLabelListMetadata(llv.getId(), "KEY", "VALUE");
		List<ListValueMetadata> metadatas = cardLabelRepository.findListValueMetadataByLabelListValueId(llv.getId());
		Assert.assertEquals(1, metadatas.size());
		ListValueMetadata metadata = metadatas.get(0);
		Assert.assertEquals("KEY", metadata.getKey());
		Assert.assertEquals("VALUE", metadata.getValue());

		//update
		cardLabelRepository.updateLabelListMetadata(new ListValueMetadata(metadata.getLabelListValueId(), metadata.getKey(), "NEW_VALUE"));
		List<ListValueMetadata> metadatas2 = cardLabelRepository.findListValueMetadataByLabelListValueId(llv.getId());
		Assert.assertEquals(1, metadatas2.size());
		ListValueMetadata metadata2 = metadatas2.get(0);
		Assert.assertEquals("KEY", metadata2.getKey());
		Assert.assertEquals("NEW_VALUE", metadata2.getValue());

		//delete
		cardLabelRepository.removeLabelListMetadata(metadata2.getLabelListValueId(), metadata2.getKey());
		Assert.assertTrue(cardLabelRepository.findListValueMetadataByLabelListValueId(llv.getId()).isEmpty());
	}
}
