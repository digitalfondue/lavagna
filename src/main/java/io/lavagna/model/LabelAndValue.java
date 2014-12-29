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
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.CardLabelValue.LabelValue;

import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang3.Validate;

@Getter
public class LabelAndValue {

	private final int labelId;
	private final int labelProjectId;
	private final String labelName;
	private final int labelColor;
	private final LabelType labelType;
	private final LabelDomain labelDomain;
	private final int labelValueId;
	private final int labelValueCardId;
	private final int labelValueLabelId;
	private final Boolean labelValueUseUniqueIndex;
	private final LabelType labelValueType;
	private final String labelValueString;
	private final Date labelValueTimestamp;
	private final Integer labelValueInt;
	private final Integer labelValueCard;
	private final Integer labelValueUser;
	private final Integer labelValueList;
	private final LabelValue value;
	private final boolean labelUnique;

	public LabelAndValue(@Column("CARD_LABEL_ID") int labelId, @Column("CARD_LABEL_PROJECT_ID_FK") int labelProjectId,
			@Column("CARD_LABEL_UNIQUE") boolean labelUnique, @Column("CARD_LABEL_TYPE") LabelType labelType,
			@Column("CARD_LABEL_DOMAIN") LabelDomain labelDomain, @Column("CARD_LABEL_NAME") String labelName,
			@Column("CARD_LABEL_COLOR") int labelColor, @Column("CARD_LABEL_VALUE_ID") int labelValueId,
			@Column("CARD_ID_FK") int labelValueCardId, @Column("CARD_LABEL_ID_FK") int labelValueLabelId,
			@Column("CARD_LABEL_VALUE_USE_UNIQUE_INDEX") Boolean labelValueUseUniqueIndex,
			@Column("CARD_LABEL_VALUE_TYPE") LabelType labelValueType,
			@Column("CARD_LABEL_VALUE_STRING") String labelValueString,
			@Column("CARD_LABEL_VALUE_TIMESTAMP") Date labelValueTimestamp,
			@Column("CARD_LABEL_VALUE_INT") Integer labelValueInt,
			@Column("CARD_LABEL_VALUE_CARD_FK") Integer labelValueCard,
			@Column("CARD_LABEL_VALUE_USER_FK") Integer labelValueUser,
			@Column("CARD_LABEL_VALUE_LIST_VALUE_FK") Integer labelValueList) {

		Validate.isTrue(labelType == labelValueType, "label type is not equal to label value type");

		this.labelId = labelId;
		this.labelProjectId = labelProjectId;
		this.labelUnique = labelUnique;
		this.labelType = labelType;
		this.labelDomain = labelDomain;
		this.labelName = labelName;
		this.labelColor = labelColor;
		// /
		this.labelValueId = labelValueId;
		this.labelValueCardId = labelValueCardId;
		this.labelValueLabelId = labelValueLabelId;
		this.labelValueUseUniqueIndex = labelValueUseUniqueIndex;
		this.labelValueType = labelValueType;
		this.labelValueString = labelValueString;
		this.labelValueTimestamp = labelValueTimestamp;
		this.labelValueInt = labelValueInt;
		this.labelValueCard = labelValueCard;
		this.labelValueUser = labelValueUser;
		this.labelValueList = labelValueList;

		this.value = new LabelValue(labelValueString, labelValueTimestamp, labelValueInt, labelValueCard,
				labelValueUser, labelValueList);
	}

	public CardLabel label() {
		return new CardLabel(labelId, labelProjectId, labelUnique, labelType, labelDomain, labelName, labelColor);
	}

	public CardLabelValue labelValue() {
		return new CardLabelValue(labelValueId, labelValueCardId, labelValueLabelId, labelValueUseUniqueIndex,
				labelValueType, labelValueString, labelValueTimestamp, labelValueInt, labelValueCard, labelValueUser,
				labelValueList);
	}
}