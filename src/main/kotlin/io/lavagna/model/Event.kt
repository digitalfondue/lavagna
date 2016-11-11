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
import io.lavagna.model.CardLabel.LabelType
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.*

open class Event(@Column("EVENT_ID") val id: Int,
                 @Column("EVENT_CARD_ID_FK") val cardId: Int,
                 @Column("EVENT_USER_ID_FK") val userId: Int,
                 @Column("EVENT_TIME") val time: Date,
                 @Column("EVENT_TYPE") val event: Event.EventType,
                 @Column("EVENT_CARD_DATA_ID_FK") val dataId: Int?,
                 @Column("EVENT_COLUMN_ID_FK") val columnId: Int?,
                 @Column("EVENT_PREV_CARD_DATA_ID_FK") val previousDataId: Int?,
                 @Column("EVENT_NEW_CARD_DATA_ID_FK") val newDataId: Int?,
                 @Column("EVENT_PREV_COLUMN_ID_FK") val previousColumnId: Int?,
                 @Column("EVENT_LABEL_NAME") val labelName: String?,
                 @Column("EVENT_LABEL_TYPE") val labelType: LabelType?,
                 @Column("EVENT_VALUE_INT") val valueInt: Int?,
                 @Column("EVENT_VALUE_STRING") val valueString: String?,
                 @Column("EVENT_VALUE_TIMESTAMP") val valueTimestamp: Date?,
                 @Column("EVENT_VALUE_CARD_FK") val valueCard: Int?,
                 @Column("EVENT_VALUE_USER_FK") val valueUser: Int?) {

    enum class EventType {
        CARD_MOVE, CARD_CREATE, CARD_ARCHIVE, CARD_BACKLOG, CARD_TRASH, CARD_UPDATE,

        ACTION_LIST_CREATE, ACTION_LIST_DELETE, ACTION_ITEM_CREATE, ACTION_ITEM_DELETE, ACTION_ITEM_MOVE, ACTION_ITEM_CHECK, ACTION_ITEM_UNCHECK,

        COMMENT_CREATE, COMMENT_UPDATE, COMMENT_DELETE,

        FILE_UPLOAD, FILE_DELETE,

        DESCRIPTION_CREATE, DESCRIPTION_UPDATE, LABEL_CREATE, LABEL_DELETE
    }

    override fun equals(o: Any?): Boolean {
        if (o === this)
            return true
        if (o !is Event)
            return false

        return EqualsBuilder().append(id, o.id)
            .append(cardId, o.cardId)
            .append(userId, o.userId)
            .append(time, o.time)
            .append(dataId, o.dataId)
            .append(columnId, o.columnId)
            .append(previousDataId, o.previousDataId)
            .append(newDataId, o.newDataId)
            .append(previousColumnId, o.previousColumnId)
            .append(labelName, o.labelName)
            .append(labelType, o.labelType)
            .append(valueInt, o.valueInt)
            .append(valueString, o.valueString)
            .append(valueTimestamp, o.valueTimestamp)
            .append(valueCard, o.valueCard)
            .append(valueUser, o.valueUser)
            .isEquals();
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(id)
            .append(cardId)
            .append(userId)
            .append(time)
            .append(dataId)
            .append(columnId)
            .append(previousDataId)
            .append(newDataId)
            .append(previousColumnId)
            .append(labelName)
            .append(labelType)
            .append(valueInt)
            .append(valueString)
            .append(valueTimestamp)
            .append(valueCard)
            .append(valueUser).toHashCode();
    }

    protected fun canEqual(other: Any): Boolean {
        return other is Event
    }
}
