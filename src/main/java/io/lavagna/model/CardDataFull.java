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

import io.lavagna.common.ConstructorAnnotationRowMapper.Column;
import io.lavagna.model.Event.EventType;

import java.util.Date;

import lombok.Getter;

@Getter
public class CardDataFull {
	private final int id;
	private final Integer referenceId;
	private final int cardId;
	private final String content;
	private final int userId;
	private final Date time;
	private final CardType type;
	private final EventType eventType;
	private final int eventReferenceId;
	private final int order;

	public CardDataFull(@Column("CARD_DATA_ID") int id, @Column("CARD_DATA_REFERENCE_ID") Integer referenceId,
			@Column("CARD_DATA_CARD_ID_FK") int cardId, @Column("CARD_DATA_CONTENT") String content,
			@Column("EVENT_USER_ID_FK") int userId, @Column("EVENT_PREV_CARD_DATA_ID_FK") int eventReferenceId,
			@Column("EVENT_TIME") Date time, @Column("CARD_DATA_TYPE") CardType type,
			@Column("CARD_DATA_ORDER") int order, @Column("EVENT_TYPE") EventType eventType) {
		this.id = id;
		this.referenceId = referenceId;
		this.cardId = cardId;
		this.content = content;
		this.userId = userId;
		this.time = time;
		this.type = type;
		this.order = order;
		this.eventType = eventType;
		this.eventReferenceId = eventReferenceId;
	}
}