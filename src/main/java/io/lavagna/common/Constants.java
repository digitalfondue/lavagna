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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Constants {
	
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	
	public static final String SYSTEM_LABEL_ASSIGNED = "ASSIGNED";
	public static final String SYSTEM_LABEL_DUE_DATE = "DUE_DATE";
	public static final String SYSTEM_LABEL_MILESTONE = "MILESTONE";
	public static final String SYSTEM_LABEL_WATCHED_BY = "WATCHED_BY";
	
	
	public static final Set<String> RESERVED_SYSTEM_LABELS_NAME = Collections.unmodifiableSet(new HashSet<>(Arrays
			.asList(SYSTEM_LABEL_ASSIGNED, SYSTEM_LABEL_DUE_DATE, SYSTEM_LABEL_MILESTONE, SYSTEM_LABEL_WATCHED_BY)));
}
