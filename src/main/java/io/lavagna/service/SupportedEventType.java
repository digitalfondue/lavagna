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

import io.lavagna.model.CardDataMetadata;
import io.lavagna.model.CardType;
import io.lavagna.model.Event;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static io.lavagna.common.Constants.*;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;

/**
 * A subset of the Event enum where the event is mapped to a text to be sent to the user.
 */
enum SupportedEventType {
	CARD_UPDATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), e.getValueString());
		}

	},
	COMMENT_CREATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(
					context.formatUser(e.getUserId()),
					firstNonNull(
							findFirstContentInHistory(e.getDataId(), CardType.COMMENT_HISTORY, cardDataRepository),
							context.cardData.get(e.getDataId())));
		}

	},
	DESCRIPTION_CREATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(
					context.formatUser(e.getUserId()),
					firstNonNull(
							findFirstContentInHistory(e.getDataId(), CardType.DESCRIPTION_HISTORY, cardDataRepository),
							context.cardData.get(e.getDataId())));
		}

	},
	COMMENT_UPDATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(
					context.formatUser(e.getUserId()),
					firstNonNull(findNextContentInHistory(e.getPreviousDataId(), cardDataRepository),
							context.cardData.get(e.getDataId())), context.cardData.get(e.getPreviousDataId()));
		}

	},
	COMMENT_DELETE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getPreviousDataId()),
					context.cardData.get(e.getDataId()));
		}
	},
	DESCRIPTION_UPDATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(
					context.formatUser(e.getUserId()),
					firstNonNull(findNextContentInHistory(e.getPreviousDataId(), cardDataRepository),
							context.cardData.get(e.getDataId())), context.cardData.get(e.getPreviousDataId()));
		}
	},
	CARD_ARCHIVE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.formatColumn(e.getPreviousColumnId()),
					context.formatColumn(e.getColumnId()));
		}
	},
	CARD_BACKLOG {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.formatColumn(e.getPreviousColumnId()),
					context.formatColumn(e.getColumnId()));
		}
	},
	CARD_TRASH {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.formatColumn(e.getPreviousColumnId()),
					context.formatColumn(e.getColumnId()));
		}

	},
	CARD_MOVE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.formatColumn(e.getPreviousColumnId()),
					context.formatColumn(e.getColumnId()));
		}

	},
	FILE_UPLOAD {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), e.getValueString());
		}

	},
	FILE_DELETE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), e.getValueString());
		}
	},
	ACTION_LIST_CREATE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()));
		}
	},
	ACTION_LIST_DELETE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()));
		}
	},
	ACTION_ITEM_CREATE {
		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()),
					context.cardData.get(e.getPreviousDataId()));
		}
	},
	ACTION_ITEM_DELETE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()),
					context.cardData.get(e.getPreviousDataId()));
		}

	},
	ACTION_ITEM_CHECK {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()),
					context.cardData.get(e.getPreviousDataId()));
		}
	},
	ACTION_ITEM_UNCHECK {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()),
					context.cardData.get(e.getPreviousDataId()));
		}
	},
	ACTION_ITEM_MOVE {

		@Override
		protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
			String newActionListName = cardDataRepository.getUndeletedDataLightById(
					cardDataRepository.getUndeletedDataLightById(e.getDataId()).getReferenceId()).getContent();
			return toArray(context.formatUser(e.getUserId()), context.cardData.get(e.getDataId()), newActionListName);
		}
	},
	LABEL_CREATE {
		@Override
		protected ImmutablePair<String, String[]> toKeyAndParam(Event e, EventsContext context,
				CardDataRepository cardDataRepository) {
			Map<String, String> messages = new HashMap<String, String>();
			messages.put(SYSTEM_LABEL_MILESTONE, "User %s has added a Milestone: %s");
			messages.put(SYSTEM_LABEL_DUE_DATE, "User %s added a due date for: %s");
			messages.put(SYSTEM_LABEL_ASSIGNED, "User %s assigned the card to: %s");
			messages.put(SYSTEM_LABEL_WATCHED_BY, "User %s is now watching this card");
			return handleLabelCreationAndDeletion(e, context, messages, this.name());
		}
	},
	LABEL_DELETE {
		@Override
		protected ImmutablePair<String, String[]> toKeyAndParam(Event e, EventsContext context,
				CardDataRepository cardDataRepository) {
			Map<String, String> messages = new HashMap<String, String>();
			messages.put(SYSTEM_LABEL_MILESTONE, "User %s removed a Milestone: %s");
			messages.put(SYSTEM_LABEL_DUE_DATE, "User %s removed a due date for %s");
			messages.put(SYSTEM_LABEL_ASSIGNED, "User %s removed %s from the assigned users");
			messages.put(SYSTEM_LABEL_WATCHED_BY, "User %s is not watching this card anymore");
			return handleLabelCreationAndDeletion(e, context, messages, this.name());
		}
	};

	private static ImmutablePair<String, String[]> handleLabelCreationAndDeletion(Event e, EventsContext context,
			Map<String, String> msg, String defaultMessage) {
		if (SYSTEM_LABEL_MILESTONE.equals(e.getLabelName())) {
			return of("event." + defaultMessage + ".MILESTONE",
					toArray(context.formatUser(e.getUserId()), e.getValueString()));
		} else if (SYSTEM_LABEL_DUE_DATE.equals(e.getLabelName())) {
			return of(
					"event." + defaultMessage + ".DUE_DATE",
					toArray(context.formatUser(e.getUserId()),
							new SimpleDateFormat("dd.MM.yyyy").format(e.getValueTimestamp())));
		} else if (SYSTEM_LABEL_ASSIGNED.equals(e.getLabelName())) {
			return of("event." + defaultMessage + ".ASSIGNED",
					toArray(context.formatUser(e.getUserId()), context.formatUser(e.getValueUser())));
		} else if (SYSTEM_LABEL_WATCHED_BY.equals(e.getLabelName())) {
			return of("event." + defaultMessage + ".WATCHED_BY", toArray(context.formatUser(e.getUserId())));
		} else {
			return of("event." + defaultMessage, toArray(context.formatUser(e.getUserId()), context.formatLabel(e)));
		}
	}

	protected String[] params(Event e, EventsContext context, CardDataRepository cardDataRepository) {
		return null;
	}

	ImmutablePair<String, String[]> toKeyAndParam(Event e, EventsContext context, CardDataRepository cardDataRepository) {
		return of("event." + this.name(), params(e, context, cardDataRepository));
	}

	private static String findFirstContentInHistory(int id, CardType type, CardDataRepository cardDataRepository) {
		CardDataMetadata m = cardDataRepository.findMetadataById(id);
		return cardDataRepository.findContentWith(m.getCardId(), m.getId(), type, 1);
	}

	private static String findNextContentInHistory(int id, CardDataRepository cardDataRepository) {
		CardDataMetadata m = cardDataRepository.findMetadataById(id);
		return cardDataRepository.findContentWith(m.getCardId(), m.getReferenceId(), m.getType(), m.getOrder() + 1);
	}
}
