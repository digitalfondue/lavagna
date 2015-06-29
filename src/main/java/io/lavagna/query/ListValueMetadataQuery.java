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

import java.util.List;

import io.lavagna.common.Bind;
import io.lavagna.common.Query;
import io.lavagna.common.QueryRepository;
import io.lavagna.model.ListValueMetadata;

@QueryRepository
public interface ListValueMetadataQuery {

	@Query("INSERT INTO LA_LIST_VALUE_METADATA(LVM_LABEL_LIST_VALUE_ID_FK, LVM_KEY, LVM_VALUE) " 
			+ "VALUES (:labelListValueId, :key, :value)")
	int insert(@Bind("labelListValueId") int labelListValueId, @Bind("key") String key, @Bind("value") String value);

	@Query("DELETE FROM LA_LIST_VALUE_METADATA WHERE LVM_ID = :id")
	int delete(@Bind("id") int id);
	
	@Query("DELETE FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId")
	int deleteAllWithLabelListValueId(@Bind("labelListValueId") int labelListValueId);

	@Query("UPDATE LA_LIST_VALUE_METADATA SET LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId, LVM_KEY = :key, LVM_VALUE = :value WHERE LVM_ID = :id ")
	int update(@Bind("id") int id, @Bind("labelListValueId") int labelListValueId, @Bind("key") String key, @Bind("value") String value);

	@Query("SELECT * FROM LA_LIST_VALUE_METADATA WHERE LVM_LABEL_LIST_VALUE_ID_FK = :labelListValueId")
	List<ListValueMetadata> findByLabelListValueId(@Bind("labelListValueId") int labelListValueId);

	@Query("SELECT * FROM LA_LIST_VALUE_METADATA WHERE LVM_ID = :id")
	ListValueMetadata findById(@Bind("id") int id);
}
