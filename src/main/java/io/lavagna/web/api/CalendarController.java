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
package io.lavagna.web.api;

import io.lavagna.model.CalendarInfo;
import io.lavagna.model.Permission;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CalendarService;
import io.lavagna.service.UserRepository;
import io.lavagna.service.calendarutils.CalendarEvents;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalendarController {

    private final UserRepository userRepository;
    private final CalendarService calendarService;

    public CalendarController(UserRepository userRepository, CalendarService calendarService) {
        this.userRepository = userRepository;
        this.calendarService = calendarService;
    }

    @ExpectPermission(Permission.UPDATE_PROFILE)
    @RequestMapping(value = "/api/calendar/disable", method = RequestMethod.POST)
    public CalendarInfo setCalendarFeedDisabled(UserWithPermission user,
        @RequestBody DisableCalendarRequest disableRequest) {
        calendarService.setCalendarFeedDisabled(user, disableRequest.isDisabled);
        return calendarService.findCalendarInfoFromUser(user);
    }

    @ExpectPermission(Permission.UPDATE_PROFILE)
    @RequestMapping(value = "/api/calendar/token", method = RequestMethod.DELETE)
    public CalendarInfo clearCalendarToken(UserWithPermission user) {
        userRepository.deleteCalendarToken(user);
        return getCalendarToken(user);
    }

    @RequestMapping(value = "/api/calendar/token", method = RequestMethod.GET)
    public CalendarInfo getCalendarToken(UserWithPermission user) {
        return calendarService.findCalendarInfoFromUser(user);
    }

    @RequestMapping(value = "/api/calendar/user", method = RequestMethod.GET)
    public CalendarEvents userStandardCalendar(UserWithPermission user) throws URISyntaxException, ParseException {
        return calendarService.getUserCalendar(user);
    }

    @RequestMapping(value = "/api/calendar/{token}/calendar.ics", method = RequestMethod.GET, produces = "text/calendar")
    public void userCalDavCalendar(@PathVariable("token") String userToken, HttpServletResponse response)
        throws IOException, URISyntaxException, ParseException {
        Calendar calendar = calendarService.getCalDavCalendar(userToken);
        response.setContentType("text/calendar");
        CalendarOutputter output = new CalendarOutputter(false); // <- no validation on the output
        output.output(calendar, response.getOutputStream());
    }

    @AllArgsConstructor
    static class DisableCalendarRequest {
        private boolean isDisabled;
    }
}
