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

import static org.apache.commons.lang3.ArrayUtils.contains;
import io.lavagna.model.CalendarInfo;
import io.lavagna.model.Permission;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CalendarService;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalendarController {

    private final UserRepository userRepository;
    private final CalendarService calendarService;
    private final Environment env;

    @Autowired
    public CalendarController(UserRepository userRepository, CalendarService calendarService, Environment env) {
        this.userRepository = userRepository;
        this.calendarService = calendarService;
        this.env = env;
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

    @RequestMapping(value = "/api/calendar/{token}/calendar.ics",
        method = RequestMethod.GET, produces = "text/calendar")
    public void userCalendar(@PathVariable("token") String userToken, HttpServletResponse response)
        throws IOException, ValidationException, URISyntaxException {
        Calendar calendar = calendarService.getUserCalendar(userToken);
        response.setContentType("text/calendar");
        CalendarOutputter output = new CalendarOutputter(false); // <- no validation on the output
        output.output(calendar, response.getOutputStream());
    }

    @AllArgsConstructor
    static class DisableCalendarRequest {
        private boolean isDisabled;
    }
}
