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

import io.lavagna.common.ConstructorAnnotationRowMapper.Column;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ListValueMetadata {

	private final int labelListValueId;
	private final String key;
	private final String value;
	
	
	public ListValueMetadata(@Column("LVM_LABEL_LIST_VALUE_ID_FK") int labelListValueId, 
			@Column("LVM_KEY") String key, @Column("LVM_VALUE") String value) {
		this.labelListValueId = labelListValueId;
		this.key = key;
		this.value = value;
	}
	
}
