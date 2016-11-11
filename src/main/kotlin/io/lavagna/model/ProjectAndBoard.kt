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

class ProjectAndBoard(
    @Column("PROJECT_ID") projectId: Int,
    @Column("PROJECT_NAME") projectName: String?,
    @Column("PROJECT_SHORT_NAME") projectShortName: String?,
    @Column("PROJECT_DESCRIPTION") projectDescription: String?,
    @Column("PROJECT_ARCHIVED") projectArchived: Boolean, //
    @Column("BOARD_ID") boardId: Int,
    @Column("BOARD_NAME") boardName: String?,
    @Column("BOARD_SHORT_NAME") boardShortName: String?,
    @Column("BOARD_DESCRIPTION") boardDescription: String?,
    @Column("BOARD_ARCHIVED") boardArchived: Boolean) {

    val project: Project
    val board: Board

    init {
        this.project = Project(projectId, projectName, projectShortName, projectDescription, projectArchived)
        this.board = Board(boardId, boardName, boardShortName, boardDescription, projectId, boardArchived)
    }
}
