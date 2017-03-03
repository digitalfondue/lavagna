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

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.ConfigurationKeyValue;
import io.lavagna.model.Key;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class ConfigurationRepositoryTest {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private NamedParameterJdbcTemplate jdbc;

	@Before
	public void prepare() {
		jdbc.update("DELETE FROM LA_CONF", new EmptySqlParameterSource());
	}

	@Test
	public void testHasKeyDefined() {
		Assert.assertFalse(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Assert.assertTrue(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
	}

	@Test(expected = DuplicateKeyException.class)
	public void insertDuplicateKey() {
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST1");
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST2");
	}

	@Test
	public void testGetValue() {
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Assert.assertEquals("TEST", configurationRepository.getValue(Key.TEST_PLACEHOLDER));
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testGetValueNotFound() {
		configurationRepository.getValue(Key.TEST_PLACEHOLDER);
	}

	@Test
	public void updateOrCreate() {
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "test");

		Assert.assertTrue(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
		Assert.assertFalse(configurationRepository.hasKeyDefined(Key.SETUP_COMPLETE));

		configurationRepository.updateOrCreate(Arrays.asList(new ConfigurationKeyValue(Key.TEST_PLACEHOLDER, "test2"),
				new ConfigurationKeyValue(Key.SETUP_COMPLETE, "true")));

		Assert.assertEquals("test2", configurationRepository.getValue(Key.TEST_PLACEHOLDER));
		Assert.assertEquals("true", configurationRepository.getValue(Key.SETUP_COMPLETE));
	}

	@Test
	public void findAll() {
		Assert.assertTrue(configurationRepository.findAll().isEmpty());
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Assert.assertFalse(configurationRepository.findAll().isEmpty());
		Assert.assertEquals(1, configurationRepository.findAll().size());
		configurationRepository.insert(Key.PERSONA_AUDIENCE, "test");
		Assert.assertEquals(2, configurationRepository.findAll().size());
	}

	@Test
    public void testGetValueOrNull() {
	    Assert.assertNull(configurationRepository.getValueOrNull(Key.TEST_PLACEHOLDER));
	    configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
	    Assert.assertEquals("TEST", configurationRepository.getValueOrNull(Key.TEST_PLACEHOLDER));
	}

	@Test
	public void findFor() {
		Map<Key, String> v = configurationRepository.findConfigurationFor(EnumSet.of(Key.TEST_PLACEHOLDER,
				Key.PERSONA_AUDIENCE));
		Assert.assertNull(v.get(Key.TEST_PLACEHOLDER));
		Assert.assertNull(v.get(Key.PERSONA_AUDIENCE));
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Map<Key, String> v1 = configurationRepository.findConfigurationFor(EnumSet.of(Key.TEST_PLACEHOLDER,
				Key.PERSONA_AUDIENCE));
		Assert.assertEquals("TEST", v1.get(Key.TEST_PLACEHOLDER));
		Assert.assertNull(v1.get(Key.PERSONA_AUDIENCE));
	}

	@Test
	public void delete() {
		Assert.assertFalse(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Assert.assertTrue(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
		configurationRepository.delete(Key.TEST_PLACEHOLDER);
		Assert.assertFalse(configurationRepository.hasKeyDefined(Key.TEST_PLACEHOLDER));
	}

	@Test
	public void testUpdate() {
		configurationRepository.insert(Key.TEST_PLACEHOLDER, "TEST");
		Assert.assertEquals("TEST", configurationRepository.getValue(Key.TEST_PLACEHOLDER));
		configurationRepository.update(Key.TEST_PLACEHOLDER, "TEST-UPDATED");
		Assert.assertEquals("TEST-UPDATED", configurationRepository.getValue(Key.TEST_PLACEHOLDER));
	}
}
