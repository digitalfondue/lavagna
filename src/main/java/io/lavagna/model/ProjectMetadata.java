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
package io.lavagna.model;

import static io.lavagna.model.util.DataOutputStreamUtils.writeEnum;
import static io.lavagna.model.util.DataOutputStreamUtils.writeInts;
import static io.lavagna.model.util.DataOutputStreamUtils.writeNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import lombok.Getter;

import org.apache.commons.codec.digest.DigestUtils;

@Getter
public class ProjectMetadata {
    private final SortedMap<Integer, CardLabel> labels;
    private final SortedMap<Integer, SortedMap<Integer, LabelListValueWithMetadata>> labelListValues;
    private final Map<ColumnDefinition, BoardColumnDefinition> columnsDefinition;
    private final String hash;

    public ProjectMetadata(SortedMap<Integer, CardLabel> labels, SortedMap<Integer, SortedMap<Integer, LabelListValueWithMetadata>> labelListValues,
            Map<ColumnDefinition, BoardColumnDefinition> columnsDefinition) {
        this.labels = labels;
        this.labelListValues = labelListValues;
        this.columnsDefinition = columnsDefinition;
        this.hash = hash(labels, labelListValues, columnsDefinition);
    }

    private static String hash(SortedMap<Integer, CardLabel> labels, SortedMap<Integer, SortedMap<Integer, LabelListValueWithMetadata>> labelListValues,
            Map<ColumnDefinition, BoardColumnDefinition> columnsDefinition) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);

        try {

            for (CardLabel cl : labels.values()) {
                hash(daos, cl);
            }

            for (SortedMap<Integer, LabelListValueWithMetadata> v : labelListValues.values()) {
                for (LabelListValueWithMetadata l : v.values()) {
                    hash(daos, l);
                }
            }

            for (BoardColumnDefinition b : columnsDefinition.values()) {
                hash(daos, b);
            }

            daos.flush();
            return DigestUtils.sha256Hex(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void hash(DataOutputStream daos, BoardColumnDefinition b) throws IOException {
        writeInts(daos, b.getId(), b.getProjectId());
        writeEnum(daos, b.getValue());
        writeInts(daos, b.getColor());
    }

    private static void hash(DataOutputStream daos, CardLabel cl) throws IOException {
        writeInts(daos, cl.getId(), cl.getProjectId());
        writeNotNull(daos, cl.getName());
        writeInts(daos, cl.getColor());
        writeNotNull(daos, cl.isUnique());
        writeEnum(daos, cl.getType());
        writeEnum(daos, cl.getDomain());
    }

    private static void hash(DataOutputStream daos, LabelListValueWithMetadata l) throws IOException {
        writeInts(daos, l.getId(), l.getCardLabelId(), l.getOrder());
        writeNotNull(daos, l.getValue());
        for (Entry<String, String> kv : l.getMetadata().entrySet()) {
            writeNotNull(daos, kv.getKey());
            writeNotNull(daos, kv.getValue());
        }
    }
}
