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

import io.lavagna.web.api.model.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GeneralHandlerExceptionResolver implements HandlerExceptionResolver {

	private static final Logger LOG = LogManager.getLogger();

	private final Map<Class<? extends Throwable>, Integer> statusCodeResolver = new LinkedHashMap<>();

	public GeneralHandlerExceptionResolver() {
		// add the exceptions from the less generic to the more one
		statusCodeResolver.put(EmptyResultDataAccessException.class, HttpStatus.NOT_FOUND.value());
		statusCodeResolver.put(ValidationException.class, HttpStatus.UNPROCESSABLE_ENTITY.value());
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		handleException(ex, response);
		return new ModelAndView();
	}

	private void handleException(Exception ex, HttpServletResponse response) {
		for (Entry<Class<? extends Throwable>, Integer> entry : statusCodeResolver.entrySet()) {
			if (ex.getClass().equals(entry.getKey())) {
				response.setStatus(entry.getValue());
				LOG.info("Class: {} - Message: {} - Cause: {}", ex.getClass(), ex.getMessage(), ex.getCause());
				LOG.info("Cnt", ex);
				return;
			}
		}
		/**
		 * Non managed exceptions flow Set HTTP status 500 and log the exception with a production visible level
		 */
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		LOG.warn(ex.getMessage(), ex);
	}

}
