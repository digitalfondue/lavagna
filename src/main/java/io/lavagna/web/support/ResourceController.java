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
package io.lavagna.web.support;

import static org.apache.commons.lang3.ArrayUtils.contains;
import io.lavagna.common.Json;
import io.lavagna.model.Permission;
import io.lavagna.web.helper.ExpectPermission;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

@Controller
public class ResourceController {

	private static final String PROJ_SHORT_NAME = "{projectShortName:[A-Z0-9_]+}";
	private static final String BOARD_SHORT_NAME = "{shortName:[A-Z0-9_]+}";
	private static final String CARD_SEQ = "{cardId:[0-9]+}";
	private final Environment env;
	// we don't care if the values are set more than one time
	private final AtomicReference<Template> indexTopTemplate = new AtomicReference<>();
	private final AtomicReference<byte[]> indexCache = new AtomicReference<>();
	private final AtomicReference<byte[]> jsCache = new AtomicReference<>();
	private final AtomicReference<byte[]> cssCache = new AtomicReference<>();

	@Autowired
	public ResourceController(Environment env) {
		this.env = env;
	}

	private static List<String> prepareTemplates(ServletContext context, String initialPath) throws IOException {
		List<String> r = new ArrayList<>();
		BeforeAfter ba = new AngularTemplate();
		for (String file : allFilesWithExtension(context, initialPath, ".html")) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			output(file, context, os, ba);
			r.add(os.toString(StandardCharsets.UTF_8.displayName()));
		}
		return r;
	}

	private static Set<String> allFilesWithExtension(ServletContext context, String initialPath, String extension) {
		Set<String> res = new TreeSet<>();
		extractFilesWithExtensionRec(context, initialPath, extension, res);
		return res;
	}

	private static void extractFilesWithExtensionRec(ServletContext context, String initialPath, String extension,
			Set<String> res) {
		for (String s : context.getResourcePaths(initialPath)) {
			if (s.endsWith("/")) {
				extractFilesWithExtensionRec(context, s, extension, res);
			} else if (s.endsWith(extension)) {
				res.add(s);
			}
		}
	}

	private static void concatenateOutput(String directory, String fileExtension, ServletContext context,
			OutputStream os, BeforeAfter ba) throws IOException {
		for (String res : new TreeSet<>(context.getResourcePaths(directory))) {
			if (res.endsWith(fileExtension)) {
				output(res, context, os, ba);
			}
		}
	}

	private static void output(String file, ServletContext context, OutputStream os, BeforeAfter ba) throws IOException {
		ba.before(file, context, os);
		StreamUtils.copy(context.getResourceAsStream(file), os);
		ba.after(file, context, os);
		os.flush();
	}

	@ExpectPermission(Permission.ADMINISTRATION)
	@RequestMapping(value = { "admin", "admin/configure-login", "admin/manage-users", "admin/role",
			"admin/export-import", "admin/endpoint-info", "admin/parameters",
			"admin/manage-anonymous-users-access", "admin/manage-smtp-configuration" }, method = RequestMethod.GET)
	public void handleIndexForAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		handleIndex(request, response);
	}

	@ExpectPermission(Permission.PROJECT_ADMINISTRATION)
	@RequestMapping(value = { PROJ_SHORT_NAME + "/manage",//
			PROJ_SHORT_NAME + "/manage/project",//
			PROJ_SHORT_NAME + "/manage/boards",//
			PROJ_SHORT_NAME + "/manage/roles",//
			PROJ_SHORT_NAME + "/manage/labels",//
			PROJ_SHORT_NAME + "/manage/import",//
			PROJ_SHORT_NAME + "/manage/milestones",//
			PROJ_SHORT_NAME + "/manage/anonymous-users-access",//
			PROJ_SHORT_NAME + "/manage/status" }, method = RequestMethod.GET)
	public void handleIndexForProjectAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		handleIndex(request, response);
	}

	@ExpectPermission(Permission.UPDATE_PROFILE)
	@RequestMapping(value = "me", method = RequestMethod.GET)
	public void handleIndexForMe(HttpServletRequest request, HttpServletResponse response) throws IOException {
		handleIndex(request, response);
	}
	
	@RequestMapping("/not-found") 
	public void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		handleIndex(request, response);
	}
	
	@RequestMapping("/error")
	public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		handleIndex(request, response);
	}
	
	@RequestMapping("/404") 
	public String handle404() {
		return "redirect:/not-found";
	}

	@RequestMapping(value = { "/",//
			"user/{provider}/{username}", "user/{provider}/{username}/projects/", "user/{provider}/{username}/activity/",//
			"about",//
			"search",//
			"search/" + PROJ_SHORT_NAME + "/" + BOARD_SHORT_NAME + "-" + CARD_SEQ,//
			PROJ_SHORT_NAME + "",//
			PROJ_SHORT_NAME + "/search",//
			PROJ_SHORT_NAME + "/search/" + BOARD_SHORT_NAME + "-" + CARD_SEQ,// /
			PROJ_SHORT_NAME + "/" + BOARD_SHORT_NAME,//
			PROJ_SHORT_NAME + "/statistics",//
			PROJ_SHORT_NAME + "/milestones",//
			PROJ_SHORT_NAME + "/milestones/" + BOARD_SHORT_NAME + "-" + CARD_SEQ,//
			PROJ_SHORT_NAME + "/" + BOARD_SHORT_NAME + "-" + CARD_SEQ }, method = RequestMethod.GET)
	public void handleIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ServletContext context = request.getServletContext();
		
		if(contains(env.getActiveProfiles(), "dev") || indexTopTemplate.get() == null) {
		    ByteArrayOutputStream indexTop = new ByteArrayOutputStream();
		    StreamUtils.copy(context.getResourceAsStream("/WEB-INF/views/index-top.html"), indexTop);
		    indexTopTemplate.set(Mustache.compiler().escapeHTML(false).compile(indexTop.toString(StandardCharsets.UTF_8.name())));
		}

		if (contains(env.getActiveProfiles(), "dev") || indexCache.get() == null) {
			ByteArrayOutputStream index = new ByteArrayOutputStream();
			output("/WEB-INF/views/index.html", context, index, new BeforeAfter());

			Map<String, Object> data = new HashMap<>();
			data.put("contextPath", request.getServletContext().getContextPath() + "/");
			data.put("inlineTemplates", prepareTemplates(context, "/partials/"));

			indexCache.set(Mustache.compiler().escapeHTML(false)
					.compile(index.toString(StandardCharsets.UTF_8.name())).execute(data)
					.getBytes(StandardCharsets.UTF_8));
		}

		try (OutputStream os = response.getOutputStream()) {
			response.setContentType("text/html; charset=UTF-8");
			
			Map<String, Object> localizationData = new HashMap<>();
			Locale currentLocale = ObjectUtils.firstNonNull(request.getLocale(), Locale.ENGLISH);
			localizationData.put("firstDayOfWeek", Calendar.getInstance(currentLocale).getFirstDayOfWeek());
			
			StreamUtils.copy(indexTopTemplate.get().execute(localizationData).getBytes(StandardCharsets.UTF_8), os);
			StreamUtils.copy(indexCache.get(), os);
		}
	}

	/**
	 * Dynamically load and concatenate the js present in the configured directories
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/resource/app.js", method = RequestMethod.GET)
	public void handleJs(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (contains(env.getActiveProfiles(), "dev") || jsCache.get() == null) {
			ServletContext context = request.getServletContext();
			response.setContentType("text/javascript");
			BeforeAfter ba = new JS();
			ByteArrayOutputStream allJs = new ByteArrayOutputStream();

			//
			for (String res : Arrays.asList("/js/d3.v3.min.js", "/js/cal-heatmap.min.js",//
					"/js/jquery.min.js", "/js/jquery-ui.min.js",//
					"/js/jquery.ui.touch-punch.min.js",//
					"/js/highlight.pack.js",//
					"/js/marked.js",//
					"/js/sockjs.min.js", "/js/stomp.min.js",//
					"/js/angular-file-upload-html5-shim.js",//
					"/js/angular.min.js", "/js/angular-sanitize.min.js",//
					"/js/angular-ui-router.min.js",//
					"/js/angular-file-upload.min.js",//
					"/js/bindonce.min.js",//
					"/js/angular-translate.min.js",//
					"/js/sortable.js", //
					"/js/spectrum.js", //
					"/js/codemirror-compressed.js",//
					"/js/peg-0.8.0.min.js",//
					"/js/moment.min.js",//
					"/js/Chart.min.js",//
					"/js/ui-bootstrap-tpls-0.11.0.min.js",//
					"/js/df-tab-menu.min.js",//
					"/js/df-autocomplete.js")) {
				output(res, context, allJs, ba);
			}
			//

			//
			addMessages(context, allJs, ba);
			//

			output("/app/app.js", context, allJs, ba);
			concatenateOutput("/app/controllers/", ".js", context, allJs, ba);
			concatenateOutput("/app/controllers/admin/", ".js", context, allJs, ba);
			concatenateOutput("/app/controllers/project/", ".js", context, allJs, ba);
			concatenateOutput("/app/directives/", ".js", context, allJs, ba);
			concatenateOutput("/app/filters/", ".js", context, allJs, ba);
			concatenateOutput("/app/services/", ".js", context, allJs, ba);

			jsCache.set(allJs.toByteArray());

		}

		try (OutputStream os = response.getOutputStream()) {
			response.setContentType("text/javascript");
			StreamUtils.copy(jsCache.get(), os);
		}
	}

	private void addMessages(ServletContext context, OutputStream os, BeforeAfter ba) throws IOException {
		ba.before("i18n", context, os);
		//
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath:io/lavagna/i18n/messages_*.properties");
		//
		os.write(("window.io_lavagna=window.io_lavagna||{};window.io_lavagna.i18n=" + Json.GSON
				.toJson(fromResources(resources))).getBytes(StandardCharsets.UTF_8));
		ba.after("i18n", context, os);
	}

	private static Map<String, Map<Object, Object>> fromResources(Resource[] resources) throws IOException {

		Pattern extractLanguage = Pattern.compile("^messages_(.*)\\.properties$");
		
		Properties buildProp = new Properties();
		buildProp.load(new ClassPathResource("io/lavagna/build.properties").getInputStream());

		Map<String, Map<Object, Object>> langs = new HashMap<>();

		for (Resource res : resources) {
			Matcher matcher = extractLanguage.matcher(res.getFilename());
			matcher.find();
			String lang = matcher.group(1);
			Properties p = new Properties();
			p.load(res.getInputStream());
			langs.put(lang, new HashMap<>(p));
			langs.get(lang).putAll(new HashMap<>(buildProp));
		}
		return langs;
	}

	@RequestMapping(value = "/css/all.css", method = RequestMethod.GET)
	public void handleCss(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (contains(env.getActiveProfiles(), "dev") || cssCache.get() == null) {
			ByteArrayOutputStream cssOs = new ByteArrayOutputStream();
			ServletContext context = request.getServletContext();
			BeforeAfter ba = new BeforeAfter();
			for (String res : Arrays.asList("/css/bootstrap.css",//
					"/css/highlight-default.css",//
					"/css/jquery-ui.css",//
					"/css/spectrum.css",//
					"/css/codemirror.css",//
					"/css/font-awesome.css",//
					"/css/df-tab-menu.css",//
					"/css/df-autocomplete.css",//
					"/css/lvg-general.css",//
					"/css/lvg-navigation.css",//
					"/css/lvg-project.css",//
					"/css/lvg-board.css",//
					"/css/lvg-card.css",//
					"/css/lvg-admin.css",//
					"/css/lvg-user.css",//
					"/css/lvg-search.css",
					"/css/lvg-login.css")) {
				output(res, context, cssOs, ba);
			}

			cssCache.set(cssOs.toByteArray());
		}

		try (OutputStream os = response.getOutputStream()) {
			response.setContentType("text/css");
			StreamUtils.copy(cssCache.get(), os);
		}
	}

	private static class BeforeAfter {
		void before(String file, ServletContext context, OutputStream os) throws IOException {
		}

		void after(String file, ServletContext context, OutputStream os) throws IOException {
		}
	}

	private static class JS extends BeforeAfter {

		@Override
		public void before(String file, ServletContext context, OutputStream os) throws IOException {
			os.write((";\n\n /* begin " + file + " */ \n\n").getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public void after(String file, ServletContext context, OutputStream os) throws IOException {
			os.write((";\n\n /* end " + file + " */ \n\n").getBytes(StandardCharsets.UTF_8));
		}
	}

	private static class AngularTemplate extends BeforeAfter {
		@Override
		void before(String file, ServletContext context, OutputStream os) throws IOException {
			os.write(("<script type=\"text/ng-template\" id=\"" + StringUtils.stripStart(file, "/") + "\">")
					.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		void after(String file, ServletContext context, OutputStream os) throws IOException {
			os.write("</script>".getBytes(StandardCharsets.UTF_8));
		}
	}
}
