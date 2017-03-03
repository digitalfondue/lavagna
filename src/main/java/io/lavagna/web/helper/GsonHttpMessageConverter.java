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
package io.lavagna.web.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.lavagna.common.Constants;
import io.lavagna.common.Json;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 *
 * Read/write json, derived from :
 * <ul>
 * <li>http://stackoverflow.com/a/5099622
 * <li>http://stackoverflow.com/a/8728500
 * </ul>
 *
 * Used as a lightweight alternative to jackson.
 */
public class GsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

	private final Gson gson = new GsonBuilder().serializeNulls().setDateFormat(Constants.DATE_FORMAT)
            .registerTypeHierarchyAdapter(Date.class, new Json.CustomDateSerializer())
			.generateNonExecutableJson().create();

	public GsonHttpMessageConverter() {
		super(new MediaType("application", "json", StandardCharsets.UTF_8));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {
		try (Reader reader = new InputStreamReader(inputMessage.getBody(), StandardCharsets.UTF_8)) {
			return gson.fromJson(reader, clazz);
		} catch (JsonSyntaxException e) {
			throw new HttpMessageNotReadableException("Could not read JSON: " + e.getMessage(), e);
		}
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException {
		try (Writer writer = new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8)) {
			gson.toJson(t, writer);
		}
	}
}
