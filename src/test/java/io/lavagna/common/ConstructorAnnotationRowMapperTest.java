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
package io.lavagna.common;

import io.lavagna.common.ConstructorAnnotationRowMapper;
import io.lavagna.common.ConstructorAnnotationRowMapper.Column;
import io.lavagna.common.ConstructorAnnotationRowMapper.ColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ConstructorAnnotationRowMapperTest {

	@Test
	public void testCorrectMapping() {
		new ConstructorAnnotationRowMapper<>(Mapping.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testsMoreThanOnePublicConstructor() {
		new ConstructorAnnotationRowMapper<>(MultiplePublicConstructor.class);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingColumnAnnotation() {
		new ConstructorAnnotationRowMapper<>(MissingColumn.class);
	}

	@Test
	public void testMultipleAnnotation() {
		new ConstructorAnnotationRowMapper<>(Mapping2.class);
	}

	@Test
	public void booleanColumnMapper() throws SQLException {
		ColumnMapper cm = new ConstructorAnnotationRowMapper.BooleanColumnMapper("test");
		checkExtractedValue(cm, null, null);
		checkExtractedValue(cm, true, true);
		checkExtractedValue(cm, false, false);
		checkExtractedValue(cm, "true", true);
		checkExtractedValue(cm, "false", false);
		checkExtractedValue(cm, "whatever", false);
		checkExtractedValue(cm, 1, true);
		checkExtractedValue(cm, 0, false);
		checkExtractedValue(cm, 2, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void booleanColumnMapperFailure() throws SQLException {
		ColumnMapper cm = new ConstructorAnnotationRowMapper.BooleanColumnMapper("test");
		checkExtractedValue(cm, new Object(), null);
	}

	private void checkExtractedValue(ColumnMapper cm, Object value, Object expected) throws SQLException {
		ResultSet rs2 = prepareRS(value);
		Assert.assertEquals(expected, cm.getObject(rs2));
	}

	private ResultSet prepareRS(Object returnValue) throws SQLException {
		ResultSet rs1 = Mockito.mock(ResultSet.class);
		Mockito.when(rs1.getObject("test")).thenReturn(returnValue);
		return rs1;
	}

	public static class Mapping {
		public Mapping(@Column("COL_1") String a, @Column("COL_2") int b) {
		}
	}

	public static class Mapping2 {
		public Mapping2(@Column("COL_1") String a, @Column("COL_2") int b) {
		}
	}

	public static class MultiplePublicConstructor {
		public MultiplePublicConstructor() {
		}

		public MultiplePublicConstructor(String s) {
		}
	}

	public static class MissingColumn {
		public MissingColumn(String a) {

		}
	}
}
