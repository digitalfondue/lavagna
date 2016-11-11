/**
 * This file is part of lavagna.

 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see //www.gnu.org/licenses/>.
 */
package io.lavagna.model

import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column
import io.lavagna.model.Event.EventType
import java.util.*

class CardDataFull(@Column("CARD_DATA_ID") val id: Int, @Column("CARD_DATA_REFERENCE_ID") val referenceId: Int?,
                   @Column("CARD_DATA_CARD_ID_FK") val cardId: Int, @Column("CARD_DATA_CONTENT") val content: String,
                   @Column("EVENT_USER_ID_FK") val userId: Int, @Column("EVENT_PREV_CARD_DATA_ID_FK") val eventReferenceId: Int,
                   @Column("EVENT_TIME") val time: Date, @Column("CARD_DATA_TYPE") val type: CardType,
                   @Column("CARD_DATA_ORDER") val order: Int, @Column("EVENT_TYPE") val eventType: EventType)
