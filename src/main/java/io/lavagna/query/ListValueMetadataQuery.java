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
package io.lavagna.query;

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import io.lavagna.model.ListValueMetadata;

import java.util.Collection;
import java.util.List;

@QueryRepository
public interface ListValueMetadataQuery {

	@Query("INSERT INTO LA_LIST_VALUE_METADATA(LVM_LABEL_LIST_VALUE_ID_FK, LVM_KEY, LVM_VALUE) "
			+ "VALUES (:labelListValueId, :key, :value)")
	int insert(@Bind("labelListValueId") int labelListValueId, @Bind("key") String key, @Bind("value") String value);

	@Query("DELETE FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId AND LVM_KEY = :key")
	int delete(@Bind("labelListValueId") int labelListValueId, @Bind("key") String key);

	@Query("DELETE FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId")
	int deleteAllWithLabelListValueId(@Bind("labelListValueId") int labelListValueId);

	@Query("UPDATE LA_LIST_VALUE_METADATA SET LVM_VALUE = :value WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId AND LVM_KEY = :key")
	int update(@Bind("labelListValueId") int labelListValueId, @Bind("key") String key, @Bind("value") String value);

	@Query("SELECT * FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId")
	List<ListValueMetadata> findByLabelListValueId(@Bind("labelListValueId") int labelListValueId);

	@Query("SELECT * FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK IN (:labelListValueIds)")
	List<ListValueMetadata> findByLabelListValueIds(@Bind("labelListValueIds") Collection<Integer> labelListValueId);

	@Query("SELECT * FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId AND LVM_KEY = :key")
	ListValueMetadata findByLabelListValueIdAndKey(@Bind("labelListValueId") int labelListValueId, @Bind("key") String key);

	@Query("SELECT COUNT(*) FROM LA_CARD_LABEL_VALUE WHERE CARD_LABEL_VALUE_LIST_VALUE_FK = :id")
	Integer countUse(@Bind("id") int labelListValueId);
}
