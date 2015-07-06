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

import io.lavagna.model.CardLabel.LabelType;
import lombok.Getter;
import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column;

@Getter
public final class LabelAndValueWithCount {

	private final int labelId;
	private final String labelName;
	private final int labelColor;
	private final LabelType labelValueType;
	private final CardLabelValue.LabelValue value;
	private final Long count;

	public LabelAndValueWithCount(@Column("CARD_LABEL_ID") int labelId, @Column("CARD_LABEL_NAME") String labelName,
			@Column("CARD_LABEL_COLOR") int labelColor, @Column("CARD_LABEL_VALUE_TYPE") LabelType labelValueType,
			@Column("CARD_LABEL_VALUE_LIST_VALUE_FK") Integer labelValueList, @Column("LABEL_COUNT") Long count) {

		this.labelId = labelId;
		this.labelName = labelName;
		this.labelColor = labelColor;
		if (labelValueType != LabelType.NULL && labelValueType != LabelType.LIST) {
			labelValueType = LabelType.NULL;
		}
		this.labelValueType = labelValueType;
		this.count = count;
		this.value = new CardLabelValue.LabelValue(null, null, null, null, null, labelValueList);
	}
}