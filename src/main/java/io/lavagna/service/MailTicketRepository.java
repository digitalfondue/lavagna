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
import io.lavagna.query.MailTicketQuery;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(readOnly = true)
public class MailTicketRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final MailTicketQuery queries;

    public MailTicketRepository(NamedParameterJdbcTemplate jdbc, MailTicketQuery queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public List<ProjectMailTicketConfig> findAll() {
        List<ProjectMailTicketConfig> configs = queries.findAllConfigs();
        List<ProjectMailTicket> ticketConfigs = queries.findAllTickets();

        Map<Integer, List<ProjectMailTicket>> ticketConfigsByConfigId = new HashMap<>();
        for(ProjectMailTicket ticketConfig: ticketConfigs) {
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
