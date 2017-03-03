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
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ProjectService;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
public class CardLabelController {

	private final CardLabelRepository cardLabelRepository;
	private final EventEmitter eventEmitter;
	private final ProjectService projectService;


	public CardLabelController(ProjectService projectService,
			CardLabelRepository cardLabelRepository, EventEmitter eventEmitter) {
		this.cardLabelRepository = cardLabelRepository;
		this.eventEmitter = eventEmitter;
		this.projectService = projectService;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/labels", method = RequestMethod.GET)
	public Map<Integer, CardLabel> findLabelsByProjectId(@PathVariable("projectShortName") String projectShortName) {
		Map<Integer, CardLabel> res = new TreeMap<>();
		int projectId = projectService.findIdByShortName(projectShortName);
		for (CardLabel cl : cardLabelRepository.findLabelsByProject(projectId)) {
			res.put(cl.getId(), cl);
		}
		return res;
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/labels", method = RequestMethod.POST)
	public CardLabel addLabel(@PathVariable("projectShortName") String projectShortName, @RequestBody Label label) {
		int projectId = projectService.findIdByShortName(projectShortName);
		CardLabel cl = cardLabelRepository.addLabel(projectId, label.getUnique(), label.getType(),
				LabelDomain.USER, label.getName(), label.getColor());
		eventEmitter.emitAddLabel(projectShortName);
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
	@RequestMapping(value = "/api/label/{labelId}/system", method = RequestMethod.POST)
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

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/label/{labelId}/label-list-values", method = RequestMethod.GET)
	public List<LabelListValueWithMetadata> findLabelListValues(@PathVariable("labelId") int labelId) {
		return cardLabelRepository.findListValuesByLabelId(labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label/{labelId}/label-list-values", method = RequestMethod.POST)
	public void addLabelListValue(@PathVariable("labelId") int labelId, @RequestBody Value labelListValue) {

		cardLabelRepository.addLabelListValue(labelId, labelListValue.value);

		CardLabel cl = cardLabelRepository.findLabelById(labelId);
		Project project = projectService.findById(cl.getProjectId());
		eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}/count-use", method = RequestMethod.GET)
	public int countUse(@PathVariable("labelListValueId") int labelListValueId) {
		return cardLabelRepository.countLabeListValueUse(labelListValueId);
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
		eventEmitter.emitUpdateLabeListValueId(labelListValueId);
	}

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/label/{labelId}/label-list-values/move", method = RequestMethod.POST)
    public void moveLabelListValueToOrder(@PathVariable("labelId") int labelId, @RequestBody SwapListValue swapListValue) {

        CardLabel cl = cardLabelRepository.findLabelById(labelId);

        cardLabelRepository.moveLabelListValueToOrder(swapListValue.first, swapListValue.second);

        Project project = projectService.findById(cl.getProjectId());
        eventEmitter.emitUpdateLabel(project.getShortName(), labelId);
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


	// metadata
	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}/metadata", method = RequestMethod.GET)
	public List<ListValueMetadata> findLabelListValueMetadata(@PathVariable("labelListValueId") int labelListValueId) {
		return cardLabelRepository.findListValueMetadataByLabelListValueId(labelListValueId);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}/metadata/{key}", method = RequestMethod.POST)
	public void updateListValueMetadata(@PathVariable("labelListValueId") int labelListValueId, @PathVariable("key") String key, @RequestBody Value value) {
        LabelListValueWithMetadata labelListValue = cardLabelRepository.findListValueById(labelListValueId);
		CardLabel cl = cardLabelRepository.findLabelById(labelListValue.getCardLabelId());
		Project project = projectService.findById(cl.getProjectId());

        if(labelListValue.getMetadata().containsKey(key)) {
            cardLabelRepository.updateLabelListMetadata(new ListValueMetadata(labelListValueId, key, value.getValue()));
        } else {
            cardLabelRepository.createLabelListMetadata(labelListValueId, key, value.getValue());
        }
		eventEmitter.emitUpdateLabel(project.getShortName(), labelListValue.getCardLabelId());
		eventEmitter.emitUpdateLabeListValueId(labelListValueId);
	}


	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/label-list-values/{labelListValueId}/metadata/{key}", method = RequestMethod.DELETE)
	public void removeLabelListValueMetadata(@PathVariable("labelListValueId") int labelListValueId, @PathVariable("key") String key) {
		LabelListValue labelListValue = cardLabelRepository.findListValueById(labelListValueId);
		CardLabel cl = cardLabelRepository.findLabelById(labelListValue.getCardLabelId());
		Project project = projectService.findById(cl.getProjectId());

		cardLabelRepository.removeLabelListMetadata(labelListValueId, key);

		eventEmitter.emitUpdateLabel(project.getShortName(), labelListValue.getCardLabelId());
		eventEmitter.emitUpdateLabeListValueId(labelListValueId);
	}
	//

	public static class Value {
		private String value;

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

	public static class SwapListValue {
		private int first;
		private int second;

        public int getFirst() {
            return this.first;
        }

        public int getSecond() {
            return this.second;
        }

        public void setFirst(int first) {
            this.first = first;
        }

        public void setSecond(int second) {
            this.second = second;
        }
    }
}
