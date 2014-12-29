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

import io.lavagna.common.ConstructorAnnotationRowMapper.Column;

import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
public class User {

	private final int id;
	private final String provider;
	private final String username;
	private final String email;
	private final String displayName;
	private final boolean enabled;
	private final boolean emailNotification;
	private final Date memberSince;

	public User(@Column("USER_ID") int id, @Column("USER_PROVIDER") String provider,
			@Column("USER_NAME") String username, @Column("USER_EMAIL") String email,
			@Column("USER_DISPLAY_NAME") String displayName, @Column("USER_ENABLED") Boolean enabled,
			@Column("USER_EMAIL_NOTIFICATION") boolean emailNotification, @Column("USER_MEMBER_SINCE") Date memberSince) {
		this.id = id;
		this.username = username;
		this.provider = provider;
		this.email = email;
		this.displayName = displayName;
		this.enabled = enabled == null ? true : enabled;
		this.emailNotification = emailNotification;
		this.memberSince = memberSince;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof User)) {
			return false;
		}
		User u = (User) obj;
		return new EqualsBuilder().append(id, u.id).append(provider, u.provider).append(username, u.username)
				.append(email, u.email).append(displayName, u.displayName).append(enabled, u.enabled)
				.append(emailNotification, u.emailNotification).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).append(provider).append(username).append(email).append(displayName)
				.append(enabled).append(emailNotification).toHashCode();
	}

	public boolean isAnonymous() {
		return "system".equals(provider) && "anonymous".equals(username);
	}

	public boolean canSendEmail() {
		return enabled && emailNotification && StringUtils.isNotBlank(email);
	}
}
