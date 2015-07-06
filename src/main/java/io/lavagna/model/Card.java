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
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class Card {

	private final int id;
	private final String name;
	private final int sequence;// sequence number, public identifier
	private final int order;
	private final int columnId;
	private final int userId;

	public Card(@Column("CARD_ID") int id,//
			@Column("CARD_NAME") String name,//
			@Column("CARD_SEQ_NUMBER") int sequence,//
			@Column("CARD_ORDER") int order,//
			@Column("CARD_BOARD_COLUMN_ID_FK") int columnId,//
			@Column("CARD_USER_ID_FK") int userId) {
		this.id = id;
		this.name = name;
		this.sequence = sequence;
		this.order = order;
		this.columnId = columnId;
		this.userId = userId;
	}
}