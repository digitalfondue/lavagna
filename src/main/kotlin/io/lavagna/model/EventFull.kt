/**
 * This file is part of lavagna.

 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see //www.gnu.org/licenses/>.
 */
package io.lavagna.model

import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.tuple.ImmutablePair

class EventFull(val event: Event,
                user: User,
                bc: ImmutablePair<Board, Card>,
                val content: String?,
                labelCard: ImmutablePair<Board, Card>?,
                labelUser: User?) {

    val userProvider: String?
    val username: String?
    val boardShortName: String?
    val cardSequenceNumber: Int?

    val labelBoardShortName: String?
    val labelCardSequenceNumber: Int?

    val labelUserProvider: String?
    val labelUsername: String?

    init {
        this.userProvider = user.provider
        this.username = user.username
        this.boardShortName = bc.getLeft().shortName
        this.cardSequenceNumber = bc.getRight().sequence
        //

        if (labelCard != null) {
            this.labelBoardShortName = labelCard.getLeft().shortName
            this.labelCardSequenceNumber = labelCard.getRight().sequence
        } else {
            this.labelBoardShortName = null
            this.labelCardSequenceNumber = null
        }

        if (labelUser != null) {
            this.labelUserProvider = labelUser.provider
            this.labelUsername = labelUser.username
        } else {
            this.labelUserProvider = null
            this.labelUsername = null
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
