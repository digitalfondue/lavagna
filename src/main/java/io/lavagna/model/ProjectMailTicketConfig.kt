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
import java.util.*

class ProjectMailTicketConfig(@Column("MAIL_CONFIG_ID") val id: Int,
                              @Column("MAIL_CONFIG_NAME") val name: String,
                              @Column("MAIL_CONFIG_ENABLED") val enabled: Boolean,
                              @Column("MAIL_CONFIG_PROJECT_ID_FK") val projectId: Int,
                              @Column("MAIL_CONFIG_LAST_CHECKED") val lastChecked: Date?,
                              @Column("MAIL_CONFIG_CONFIG") @Transient val configJson: String,
                              @Column("MAIL_CONFIG_SUBJECT") val subject: String,
                              @Column("MAIL_CONFIG_BODY") val body: String) {

    val config: ProjectMailTicketConfigData
    val entries: List<ProjectMailTicket>

    init {
        config = Json.GSON.fromJson(configJson, ProjectMailTicketConfigData::class.java)
        entries = ArrayList<ProjectMailTicket>()
    }
}
