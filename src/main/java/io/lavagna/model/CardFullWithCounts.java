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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.apache.commons.codec.digest.DigestUtils;

@Getter
public class CardFullWithCounts extends CardFull {

	private final Integer creationUser;
	private final Date creationDate;
	private final Map<String, CardDataCount> counts;
	private final List<LabelAndValue> labels;
	private final String hash;

	public CardFullWithCounts(CardFull cardInfo, Map<String, CardDataCount> counts, List<LabelAndValue> labels) {
		super(cardInfo.getId(), cardInfo.getName(), cardInfo.getSequence(), cardInfo.getOrder(),
				cardInfo.getColumnId(), cardInfo.getUserId(), cardInfo.getCreateTime(), cardInfo.getLastUpdateUserId(),
				cardInfo.getLastUpdateTime(), cardInfo.getColumnDefinition(), cardInfo.getBoardShortName(), cardInfo
						.getProjectShortName());
		this.counts = counts;
		this.labels = labels == null ? Collections.<LabelAndValue> emptyList() : labels;
		// FIXME: this data is already contained in CardFull, leaving it here for retrocompatibility
		this.creationUser = cardInfo.getUserId();
		this.creationDate = cardInfo.getCreateTime();

		hash = hash(this);
	}

	private static String hash(CardFullWithCounts cwc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream daos = new DataOutputStream(baos);

		try {
			// card
			daos.writeChars(Integer.toString(cwc.getId()));

			writeNotNull(daos, cwc.getName());
			writeInts(daos, cwc.getSequence(), cwc.getOrder(), cwc.getColumnId(), cwc.getUserId());
			// end card
			writeNotNull(daos, cwc.creationUser);
			writeNotNull(daos, cwc.creationDate);

			if (cwc.counts != null) {
				for (Map.Entry<String, CardDataCount> count : cwc.counts.entrySet()) {
					writeNotNull(daos, count.getKey());
					CardDataCount dataCount = count.getValue();
					daos.writeChars(Integer.toString(dataCount.getCardId()));
					if (dataCount.getCount() != null) {
						daos.writeChars(Long.toString(dataCount.getCount().longValue()));
					}
					writeNotNull(daos, dataCount.getType());
				}
			}
			for (LabelAndValue lv : cwc.labels) {
				//
				writeInts(daos, lv.getLabelId(), lv.getLabelProjectId());
				writeNotNull(daos, lv.getLabelName());
				daos.writeChars(Integer.toString(lv.getLabelColor()));
				writeEnum(daos, lv.getLabelType());
				writeEnum(daos, lv.getLabelDomain());
				//
				writeInts(daos, lv.getLabelValueId(), lv.getLabelValueCardId(), lv.getLabelValueLabelId());
				writeNotNull(daos, lv.getLabelValueUseUniqueIndex());
				writeEnum(daos, lv.getLabelValueType());
				writeNotNull(daos, lv.getLabelValueString());
				writeNotNull(daos, lv.getLabelValueTimestamp());
				writeNotNull(daos, lv.getLabelValueInt());
				writeNotNull(daos, lv.getLabelValueCard());
				writeNotNull(daos, lv.getLabelValueUser());
			}
			daos.flush();

			return DigestUtils.sha256Hex(new ByteArrayInputStream(baos.toByteArray()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void writeInts(DataOutputStream daos, int... e) throws IOException {
		for (int i : e) {
			daos.writeChars(Integer.toString(i));
		}
	}

	private static void writeNotNull(DataOutputStream daos, Boolean s) throws IOException {
		if (s != null) {
			daos.writeChars(Boolean.toString(s));
		}
	}

	private static void writeNotNull(DataOutputStream daos, Date s) throws IOException {
		if (s != null) {
			daos.writeChars(Long.toString(s.getTime()));
		}
	}

	private static void writeNotNull(DataOutputStream daos, String s) throws IOException {
		if (s != null) {
			daos.writeChars(s);
		}
	}

	private static void writeNotNull(DataOutputStream daos, Integer val) throws IOException {
		if (val != null) {
			daos.writeChars(Integer.toString(val));
		}
	}

	private static <T extends Enum<T>> void writeEnum(DataOutputStream daos, T e) throws IOException {
		if (e != null) {
			daos.writeChars(e.toString());
		}
	}
}
