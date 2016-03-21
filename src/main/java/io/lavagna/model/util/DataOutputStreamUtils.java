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
package io.lavagna.model.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public final class DataOutputStreamUtils {
    
    
    public static void writeInts(DataOutputStream daos, int... e) throws IOException {
        for (int i : e) {
            daos.writeChars(Integer.toString(i));
        }
    }

    public static void writeNotNull(DataOutputStream daos, Boolean s) throws IOException {
        if (s != null) {
            daos.writeChars(Boolean.toString(s));
        }
    }

    public static void writeNotNull(DataOutputStream daos, Date s) throws IOException {
        if (s != null) {
            daos.writeChars(Long.toString(s.getTime()));
        }
    }

    public static void writeNotNull(DataOutputStream daos, String s) throws IOException {
        if (s != null) {
            daos.writeChars(s);
        }
    }

    public static void writeNotNull(DataOutputStream daos, Integer val) throws IOException {
        if (val != null) {
            daos.writeChars(Integer.toString(val));
        }
    }

    public static <T extends Enum<T>> void writeEnum(DataOutputStream daos, T e) throws IOException {
        if (e != null) {
            daos.writeChars(e.toString());
        }
    }

}
