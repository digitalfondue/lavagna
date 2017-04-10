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

import io.lavagna.model.*;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Event.EventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LabelService {

	private final EventService eventService;
	private final CardLabelRepository labelRepository;

	public LabelService(EventService eventService, CardLabelRepository labelRepository) {
		this.eventService = eventService;
		this.labelRepository = labelRepository;
	}

	@Transactional(readOnly = false)
	public CardLabelValue addLabelValueToCard(int labelId, int cardId, LabelValue val, User user, Date time) {
		return addLabelValueToCard(labelRepository.findLabelById(labelId), cardId, val, user, time);
	}

	@Transactional(readOnly = false)
	public CardLabelValue addLabelValueToCard(CardLabel cl, int cardId, LabelValue val, User user, Date time) {
		CardLabelValue labelValue = labelRepository.addLabelValueToCard(cl, cardId, val);
		eventService.insertLabelEvent(cl.getName(), cardId, user.getId(), EventType.LABEL_CREATE, val, cl.getType(),
				time);
		return labelValue;
	}

	@Transactional(readOnly = false)
	public void addLabelValueToCards(int labelId, List<Integer> cardIds, LabelValue val, User user, Date time) {
		CardLabel cl = labelRepository.findLabelById(labelId);
		for (int cardId : cardIds) {
			addLabelValueToCard(cl, cardId, val, user, time);
		}
	}

	@Transactional(readOnly = false)
	public void updateLabelValue(CardLabelValue cardLabelValue, User user, Date time) {
		CardLabel cl = labelRepository.findLabelById(cardLabelValue.getLabelId());
		removeLabelValue(labelRepository.findLabelValueById(cardLabelValue.getCardLabelValueId()), user, time);
		addLabelValueToCard(cl, cardLabelValue.getCardId(), cardLabelValue.getValue(), user, time);
	}

	@Transactional(readOnly = false)
	public Event removeLabelValue(CardLabelValue cardLabelValue, User user, Date time) {
		CardLabel cl = labelRepository.findLabelById(cardLabelValue.getLabelId());
		labelRepository.removeLabelValue(cardLabelValue);
		return eventService.insertLabelEvent(cl.getName(), cardLabelValue.getCardId(), user.getId(),
				EventType.LABEL_DELETE, cardLabelValue.getValue(), cl.getType(), time);
	}

	public CardLabel findLabelById(int labelId) {
	    return labelRepository.findLabelById(labelId);
    }

    public LabelListValue findLabelListValueById(int id) {
	    return labelRepository.findSimpleListValueById(id);
    }
}
