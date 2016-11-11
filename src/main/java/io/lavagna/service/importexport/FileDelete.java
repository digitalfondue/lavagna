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
package io.lavagna.service.importexport;

import io.lavagna.model.Event;
import io.lavagna.model.EventFull;
import io.lavagna.model.ImportContext;
import io.lavagna.model.User;
import io.lavagna.service.CardDataService;
import io.lavagna.service.CardRepository;
import io.lavagna.service.UserRepository;

import java.nio.file.Path;
import java.util.Date;

class FileDelete extends AbstractProcessEvent {

	FileDelete(CardRepository cardRepository, UserRepository userRepository, CardDataService cardDataService) {
		super(cardRepository, userRepository, cardDataService);
	}

	@Override
	void process(EventFull e, Event event, Date time, User user, ImportContext context, Path tempFile) {
		Integer cardDataId;
		if ((cardDataId = context.getFileId().get(event.getDataId())) != null) {
			cardDataService.deleteFile(cardDataId, user, time);
		}
	}

}
