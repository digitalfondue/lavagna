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
package io.lavagna.config;


import io.lavagna.common.LavagnaEnvironment;
import io.lavagna.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
public class SchedulingServiceConfig {

	@Bean
	public Scheduler getScheduler(TaskScheduler taskScheduler, LavagnaEnvironment env,
                                  ConfigurationRepository configurationRepository,
                                  MySqlFullTextSupportService mySqlFullTextSupportService,
                                  NotificationService notificationService,
                                  StatisticsService statisticsService,
                                  MailTicketService mailTicketService,
                                  ExportImportService exportImportService) {
		return new Scheduler(taskScheduler, env, configurationRepository,
				mySqlFullTextSupportService, notificationService,
				statisticsService, mailTicketService, exportImportService);
	}
}
