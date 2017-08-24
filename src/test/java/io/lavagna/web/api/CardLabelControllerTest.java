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
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.mockito.Mockito.when;

//TODO complete with verify
@RunWith(MockitoJUnitRunner.class)
public class CardLabelControllerTest {

	private final int labelId = 0;
	private final int cardId = 0;
	private final String projectShortName = "TEST";
	@Mock
	private ProjectService projectService;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private BoardColumnRepository boardColumnRepository;
	@Mock
	private LabelService labelService;
	@Mock
	private CardLabelRepository cardLabelRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private EventEmitter eventEmitter;
	@Mock
	private CardLabel cardLabel;
	@Mock
	private CardLabelValue cardLabelValue;
	@Mock
	private Board board;
	@Mock
	private CardFull card;
	@Mock
	private BoardColumn boardColumn;
	@Mock
	private User user;
	private CardLabelController cardLabelController;

	private Project project;

	@Before
	public void prepare() {
		cardLabelController = new CardLabelController(projectService,
				cardLabelRepository, eventEmitter);

		project = new Project(0, "test", "TEST", "Test Project", false);
	}

	@Test
	public void addLabelTest() {
		Label label = new Label("test", false, LabelType.NULL, 42);
		cardLabelController.addLabel(projectShortName, label);
	}

	@Test
	public void findLabelsByBoardShortNameTest() {
		cardLabelController.findLabelsByProjectId(projectShortName);
	}

	@Test
	public void removeLabelTest() {
		when(cardLabelRepository.findLabelById(labelId)).thenReturn(cardLabel);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);
		cardLabelController.removeLabel(labelId);
	}

	@Test
	public void updateLabelTest() {
		Label label = new Label("test", false, LabelType.STRING, 0);
		CardLabel cl = new CardLabel(labelId, board.getId(), false, LabelType.STRING, LabelDomain.USER, "test", 0);

		when(cardLabelRepository.updateLabel(labelId, label)).thenReturn(cl);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);

		cardLabelController.updateLabel(labelId, label);
	}

	@Test
	public void updateSystemLabelTest() {
		Label label = new Label("test", false, LabelType.STRING, 0);
		CardLabel cl = new CardLabel(labelId, board.getId(), false, LabelType.STRING, LabelDomain.SYSTEM, "test", 0);

		when(cardLabelRepository.updateSystemLabel(labelId, label)).thenReturn(cl);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);

		cardLabelController.updateSystemLabel(labelId, label);
	}

	@Test
	public void findLabelListValuesTest() {
		cardLabelController.findLabelListValues(labelId);
	}

	@Test
	public void swapLabelListValuesTest() {
		when(cardLabelRepository.findLabelById(0)).thenReturn(cardLabel);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);
		when(cardLabelRepository.findListValueById(1)).thenReturn(new LabelListValueWithMetadata(new LabelListValue(1, 0, 0, "value1"), new HashMap<String, String>()));
		when(cardLabelRepository.findListValueById(2)).thenReturn(new LabelListValueWithMetadata(new LabelListValue(2, 0, 0, "value2"), new HashMap<String, String>()));

		CardLabelController.SwapListValue v = new CardLabelController.SwapListValue();
		v.setFirst(1);
		v.setSecond(2);
		cardLabelController.swapLabelListValues(labelId, v);
	}

	@Test(expected = IllegalArgumentException.class)
	public void swapWrongLabelListValuesTest() {
		when(cardLabelRepository.findLabelById(0)).thenReturn(cardLabel);
		when(cardLabelRepository.findListValueById(1)).thenReturn(new LabelListValueWithMetadata(new LabelListValue(1, 0, 0, "value1"), new HashMap<String, String>()));
		when(cardLabelRepository.findListValueById(2)).thenReturn(new LabelListValueWithMetadata(new LabelListValue(2, 1, 0, "value2-of-another-label"), new HashMap<String, String>()));

		CardLabelController.SwapListValue v = new CardLabelController.SwapListValue();
		v.setFirst(1);
		v.setSecond(2);
		cardLabelController.swapLabelListValues(labelId, v);
	}

	@Test
	public void addLabelListValueTest() {
		when(cardLabelRepository.findLabelById(0)).thenReturn(cardLabel);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);

		cardLabelController.addLabelListValue(labelId, new CardLabelController.Value());
	}

	@Test
	public void removeLabelListValueTest() {
		LabelListValueWithMetadata llv = Mockito.mock(LabelListValueWithMetadata.class);
		when(cardLabelRepository.findListValueById(0)).thenReturn(llv);
		when(cardLabelRepository.findLabelById(llv.getCardLabelId())).thenReturn(cardLabel);
		when(projectService.findById(cardLabel.getProjectId())).thenReturn(project);

		cardLabelController.removeLabelListValue(0);
	}
}
