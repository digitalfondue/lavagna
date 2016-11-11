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
package io.lavagna.service.calendarutils

import io.lavagna.model.CardFullWithCounts
import io.lavagna.model.LabelListValueWithMetadata
import java.util.*

class CalendarEvents
@java.beans.ConstructorProperties("dailyEvents") constructor(
    val dailyEvents: Map<Date, CalendarEvents.MilestoneDayEvents>) {

    class MilestoneDayEvents
    @java.beans.ConstructorProperties("milestones", "cards") constructor(
        val milestones: Set<MilestoneEvent>, val cards: Set<CardFullWithCounts>)

    class MilestoneEvent
    @java.beans.ConstructorProperties("projectShortName", "name", "label") constructor(
        projectShortName: String, name: String, label: LabelListValueWithMetadata) {
        var projectShortName: String
            internal set
        var name: String
            internal set
        var label: LabelListValueWithMetadata
            internal set

        init {
            this.projectShortName = projectShortName
            this.name = name
            this.label = label
        }
    }
}
