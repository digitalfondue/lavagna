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
package io.lavagna.model;

import java.util.Locale;
import java.util.Objects;

import lombok.Getter;

@Getter
public final class Role {

	private final String name;

	public Role(String name) {
		this.name = name.toUpperCase(Locale.ENGLISH);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && obj instanceof Role) ? Objects.equals(name, ((Role) obj).name) : false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public static final Role ADMIN_ROLE = new Role("ADMIN");
	public static final Role DEFAULT_ROLE = new Role("DEFAULT");
}
