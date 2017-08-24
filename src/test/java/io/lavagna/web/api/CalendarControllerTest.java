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
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CalendarService;
import io.lavagna.service.UserRepository;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CalendarControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWithPermission user;

    @Mock
    private CalendarService calendarService;

    private CalendarController calendarController;

    @Before
    public void prepare() {
        calendarController = new CalendarController(userRepository, calendarService);
    }

    @Test
    public void testGetCalendarToken() {

        CalendarInfo retCi = new CalendarInfo("1234abcd", false);

        when(calendarService.findCalendarInfoFromUser(user)).thenReturn(retCi);

        CalendarInfo ci = calendarController.getCalendarToken(user);

        verify(calendarService).findCalendarInfoFromUser(eq(user));
        Assert.assertEquals(retCi.getToken(), ci.getToken());
    }

    @Test
    public void testUpdateCalendarFeedStatus() {

        CalendarInfo retCi = new CalendarInfo("1234abcd", false);

        when(calendarService.findCalendarInfoFromUser(user)).thenReturn(retCi);

        CalendarController.DisableCalendarRequest req = new CalendarController.DisableCalendarRequest(true);
        calendarController.setCalendarFeedDisabled(user, req);

        verify(calendarService).setCalendarFeedDisabled(eq(user), eq(true));
        verify(calendarService).findCalendarInfoFromUser(eq(user));
    }

    @Test
    public void testClearCalendarToken() {

        calendarController.clearCalendarToken(user);

        verify(userRepository).deleteCalendarToken(eq(user));
        verify(calendarService).findCalendarInfoFromUser(eq(user));
    }

    @Test
    public void testProjectStandardCalendar() throws IOException, URISyntaxException, ParseException {

        calendarController.projectStandardCalendar("name", user);

        verify(calendarService).getProjectCalendar(eq("name"), eq(user));
    }

    @Test
    public void testUserStandardCalendar() throws IOException, URISyntaxException, ParseException {

        calendarController.userStandardCalendar(user);

        verify(calendarService).getUserCalendar(eq(user));
    }

    @Test
    public void testUserCalDavCalendar() throws IOException, URISyntaxException, ParseException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        final StubServletOutputStream servletOutputStream = new StubServletOutputStream();
        when(resp.getOutputStream()).thenReturn(servletOutputStream);

        String token = "1234abcd";

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.PUBLISH);
        VEvent event = new VEvent(new net.fortuna.ical4j.model.Date(), "name");
        event.getProperties().add(new Uid(UUID.randomUUID().toString()));
        Organizer organizer = new Organizer(URI.create("mailto:lavagna"));
        event.getProperties().add(organizer);
        calendar.getComponents().add(event);
        when(calendarService.getCalDavCalendar(eq(token))).thenReturn(calendar);

        calendarController.userCalDavCalendar(token, resp);

        verify(calendarService).getCalDavCalendar(eq(token));
    }

    class StubServletOutputStream extends ServletOutputStream {
        public ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public void write(int i) throws IOException {
            baos.write(i);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}
