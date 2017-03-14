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

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Read {

    public static Path readFile(String name, Path tempFile) throws IOException {
        try (InputStream is = Files.newInputStream(tempFile); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (ze.getName().equals(name)) {
                    Path p = Files.createTempFile(null, null);
                    Files.copy(zis, p, StandardCopyOption.REPLACE_EXISTING);
                    return p;
                }
                ze = zis.getNextEntry();
            }
        }
        return null;
    }

    public static <T> T readObject(String name, Path tempFile, TypeToken<T> t) {
        return readMatchingObjects(name, tempFile, t).get(0);
    }

    public static boolean hasMatchingObject(String regex, Path tempFile) {
        try (InputStream is = Files.newInputStream(tempFile); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (ze.getName().matches("^" + regex + "$")) {
                    return true;
                }
                ze = zis.getNextEntry();
            }
        } catch (IOException ioe) {
            return false;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> readMatchingObjects(String regex, Path tempFile, TypeToken<T> t) {
        try {
            List<T> res = new ArrayList<>();
            try (InputStream is = Files.newInputStream(tempFile); ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    if (ze.getName().matches("^" + regex + "$")) {
                        res.add((T) Json.GSON.fromJson(new InputStreamReader(zis, StandardCharsets.UTF_8), t.getType()));
                    }
                    ze = zis.getNextEntry();
                }
                return res;
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("error while reading data for " + regex, ioe);
        }
    }
}
