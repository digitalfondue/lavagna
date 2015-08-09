/**
 * This file is part of lavagna.
 * <p/>
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.service;

import static io.lavagna.service.SearchFilter.filter;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.CardLabel;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.UserWithPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.TimeZones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

	private final SearchService searchService;
	private final UserService userService;

	@Autowired
	public CalendarService(SearchService searchService, UserService userService) {
		this.searchService = searchService;
		this.userService = userService;
	}

	public Calendar getUserCalendar(String userToken) {

		UserWithPermission user = userService.findUserFromCalendarToken(userToken);

		final Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		Map<Integer, CardFullWithCounts> map = new LinkedHashMap<>();

		SearchFilter myCardsFilter = filter(SearchFilter.FilterType.ASSIGNED, null, null);
		for (CardFullWithCounts card : searchService.find(Arrays.asList(myCardsFilter), null, null, user).getFound()) {
			map.put(card.getId(), card);
		}

		SearchFilter watchedFilter = filter(SearchFilter.FilterType.WATCHED_BY, null, null);
		for (CardFullWithCounts card : searchService.find(Arrays.asList(watchedFilter), null, null, user).getFound()) {
			map.put(card.getId(), card);
		}

		final List<VEvent> events = new ArrayList<>();
		final String utcTimeZone = TimeZones.getUtcTimeZone().getDisplayName();
		for (CardFullWithCounts card : map.values()) {
			for (LabelAndValue lav : card.getLabels()) {
				if (lav.getLabelType() == CardLabel.LabelType.TIMESTAMP) {
					final String cardName = lav.getLabelName() + ": " + card.getName();
					final VEvent event = new VEvent(new Date(lav.getLabelValueTimestamp()), cardName);
					event.getProperties().add(new Uid(new UUID(card.getColumnId(), card.getId()).toString()));
					event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);
					TzId tzParam = new TzId(utcTimeZone);
					event.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);
					//event.getProperties().add(new Description("Event desc"));
					events.add(event);
				}
			}
		}

		calendar.getComponents().addAll(events);

		return calendar;
	}

}
