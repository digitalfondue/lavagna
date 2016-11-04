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

import org.springframework.stereotype.Service;

import io.lavagna.model.BoardColumn;
import io.lavagna.model.Card;

@Service
public class ApiHooksService {

	public void createdProject(String projectShortName) {
		//FIXME run scripts
	}

	public void updatedProject(String projectShortName) {
		//FIXME run scripts
	}

	public void createdBoard(String boardShortName) {
		//FIXME run scripts
	}

	public void updatedBoard(String boardShortName) {
		//FIXME run scripts
	}

	public void createdColumn(String boardShortName, String columnName) {
		//FIXME run scripts
	}

	public void updateColumn(String boardShortName, BoardColumn oldColumn, BoardColumn updatedColumn) {
		//FIXME run scripts
	}

	public void createdCard(String boardShortName, Card card) {
		//FIXME run scripts
	}

	public void updatedCard(String boardShortName, Card beforeUpdate, Card newCard) {
		//FIXME run scripts
	}

}
