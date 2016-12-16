/**
 * This file is part of lavagna.
 *
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.query;

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import io.lavagna.model.ProjectMailTicket;
import io.lavagna.model.ProjectMailTicketConfig;

import java.util.Date;
import java.util.List;

@QueryRepository
public interface MailTicketQuery {

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG")
    List<ProjectMailTicketConfig> findAllConfigs();

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_PROJECT_ID_FK = :projectId")
    List<ProjectMailTicketConfig> findConfigsByProject(@Bind("projectId") int projectId);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET")
    List<ProjectMailTicket> findAllTickets();

    @Query("UPDATE LA_PROJECT_MAIL_CONFIG SET MAIL_CONFIG_LAST_CHECKED = :date WHERE MAIL_CONFIG_ID = :id")
    int updateConfigLastChecked(@Bind("id") int id, @Bind("date") Date date);

}
