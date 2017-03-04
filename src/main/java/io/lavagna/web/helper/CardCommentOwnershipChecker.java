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

import io.lavagna.model.Event.EventType;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CardCommentOwnershipChecker implements OwnershipChecker {

	private final Pattern pattern = Pattern.compile("^.*/comment/(\\d+)$");

	private final EventRepository eventRepository;

	@Autowired
	public CardCommentOwnershipChecker(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public boolean hasOwnership(HttpServletRequest request, UserWithPermission user) {

		Matcher matcher = pattern.matcher(request.getRequestURI());
		try {
			if (matcher.matches()) {
				int commentId = Integer.parseInt(matcher.group(1), 10);
				return eventRepository.findUsersIdFor(commentId, EventType.COMMENT_CREATE).contains(user.getId());
			}
		} catch (NumberFormatException nfe) {
			return false;
		}

		return false;
	}
}
