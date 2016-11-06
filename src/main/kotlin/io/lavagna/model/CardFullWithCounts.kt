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

import io.lavagna.model.CardLabel.LabelType
import io.lavagna.model.util.DataOutputStreamUtils.*
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.cglib.core.CollectionUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class CardFullWithCounts(cardInfo: CardFull, val counts: Map<String, CardDataCount>?, labels: List<LabelAndValue>?) : CardFull(cardInfo.id, cardInfo.name, cardInfo.sequence, cardInfo.order, cardInfo.columnId, cardInfo.userId, cardInfo.createTime, cardInfo.lastUpdateUserId, cardInfo.lastUpdateTime, cardInfo.columnDefinition, cardInfo.boardShortName, cardInfo.projectShortName) {

    val creationUser: Int?
    val creationDate: Date
    val labels: List<LabelAndValue>
    val hash: String

    init {
        this.labels = labels ?: emptyList<LabelAndValue>()
        // FIXME: this data is already contained in CardFull, leaving it here
        // for retrocompatibility
        this.creationUser = cardInfo.userId
        this.creationDate = cardInfo.createTime

        hash = hash(this)
    }

    fun getLabelsWithType(type: LabelType): List<LabelAndValue> {
        val filteredValues = ArrayList(labels)
        CollectionUtils.filter(filteredValues) { o -> (o as LabelAndValue).labelType == type }
        return filteredValues
    }

    private fun hash(cwc: CardFullWithCounts): String {
        val baos = ByteArrayOutputStream()
        val daos = DataOutputStream(baos)

        try {
            // card
            daos.writeChars(Integer.toString(cwc.id))

            writeNotNull(daos, cwc.name)
            writeInts(daos, cwc.sequence, cwc.order, cwc.columnId, cwc.userId)
            // end card
            writeNotNull(daos, cwc.creationUser)
            writeNotNull(daos, cwc.creationDate)

            if (cwc.counts != null) {
                for ((key, dataCount) in cwc.counts) {
                    writeNotNull(daos, key)
                    daos.writeChars(Integer.toString(dataCount.cardId))
                    if (dataCount.count != null) {
                        daos.writeChars(java.lang.Long.toString(dataCount.count.toLong()))
                    }
                    writeNotNull(daos, dataCount.type)
                }
            }
            for (lv in cwc.labels) {
                //
                writeInts(daos, lv.labelId, lv.labelProjectId)
                writeNotNull(daos, lv.labelName)
                writeInts(daos, lv.labelColor)
                writeEnum(daos, lv.labelType)
                writeEnum(daos, lv.labelDomain)
                //
                writeInts(daos, lv.labelValueId, lv.labelValueCardId, lv.labelValueLabelId)
                writeNotNull(daos, lv.labelValueUseUniqueIndex)
                writeEnum(daos, lv.labelValueType)
                writeNotNull(daos, lv.labelValueString)
                writeNotNull(daos, lv.labelValueTimestamp)
                writeNotNull(daos, lv.labelValueInt)
                writeNotNull(daos, lv.labelValueCard)
                writeNotNull(daos, lv.labelValueUser)
            }
            daos.flush()

            return DigestUtils.sha256Hex(ByteArrayInputStream(baos.toByteArray()))
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }

    }
}
