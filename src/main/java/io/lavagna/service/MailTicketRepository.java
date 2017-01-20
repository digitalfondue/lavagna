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
package io.lavagna.service;

import io.lavagna.model.ProjectMailTicket;
import io.lavagna.model.ProjectMailTicketConfig;
import io.lavagna.model.ProjectMailTicketConfigData;
import io.lavagna.query.MailTicketQuery;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional(readOnly = true)
public class MailTicketRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final MailTicketQuery queries;

    public MailTicketRepository(NamedParameterJdbcTemplate jdbc, MailTicketQuery queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public ProjectMailTicketConfig findConfig(final int id) {
        return queries.findConfigById(id);
    }

    public ProjectMailTicket findTicket(final int id) {
        return queries.findTicketById(id);
    }

    @Transactional(readOnly = false)
    public int addConfig(final String name, final int projectId, final ProjectMailTicketConfigData config, String properties) {
        return queries.addConfig(name, projectId, config.toString(), properties);
    }

    public ProjectMailTicketConfig findLastCreatedConfig() {
        return queries.findLastCreatedConfig();
    }

    @Transactional(readOnly = false)
    public int updateConfig(final int id, final String name, final boolean enabled, final ProjectMailTicketConfigData config, String properties, final int projectId) {
        return queries.updateConfig(id, name, enabled, config.toString(), properties, projectId);
    }

    @Transactional(readOnly = false)
    public int addTicket(final String name, final int columnId, final int configId, String metadata) {
        return queries.addTicket(name, columnId, configId, metadata);
    }

    public ProjectMailTicket findLastCreatedTicket() {
        return queries.findLastCreatedTicket();
    }

    @Transactional(readOnly = false)
    public int updateTicket(final int id, final String name, final boolean enabled, final int columnId, final int configId, String metadata) {
        return queries.updateTicket(id, name, enabled, columnId, configId, metadata);
    }

    public List<ProjectMailTicketConfig> findAll() {
        return aggregateByConfig(queries.findAllConfigs(), queries.findAllTickets());
    }

    public List<ProjectMailTicketConfig> findAllByProject(final int projectId) {
        return aggregateByConfig(queries.findConfigsByProject(projectId), queries.findAllByProject(projectId));
    }

    private List<ProjectMailTicketConfig> aggregateByConfig(List<ProjectMailTicketConfig> configs, List<ProjectMailTicket> tickets) {
        Map<Integer, List<ProjectMailTicket>> ticketConfigsByConfigId = new HashMap<>();
        for(ProjectMailTicket ticketConfig: tickets) {
            if(!ticketConfigsByConfigId.containsKey(ticketConfig.getConfigId())) {
                ticketConfigsByConfigId.put(ticketConfig.getConfigId(), new ArrayList<ProjectMailTicket>());
            }

            ticketConfigsByConfigId.get(ticketConfig.getConfigId()).add(ticketConfig);
        }

        for(ProjectMailTicketConfig config: configs) {
            if(ticketConfigsByConfigId.containsKey(config.getId())) {
                config.getEntries().addAll(ticketConfigsByConfigId.get(config.getId()));
            }
        }

        return configs;
    }
}
