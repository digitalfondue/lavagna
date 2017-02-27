/**
 * This file is part of lavagna.
 * <p>
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.web.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.lavagna.model.ApiHook;
import io.lavagna.model.Permission;
import io.lavagna.service.ApiHooksService;
import io.lavagna.web.api.model.PluginCode;
import io.lavagna.web.helper.ExpectPermission;

@RestController
public class ApiHooksController {

    private final ApiHooksService apiHooksService;

    public ApiHooksController(ApiHooksService apiHooksService) {
        this.apiHooksService = apiHooksService;
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin", method = RequestMethod.POST)
    public void addOrUpdateGlobalPlugin(@RequestBody PluginCode plugin) {
        apiHooksService.createOrUpdateApiHook(plugin.getName(), plugin.getCode(), plugin.getProperties(), plugin.getProjects());
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin/{name}/enable/{status}", method = RequestMethod.POST)
    public void enable(@PathVariable("name") String name, @PathVariable("status") boolean status) {
        apiHooksService.enable(name, status);
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin", method = RequestMethod.GET)
    public List<ApiHook> listPlugins() {
        return apiHooksService.findAllPlugins();
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin/{name}", method = RequestMethod.DELETE)
    public void deletePlugin(@PathVariable("name") String name) {
        apiHooksService.deleteHook(name);
    }

    @ExpectPermission(Permission.GLOBAL_HOOK_API_ACCESS)
    @RequestMapping(value = "/api/plugin/{name}/hook", method = {RequestMethod.GET, RequestMethod.POST})
    public void handleHook(@PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) {
        //FIXME:call hook and pass request, response
    }

    @ExpectPermission(Permission.PROJECT_HOOK_API_ACCESS)
    @RequestMapping(value = "/api/project/{projectShortName}/plugin/{name}/hook", method = {RequestMethod.GET, RequestMethod.POST})
    public void handleHook(@PathVariable("projectShortName") String projectShortName, @PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) {
        // FIXME:call hook and pass request, response
    }
}
