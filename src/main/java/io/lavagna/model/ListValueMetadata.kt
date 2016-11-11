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
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

class ListValueMetadata(@Column("LVM_LABEL_LIST_VALUE_ID_FK") val labelListValueId: Int,
                        @Column("LVM_KEY") val key: String, @Column("LVM_VALUE") val value: String) {

    override fun equals(o: Any?): Boolean {
        if (o === this)
            return true
        if (o !is ListValueMetadata)
            return false
        return EqualsBuilder().append(labelListValueId, o.labelListValueId).append(key, o.key).append(value, o.value).isEquals();
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(labelListValueId).append(key).append(value).toHashCode();
    }

    protected fun canEqual(other: Any): Boolean {
        return other is ListValueMetadata
    }
}
