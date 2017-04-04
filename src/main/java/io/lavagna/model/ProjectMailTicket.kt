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

class ProjectMailTicket(@Column("MAIL_TICKET_ID") val id: Int,
                        @Column("MAIL_TICKET_NAME") val name: String,
                        @Column("MAIL_TICKET_ENABLED") val enabled: Boolean,
                        @Column("MAIL_TICKET_ALIAS") val alias: String,
                        @Column("MAIL_TICKET_USE_ALIAS") val sendByAlias: Boolean,
                        @Column("MAIL_TICKET_NOTIFICATION_OVERRIDE") val notificationOverride: Boolean,
                        @Column("MAIL_TICKET_SUBJECT") val subject: String?,
                        @Column("MAIL_TICKET_BODY") val body: String?,
                        @Column("MAIL_TICKET_COLUMN_ID_FK") val columnId: Int,
                        @Column("MAIL_TICKET_CONFIG_ID_FK") val configId: Int,
                        @Column("MAIL_TICKET_METADATA") @Transient val metadataRaw: String?) {

    val metadata: String?

    init {
        metadata = metadataRaw
    }
}
