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

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDataHistory {
    private final int id;
    private int userId;
    private Date time;
    private final String content;
    private int updatedCount = 0;
    private int updateUser;
    private Date updateDate;
    private final int order;

    public CardDataHistory(int id, String content, int order, int createUser, Date createDate, int updateUser,
        Date updatedDate) {
        this.id = id;
        this.content = content;
        this.order = order;

        this.userId = createUser;
        this.time = createDate;

        this.updateUser = updateUser;
        this.updateDate = updatedDate;
    }
}
