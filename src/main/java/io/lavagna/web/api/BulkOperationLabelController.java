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
import io.lavagna.web.api.model.BulkOperation;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static io.lavagna.common.Constants.*;

@RestController
public class BulkOperationLabelController {

	private final BulkOperationService bulkOperationService;
	private final CardRepository cardRepository;
	private final EventEmitter eventEmitter;


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
		List<Integer> affected = bulkOperationService.assign(projectShortName, op.getCardIds(), op.getValue(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_ASSIGNED);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-assign", method = RequestMethod.POST)
	public void removeAssign(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeAssign(projectShortName, op.getCardIds(), op.getValue(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_ASSIGNED);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/re-assign", method = RequestMethod.POST)
	public void reAssign(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.reAssign(projectShortName, op.getCardIds(), op.getValue(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_ASSIGNED);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/set-due-date", method = RequestMethod.POST)
	public void setDueDate(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		ImmutablePair<List<Integer>, List<Integer>> updatedAndAdded = bulkOperationService.setDueDate(projectShortName,
				op.getCardIds(), op.getValue(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_DUE_DATE);
		eventEmitter.emitUpdateOrAddValueToCards(cardRepository.findAllByIds(updatedAndAdded.getLeft()),
				cardRepository.findAllByIds(updatedAndAdded.getRight()), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-due-date", method = RequestMethod.POST)
	public void removeDueDate(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeDueDate(projectShortName, op.getCardIds(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_DUE_DATE);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected), labelId, null, user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/watch", method = RequestMethod.POST)
	public void watch(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.watch(projectShortName, op.getCardIds(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_WATCHED_BY);
		eventEmitter.emitAddLabelValueToCards(cardRepository.findAllByIds(affected), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-watch", method = RequestMethod.POST)
	public void unWatch(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeWatch(projectShortName, op.getCardIds(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_WATCHED_BY);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected), labelId, new LabelValue(user.getId()), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/set-milestone", method = RequestMethod.POST)
	public void setMilestone(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		ImmutablePair<List<Integer>, List<Integer>> updatedAndAdded = bulkOperationService.setMilestone(
				projectShortName, op.getCardIds(), op.getValue(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_MILESTONE);
		eventEmitter.emitUpdateOrAddValueToCards(cardRepository.findAllByIds(updatedAndAdded.getLeft()),
				cardRepository.findAllByIds(updatedAndAdded.getRight()), labelId, op.getValue(), user);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-milestone", method = RequestMethod.POST)
	public void removeMilestone(@PathVariable("projectShortName") String projectShortName,
			@RequestBody BulkOperation op, User user) {
		List<Integer> affected = bulkOperationService.removeMilestone(projectShortName, op.getCardIds(), user);
		int labelId = bulkOperationService.findIdForSystemLabel(projectShortName, SYSTEM_LABEL_MILESTONE);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected), labelId, null, user);
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/add-label", method = RequestMethod.POST)
	public void addLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.addUserLabel(projectShortName, op.getLabelId(), op.getValue(), op.getCardIds(), user);
		eventEmitter.emitUpdateOrAddValueToCards(Collections.<CardFull>emptyList(), cardRepository.findAllByIds(affected), op.getLabelId(), op.getValue(), user);
	}

	@ExpectPermission(Permission.MANAGE_LABEL_VALUE)
	@RequestMapping(value = "/api/project/{projectShortName}/bulk-op/remove-label", method = RequestMethod.POST)
	public void removeLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody BulkOperation op,
			User user) {
		List<Integer> affected = bulkOperationService.removeUserLabel(projectShortName, op.getLabelId(), op.getValue(), op.getCardIds(), user);
		eventEmitter.emitRemoveLabelValueToCards(cardRepository.findAllByIds(affected), op.getLabelId(), op.getValue(), user);
	}

}
