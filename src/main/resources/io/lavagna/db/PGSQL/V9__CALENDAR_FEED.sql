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

CREATE TABLE LA_USER_CALENDAR (
	USER_CALENDAR_TOKEN CHAR(64) NOT NULL,
	USER_CALENDAR_ID_FK INTEGER NOT NULL,
	PRIMARY KEY(USER_CALENDAR_ID_FK)
);
-- CONSTRAINTS
ALTER TABLE LA_USER_CALENDAR ADD FOREIGN KEY(USER_CALENDAR_ID_FK) REFERENCES LA_USER(USER_ID);