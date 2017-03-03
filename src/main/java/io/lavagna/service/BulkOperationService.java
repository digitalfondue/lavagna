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

import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.User;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;

import static io.lavagna.common.Constants.*;

@Service
@Transactional(readOnly = false)
public class BulkOperationService {

	private final CardRepository cardRepository;
	private final CardLabelRepository cardLabelRepository;
	private final ProjectService projectService;
	private final LabelService labelService;

	public BulkOperationService(CardRepository cardRepository, CardLabelRepository cardLabelRepository,
			LabelService labelService, ProjectService projectService) {
		this.cardRepository = cardRepository;
		this.cardLabelRepository = cardLabelRepository;
		this.labelService = labelService;
		this.projectService = projectService;
	}

	public List<Integer> assign(String projectShortName, List<Integer> cardIds, LabelValue value, User user) {

		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);
		int labelId = findBy(projectShortName, SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM).getId();

		// we remove the cards that have _already_ the user assigned
		Collection<Integer> alreadyWithUserAssigned = keepCardWithMatching(filteredCardIds,
				new FilterByLabelIdAndLabelValue(labelId, value)).keySet();
		filteredCardIds.removeAll(alreadyWithUserAssigned);
		//

		labelService.addLabelValueToCards(labelId, filteredCardIds, value, user, new Date());
		return filteredCardIds;
	}

	public List<Integer> removeAssign(String projectShortName, List<Integer> cardIds, LabelValue value, User user) {
		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);
		int labelId = findBy(projectShortName, SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM).getId();

		List<Integer> removedIds = new ArrayList<>();
		for (LabelAndValue lv : flatten(keepCardWithMatching(filteredCardIds,
				new FilterByLabelIdAndLabelValue(labelId, value)).values())) {
			labelService.removeLabelValue(lv.labelValue(), user, new Date());
			removedIds.add(lv.getLabelValueCardId());
		}

		return removedIds;
	}

	public List<Integer> reAssign(String projectShortName, List<Integer> cardIds, LabelValue value, User user) {
		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);
		int labelId = findBy(projectShortName, SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM).getId();

		// remove all assigned labels
		for (LabelAndValue lv : flatten(keepCardWithMatching(filteredCardIds, new FilterByLabelId(labelId)).values())) {
			labelService.removeLabelValue(lv.labelValue(), user, new Date());
		}
		//
		labelService.addLabelValueToCards(labelId, filteredCardIds, value, user, new Date());
		return filteredCardIds;
	}

	public ImmutablePair<List<Integer>, List<Integer>> setDueDate(String projectShortName, List<Integer> cardIds,
			LabelValue value, User user) {
		return addLabelOrUpdate(projectShortName, cardIds, value, user, SYSTEM_LABEL_DUE_DATE, LabelDomain.SYSTEM);
	}

	public List<Integer> removeDueDate(String projectShortName, List<Integer> cardIds, User user) {
		return removeLabelWithName(projectShortName, cardIds, user, SYSTEM_LABEL_DUE_DATE, LabelDomain.SYSTEM);
	}

	public ImmutablePair<List<Integer>, List<Integer>> setMilestone(String projectShortName, List<Integer> cardIds,
			LabelValue value, User user) {
		return addLabelOrUpdate(projectShortName, cardIds, value, user, SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);
	}

	public List<Integer> removeMilestone(String projectShortName, List<Integer> cardIds, User user) {
		return removeLabelWithName(projectShortName, cardIds, user, SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);
	}

	public List<Integer> watch(String projectShortName, List<Integer> cardIds, User user) {
		CardLabel cl = findBy(projectShortName, SYSTEM_LABEL_WATCHED_BY, LabelDomain.SYSTEM);
		return addLabel(projectShortName, new LabelValue(user.getId()), cardIds, user, cl);
	}

	public List<Integer> removeWatch(String projectShortName, List<Integer> cardIds, User user) {
		return removeLabelWithNameAndValue(projectShortName, cardIds, user, SYSTEM_LABEL_WATCHED_BY, LabelDomain.SYSTEM, new LabelValue(user.getId()));
	}

	public List<Integer> removeUserLabel(String projectShortName, int labelId, LabelValue value, List<Integer> cardIds, User user) {
		CardLabel cl = cardLabelRepository.findLabelById(labelId);
		Validate.isTrue(cl.getDomain() == LabelDomain.USER);
		int projectId = projectService.findIdByShortName(projectShortName);
		Validate.isTrue(cl.getProjectId() == projectId);

		return value == null ? removeLabelWithName(projectShortName, cardIds, user, cl.getName(), LabelDomain.USER) :
			removeLabelWithNameAndValue(projectShortName, cardIds, user, cl.getName(), LabelDomain.USER, value);
	}

	public List<Integer> addUserLabel(String projectShortName, Integer labelId, LabelValue value, List<Integer> cardIds,
			User user) {
		CardLabel cl = cardLabelRepository.findLabelById(labelId);
		Validate.isTrue(cl.getDomain() == LabelDomain.USER);
		return addLabel(projectShortName, value, cardIds, user, cl);
	}

	private List<Integer> addLabel(String projectShortName, LabelValue value, List<Integer> cardIds, User user, CardLabel cl) {
		int labelId = cl.getId();
		int projectId = projectService.findIdByShortName(projectShortName);
		Validate.isTrue(cl.getProjectId() == projectId);

		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);

		Collection<Integer> alreadyWithLabel = keepCardWithMatching(filteredCardIds,
				new FilterByLabelIdAndLabelValueAndUniqueness(labelId, value)).keySet();
		filteredCardIds.removeAll(alreadyWithLabel);
		//

		labelService.addLabelValueToCards(labelId, filteredCardIds, value, user, new Date());
		return filteredCardIds;
	}

	private List<Integer> removeLabelWithName(String projectShortName, List<Integer> cardIds, User user,
			String labelName, LabelDomain labelDomain) {
		int labelId = findBy(projectShortName, labelName, labelDomain).getId();
		return removeMatchingLabel(projectShortName, user, cardIds, new FilterByLabelId(labelId));
	}

	private List<Integer> removeLabelWithNameAndValue(String projectShortName, List<Integer> cardIds, User user,
			String labelName, LabelDomain labelDomain, LabelValue labelValue) {

		int labelId = findBy(projectShortName, labelName, labelDomain).getId();
		return removeMatchingLabel(projectShortName, user, cardIds, new FilterByLabelIdAndLabelValue(labelId, labelValue));
	}

	private List<Integer> removeMatchingLabel(String projectShortName, User user, List<Integer> cardIds, FilterLabelAndValue filter) {

		List<Integer> affected = new ArrayList<>();

		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);

		for (LabelAndValue lv : flatten(keepCardWithMatching(filteredCardIds, filter).values())) {
			labelService.removeLabelValue(lv.labelValue(), user, new Date());
			affected.add(lv.getLabelValueCardId());
		}

		return affected;
	}

	private ImmutablePair<List<Integer>, List<Integer>> addLabelOrUpdate(String projectShortName,
			List<Integer> cardIds, LabelValue value, User user, String labelName, LabelDomain labelDomain) {
		List<Integer> filteredCardIds = keepCardIdsInProject(cardIds, projectShortName);
		int labelId = findBy(projectShortName, labelName, labelDomain).getId();

		Map<Integer, List<LabelAndValue>> cardsWithDueDate = keepCardWithMatching(filteredCardIds, new FilterByLabelId(
				labelId));

		List<Integer> updatedCardIds = new ArrayList<>();
		// to update only if the label value has changed
		for (LabelAndValue lv : flatten(cardsWithDueDate.values())) {
			if (!lv.labelValue().getValue().equals(value)) {
				labelService.updateLabelValue(lv.labelValue().newValue(lv.getLabelType(), value), user, new Date());
				updatedCardIds.add(lv.getLabelValueCardId());
			}
		}

		// to add
		filteredCardIds.removeAll(cardsWithDueDate.keySet());
		labelService.addLabelValueToCards(labelId, filteredCardIds, value, user, new Date());
		return ImmutablePair.of(updatedCardIds, filteredCardIds);
	}

	private Map<Integer, List<LabelAndValue>> keepCardWithMatching(List<Integer> cardIds, FilterLabelAndValue filter) {
		Map<Integer, List<LabelAndValue>> res = new HashMap<>();
		for (Entry<Integer, List<LabelAndValue>> kv : cardLabelRepository.findCardLabelValuesByCardIds(cardIds)
				.entrySet()) {
			List<LabelAndValue> matchingLabelIdAndLabelValue = filter.filter(kv.getValue());
			if (!matchingLabelIdAndLabelValue.isEmpty()) {
				res.put(kv.getKey(), matchingLabelIdAndLabelValue);
			}
		}
		return res;
	}

	private static class FilterByLabelId implements FilterLabelAndValue {
		private final int labelId;

		private FilterByLabelId(int labelId) {
			this.labelId = labelId;
		}

		@Override
		public List<LabelAndValue> filter(List<LabelAndValue> lvs) {
			List<LabelAndValue> matching = new ArrayList<>();
			for (LabelAndValue lv : lvs) {
				if (lv.getLabelId() == labelId) {
					matching.add(lv);
				}
			}
			return matching;
		}
	}

	/**
	 * Keep a list of all the cards that already have a label assigned (if it's a unique label) or a label+value
	 * combination
	 */
	private static class FilterByLabelIdAndLabelValueAndUniqueness implements FilterLabelAndValue {

		private final int labelId;
		private final LabelValue value;

		private FilterByLabelIdAndLabelValueAndUniqueness(int labelId, LabelValue value) {
			this.labelId = labelId;
			this.value = value;
		}

		@Override
		public List<LabelAndValue> filter(List<LabelAndValue> lvs) {
			List<LabelAndValue> matching = new ArrayList<>();
			for (LabelAndValue lv : lvs) {
				if (lv.getLabelId() == labelId && (lv.getLabelUnique() || lv.getValue().equals(value))) {
					matching.add(lv);
				}
			}
			return matching;
		}

	}

	private static class FilterByLabelIdAndLabelValue implements FilterLabelAndValue {

		private final int labelId;
		private final LabelValue value;

		private FilterByLabelIdAndLabelValue(int labelId, LabelValue value) {
			this.labelId = labelId;
			this.value = value;
		}

		@Override
		public List<LabelAndValue> filter(List<LabelAndValue> lvs) {
			List<LabelAndValue> matching = new ArrayList<>();
			for (LabelAndValue lv : lvs) {
				if (lv.getLabelId() == labelId && lv.getValue().equals(value)) {
					matching.add(lv);
				}
			}
			return matching;
		}
	}

	private interface FilterLabelAndValue {
		List<LabelAndValue> filter(List<LabelAndValue> lvs);
	}

	private static <T> List<T> flatten(Collection<? extends Collection<T>> cc) {
		List<T> res = new ArrayList<>();
		for (Collection<T> c : cc) {
			res.addAll(c);
		}
		return res;
	}

	private List<Integer> keepCardIdsInProject(List<Integer> ids, String projectShortName) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		List<Integer> res = new ArrayList<>(ids.size());
		for (CardFull cf : cardRepository.findAllByIds(ids)) {
			if (projectShortName.equals(cf.getProjectShortName())) {
				res.add(cf.getId());
			}
		}
		return res;
	}

	public int findIdForSystemLabel(String shortName, String name) {
		return findBy(shortName, name, LabelDomain.SYSTEM).getId();
	}

	private CardLabel findBy(String shortName, String name, LabelDomain labelDomain) {
	    int projectId = projectService.findIdByShortName(shortName);
		return cardLabelRepository.findLabelByName(projectId, name, labelDomain);
	}
}
