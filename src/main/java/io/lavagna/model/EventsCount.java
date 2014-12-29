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

import java.util.Calendar;
import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang3.time.DateUtils;

@Getter
public class EventsCount {

	private final long date;
	private final long count;

	public EventsCount(@Column("EVENT_DATE") Date date, @Column("EVENT_COUNT") long count) {
		this.date = DateUtils.truncate(date, Calendar.DATE).getTime();
		this.count = count;
	}
}
