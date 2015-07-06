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

import java.util.Date;

import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public class CardFull extends Card {

	private final Date createTime;
	private final Date lastUpdateTime;
	private final int lastUpdateUserId;
	private final String projectShortName;
	private final String boardShortName;
	private final ColumnDefinition columnDefinition;

	public CardFull(
			@Column("CARD_ID") int id,//
			@Column("CARD_NAME") String name,//
			@Column("CARD_SEQ_NUMBER") int sequence,//
			@Column("CARD_ORDER") int order,//
			@Column("CARD_BOARD_COLUMN_ID_FK") int columnId,//
			@Column("CREATE_USER") int createUserId,//
			@Column("CREATE_TIME") Date createTime,//
			@Column("LAST_UPDATE_USER") int lastUpdateUserId, @Column("LAST_UPDATE_TIME") Date lastUpdateTime,
			@Column("BOARD_COLUMN_DEFINITION_VALUE") ColumnDefinition columnDefinition,
			@Column("BOARD_SHORT_NAME") String boardShortName, @Column("PROJECT_SHORT_NAME") String projectShortName) {
		super(id, name, sequence, order, columnId, createUserId);
		this.createTime = createTime;
		this.lastUpdateTime = lastUpdateTime;
		this.lastUpdateUserId = lastUpdateUserId;
		this.projectShortName = projectShortName;
		this.boardShortName = boardShortName;
		this.columnDefinition = columnDefinition;
	}
}
