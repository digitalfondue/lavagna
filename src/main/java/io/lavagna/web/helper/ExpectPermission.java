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

import io.lavagna.model.Permission;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation can be used at both method and class level. Method level annotation will take precedence over the
 * class one.<br>
 *
 * If an user does not have the related permission, a 403 error will be triggered.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ExpectPermission {
	Permission value();

	Class<? extends OwnershipChecker> ownershipChecker() default NoOpOwnershipChecker.class;

	public static final class Helper {

		private Helper() {
		}

		public static ExpectPermission getAnnotation(HandlerMethod handler) {
			return getAnnotation((Object) handler);
		}

		static ExpectPermission getAnnotation(Object handler) {
			if (handler == null || !(handler instanceof HandlerMethod)) {
				return null;
			}

			HandlerMethod hm = (HandlerMethod) handler;

			ExpectPermission expectPermission = hm.getMethodAnnotation(ExpectPermission.class);
			if (expectPermission == null) {
				expectPermission = hm.getBeanType().getAnnotation(ExpectPermission.class);
			}
			return expectPermission;
		}
	}
}
