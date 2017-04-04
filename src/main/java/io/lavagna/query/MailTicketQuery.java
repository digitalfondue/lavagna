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

import ch.digitalfondue.npjt.*;
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

    @Query("SELECT MAIL_TICKET_ID, MAIL_TICKET_NAME, MAIL_TICKET_ENABLED, MAIL_TICKET_ALIAS, MAIL_TICKET_USE_ALIAS, MAIL_TICKET_NOTIFICATION_OVERRIDE, MAIL_TICKET_SUBJECT, MAIL_TICKET_BODY, MAIL_TICKET_COLUMN_ID_FK, MAIL_TICKET_CONFIG_ID_FK, MAIL_TICKET_METADATA FROM LA_PROJECT_MAIL_TICKET"
        + " INNER JOIN LA_PROJECT_MAIL_TICKET_CONFIG ON MAIL_TICKET_CONFIG_ID_FK = MAIL_CONFIG_ID WHERE MAIL_CONFIG_PROJECT_ID_FK = :projectId")
    List<ProjectMailTicket> findAllByProject(@Bind("projectId") int projectId);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_CONFIG_ID_FK = :configId")
    List<ProjectMailTicket> findAllByConfig(@Bind("configId") int configId);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_ID = :id")
    ProjectMailTicketConfig findConfigById(@Bind("id") int id);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_ID = :id")
    ProjectMailTicket findTicketById(@Bind("id") int id);

    @Query("INSERT INTO LA_PROJECT_MAIL_TICKET_CONFIG(MAIL_CONFIG_NAME, MAIL_CONFIG_PROJECT_ID_FK, MAIL_CONFIG_CONFIG, MAIL_CONFIG_SUBJECT, MAIL_CONFIG_BODY)"
        + "VALUES (:name, :projectId, :config, :subject, :body)")
    int addConfig(@Bind("name") String name, @Bind("projectId") int projectId, @Bind("config") String config, @Bind("subject") String subject, @Bind("body") String body);

    @Query("UPDATE LA_PROJECT_MAIL_TICKET_CONFIG SET MAIL_CONFIG_NAME = :name, MAIL_CONFIG_ENABLED = :enabled, MAIL_CONFIG_CONFIG = :config, MAIL_CONFIG_SUBJECT = :subject, MAIL_CONFIG_BODY = :body WHERE MAIL_CONFIG_ID = :id AND MAIL_CONFIG_PROJECT_ID_FK = :projectId")
    int updateConfig(@Bind("id") int id, @Bind("name") String name, @Bind("enabled") boolean enabled, @Bind("config") String config, @Bind("subject") String subject, @Bind("body") String body, @Bind("projectId") int projectId);

    @Query("DELETE FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_ID = :id AND MAIL_CONFIG_PROJECT_ID_FK = :projectId")
    int deleteConfig(@Bind("id") int id, @Bind("projectId") int projectId);

    @Query("UPDATE LA_PROJECT_MAIL_TICKET_CONFIG SET MAIL_CONFIG_LAST_CHECKED = :date WHERE MAIL_CONFIG_ID = :id")
    int updateConfigLastChecked(@Bind("id") int id, @Bind("date") Date date);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_ID = IDENTITY()")
    @QueriesOverride({
        @QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_ID = LAST_INSERT_ID()"),
        @QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_PROJECT_MAIL_TICKET_CONFIG WHERE MAIL_CONFIG_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_project_mail_ticket_config','mail_config_id')))") })
    ProjectMailTicketConfig findLastCreatedConfig();

    @Query("INSERT INTO LA_PROJECT_MAIL_TICKET(MAIL_TICKET_NAME, MAIL_TICKET_ALIAS, MAIL_TICKET_USE_ALIAS, MAIL_TICKET_NOTIFICATION_OVERRIDE, MAIL_TICKET_SUBJECT, MAIL_TICKET_BODY, MAIL_TICKET_COLUMN_ID_FK, MAIL_TICKET_CONFIG_ID_FK, MAIL_TICKET_METADATA)"
        + "VALUES (:name, :alias, :useAlias, :notificationOverride, :subject, :body, :columnId, :configId, :metadata)")
    int addTicket(@Bind("name") String name, @Bind("alias") String alias, @Bind("useAlias") boolean useAlias, @Bind("notificationOverride") boolean notificationOverride, @Bind("subject") String subject, @Bind("body") String body, @Bind("columnId") int columnId, @Bind("configId") int configId, @Bind("metadata") String metadata);

    @Query("UPDATE LA_PROJECT_MAIL_TICKET SET MAIL_TICKET_NAME = :name, MAIL_TICKET_ENABLED = :enabled, MAIL_TICKET_ALIAS = :alias, MAIL_TICKET_USE_ALIAS = :useAlias, MAIL_TICKET_NOTIFICATION_OVERRIDE = :notificationOverride, MAIL_TICKET_SUBJECT = :subject, MAIL_TICKET_BODY = :body, MAIL_TICKET_COLUMN_ID_FK = :columnId, MAIL_TICKET_CONFIG_ID_FK = :configId, MAIL_TICKET_METADATA = :metadata WHERE MAIL_TICKET_ID = :id")
    int updateTicket(@Bind("id") int id, @Bind("name") String name, @Bind("enabled") boolean enabled, @Bind("alias") String alias, @Bind("useAlias") boolean useAlias, @Bind("notificationOverride") boolean notificationOverride, @Bind("subject") String subject, @Bind("body") String body, @Bind("columnId") int columnId, @Bind("configId") int configId, @Bind("metadata") String metadata);

    @Query("DELETE FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_ID = :id")
    int deleteTicket(@Bind("id") int id);

    @Query("SELECT * FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_ID = IDENTITY()")
    @QueriesOverride({
        @QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_ID = LAST_INSERT_ID()"),
        @QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_PROJECT_MAIL_TICKET WHERE MAIL_TICKET_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_project_mail_ticket','mail_ticket_id')))") })
    ProjectMailTicket findLastCreatedTicket();

}
