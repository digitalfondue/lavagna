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

import io.lavagna.model.util.DataOutputStreamUtils.*
import org.apache.commons.codec.digest.DigestUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class ProjectMetadata(val shortName: String, val labels: SortedMap<Int, CardLabel>, val labelListValues: SortedMap<Int, LabelListValueWithMetadata>,
                      val columnsDefinition: Map<ColumnDefinition, BoardColumnDefinition>) {
    val hash: String

    init {
        this.hash = hash(shortName, labels, labelListValues, columnsDefinition)
    }

    private fun hash(shortName: String, labels: SortedMap<Int, CardLabel>, labelListValues: SortedMap<Int, LabelListValueWithMetadata>,
                     columnsDefinition: Map<ColumnDefinition, BoardColumnDefinition>): String {

        val baos = ByteArrayOutputStream()
        val daos = DataOutputStream(baos)

        try {

            hash(daos, shortName)

            for (cl in labels.values) {
                hash(daos, cl)
            }

            for (l in labelListValues.values) {
                hash(daos, l)
            }

            for (b in columnsDefinition.values) {
                hash(daos, b)
            }

            daos.flush()
            return DigestUtils.sha256Hex(ByteArrayInputStream(baos.toByteArray()))
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }

    }

    @Throws(IOException::class)
    private fun hash(daos: DataOutputStream, s: String) {
        writeNotNull(daos, s)
    }

    @Throws(IOException::class)
    private fun hash(daos: DataOutputStream, b: BoardColumnDefinition) {
        writeInts(daos, b.id, b.projectId)
        writeEnum(daos, b.value)
        writeInts(daos, b.color)
    }

    @Throws(IOException::class)
    private fun hash(daos: DataOutputStream, cl: CardLabel) {
        writeInts(daos, cl.id, cl.projectId)
        writeNotNull(daos, cl.name)
        writeInts(daos, cl.color)
        writeNotNull(daos, cl.unique)
        writeEnum(daos, cl.type)
        writeEnum(daos, cl.domain)
    }

    @Throws(IOException::class)
    private fun hash(daos: DataOutputStream, l: LabelListValueWithMetadata) {
        writeInts(daos, l.id, l.cardLabelId, l.order)
        writeNotNull(daos, l.value)
        for ((key, value) in l.metadata) {
            writeNotNull(daos, key)
            writeNotNull(daos, value)
        }
    }
}
