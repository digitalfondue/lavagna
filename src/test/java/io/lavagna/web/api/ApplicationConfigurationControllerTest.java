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

import io.lavagna.model.ConfigurationKeyValue;
import io.lavagna.model.Key;
import io.lavagna.model.MailConfig;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.web.api.model.Conf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConfigurationControllerTest {

	@Mock
	private ConfigurationRepository configurationRepository;
	@Mock
	private Ldap ldap;
	@Mock
	private Map<String, String> ldapParams;
	@Mock
	private HttpServletRequest req;

	private ApplicationConfigurationController applConfCtrl;

	@Before
	public void prepare() {
		applConfCtrl = new ApplicationConfigurationController(configurationRepository, ldap);
	}

	@Test
	public void findAll() {
		when(configurationRepository.findAll()).thenReturn(
				Arrays.asList(new ConfigurationKeyValue(Key.TEST_PLACEHOLDER, "value")));
		applConfCtrl.findAll();
		verify(configurationRepository).findAll();
	}

	@Test
	public void findByKey() {
		when(configurationRepository.hasKeyDefined(Key.SETUP_COMPLETE)).thenReturn(true);
		when(configurationRepository.getValue(Key.SETUP_COMPLETE)).thenReturn("true");
		ConfigurationKeyValue findByKey = applConfCtrl.findByKey(Key.SETUP_COMPLETE);
		Assert.assertEquals(Key.SETUP_COMPLETE, findByKey.getFirst());
		Assert.assertEquals("true", findByKey.getSecond());
		verify(configurationRepository).hasKeyDefined(Key.SETUP_COMPLETE);
		verify(configurationRepository).getValue(Key.SETUP_COMPLETE);

	}

	@Test
	public void findByKeyNotFound() {
		ConfigurationKeyValue findByKey = applConfCtrl.findByKey(Key.SETUP_COMPLETE);
		Assert.assertEquals(Key.SETUP_COMPLETE, findByKey.getFirst());
		Assert.assertNull(findByKey.getSecond());
		verify(configurationRepository).hasKeyDefined(Key.SETUP_COMPLETE);
	}

	@Test
	public void setKeyValue() {
		Conf conf = new Conf();
		conf.setToUpdateOrCreate(Arrays.asList(new ConfigurationKeyValue(Key.SETUP_COMPLETE, "true")));
		applConfCtrl.setKeyValue(conf);
		verify(configurationRepository).updateOrCreate(conf.getToUpdateOrCreate());
	}

	@Test
	public void checkLdap() {
		applConfCtrl.checkLdap(ldapParams);
		for (String s : Arrays.asList("serverUrl", "managerDn", "managerPassword", "userSearchBase",
				"userSearchFilter", "username", "password")) {
			verify(ldapParams).get(s);
		}
	}

	@Test
	public void checkEmail() {
		MailConfig mc = mock(MailConfig.class);
		applConfCtrl.checkSmtp(mc, "test@test.test");
		verify(mc).send(any(String.class), any(String.class), any(String.class));
	}
}
