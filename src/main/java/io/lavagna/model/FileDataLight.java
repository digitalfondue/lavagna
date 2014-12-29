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

@Getter
public class FileDataLight {
	private final Integer referenceId;
	private final int cardId;
	private final int cardDataId;
	private final String digest;
	private final int userId;
	private final Date time;
	private final int size;
	private final String name;
	private final String contentType;

	public FileDataLight(@Column("CARD_DATA_CARD_ID_FK") int cardId, @Column("CARD_DATA_ID") int cardDataId,
			@Column("CARD_DATA_REFERENCE_ID") Integer referenceId, @Column("CARD_DATA_CONTENT") String digest,
			@Column("SIZE") int size, @Column("DISPLAYED_NAME") String name,
			@Column("CONTENT_TYPE") String contentType, @Column("EVENT_USER_ID_FK") int userId,
			@Column("EVENT_TIME") Date time) {
		this.cardId = cardId;
		this.cardDataId = cardDataId;
		this.referenceId = referenceId;
		this.digest = digest;
		this.size = size;
		this.name = name;
		this.contentType = contentType;
		this.userId = userId;
		this.time = time;
	}
}