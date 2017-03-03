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

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import io.lavagna.model.ConfigurationKeyValue;

import java.util.List;
import java.util.Set;

@QueryRepository
public interface ConfigurationQuery {

	@Query("SELECT COUNT(*) FROM LA_CONF WHERE CONF_KEY = :key")
	Integer hasKeyDefined(@Bind("key") String key);

	@Query("SELECT * FROM LA_CONF WHERE CONF_KEY IN (:keys)")
	List<ConfigurationKeyValue> findConfigurationFor(@Bind("keys") Set<String> keys);

	@Query("SELECT CONF_VALUE FROM LA_CONF WHERE CONF_KEY = :key")
	List<String> getValue(@Bind("key") String key);

	@Query("INSERT INTO LA_CONF(CONF_KEY, CONF_VALUE) VALUES(:key, :value)")
	int set(@Bind("key") String key, @Bind("value") String value);

	@Query("UPDATE LA_CONF SET CONF_VALUE = :value WHERE CONF_KEY = :key")
	int update(@Bind("key") String key, @Bind("value") String value);

	@Query("SELECT * FROM LA_CONF ORDER BY CONF_KEY")
	List<ConfigurationKeyValue> findAll();

	@Query("DELETE FROM LA_CONF WHERE CONF_KEY = :key")
	int delete(@Bind("key") String key);
}
