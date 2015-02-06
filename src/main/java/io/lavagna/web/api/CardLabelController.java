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

import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Event;
import io.lavagna.model.Label;
import io.lavagna.model.LabelListValue;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.User;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.CardRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.LabelService;
import io.lavagna.service.ProjectService;
import io.lavagna.web.helper.ExpectPermission;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardLabelController {

	private final CardRepository cardRepository;
	private final LabelService labelService;
	private final CardLabelRepository cardLabelRepository;
	private final EventEmitter eventEmitter;
	private final ProjectService projectService;

	@Autowired
	public CardLabelController(CardRepository cardRepository, ProjectService projectService, LabelService labelService,
			CardLabelRepository cardLabelRepository, EventEmitter eventEmitter) {
		this.cardRepository = cardRepository;
		this.labelService = labelService;
		this.cardLabelRepository = cardLabelRepository;
		this.eventEmitter = eventEmitter;
		this.projectService = projectService;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/labels", method = RequestMethod.GET)
	public Map<Integer, CardLabel> findLabelsByProjectId(@PathVariable("projectShortName") String projectShortName) {
		Map<Integer, CardLabel> res = new TreeMap<>();
		Project project = projectService.findByShortName(projectShortName);
		for (CardLabel cl : cardLabelRepository.findLabelsByProject(project.getId())) {
			res.put(cl.getId(), cl);
		}
		return res;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/label-values", method = RequestMethod.GET)
	public Map<Integer, List<CardLabelValue>> findCardLabelValuesByCardId(@PathVariable("cardId") int cardId) {
		return from(cardLabelRepository.findCardLabelValuesByCardId(cardId));
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/labels", method = RequestMethod.POST)
	public CardLabel addLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody Label label) {
		Project project = projectService.findByShortName(projectShortName);
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), label.isUnique(), label.getType(),
				LabelDomain.USER, label.getName(), label.getColor());
		eventEmitter.emitAddLabel(project.getShortName());
		return cl;
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}/use-count", method = RequestMethod.GET)
	public int labelUseCount(@PathVariable("labelId") int labelId) {
		return cardLabelRepository.labelUsedCount(labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}", method = RequestMethod.POST)
	public void updateLabel(@PathVariable("labelId") int labelId, @RequestBody Label label) {
		CardLabel cl = cardLabelRepository.updateLabel(labelId, label);
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/system-label/{labelId}", method = RequestMethod.POST)
	public void updateSystemLabel(@PathVariable("labelId") int labelId, @RequestBody Label label) {
		CardLabel cl = cardLabelRepository.updateSystemLabel(labelId, label);
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}", method = RequestMethod.DELETE)
	public void removeLabel(@PathVariable("labelId") int labelId) {

		CardLabel cl = cardLabelRepository.findLabelById(labelId);
		cardLabelRepository.removeLabel(labelId);
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitDeleteLabel(project.getShortName(), labelId);
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/card/{cardId}/label-value", method = RequestMethod.POST)
	public void addLabelValueToCard(@PathVariable("cardId") int cardId,
			@RequestBody LabelIdAndLabelValue labelIdAndLabelValue, User user) {

		// ensure that the project from the card id is the same as the one from the label id
		Validate.isTrue(projectService.findRelatedProjectShortNameByCardId(cardId).equals(
				projectService.findRelatedProjectShortNameByLabelId(labelIdAndLabelValue.labelId)));

		labelService.addLabelValueToCard(labelIdAndLabelValue.labelId, cardId, labelIdAndLabelValue.labelValue, user,
				new Date());

		CardFull card = cardRepository.findFullBy(cardId);
		eventEmitter.emitAddLabelValueToCard(card.getProjectShortName(), card.getColumnId(), cardId);
	}

	@Getter
	@Setter
	public static class LabelIdAndLabelValue {
		private int labelId;
		private LabelValue labelValue;
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/card-label-value/{labelValueId}", method = RequestMethod.POST)
	public void updateLabelValue(@PathVariable("labelValueId") int labelValueId, @RequestBody LabelValue labelValue,
			User user) {

		CardLabelValue cardLabelValue = cardLabelRepository.findLabelValueById(labelValueId);
		CardLabel cl = cardLabelRepository.findLabelById(cardLabelValue.getLabelId());

		labelService.updateLabelValue(cardLabelValue.newValue(cl.getType(), labelValue), user, new Date());

		CardFull card = cardRepository.findFullBy(cardLabelValue.getCardId());
		eventEmitter.emitUpdateLabelValue(card.getProjectShortName(), card.getColumnId(), cardLabelValue.getCardId());
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/card-label-value/{labelValueId}", method = RequestMethod.DELETE)
	public Event removeLabelValue(@PathVariable("labelValueId") int labelValueId, User user) {

		CardLabelValue cardLabelValue = cardLabelRepository.findLabelValueById(labelValueId);

		Event res = labelService.removeLabelValue(cardLabelValue, user, new Date());

		CardFull card = cardRepository.findFullBy(cardLabelValue.getCardId());
		eventEmitter.emitRemoveLabelValue(card.getProjectShortName(), card.getColumnId(), cardLabelValue.getCardId());

		return res;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/label/{labelId}/label-list-values", method = RequestMethod.GET)
	public List<LabelListValue> findLabelListValues(@PathVariable("labelId") int labelId) {
		return cardLabelRepository.findListValuesByLabelId(labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}/label-list-values", method = RequestMethod.POST)
	public void addLabelListValue(@PathVariable("labelId") int labelId, @RequestBody ListValue labelListValue) {

		cardLabelRepository.addLabelListValue(labelId, labelListValue.value);

		CardLabel cl = cardLabelRepository.findLabelById(labelId);
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}", method = RequestMethod.DELETE)
	public void removeLabelListValue(@PathVariable("labelListValueId") int labelListValueId) {

		LabelListValue labelListValue = cardLabelRepository.findListValueById(labelListValueId);
		cardLabelRepository.removeLabelListValue(labelListValueId);

		CardLabel cl = cardLabelRepository.findLabelById(labelListValue.getCardLabelId());
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelListValue.getCardLabelId());
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}", method = RequestMethod.POST)
	public void updateLabelListValue(@PathVariable("labelListValueId") int labelListValueId,
			@RequestBody LabelListValue newLabelListValue) {

		LabelListValue labelListValue = cardLabelRepository.findListValueById(labelListValueId);

		cardLabelRepository.updateLabelListValue(labelListValue.newValue(newLabelListValue.getValue()));

		CardLabel cl = cardLabelRepository.findLabelById(labelListValue.getCardLabelId());
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelListValue.getCardLabelId());
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}/label-list-values/swap", method = RequestMethod.POST)
	public void swapLabelListValues(@PathVariable("labelId") int labelId, @RequestBody SwapListValue swapListValue) {

		LabelListValue llv1 = cardLabelRepository.findListValueById(swapListValue.first);
		LabelListValue llv2 = cardLabelRepository.findListValueById(swapListValue.second);
		CardLabel cl = cardLabelRepository.findLabelById(labelId);

		//
		Validate.isTrue(cl.getId() == llv1.getCardLabelId() && llv1.getCardLabelId() == llv2.getCardLabelId());
		//

		cardLabelRepository.swapLabelListValues(swapListValue.first, swapListValue.second);

		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
	}

	@Getter
	@Setter
	public static class ListValue {
		private String value;
	}

	@Getter
	@Setter
	public static class SwapListValue {
		private int first;
		private int second;
	}

	private static Map<Integer, List<CardLabelValue>> from(Map<CardLabel, List<CardLabelValue>> from) {
		Map<Integer, List<CardLabelValue>> res = new TreeMap<>();
		for (Entry<CardLabel, List<CardLabelValue>> kv : from.entrySet()) {
			res.put(kv.getKey().getId(), kv.getValue());
		}
		return res;
	}
}
