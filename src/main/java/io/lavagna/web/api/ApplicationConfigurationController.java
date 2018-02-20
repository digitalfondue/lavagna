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
package io.lavagna.web.api;

import io.lavagna.model.*;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.web.api.model.Conf;
import io.lavagna.web.helper.ExpectPermission;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@ExpectPermission(Permission.ADMINISTRATION)
public class ApplicationConfigurationController {

	private final ConfigurationRepository configurationRepository;
	private final Ldap ldap;


	public ApplicationConfigurationController(ConfigurationRepository configurationRepository, Ldap ldap) {
		this.configurationRepository = configurationRepository;
		this.ldap = ldap;
	}

	@RequestMapping(value = "/api/application-configuration/", method = RequestMethod.GET)
	public Map<Key, String> findAll() {
		Map<Key, String> res = new EnumMap<>(Key.class);
		for (ConfigurationKeyValue kv : configurationRepository.findAll()) {
			res.put(kv.getFirst(), kv.getSecond());
		}
		return res;
	}

	@RequestMapping(value = "/api/application-configuration/{key}", method = RequestMethod.GET)
	public ConfigurationKeyValue findByKey(@PathVariable("key") Key key) {
		String value = configurationRepository.hasKeyDefined(key) ? configurationRepository.getValue(key) : null;
		return new ConfigurationKeyValue(key, value);
	}

	@RequestMapping(value = "/api/application-configuration/{key}", method = RequestMethod.DELETE)
	public void deleteByKey(@PathVariable("key") Key key) {
		configurationRepository.delete(key);
	}

	@RequestMapping(value = "/api/application-configuration/", method = RequestMethod.POST)
	public void setKeyValue(@RequestBody Conf conf) {
		configurationRepository.updateOrCreate(conf.getToUpdateOrCreate());
	}

	@RequestMapping(value = "/api/check-ldap/", method = RequestMethod.POST)
	public Pair<Boolean, List<String>> checkLdap(@RequestBody Map<String, String> r) {
		return ldap.authenticateWithParams(r.get("serverUrl"), r.get("managerDn"), r.get("managerPassword"),
				r.get("userSearchBase"), r.get("userSearchFilter"), r.get("username"), r.get("password"));
	}

	@RequestMapping(value = "/api/check-smtp/", method = RequestMethod.POST)
	public void checkSmtp(@RequestBody MailConfig mailConfig, @RequestParam("to") String to) {
		mailConfig.send(to, "LAVAGNA: TEST", "TEST");
	}
}
