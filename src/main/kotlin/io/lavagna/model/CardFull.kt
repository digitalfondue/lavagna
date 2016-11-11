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
import java.util.*

open class CardFull(
        @Column("CARD_ID") id: Int, //
        @Column("CARD_NAME") name: String, //
        @Column("CARD_SEQ_NUMBER") sequence: Int, //
        @Column("CARD_ORDER") order: Int, //
        @Column("CARD_BOARD_COLUMN_ID_FK") columnId: Int, //
        @Column("CREATE_USER") createUserId: Int, //
        @Column("CREATE_TIME") val createTime: Date, //
        @Column("LAST_UPDATE_USER") val lastUpdateUserId: Int?, @Column("LAST_UPDATE_TIME") val lastUpdateTime: Date?,
        @Column("BOARD_COLUMN_DEFINITION_VALUE") val columnDefinition: ColumnDefinition,
        @Column("BOARD_SHORT_NAME") val boardShortName: String, @Column("PROJECT_SHORT_NAME") val projectShortName: String) : Card(id, name, sequence, order, columnId, createUserId)
