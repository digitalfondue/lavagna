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
import io.lavagna.model.Key;
import io.lavagna.query.ConfigurationQuery;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional(readOnly = true)
public class ConfigurationRepository {

	private final ConfigurationQuery queries;

	public ConfigurationRepository(ConfigurationQuery queries) {
		this.queries = queries;
	}

	public List<ConfigurationKeyValue> findAll() {
		return queries.findAll();
	}

	public Map<Key, String> findConfigurationFor(Set<Key> keys) {
		Set<String> s = new HashSet<>();
		Map<Key, String> res = new EnumMap<>(Key.class);
		for (Key k : keys) {
			s.add(k.toString());
			res.put(k, null);
		}

		for (ConfigurationKeyValue kv : queries.findConfigurationFor(s)) {
			res.put(kv.getFirst(), kv.getSecond());
		}
		return res;
	}

	public boolean hasKeyDefined(Key key) {
		return !Integer.valueOf(0).equals(queries.hasKeyDefined(key.toString()));
	}

	public String getValueOrNull(Key key) {
		List<String> res = queries.getValue(key.toString());
		return res.isEmpty() ? null : res.get(0);
	}

	public String getValue(Key key) {
		List<String> res = queries.getValue(key.toString());
		if (res.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		} else {
			return res.get(0);
		}
	}

	@Transactional(readOnly = false)
	public void insert(Key key, String value) {
		queries.set(key.toString(), value);
	}

	@Transactional(readOnly = false)
	public void update(Key key, String value) {
		queries.update(key.toString(), value);
	}

	@Transactional(readOnly = false)
	public void delete(Key key) {
		queries.delete(key.toString());
	}

	@Transactional(readOnly = false)
	public void updateOrCreate(List<ConfigurationKeyValue> toUpdateOrCreate) {
		for (ConfigurationKeyValue kv : toUpdateOrCreate) {
			if (hasKeyDefined(kv.getFirst())) {
				update(kv.getFirst(), kv.getSecond());
			} else {
				insert(kv.getFirst(), kv.getSecond());
			}
		}
	}
}
