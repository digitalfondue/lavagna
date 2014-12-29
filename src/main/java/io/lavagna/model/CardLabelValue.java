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
import io.lavagna.model.CardLabel.LabelType;

import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
public class CardLabelValue {

	private final int cardLabelValueId;
	private final int cardId;
	private final int labelId;
	private final Boolean useUniqueIndex;
	private final LabelType labelValueType;
	private final LabelValue value;

	public CardLabelValue(@Column("CARD_LABEL_VALUE_ID") int cardLabelValueId, @Column("CARD_ID_FK") int cardId,
			@Column("CARD_LABEL_ID_FK") int labelId,
			@Column("CARD_LABEL_VALUE_USE_UNIQUE_INDEX") Boolean useUniqueIndex,
			@Column("CARD_LABEL_VALUE_TYPE") LabelType labelValueType,
			@Column("CARD_LABEL_VALUE_STRING") String valueString,
			@Column("CARD_LABEL_VALUE_TIMESTAMP") Date valueDate, @Column("CARD_LABEL_VALUE_INT") Integer valueInt,
			@Column("CARD_LABEL_VALUE_CARD_FK") Integer valueCard,
			@Column("CARD_LABEL_VALUE_USER_FK") Integer valueUser,
			@Column("CARD_LABEL_VALUE_LIST_VALUE_FK") Integer valueList) {
		this.cardLabelValueId = cardLabelValueId;
		this.cardId = cardId;
		this.labelId = labelId;
		this.useUniqueIndex = useUniqueIndex;
		this.labelValueType = labelValueType;
		this.value = new LabelValue(valueString, valueDate, valueInt, valueCard, valueUser, valueList);
	}

	public CardLabelValue newValue(String value) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.STRING, value, null,
				null, null, null, null);
	}

	public CardLabelValue newValue(Date date) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.TIMESTAMP, null, date,
				null, null, null, null);
	}

	public CardLabelValue newValue(Integer integer) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.INT, null, null,
				integer, null, null, null);
	}

	public CardLabelValue newCardValue(Integer cardId) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.CARD, null, null, null,
				cardId, null, null);
	}

	public CardLabelValue newUserValue(Integer userId) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.USER, null, null, null,
				null, userId, null);
	}

	public CardLabelValue newListValue(Integer listId) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.CARD, null, null, null,
				null, null, listId);
	}

	public CardLabelValue newNullValue() {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, LabelType.NULL, null, null, null,
				null, null, null);
	}

	public CardLabelValue newValue(LabelType type, LabelValue value) {
		return new CardLabelValue(cardLabelValueId, cardId, labelId, useUniqueIndex, type, value.valueString,
				value.valueTimestamp, value.valueInt, value.valueCard, value.valueUser, value.valueList);
	}

	@Getter
	public static class LabelValue {
		private final String valueString;
		private final Date valueTimestamp;
		private final Integer valueInt;
		private final Integer valueCard;
		private final Integer valueUser;
		private final Integer valueList;

		public LabelValue(@Column("CARD_LABEL_VALUE_STRING") String valueString,
				@Column("CARD_LABEL_VALUE_TIMESTAMP") Date valueTimestamp,
				@Column("CARD_LABEL_VALUE_INT") Integer valueInt,
				@Column("CARD_LABEL_VALUE_CARD_FK") Integer valueCard,
				@Column("CARD_LABEL_VALUE_USER_FK") Integer valueUser,
				@Column("CARD_LABEL_VALUE_LIST_VALUE_FK") Integer valueList) {
			this.valueString = valueString;
			this.valueTimestamp = valueTimestamp;
			this.valueInt = valueInt;
			this.valueCard = valueCard;
			this.valueUser = valueUser;
			this.valueList = valueList;
		}

		public LabelValue(String valueString) {
			this(valueString, null, null, null, null, null);
		}

		public LabelValue(Date valueTimestamp) {
			this(null, valueTimestamp, null, null, null, null);
		}

		public LabelValue(Integer valueUser) {
			this(null, null, null, null, valueUser, null);
		}

		public LabelValue() {
			this(null, null, null, null, null, null);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof LabelValue)) {
				return false;
			}
			LabelValue lv = (LabelValue) obj;
			return new EqualsBuilder().append(valueString, lv.valueString).append(valueTimestamp, lv.valueTimestamp)
					.append(valueInt, lv.valueInt).append(valueCard, lv.valueCard).append(valueUser, lv.valueUser)
					.append(valueList, lv.valueList).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(valueString).append(valueTimestamp).append(valueInt).append(valueCard)
					.append(valueUser).append(valueList).toHashCode();
		}
	}
}
