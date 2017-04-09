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
package io.lavagna.common;

import com.google.gson.*;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.TimeZone;

public final class Json {

    private Json() {
    }

    public static class CustomDateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            return date == null ? JsonNull.INSTANCE : new JsonPrimitive(formatDate(date));
        }
    }

    public static String formatDate(Date date) {
        return DateFormatUtils.format(date, Constants.DATE_FORMAT, TimeZone.getTimeZone("Z"));
    }

    public static final Gson GSON = new GsonBuilder()
        .serializeNulls()
        .setDateFormat(Constants.DATE_FORMAT)
        .registerTypeHierarchyAdapter(Date.class, new Json.CustomDateSerializer())
        .create();
}
