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
package io.lavagna.web.api;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.lavagna.model.Permission;
import io.lavagna.web.api.model.PluginCode;
import io.lavagna.web.helper.ExpectPermission;

@RestController
public class ApiHooksController {

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/plugin", method = RequestMethod.POST)
	public void addOrUpdateGlobalPlugin(@RequestBody PluginCode plugin) {
		// FIXME save or update code
	}

	// FIXME define model
	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/plugin", method = RequestMethod.GET)
	public List<Object> listPlugins() {
		return Collections.emptyList();
	}

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = "/api/plugin/{name}", method = RequestMethod.DELETE)
	public void deletePlugin(@PathVariable("name") String name) {
		// FIXME remove plugin
	}
	
	@ExpectPermission(Permission.GLOBAL_HOOK_API_ACCESS)
	@RequestMapping(value = "/api/plugin/{name}/hook", method = {RequestMethod.GET, RequestMethod.POST})
	public void handleHook(@PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) {
		//FIXME:call hook and pass request, response
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/plugin", method = RequestMethod.POST)
	public void addOrUpdateProjectPlugin(@PathVariable("projectShortName") String projectShortName,
			@RequestBody PluginCode plugin) {
		// FIXME save or update code
	}

	// FIXME define model
	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/plugin", method = RequestMethod.GET)
	public List<Object> listPlugins(@PathVariable("projectShortName") String projectShortName) {
		return Collections.emptyList();
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = "/api/project/{projectShortName}/plugin/{name}", method = RequestMethod.DELETE)
	public void deletePlugin(@PathVariable("projectShortName") String projectShortName,
			@PathVariable("name") String name) {
		// FIXME remove plugin
	}
	
	@ExpectPermission(Permission.PROJECT_HOOK_API_ACCESS)
	@RequestMapping(value = "/api/project/{projectShortName}/plugin/{name}/hook", method = {RequestMethod.GET, RequestMethod.POST})
	public void handleHook(@PathVariable("projectShortName") String projectShortName, @PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) {
		// FIXME:call hook and pass request, response
	}
}
