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

class LabelAndValueWithCount(@Column("CARD_LABEL_ID") val labelId: Int, @Column("CARD_LABEL_NAME") val labelName: String,
                             @Column("CARD_LABEL_COLOR") val labelColor: Int, @Column("CARD_LABEL_VALUE_TYPE") labelValueType: LabelType,
                             @Column("CARD_LABEL_VALUE_LIST_VALUE_FK") labelValueList: Int?, @Column("LABEL_COUNT") val count: Long?) {
    val labelValueType: LabelType
    val value: CardLabelValue.LabelValue

    init {
        var labelValueType = labelValueType
        if (labelValueType != LabelType.NULL && labelValueType != LabelType.LIST) {
            labelValueType = LabelType.NULL
        }
        this.labelValueType = labelValueType
        this.value = CardLabelValue.LabelValue(null, null, null, null, null, labelValueList)
    }
}
