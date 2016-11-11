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
        if (!o.canEqual(this as Any))
            return false
        if (this.id != o.id)
            return false
        if (this.cardId != o.cardId)
            return false
        if (this.userId != o.userId)
            return false
        val `this$time` = this.time
        val `other$time` = o.time
        if (if (`this$time` == null) `other$time` != null else `this$time` != `other$time`)
            return false
        val `this$event` = this.event
        val `other$event` = o.event
        if (if (`this$event` == null) `other$event` != null else `this$event` != `other$event`)
            return false
        val `this$dataId` = this.dataId
        val `other$dataId` = o.dataId
        if (if (`this$dataId` == null) `other$dataId` != null else `this$dataId` != `other$dataId`)
            return false
        val `this$columnId` = this.columnId
        val `other$columnId` = o.columnId
        if (if (`this$columnId` == null) `other$columnId` != null else `this$columnId` != `other$columnId`)
            return false
        val `this$labelName` = this.labelName
        val `other$labelName` = o.labelName
        if (if (`this$labelName` == null) `other$labelName` != null else `this$labelName` != `other$labelName`)
            return false
        val `this$labelType` = this.labelType
        val `other$labelType` = o.labelType
        if (if (`this$labelType` == null) `other$labelType` != null else `this$labelType` != `other$labelType`)
            return false
        val `this$previousDataId` = this.previousDataId
        val `other$previousDataId` = o.previousDataId
        if (if (`this$previousDataId` == null)
            `other$previousDataId` != null
        else
            `this$previousDataId` != `other$previousDataId`)
            return false
        val `this$newDataId` = this.newDataId
        val `other$newDataId` = o.newDataId
        if (if (`this$newDataId` == null) `other$newDataId` != null else `this$newDataId` != `other$newDataId`)
            return false
        val `this$previousColumnId` = this.previousColumnId
        val `other$previousColumnId` = o.previousColumnId
        if (if (`this$previousColumnId` == null)
            `other$previousColumnId` != null
        else
            `this$previousColumnId` != `other$previousColumnId`)
            return false
        val `this$valueInt` = this.valueInt
        val `other$valueInt` = o.valueInt
        if (if (`this$valueInt` == null) `other$valueInt` != null else `this$valueInt` != `other$valueInt`)
            return false
        val `this$valueString` = this.valueString
        val `other$valueString` = o.valueString
        if (if (`this$valueString` == null) `other$valueString` != null else `this$valueString` != `other$valueString`)
            return false
        val `this$valueTimestamp` = this.valueTimestamp
        val `other$valueTimestamp` = o.valueTimestamp
        if (if (`this$valueTimestamp` == null)
            `other$valueTimestamp` != null
        else
            `this$valueTimestamp` != `other$valueTimestamp`)
            return false
        val `this$valueCard` = this.valueCard
        val `other$valueCard` = o.valueCard
        if (if (`this$valueCard` == null) `other$valueCard` != null else `this$valueCard` != `other$valueCard`)
            return false
        val `this$valueUser` = this.valueUser
        val `other$valueUser` = o.valueUser
        if (if (`this$valueUser` == null) `other$valueUser` != null else `this$valueUser` != `other$valueUser`)
            return false
        return true
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + this.id
        result = result * PRIME + this.cardId
        result = result * PRIME + this.userId
        val `$time` = this.time
        result = result * PRIME + if (`$time` == null) 43 else `$time`.hashCode()
        val `$event` = this.event
        result = result * PRIME + if (`$event` == null) 43 else `$event`.hashCode()
        val `$dataId` = this.dataId
        result = result * PRIME + if (`$dataId` == null) 43 else `$dataId`.hashCode()
        val `$columnId` = this.columnId
        result = result * PRIME + if (`$columnId` == null) 43 else `$columnId`.hashCode()
        val `$labelName` = this.labelName
        result = result * PRIME + if (`$labelName` == null) 43 else `$labelName`.hashCode()
        val `$labelType` = this.labelType
        result = result * PRIME + if (`$labelType` == null) 43 else `$labelType`.hashCode()
        val `$previousDataId` = this.previousDataId
        result = result * PRIME + if (`$previousDataId` == null) 43 else `$previousDataId`.hashCode()
        val `$newDataId` = this.newDataId
        result = result * PRIME + if (`$newDataId` == null) 43 else `$newDataId`.hashCode()
        val `$previousColumnId` = this.previousColumnId
        result = result * PRIME + if (`$previousColumnId` == null) 43 else `$previousColumnId`.hashCode()
        val `$valueInt` = this.valueInt
        result = result * PRIME + if (`$valueInt` == null) 43 else `$valueInt`.hashCode()
        val `$valueString` = this.valueString
        result = result * PRIME + if (`$valueString` == null) 43 else `$valueString`.hashCode()
        val `$valueTimestamp` = this.valueTimestamp
        result = result * PRIME + if (`$valueTimestamp` == null) 43 else `$valueTimestamp`.hashCode()
        val `$valueCard` = this.valueCard
        result = result * PRIME + if (`$valueCard` == null) 43 else `$valueCard`.hashCode()
        val `$valueUser` = this.valueUser
        result = result * PRIME + if (`$valueUser` == null) 43 else `$valueUser`.hashCode()
        return result
    }

    protected fun canEqual(other: Any): Boolean {
        return other is Event
    }
}
