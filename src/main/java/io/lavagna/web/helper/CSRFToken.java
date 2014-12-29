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
package io.lavagna.web.helper;

import java.util.regex.Pattern;

public final class CSRFToken {

	private CSRFToken() {
	}

	//
	public static final String CSRF_TOKEN = CSRFToken.class.getName() + ".CSRF_TOKEN";
	public static final String CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";
	public static final String CSRF_FORM_PARAMETER = "_csrf";
	public static final Pattern CSRF_METHOD_DONT_CHECK = Pattern.compile("^GET|HEAD|OPTIONS$");

	// ------------------------------------------------------------------------
	// this function has been imported from KeyCzar.

	/*
	 * Copyright 2008 Google Inc.
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
	 * with the License. You may obtain a copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
	 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
	 * the specific language governing permissions and limitations under the License.
	 */

	/**
	 * An array comparison that is safe from timing attacks. If two arrays are of equal length, this code will always
	 * check all elements, rather than exiting once it encounters a differing byte.
	 * 
	 * @param a1
	 *            An array to compare
	 * @param a2
	 *            Another array to compare
	 * @return True if these arrays are both null or if they have equal length and equal bytes in all elements
	 */
	public static boolean safeArrayEquals(byte[] a1, byte[] a2) {
		if (a1 == null || a2 == null) {
			return a1 == a2;
		}
		if (a1.length != a2.length) {
			return false;
		}
		byte result = 0;
		for (int i = 0; i < a1.length; i++) {
			result |= a1[i] ^ a2[i];
		}
		return result == 0;
	}
}
