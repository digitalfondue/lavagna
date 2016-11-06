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
package io.lavagna.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.lavagna.model.BoardColumn;
import io.lavagna.model.Card;
import io.lavagna.model.CardData;
import io.lavagna.model.CardDataHistory;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.User;

@Service
public class ApiHooksService {

	public void createdProject(String projectShortName, User user) {
		//FIXME run scripts
	}

	public void updatedProject(String projectShortName, User user) {
		//FIXME run scripts
	}

	public void createdBoard(String boardShortName, User user) {
		//FIXME run scripts
	}

	public void updatedBoard(String boardShortName, User user) {
		//FIXME run scripts
	}

	public void createdColumn(String boardShortName, String columnName, User user) {
		//FIXME run scripts
	}

	public void updateColumn(String boardShortName, BoardColumn oldColumn, BoardColumn updatedColumn, User user) {
		//FIXME run scripts
	}

	public void createdCard(String boardShortName, Card card, User user) {
		//FIXME run scripts
	}

	public void updatedCard(String boardShortName, Card beforeUpdate, Card newCard, User user) {
		//FIXME run scripts
	}

	public void updateCardDescription(int cardId, CardDataHistory previousDescription, CardDataHistory newDescription, User user) {
		//FIXME run scripts
	}

	public void createdComment(int cardId, CardData comment, User user) {
		//FIXME run scripts
	}

	public void updatedComment(int cardId, CardData previousComment, String newComment, User user) {
		//FIXME run scripts
	}

	public void deletedComment(int cardId, CardData deletedComment, User user) {
		//FIXME run scripts
	}

	public void undeletedComment(int cardId, CardData undeletedComment, User user) {
		//FIXME run scripts
	}

	public void uploadedFile(int cardId, List<String> fileNames) {
		//FIXME run scripts		
	}

	public void deletedFile(int cardId, String fileName) {
		//FIXME run scripts
	}

	public void undoDeletedFile(int cardId, String fileName) {
		//FIXME run scripts
	}

	public void removedLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue) {
		//FIXME run scripts
	}

	public void addLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue) {
		//FIXME run scripts
	}

	public void updateLabelValueToCards(List<CardFull> updated, int labelId, LabelValue labelValue) {
		//FIXME run scripts
	}

	public void createActionList(int cardId, String name) {
		//FIXME run scripts	
	}

	public void deleteActionList(int cardId, String name) {
		//FIXME run scripts
	}

	public void updatedNameActionList(int cardId, String oldName, String newName) {
		//FIXME run scripts		
	}

}
