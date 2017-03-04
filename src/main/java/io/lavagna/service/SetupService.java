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

import io.lavagna.model.ConfigurationKeyValue;
import io.lavagna.model.UserToCreate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SetupService {

	private final ConfigurationRepository configurationRepository;
	private final UserService userService;


	public SetupService(ConfigurationRepository configurationRepository, UserService userService) {
		this.configurationRepository = configurationRepository;
		this.userService = userService;
	}

	public void updateConfiguration(List<ConfigurationKeyValue> toUpdateOrCreate, UserToCreate user) {
		configurationRepository.updateOrCreate(toUpdateOrCreate);
		userService.createUser(user);
	}
}
