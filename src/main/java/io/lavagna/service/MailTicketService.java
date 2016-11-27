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

import io.lavagna.model.Project;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailTicketService {

    private final ProjectService projectService;
    private final CardService cardService;
    private final CardDataService cardDataService;

    private final static String EMAIL_PROVIDER = "LAVAGNATICKETMAIL";

    public MailTicketService(ProjectService projectService,
                             CardService cardService,
                             CardDataService cardDataService) {
        this.projectService = projectService;
        this.cardService = cardService;
        this.cardDataService = cardDataService;
    }

    public void checkNew() {
        List<Project> projectList = projectService.findAll();
    }
}
