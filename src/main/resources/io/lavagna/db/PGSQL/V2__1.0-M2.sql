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

-- for version 1.0-M2

-- remove label managing permission as they are now folded inside PROJECT_ADMINISTRATION
DELETE FROM LA_ROLE_PERMISSION WHERE PERMISSION IN ('CREATE_LABEL', 'UPDATE_LABEL', 'DELETE_LABEL');
DELETE FROM LA_PROJECT_ROLE_PERMISSION WHERE PERMISSION IN ('CREATE_LABEL', 'UPDATE_LABEL', 'DELETE_LABEL');


-- merge action list permissions in a single MANAGE_ACTION_LIST permission
insert into la_role_permission(role_id_fk, permission) select distinct ROLE_ID_FK, 'MANAGE_ACTION_LIST' from la_role_permission where permission in 
(
'CREATE_ACTION_LIST', 
'DELETE_ACTION_LIST',
'UPDATE_ACTION_LIST',
'ORDER_ACTION_LIST',
'CREATE_ACTION_LIST_ITEM',
'DELETE_ACTION_LIST_ITEM',
'TOGGLE_ACTION_LIST_ITEM',
'UPDATE_ACTION_LIST_ITEM',
'MOVE_ACTION_LIST_ITEM'
);
insert into la_project_role_permission(PROJECT_ROLE_ID_FK, permission) select distinct PROJECT_ROLE_ID_FK, 'MANAGE_ACTION_LIST' from la_project_role_permission where permission in 
(
'CREATE_ACTION_LIST', 
'DELETE_ACTION_LIST',
'UPDATE_ACTION_LIST',
'ORDER_ACTION_LIST',
'CREATE_ACTION_LIST_ITEM',
'DELETE_ACTION_LIST_ITEM',
'TOGGLE_ACTION_LIST_ITEM',
'UPDATE_ACTION_LIST_ITEM',
'MOVE_ACTION_LIST_ITEM'
);
DELETE FROM LA_ROLE_PERMISSION WHERE PERMISSION IN (
'CREATE_ACTION_LIST', 
'DELETE_ACTION_LIST',
'UPDATE_ACTION_LIST',
'ORDER_ACTION_LIST',
'CREATE_ACTION_LIST_ITEM',
'DELETE_ACTION_LIST_ITEM',
'TOGGLE_ACTION_LIST_ITEM',
'UPDATE_ACTION_LIST_ITEM',
'MOVE_ACTION_LIST_ITEM'
);
DELETE FROM la_project_role_permission WHERE PERMISSION IN (
'CREATE_ACTION_LIST', 
'DELETE_ACTION_LIST',
'UPDATE_ACTION_LIST',
'ORDER_ACTION_LIST',
'CREATE_ACTION_LIST_ITEM',
'DELETE_ACTION_LIST_ITEM',
'TOGGLE_ACTION_LIST_ITEM',
'UPDATE_ACTION_LIST_ITEM',
'MOVE_ACTION_LIST_ITEM'
);