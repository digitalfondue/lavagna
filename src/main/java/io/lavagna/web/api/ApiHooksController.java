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

import io.lavagna.model.ApiHook;
import io.lavagna.model.Permission;
import io.lavagna.service.ApiHooksService;
import io.lavagna.web.api.model.PluginCode;
import io.lavagna.web.helper.ExpectPermission;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class ApiHooksController {

    private final ApiHooksService apiHooksService;

    public ApiHooksController(ApiHooksService apiHooksService) {
        this.apiHooksService = apiHooksService;
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin", method = RequestMethod.POST)
    public ApiHook addGlobalPlugin(@RequestBody PluginCode plugin) {
        apiHooksService.createApiHook(plugin.getName(), plugin.getCode(), plugin.getProperties(), plugin.getProjects(), plugin.getMetadata());
        return apiHooksService.findByName(plugin.getName());
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

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/plugin/{name}", method = RequestMethod.POST)
    public void update(@PathVariable("name") String name, @RequestBody PluginCode plugin) {
        apiHooksService.updateApiHook(name, plugin.getCode(), plugin.getProperties(), plugin.getProjects(), plugin.getMetadata());
    }

    @ExpectPermission(Permission.GLOBAL_HOOK_API_ACCESS)
    @RequestMapping(value = "/api/api-hook/hook/{name}", method = {RequestMethod.GET, RequestMethod.POST})
    public void handleHook(@PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        apiHooksService.handleHook(name, request, response);
    }

    @ExpectPermission(Permission.PROJECT_HOOK_API_ACCESS)
    @RequestMapping(value = "/api/api-hook/project/{projectShortName}/hook/{name}", method = {RequestMethod.GET, RequestMethod.POST})
    public void handleHook(@PathVariable("projectShortName") String projectShortName, @PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        apiHooksService.handleHook(projectShortName, name, request, response);
    }
}
