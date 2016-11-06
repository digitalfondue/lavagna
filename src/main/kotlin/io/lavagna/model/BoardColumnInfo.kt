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
import io.lavagna.model.BoardColumn.BoardColumnLocation

class BoardColumnInfo(@Column("BOARD_COLUMN_ID") val columnId: Int,
                      @Column("BOARD_COLUMN_NAME") val columnName: String,
                      @Column("BOARD_COLUMN_LOCATION") val columnLocation: BoardColumnLocation,
                      @Column("BOARD_ID") val boardId: Int,
                      @Column("BOARD_NAME") val boardName: String,
                      @Column("BOARD_SHORT_NAME") val boardShortName: String,
                      @Column("PROJECT_ID") val projectId: Int,
                      @Column("PROJECT_NAME") val projectName: String,
                      @Column("BOARD_COLUMN_DEFINITION_VALUE") val columnDefinition: ColumnDefinition,
                      @Column("BOARD_COLUMN_DEFINITION_COLOR") val columnColor: Int)
