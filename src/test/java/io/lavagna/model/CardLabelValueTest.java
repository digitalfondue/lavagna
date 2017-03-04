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
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class CardLabelValueTest {

	@Test
	public void testValues() {
		CardLabelValue initial = new CardLabelValue(42, 42, 42, false, LabelType.NULL, null, null, null, null, null,
				null);

		Date dNow = new Date();
		CardLabelValue d = initial.newValue(dNow);
		checkTimestampValue(d, dNow);

		CardLabelValue n = d.newNullValue();
		checkNullValue(n);

		CardLabelValue i = d.newValue(42);
		checkIntValue(i, 42);

		CardLabelValue s = d.newValue("test");
		checkStringValue(s, "test");

		CardLabelValue n1 = s.newValue(LabelType.NULL,
				new CardLabelValue.LabelValue(null, null, null, null, null, null));
		checkNullValue(n1);

		Date d1Now = new Date();
		CardLabelValue d1 = s.newValue(LabelType.TIMESTAMP, new CardLabelValue.LabelValue(null, d1Now, null, null,
				null, null));
		checkTimestampValue(d1, d1Now);

		CardLabelValue s1 = s.newValue(LabelType.STRING, new CardLabelValue.LabelValue("string"));
		checkStringValue(s1, "string");

		CardLabelValue i1 = s.newValue(LabelType.INT, new CardLabelValue.LabelValue(null, null, 84, null, null, null));
		checkIntValue(i1, 84);

		CardLabelValue c1 = s.newValue(LabelType.CARD, new CardLabelValue.LabelValue(null, null, null, 84, null, null));
		checkCardValue(c1, 84);

		CardLabelValue u1 = s.newValue(LabelType.USER, new CardLabelValue.LabelValue(null, null, null, null, 84, null));
		checkUserValue(u1, 84);
	}

	private void checkStringValue(CardLabelValue s, String string) {
		Assert.assertNotNull(s.getValue().getValueString());
		Assert.assertEquals(string, s.getValue().getValueString());
		Assert.assertNull(s.getValue().getValueInt());
		Assert.assertNull(s.getValue().getValueTimestamp());
		Assert.assertNull(s.getValue().getValueCard());
		Assert.assertNull(s.getValue().getValueUser());
	}

	private void checkTimestampValue(CardLabelValue d, Date dNow) {
		Assert.assertNotNull(d.getValue().getValueTimestamp());
		Assert.assertEquals(dNow, d.getValue().getValueTimestamp());
		Assert.assertNull(d.getValue().getValueString());
		Assert.assertNull(d.getValue().getValueInt());
		Assert.assertNull(d.getValue().getValueCard());
		Assert.assertNull(d.getValue().getValueUser());
	}

	private void checkIntValue(CardLabelValue i, Integer j) {
		Assert.assertNotNull(i.getValue().getValueInt());
		Assert.assertEquals(j, i.getValue().getValueInt());
		Assert.assertNull(i.getValue().getValueTimestamp());
		Assert.assertNull(i.getValue().getValueString());
		Assert.assertNull(i.getValue().getValueCard());
		Assert.assertNull(i.getValue().getValueUser());
	}

	private void checkCardValue(CardLabelValue i, Integer j) {
		Assert.assertNotNull(i.getValue().getValueCard());
		Assert.assertEquals(j, i.getValue().getValueCard());
		Assert.assertNull(i.getValue().getValueTimestamp());
		Assert.assertNull(i.getValue().getValueString());
		Assert.assertNull(i.getValue().getValueInt());
		Assert.assertNull(i.getValue().getValueUser());
	}

	private void checkUserValue(CardLabelValue i, Integer j) {
		Assert.assertNotNull(i.getValue().getValueUser());
		Assert.assertEquals(j, i.getValue().getValueUser());
		Assert.assertNull(i.getValue().getValueTimestamp());
		Assert.assertNull(i.getValue().getValueString());
		Assert.assertNull(i.getValue().getValueCard());
		Assert.assertNull(i.getValue().getValueInt());
	}

	private void checkNullValue(CardLabelValue n) {
		Assert.assertNull(n.getValue().getValueTimestamp());
		Assert.assertNull(n.getValue().getValueString());
		Assert.assertNull(n.getValue().getValueInt());
		Assert.assertNull(n.getValue().getValueCard());
		Assert.assertNull(n.getValue().getValueUser());
	}

}
