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

-- metadata for a list value element
CREATE TABLE LA_LIST_VALUE_METADATA (
	LVM_LABEL_LIST_VALUE_ID_FK INTEGER NOT NULL,
	LVM_KEY VARCHAR(24) NOT NULL,
	LVM_VALUE VARCHAR(255) NOT NULL
);

ALTER TABLE LA_LIST_VALUE_METADATA ADD FOREIGN KEY(LVM_LABEL_LIST_VALUE_ID_FK) REFERENCES LA_CARD_LABEL_LIST_VALUE(CARD_LABEL_LIST_VALUE_ID) ON DELETE CASCADE;
ALTER TABLE LA_LIST_VALUE_METADATA ADD CONSTRAINT UNIQUE_LVM_FK_KEY UNIQUE(LVM_LABEL_LIST_VALUE_ID_FK, LVM_KEY);