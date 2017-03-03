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

import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.service.ExportImportService;
import io.lavagna.service.ImportService;
import io.lavagna.service.ImportService.TrelloBoardsResponse;
import io.lavagna.service.ImportService.TrelloImportResponse;
import io.lavagna.web.api.model.TrelloImportRequest;
import io.lavagna.web.api.model.TrelloRequest;
import io.lavagna.web.helper.ExpectPermission;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

@ExpectPermission(Permission.ADMINISTRATION)
@Controller
public class ExportImportController {

	private final ExportImportService exportImportService;
	private final ImportService importService;


	public ExportImportController(ExportImportService exportImportService, ImportService importService) {
		this.exportImportService = exportImportService;
		this.importService = importService;
	}

	@RequestMapping(value = "/api/export", method = RequestMethod.POST)
	public void export(HttpServletResponse resp) throws IOException {
		resp.setHeader("Content-Disposition", "attachment; filename=\"lavagna-export-"
				+ new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".zip\"");
		resp.setContentType("application/octet-stream");
		exportImportService.exportData(resp.getOutputStream());
	}

	@RequestMapping(value = "/api/import/lavagna", method = RequestMethod.POST)
	@ResponseBody
	public void importFromLavagna(
			@RequestParam(value = "overrideConfiguration", defaultValue = "false") Boolean overrideConfiguration,
			@RequestParam("file") MultipartFile file) throws IOException {

		Path tempFile = Files.createTempFile(null, null);

		try {
			try (InputStream is = file.getInputStream(); OutputStream os = Files.newOutputStream(tempFile)) {
				StreamUtils.copy(is, os);
			}

			exportImportService.importData(overrideConfiguration, tempFile);
		} finally {
			Files.delete(tempFile);
		}

	}

	@RequestMapping(value = "/api/import/trello/boards", method = RequestMethod.POST)
	@ResponseBody
	public TrelloBoardsResponse getAvailableTrelloBoards(@RequestBody TrelloRequest request) {
		return importService.getAvailableTrelloBoards(request);
	}

	@RequestMapping(value = "/api/import/trello", method = RequestMethod.POST)
	@ResponseBody
	public void importFromTrello(@RequestBody TrelloImportRequest importRequest, User user) {
		TrelloImportResponse result = importService.importFromTrello(importRequest, user);
		importService.saveTrelloBoardsToDb(importRequest.getProjectShortName(), result, user);
	}

}
