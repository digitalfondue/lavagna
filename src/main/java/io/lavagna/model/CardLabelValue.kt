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

open class CardLabelValue(@Column("CARD_LABEL_VALUE_ID") val cardLabelValueId: Int,
                          @Column("CARD_ID_FK") val cardId: Int,
                          @Column("CARD_LABEL_ID_FK") val labelId: Int,
                          @Column("CARD_LABEL_VALUE_USE_UNIQUE_INDEX") val useUniqueIndex: Boolean?,
                          @Column("CARD_LABEL_VALUE_TYPE") val labelValueType: LabelType,
                          @Column("CARD_LABEL_VALUE_STRING") valueString: String?,
                          @Column("CARD_LABEL_VALUE_TIMESTAMP") valueDate: Date?,
                          @Column("CARD_LABEL_VALUE_INT") valueInt: Int?,
                          @Column("CARD_LABEL_VALUE_CARD_FK") valueCard: Int?,
                          @Column("CARD_LABEL_VALUE_USER_FK") valueUser: Int?,
                          @Column("CARD_LABEL_VALUE_LIST_VALUE_FK") valueList: Int?) {
    val value: LabelValue

    init {
        this.value = LabelValue(valueString, valueDate, valueInt, valueCard, valueUser, valueList)
    }

    fun newValue(value: String): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.STRING, value, null,
            null, null, null, null)
    }

    fun newValue(date: Date): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.TIMESTAMP, null, date,
            null, null, null, null)
    }

    fun newValue(integer: Int?): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.INT, null, null,
            integer, null, null, null)
    }

    fun newCardValue(cardId: Int?): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId!!, labelId, useUniqueIndex, LabelType.CARD, null, null, null,
            cardId, null, null)
    }

    fun newUserValue(userId: Int?): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.USER, null, null, null,
            null, userId, null)
    }

    fun newListValue(listId: Int?): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.CARD, null, null, null,
            null, null, listId)
    }

    fun newNullValue(): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.NULL, null, null, null,
            null, null, null)
    }

    fun newValue(type: LabelType, value: LabelValue): CardLabelValue {
        return CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, type, value.valueString,
            value.valueTimestamp, value.valueInt, value.valueCard, value.valueUser, value.valueList)
    }

    class LabelValue @JvmOverloads constructor(@Column("CARD_LABEL_VALUE_STRING") val valueString: String? = null,
                                               @Column("CARD_LABEL_VALUE_TIMESTAMP") val valueTimestamp: Date? = null,
                                               @Column("CARD_LABEL_VALUE_INT") val valueInt: Int? = null,
                                               @Column("CARD_LABEL_VALUE_CARD_FK") val valueCard: Int? = null,
                                               @Column("CARD_LABEL_VALUE_USER_FK") val valueUser: Int? = null,
                                               @Column("CARD_LABEL_VALUE_LIST_VALUE_FK") val valueList: Int? = null) {

        constructor(valueTimestamp: Date) : this(null, valueTimestamp, null, null, null, null) {
        }

        constructor(valueUser: Int?) : this(null, null, null, null, valueUser, null) {
        }

        override fun equals(obj: Any?): Boolean {
            if (obj == null || obj !is LabelValue) {
                return false
            }
            return EqualsBuilder().append(valueString, obj.valueString).append(if (valueTimestamp == null) null else valueTimestamp.time / 1000, if (obj.valueTimestamp == null) null else obj.valueTimestamp.time / 1000)//go down to second...
                .append(valueInt, obj.valueInt).append(valueCard, obj.valueCard).append(valueUser, obj.valueUser).append(valueList, obj.valueList).isEquals
        }

        override fun hashCode(): Int {
            return HashCodeBuilder().append(valueString).append(valueTimestamp).append(valueInt).append(valueCard).append(valueUser).append(valueList).toHashCode()
        }
    }
}
