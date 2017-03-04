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

import io.lavagna.model.Pair;
import io.lavagna.model.UserToCreate;
import io.lavagna.service.ExportImportService;
import io.lavagna.service.Ldap;
import io.lavagna.service.SetupService;
import io.lavagna.web.api.model.Conf;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Controller
public class SetupController {

	private final Ldap ldap;
	private final ExportImportService exportImportService2;
	private final SetupService setupService;


	public SetupController(SetupService setupService, Ldap ldap, ExportImportService exportImportService2) {

		this.ldap = ldap;
		this.exportImportService2 = exportImportService2;
		this.setupService = setupService;
	}

	@RequestMapping(value = "/setup/api/setup/", method = RequestMethod.POST)
	@ResponseBody
	public void setup(@RequestBody ConfWithUser conf) {
		setupService.updateConfiguration(conf.getToUpdateOrCreate(), conf.getUser());
	}

	@RequestMapping(value = "/setup/api/check-ldap/", method = RequestMethod.POST)
	@ResponseBody
	public Pair<Boolean, List<String>> checkLdap(@RequestBody Map<String, String> r) {
		return ldap.authenticateWithParams(r.get("serverUrl"), r.get("managerDn"), r.get("managerPassword"),
				r.get("userSearchBase"), r.get("userSearchFilter"), r.get("username"), r.get("password"));
	}

	@RequestMapping(value = "/setup/api/import", method = RequestMethod.POST)
	public void importLavagna(@RequestParam("file") MultipartFile file, HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		// TODO: move to a helper, as it has the same code as the one in the ExportImportController
		Path tempFile = Files.createTempFile(null, null);
		try {
			try (InputStream is = file.getInputStream(); OutputStream os = Files.newOutputStream(tempFile)) {
				StreamUtils.copy(is, os);
			}
			exportImportService2.importData(true, tempFile);
		} finally {
			Files.delete(tempFile);
		}
		//

		res.sendRedirect(req.getServletContext().getContextPath());
	}

	public static class ConfWithUser extends Conf {
		private UserToCreate user;

        public UserToCreate getUser() {
            return this.user;
        }

        public void setUser(UserToCreate user) {
            this.user = user;
        }
    }
}
