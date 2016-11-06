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

open class Card(@Column("CARD_ID") val id: Int, //
                @Column("CARD_NAME") val name: String, //
                @Column("CARD_SEQ_NUMBER") val sequence: Int// sequence number, public identifier
                , //
                @Column("CARD_ORDER") val order: Int, //
                @Column("CARD_BOARD_COLUMN_ID_FK") val columnId: Int, //
                @Column("CARD_USER_ID_FK") val userId: Int)
