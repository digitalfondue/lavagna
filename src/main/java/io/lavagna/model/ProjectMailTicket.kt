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
import io.lavagna.common.Json
import java.util.Date

class ProjectMailTicket(@Column("MAIL_TICKET_ID") val id: Int,
                        @Column("MAIL_TICKET_NAME") val name: String,
                        @Column("MAIL_TICKET_PROJECT_ID_FK") val projectId: Int,
                        @Column("MAIL_TICKET_BOARD_ID_FK") val boardId: Int,
                        @Column("MAIL_TICKET_COLUMN_ID_FK") val columnId: Int,
                        @Column("MAIL_TICKET_LAST_CHECKED") val lastChecked: Date?,
                        @Column("MAIL_TICKET_MAIL_CONFIG") @Transient val configRaw: String,
                        @Column("MAIL_TICKET_METADATA") @Transient val metadataRaw: String?) {

    val config: ProjectMailTicketMailConfig
    var metadata: String?

    init {
        config = Json.GSON.fromJson(configRaw, ProjectMailTicketMailConfig::class.java)
        metadata = metadataRaw
    }
}
