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
package io.lavagna.service.importexport;

import io.lavagna.model.*;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Event.EventType;
import io.lavagna.service.*;

import java.nio.file.Path;
import java.util.Date;

class LabelCreate extends AbstractProcessLabelEvent {

	LabelCreate(CardRepository cardRepository, UserRepository userRepository, CardDataService cardDataService,
			LabelService labelService, CardLabelRepository cardLabelRepository, BoardRepository boardRepository,
			EventRepository eventRepository) {
		super(cardRepository, userRepository, cardDataService, labelService, cardLabelRepository, boardRepository,
				eventRepository);
	}

	@Override
	void process(EventFull e, Event event, Date time, User user, ImportContext context, Path tempFile) {
		CardLabel cl = findLabelByEvent(e);
		LabelValue lv;
		if (cl != null && (lv = labelValue(cl, e)) != null) {
			labelService.addLabelValueToCard(cl.getId(), cardId(e), lv, user, time);
		} else {
			insertLabelEvent(e, event, time, EventType.LABEL_CREATE);
		}
	}

}
