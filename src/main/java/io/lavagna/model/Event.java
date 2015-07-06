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
package io.lavagna.model;

import io.lavagna.model.CardLabel.LabelType;

import java.util.Date;

import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class Event {

	public enum EventType {
		CARD_MOVE, CARD_CREATE, CARD_ARCHIVE, CARD_BACKLOG, CARD_TRASH, CARD_UPDATE,

		ACTION_LIST_CREATE, ACTION_LIST_DELETE, ACTION_ITEM_CREATE, ACTION_ITEM_DELETE, ACTION_ITEM_MOVE, ACTION_ITEM_CHECK, ACTION_ITEM_UNCHECK,

		COMMENT_CREATE, COMMENT_UPDATE, COMMENT_DELETE,

		FILE_UPLOAD, FILE_DELETE,

		DESCRIPTION_CREATE, DESCRIPTION_UPDATE, LABEL_CREATE, LABEL_DELETE
	}

	private final int id;
	private final int cardId;
	private final int userId;
	private final Date time;
	private final EventType event;
	private final Integer dataId;
	private final Integer columnId;
	private final String labelName;
	private final LabelType labelType;
	private final Integer previousDataId;
	private final Integer newDataId;
	private final Integer previousColumnId;
	private final Integer valueInt;
	private final String valueString;
	private final Date valueTimestamp;
	private final Integer valueCard;
	private final Integer valueUser;

	public Event(@Column("EVENT_ID") int id, @Column("EVENT_CARD_ID_FK") int cardId,
			@Column("EVENT_USER_ID_FK") int userId, @Column("EVENT_TIME") Date time,
			@Column("EVENT_TYPE") EventType event, @Column("EVENT_CARD_DATA_ID_FK") Integer dataId,
			@Column("EVENT_COLUMN_ID_FK") Integer columnId,
			@Column("EVENT_PREV_CARD_DATA_ID_FK") Integer previousDataId,
			@Column("EVENT_NEW_CARD_DATA_ID_FK") Integer newDataId,
			@Column("EVENT_PREV_COLUMN_ID_FK") Integer previousColumnId, @Column("EVENT_LABEL_NAME") String labelName,
			@Column("EVENT_LABEL_TYPE") LabelType labelType, @Column("EVENT_VALUE_INT") Integer valueInt,
			@Column("EVENT_VALUE_STRING") String valueString, @Column("EVENT_VALUE_TIMESTAMP") Date valueTimestamp,
			@Column("EVENT_VALUE_CARD_FK") Integer valueCard, @Column("EVENT_VALUE_USER_FK") Integer valueUser) {
		this.id = id;
		this.cardId = cardId;
		this.userId = userId;
		this.event = event;
		this.time = time;

		this.dataId = dataId;
		this.columnId = columnId;
		this.labelName = labelName;
		this.labelType = labelType;

		this.previousColumnId = previousColumnId;
		this.previousDataId = previousDataId;
		this.newDataId = newDataId;

		this.valueInt = valueInt;
		this.valueString = valueString;
		this.valueTimestamp = valueTimestamp;
		this.valueCard = valueCard;
		this.valueUser = valueUser;
	}

}
