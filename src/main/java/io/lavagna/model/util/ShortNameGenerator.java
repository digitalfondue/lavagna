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

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ShortNameGenerator {

	private ShortNameGenerator() {
	}

	public static boolean isShortNameValid(String shortName) {
		Pattern acceptedChars = Pattern.compile("^[A-Z0-9_]+$");
		return acceptedChars.matcher(shortName).matches();
	}

	/**
	 * <p>
	 * Generate a short name given the full project name.
	 *
	 * Total length returned is equal or less than 8.
	 * </p>
	 *
	 * <p>
	 * Heuristic:
	 * </p>
	 * <ul>
	 * <li>if name is less or equals than 6 chars and is a single word, the short name will be UPPER(name)</li>
	 * <li>if the project has multiple words, it will concatenate each words. If a word has a length more than 4:
	 * <ul>
	 * <li>it will take only the upper case characters if there are more than 1.</li>
	 * <li>else it will take the first two characters.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param name
	 * @return
	 */
	public static String generateShortNameFrom(String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}

		String t = name.trim().replace("-", "");
		String[] splitted = t.split("\\s+");
		if (splitted.length == 1) {
			return splitted[0].substring(0, Math.min(6, splitted[0].length())).toUpperCase(Locale.ENGLISH);
		}
		StringBuilder sb = new StringBuilder();
		for (String token : splitted) {
			if (token.length() <= 4) {
				sb.append(token);
			} else if (countUpperCase(token) > 1) {
				sb.append(takeFirstFourUpperCaseChars(token));
			} else {
				sb.append(token.substring(0, 3));
			}
		}
		return sb.toString().toUpperCase(Locale.ENGLISH).substring(0, Math.min(8, sb.length()));
	}

	private static String takeFirstFourUpperCaseChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (Character.isUpperCase(c)) {
				sb.append(c);
			}
		}
		return sb.substring(0, Math.min(sb.length(), 4));
	}

	private static int countUpperCase(String s) {
		int cnt = 0;
		for (char c : s.toCharArray()) {
			if (Character.isUpperCase(c)) {
				cnt++;
			}
		}
		return cnt;
	}

}
