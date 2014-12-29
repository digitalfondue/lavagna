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

import org.junit.Assert;
import org.junit.Test;

public class RoleTest {

	@Test
	public void testEquality() {
		Role r = new Role("test");
		Role r2 = new Role("TEST");
		Role r3 = new Role("NOT_TEST");

		Assert.assertFalse(r.equals(null));
		Assert.assertFalse(r.equals(new Object()));

		Assert.assertEquals(r, r2);
		Assert.assertNotEquals(r, r3);

		Assert.assertEquals(r.hashCode(), r2.hashCode());
		Assert.assertNotEquals(r.hashCode(), r3.hashCode());
	}
}
