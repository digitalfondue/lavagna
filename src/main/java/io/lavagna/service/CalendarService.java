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
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;

import java.net.URI;
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
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.TimeZones;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarService {

	private final SearchService searchService;
	private final UserRepository userRepository;
	private final UserService userService;

	@Autowired
	public CalendarService(SearchService searchService, UserService userService, UserRepository userRepository) {
		this.searchService = searchService;
		this.userRepository = userRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = false)
	public String findCalendarTokenFromUser(User user) {
		try {
			return userRepository.findCalendarTokenFromUser(user);
		} catch (CalendarTokenNotFoundException ex) {
			String token = UUID.randomUUID().toString();// <- this use secure random
			String hashedToken = DigestUtils.sha256Hex(token);
			userRepository.registerCalendarToken(user, hashedToken);
			return hashedToken;
		}
	}

	private UserWithPermission findUserFromCalendarToken(String token) {
		int userId = userRepository.findUserIdFromCalendarToken(token);
		return userService.findUserWithPermission(userId);
	}

	public Calendar getUserCalendar(String userToken) {

		UserWithPermission user = findUserFromCalendarToken(userToken);

		final Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		calendar.getProperties().add(Method.PUBLISH);

		Map<Integer, CardFullWithCounts> map = new LinkedHashMap<>();

		SearchFilter aFilter = filter(SearchFilter.FilterType.ASSIGNED, SearchFilter.ValueType.CURRENT_USER, "me");
		for (CardFullWithCounts card : searchService.find(Arrays.asList(aFilter), null, null, user).getFound()) {
			map.put(card.getId(), card);
		}

		SearchFilter wFilter = filter(SearchFilter.FilterType.WATCHED_BY, SearchFilter.ValueType.CURRENT_USER, "me");
		for (CardFullWithCounts card : searchService.find(Arrays.asList(wFilter), null, null, user).getFound()) {
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
					TzId tzParam = new TzId(utcTimeZone);
					event.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);
					// TODO add organizer e-mail
					Organizer organizer = new Organizer(URI.create("mailto:lavagna"));
					event.getProperties().add(organizer);
					// TODO add description
					//event.getProperties().add(new Description("Event desc"));
					// TODO add direct link to Lavagna
					// TODO add some tests!
					events.add(event);
				}
			}
		}

		calendar.getComponents().addAll(events);

		return calendar;
	}

}
