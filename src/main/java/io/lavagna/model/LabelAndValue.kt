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
import io.lavagna.model.CardLabel.LabelDomain
import io.lavagna.model.CardLabel.LabelType
import io.lavagna.model.CardLabelValue.LabelValue
import org.apache.commons.lang3.Validate
import java.util.*

class LabelAndValue(@Column("CARD_LABEL_ID") val labelId: Int,
                    @Column("CARD_LABEL_PROJECT_ID_FK") val labelProjectId: Int,
                    @Column("CARD_LABEL_UNIQUE") val labelUnique: Boolean,
                    @Column("CARD_LABEL_TYPE") val labelType: LabelType,
                    @Column("CARD_LABEL_DOMAIN") val labelDomain: LabelDomain,
                    @Column("CARD_LABEL_NAME") val labelName: String,
                    @Column("CARD_LABEL_COLOR") val labelColor: Int,
                    @Column("CARD_LABEL_VALUE_ID") val labelValueId: Int,
                    @Column("CARD_ID_FK") val labelValueCardId: Int,
                    @Column("CARD_LABEL_ID_FK") val labelValueLabelId: Int,
                    @Column("CARD_LABEL_VALUE_USE_UNIQUE_INDEX") val labelValueUseUniqueIndex: Boolean?,
                    @Column("CARD_LABEL_VALUE_TYPE") val labelValueType: LabelType,
                    @Column("CARD_LABEL_VALUE_STRING") val labelValueString: String?,
                    @Column("CARD_LABEL_VALUE_TIMESTAMP") val labelValueTimestamp: Date?,
                    @Column("CARD_LABEL_VALUE_INT") val labelValueInt: Int?,
                    @Column("CARD_LABEL_VALUE_CARD_FK") val labelValueCard: Int?,
                    @Column("CARD_LABEL_VALUE_USER_FK") val labelValueUser: Int?,
                    @Column("CARD_LABEL_VALUE_LIST_VALUE_FK") val labelValueList: Int?) {
    val value: LabelValue

    init {

        Validate.isTrue(labelType === labelValueType, "label type is not equal to label value type")

        this.value = LabelValue(labelValueString, labelValueTimestamp, labelValueInt, labelValueCard,
                labelValueUser, labelValueList)
    }// /

    fun label(): CardLabel {
        return CardLabel(labelId, labelProjectId, labelUnique, labelType, labelDomain, labelName, labelColor)
    }

    fun labelValue(): CardLabelValue {
        return CardLabelValue(labelValueId, labelValueCardId, labelValueLabelId, labelValueUseUniqueIndex,
                labelValueType, labelValueString, labelValueTimestamp, labelValueInt, labelValueCard, labelValueUser,
                labelValueList)
    }
}
