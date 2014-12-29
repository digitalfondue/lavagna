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
package io.lavagna.model.util;

import org.junit.Assert;
import org.junit.Test;

public class ShortNameGeneratorTest {
	
	@Test
	public void testShortNameGenerator() {

		Assert.assertEquals(null, ShortNameGenerator.generateShortNameFrom(null));
		Assert.assertEquals("", ShortNameGenerator.generateShortNameFrom(""));
		Assert.assertEquals("   ", ShortNameGenerator.generateShortNameFrom("   "));

		Assert.assertEquals("TEST", ShortNameGenerator.generateShortNameFrom("test"));

		Assert.assertEquals("TESTTEST", ShortNameGenerator.generateShortNameFrom("test test"));

		Assert.assertEquals("TESTTE", ShortNameGenerator.generateShortNameFrom("testtest"));

		Assert.assertEquals("TESTTE", ShortNameGenerator.generateShortNameFrom("testtesttesttesttesttesttesttesttesttesttesttest"));

		//
		Assert.assertEquals("TESTDHD", ShortNameGenerator.generateShortNameFrom("test DerpoHurpoDurr"));

		Assert.assertEquals("TESTTEST", ShortNameGenerator.generateShortNameFrom("test test test test test"));

		// token has length <= 4
		Assert.assertEquals("TESTTEST", ShortNameGenerator.generateShortNameFrom("test TeSt Test test tEst"));

		Assert.assertEquals("TESTSTES", ShortNameGenerator.generateShortNameFrom("testa TeSta Testa testa tEsta"));
	}

}
