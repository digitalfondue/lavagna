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
package io.lavagna.query;

import java.util.List;

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import io.lavagna.model.ApiHook;

@QueryRepository
public interface ApiHookQuery {
	
	@Query("select API_HOOK_NAME, API_HOOK_SCRIPT, API_HOOK_CONFIGURATION from LA_API_HOOK")
	List<ApiHook> findAll();
	
	@Query("delete from LA_API_HOOK where API_HOOK_NAME = :name")
	int delete(@Bind("name") String name);
	
	@Query("insert into LA_API_HOOK(API_HOOK_NAME, API_HOOK_SCRIPT, API_HOOK_CONFIGURATION) values (:name, :script, :configuration)")
	int insert(@Bind("name") String name, @Bind("script") String script, @Bind("configuration") String configuration);
	
	@Query("update LA_API_HOOK set API_HOOK_SCRIPT = :script, API_HOOK_CONFIGURATION = :configuration where API_HOOK_NAME = :name")
	int update(@Bind("name") String name, @Bind("script") String script, @Bind("configuration") String configuration);
}
