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

import io.lavagna.common.LavagnaEnvironment;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import javax.sql.DataSource;

public class DatabaseMigrator {

	public DatabaseMigrator(LavagnaEnvironment env, DataSource dataSource, MigrationVersion target) {
		if (canMigrate(env)) {
			doMigration(env, dataSource, target);
		}
	}

	private void doMigration(LavagnaEnvironment env, DataSource dataSource, MigrationVersion version) {
		String sqlDialect = env.getRequiredProperty("datasource.dialect");
		Flyway migration = new Flyway();
		migration.setDataSource(dataSource);
		// FIXME remove the validation = false when the schemas will be stable
		migration.setValidateOnMigrate(false);
		//

		migration.setTarget(version);

		migration.setLocations("io/lavagna/db/" + sqlDialect + "/");
		migration.migrate();
	}

	private boolean canMigrate(LavagnaEnvironment env) {
		return !"true".equals(env.getProperty("datasource.disable.migration"));
	}
}
