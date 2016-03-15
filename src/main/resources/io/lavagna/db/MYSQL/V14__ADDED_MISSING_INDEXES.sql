--
-- This file is part of lavagna.
--
-- lavagna is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- lavagna is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
--

CREATE INDEX LA_CARD_LABEL_IDX ON LA_CARD_LABEL(CARD_LABEL_DOMAIN, CARD_LABEL_NAME);

CREATE INDEX LA_USER_USER_LAST_CHECKED_IDX ON LA_USER(USER_LAST_CHECKED);

CREATE INDEX LA_USER_USER_LAST_CHECKPOINT_COUNT_IDX ON LA_USER(USER_LAST_CHECKPOINT_COUNT);

CREATE INDEX LA_BOARD_COLUMN_BOARD_COLUMN_LOCATION_IDX ON LA_BOARD_COLUMN(BOARD_COLUMN_LOCATION);

CREATE INDEX LA_BOARD_COLUMN_DEFINITION_VALUE_IDX ON LA_BOARD_COLUMN_DEFINITION(BOARD_COLUMN_DEFINITION_VALUE);

CREATE INDEX LA_USER_CALENDAR_USER_CALENDAR_TOKEN_IDX ON LA_USER_CALENDAR(USER_CALENDAR_TOKEN);

CREATE INDEX LA_BOARD_STATISTICS_IDX ON LA_BOARD_STATISTICS(BOARD_STATISTICS_LOCATION, BOARD_STATISTICS_TIME);


