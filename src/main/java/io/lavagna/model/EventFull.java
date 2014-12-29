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

import lombok.Getter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Getter
public class EventFull {

	private final Event event;

	private final String userProvider;
	private final String username;
	private final String boardShortName;
	private final Integer cardSequenceNumber;
	private final String content;

	private final String labelBoardShortName;
	private final Integer labelCardSequenceNumber;

	private final String labelUserProvider;
	private final String labelUsername;

	public EventFull(Event event, User user, ImmutablePair<Board, Card> bc, String content,
			ImmutablePair<Board, Card> labelCard, User labelUser) {
		this.event = event;
		this.userProvider = user.getProvider();
		this.username = user.getUsername();
		this.boardShortName = bc.getLeft().getShortName();
		this.cardSequenceNumber = bc.getRight().getSequence();
		this.content = content;
		//

		if (labelCard != null) {
			this.labelBoardShortName = labelCard.getLeft().getShortName();
			this.labelCardSequenceNumber = labelCard.getRight().getSequence();
		} else {
			this.labelBoardShortName = null;
			this.labelCardSequenceNumber = null;
		}

		if (labelUser != null) {
			this.labelUserProvider = labelUser.getProvider();
			this.labelUsername = labelUser.getUsername();
		} else {
			this.labelUserProvider = null;
			this.labelUsername = null;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
