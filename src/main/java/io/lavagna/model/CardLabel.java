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

import lombok.Getter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class CardLabel {

	private final int id;
	private final int projectId;
	private final String name;
	private final int color;
	private final boolean unique;
	private final LabelType type;
	private final LabelDomain domain;

	public CardLabel(@Column("CARD_LABEL_ID") int id, @Column("CARD_LABEL_PROJECT_ID_FK") int projectId,
			@Column("CARD_LABEL_UNIQUE") boolean unique, @Column("CARD_LABEL_TYPE") LabelType type,
			@Column("CARD_LABEL_DOMAIN") LabelDomain domain, @Column("CARD_LABEL_NAME") String name,
			@Column("CARD_LABEL_COLOR") int color) {
		this.id = id;
		this.projectId = projectId;
		this.unique = unique;
		this.name = name;
		this.color = color;
		this.type = type;
		this.domain = domain;
	}

	public CardLabel name(String newName) {
		return new CardLabel(id, projectId, unique, type, domain, newName, color);
	}

	public CardLabel color(int newColor) {
		return new CardLabel(id, projectId, unique, type, domain, name, newColor);
	}

	public CardLabel set(String newName, LabelType newType, int newColor) {
		return new CardLabel(id, projectId, unique, newType, domain, newName, newColor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CardLabel)) {
			return false;
		}
		CardLabel other = (CardLabel) obj;
		return new EqualsBuilder().append(id, other.id).append(projectId, other.projectId).append(unique, other.unique)
				.append(type, other.type).append(domain, other.domain).append(name, other.name)
				.append(color, other.color).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).append(projectId).append(unique).append(type).append(domain)
				.append(name).append(color).toHashCode();
	}

	public enum LabelDomain {
		SYSTEM, USER
	}

	public enum LabelType {
		NULL, STRING, TIMESTAMP, INT, CARD, USER, LIST
	}
}
