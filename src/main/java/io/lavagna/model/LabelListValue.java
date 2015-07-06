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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
@EqualsAndHashCode
public class LabelListValue {

	private final int id;
	private final int cardLabelId;
	private final int order;
	private final String value;

	public LabelListValue(@Column("CARD_LABEL_LIST_VALUE_ID") int id, @Column("CARD_LABEL_ID_FK") int cardLabelId,
			@Column("CARD_LABEL_LIST_VALUE_ORDER") int order, @Column("CARD_LABEL_LIST_VALUE") String value) {
		this.id = id;
		this.cardLabelId = cardLabelId;
		this.order = order;
		this.value = value;
	}


	public LabelListValue newValue(String newValue) {
		return new LabelListValue(id, cardLabelId, order, newValue);
	}
}
