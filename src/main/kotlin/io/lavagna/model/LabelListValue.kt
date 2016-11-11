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

open class LabelListValue(@Column("CARD_LABEL_LIST_VALUE_ID") val id: Int, @Column("CARD_LABEL_ID_FK") val cardLabelId: Int,
                          @Column("CARD_LABEL_LIST_VALUE_ORDER") val order: Int, @Column("CARD_LABEL_LIST_VALUE") val value: String) {


    fun newValue(newValue: String): LabelListValue {
        return LabelListValue(id, cardLabelId, order, newValue)
    }

    override fun equals(o: Any?): Boolean {
        if (o === this)
            return true
        if (o !is LabelListValue)
            return false
        if (!o.canEqual(this as Any))
            return false
        if (this.id != o.id)
            return false
        if (this.cardLabelId != o.cardLabelId)
            return false
        if (this.order != o.order)
            return false
        val `this$value` = this.value
        val `other$value` = o.value
        if (if (`this$value` == null) `other$value` != null else `this$value` != `other$value`)
            return false
        return true
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + this.id
        result = result * PRIME + this.cardLabelId
        result = result * PRIME + this.order
        val `$value` = this.value
        result = result * PRIME + if (`$value` == null) 43 else `$value`.hashCode()
        return result
    }

    protected fun canEqual(other: Any): Boolean {
        return other is LabelListValue
    }
}
