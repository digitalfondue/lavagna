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
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.service.BulkOperationService;
import io.lavagna.service.CardRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.web.helper.ExpectPermission;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BulkOperationLabelController {

	private final BulkOperationService bulkOperationService;
	private final CardRepository cardRepository;
	private final EventEmitter eventEmitter;

	@Autowired
	public BulkOperationLabelController(BulkOperationService bulkOperationService, CardRepository cardRepository,
			EventEmitter eventEmitter) {
		this.bulkOperationService = bulkOperationService;
		this.cardRepository = cardRepository;
		this.eventEmitter = eventEmitter;
	}

	/**
	 * ASSIGN a user to the cards if it's not present.
	 * 
	 * @param projectShortName
	 * @param op
	 * @param user
	 */
	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/assign", method = RequestMethod.POST)
	public void assign(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.assign(projectShortName, op.cardIds, op.value, user);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-assign", method = RequestMethod.POST)
	public void removeAssign(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeAssign(projectShortName, op.cardIds, op.value, user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/re-assign", method = RequestMethod.POST)
	public void reAssign(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.reAssign(projectShortName, op.cardIds, op.value, user);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/set-due-date", method = RequestMethod.POST)
	public void setDueDate(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		ImmutablePair<List<Integer>, List<Integer>> updatedAndAdded = bulkOperationService.setDueDate(projectShortName,
				op.cardIds, op.value, user);
		eventEmitter.emitUpdateOrAddValueToCards(cardRepository.findAllByIds(updatedAndAdded.getLeft()),
				cardRepository.findAllByIds(updatedAndAdded.getRight()));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-due-date", method = RequestMethod.POST)
	public void removeDueDate(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeDueDate(projectShortName, op.cardIds, user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected));
	}
	
	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/watch", method = RequestMethod.POST)
	public void watch(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.watch(projectShortName, op.cardIds, user);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected));
	}
	
	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-watch", method = RequestMethod.POST)
	public void unWatch(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeWatch(projectShortName, op.cardIds, user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/set-milestone", method = RequestMethod.POST)
	public void setMilestone(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		ImmutablePair<List<Integer>, List<Integer>> updatedAndAdded = bulkOperationService.setMilestone(
				projectShortName, op.cardIds, op.value, user);
		eventEmitter.emitUpdateOrAddValueToCards(cardRepository.findAllByIds(updatedAndAdded.getLeft()),
				cardRepository.findAllByIds(updatedAndAdded.getRight()));
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-milestone", method = RequestMethod.POST)
	public void removeMilestone(@PathVariable("projectShortName") String projectShortName,
			@RequestBody BulkOperation op, User user) {
		List<Integer> affected = bulkOperationService.removeMilestone(projectShortName, op.cardIds, user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected));
	}
	
	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/add-label", method = RequestMethod.POST)
	public void addLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.addUserLabel(projectShortName, op.labelId, op.value, op.cardIds, user);
		eventEmitter.emitUpdateOrAddValueToCards(Collections.<CardFull>emptyList(), cardRepository.findAllByIds(affected));
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-label", method = RequestMethod.POST)
	public void removeLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeUserLabel(projectShortName, op.labelId, op.value, op.cardIds, user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected));
	}

	
	@Getter
	@Setter
	public static class BulkOperation {
		private Integer labelId;// can be null
		private LabelValue value;// can be null
		private List<Integer> cardIds;
	}

}
