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
package io.lavagna.service;

import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.UserWithPermission;
import io.lavagna.query.SearchQuery;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.lavagna.common.Constants.*;
import static org.apache.commons.lang3.time.DateUtils.*;

public class SearchFilter {

	private final FilterType type;
	private final String name;
	private final SearchFilterValue value;

	public static SearchFilter filter(FilterType type, ValueType valueType, Object value) {
		return new SearchFilter(type, null, new SearchFilterValue(valueType, value));
	}

	public static SearchFilter filterByColumnDefinition(ColumnDefinition value) {
		return filter(SearchFilter.FilterType.STATUS, SearchFilter.ValueType.STRING, value.toString());
	}

	public SearchFilter(FilterType type, String name, SearchFilterValue value) {
		this.type = type;
		this.name = name;
		this.value = value;
	}


	public static class SearchContext {
		private final UserWithPermission currentUser;
		private final Map<String, Integer> userNameToId;
		private final Map<String, Integer> cardNameToId;

		public SearchContext(UserWithPermission currentUser, Map<String, Integer> userNameToId,
				final Map<String, Integer> cardNameToId) {
			this.currentUser = currentUser;
			this.userNameToId = userNameToId;
			this.cardNameToId = cardNameToId;
		}

        public UserWithPermission getCurrentUser() {
            return this.currentUser;
        }

        public Map<String, Integer> getUserNameToId() {
            return this.userNameToId;
        }

        public Map<String, Integer> getCardNameToId() {
            return this.cardNameToId;
        }
    }

	public enum FilterType {

		USER_LABEL {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				String r = queries.findByUserLabel();
				params.add(sf.name);
				if (sf.value == null) {
					return r;
				}

				String val = sf.value.value.toString();
				// string
				params.add(val);
				// int
				params.add(tryParse(val));
				// timestamp from/to
				addDateParams(sf, params);
				// user
				params.add(from(context.userNameToId, val));
				// card
				params.add(from(context.cardNameToId, val));
				// list value
				params.add(val);

				return r + " " + queries.andLabelValueString();
			}

		},
		ASSIGNED {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {

				params.add(SYSTEM_LABEL_ASSIGNED);

				if (sf.value.type == ValueType.UNASSIGNED) {
					return queries.findCardIdNotInOpen() + " " + queries.findBySystemLabel() + " "
							+ queries.findCardIdNotInClose();
				} else {
					addUserToParam(context.currentUser, params, context.userNameToId, sf);
					return queries.findBySystemLabel() + " " + queries.andLabelValueUser();
				}
			}
		},
		CREATED_BY {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				addUserToParam(context.currentUser, params, context.userNameToId, sf);
				return queries.findByCardCreationEventUser();
			}
		},
		CREATED {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {

				addDateParams(sf, params);

				return queries.findByCardCreationEventDate();
			}
		},
		WATCHED_BY {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				params.add(SYSTEM_LABEL_WATCHED_BY);

				if (sf.value.type == ValueType.UNASSIGNED) {
					return queries.findCardIdNotInOpen() + " " + queries.findBySystemLabel() + " "
							+ queries.findCardIdNotInClose();
				} else {
					addUserToParam(context.currentUser, params, context.userNameToId, sf);
					return queries.findBySystemLabel() + " " + queries.andLabelValueUser();
				}
			}
		},
		MILESTONE {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {

				params.add(SYSTEM_LABEL_MILESTONE);

				if (sf.value.type == ValueType.UNASSIGNED) {
					return queries.findCardIdNotInOpen() + " " + queries.findBySystemLabel() + " "
							+ queries.findCardIdNotInClose();
				} else {
					params.add(sf.value.value);
					return queries.findBySystemLabel() + " " + queries.andLabelListValueEq();
				}

			}

		},

		DUE_DATE {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {

				params.add(SYSTEM_LABEL_DUE_DATE);

				addDateParams(sf, params);

				return queries.findBySystemLabel() + " " + queries.andLabelValueDate();
			}

		},

		STATUS {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				params.add(sf.value.value);
				return queries.findByStatus();
			}
		},

        NOTLOCATION {
            @Override
            public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
                SearchContext context) {
                params.add(sf.value.value);
                return queries.findByNotLocation();
            }
        },

		LOCATION {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				params.add(sf.value.value);
				return queries.findByLocation();
			}
		},

		UPDATED {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				addDateParams(sf, params);
				return queries.findByUpdated();
			}

		},

		UPDATED_BY {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				addUserToParam(context.currentUser, params, context.userNameToId, sf);
				return queries.findByUpdatedBy();
			}
		},

		BOARD_STATUS {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				params.add(sf.value.value);
				return queries.findByBoardStatus();
			}
		},

		FREETEXT {
			@Override
			public String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
					SearchContext context) {
				params.add(sf.value.value);// for card sequence number
				params.add(sf.value.value);// for card name
				params.add(sf.value.value);// for card data
				return queries.findByFreeText();
			}
		};

		public abstract String toBaseQuery(SearchFilter sf, SearchQuery queries, List<Object> params,
				SearchContext context);

	}

	private static void addDateParams(SearchFilter sf, List<Object> params) {
		if (sf.value.type == ValueType.DATE_IDENTIFIER) {
			fromDateIdentifier(sf.value.value.toString(), params);
		} else {
			Date from = null, to = null;
			try {
				String[] splitted = sf.value.value.toString().split(Pattern.quote(".."));
				String[] format = { "dd.MM.yyyy", "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "yyyy.MM.dd",
						"yyyy/MM/dd" };
				if (splitted.length == 2) {
					from = truncate(parseDateStrictly(splitted[0], format), Calendar.DAY_OF_MONTH);
					to = addDays(truncate(parseDateStrictly(splitted[1], format), Calendar.DAY_OF_MONTH), 1);
				} else {
					from = truncate(parseDateStrictly(sf.value.value.toString(), format), Calendar.DAY_OF_MONTH);
					to = addDays(from, 1);
				}
			} catch (ParseException pe) {
				//
			}
			params.add(from);
			params.add(to);
		}
	}

	// TODO: refactor/cleanup
	private static void fromDateIdentifier(String identifier, List<Object> params) {
		Date todayTruncated = truncate(new Date(), Calendar.DAY_OF_MONTH);
		Date beginMonth = truncate(new Date(), Calendar.MONTH);

		// TODO: internazionalization...
		Calendar c = truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Date beginOfWeek = c.getTime();
		//

		switch (identifier) {
		case "late":
			params.add(new Date(0));
			params.add(todayTruncated);
			break;
		case "today":
			params.add(todayTruncated);
			params.add(addDays(todayTruncated, 1));
			break;
		case "yesterday":
			params.add(addDays(todayTruncated, -1));
			params.add(todayTruncated);
			break;
		case "tomorrow":
			params.add(addDays(todayTruncated, 1));
			params.add(addDays(todayTruncated, 2));
			break;
		case "this week":
			params.add(beginOfWeek);
			params.add(addWeeks(beginOfWeek, 1));
			break;
		case "this month":
			params.add(beginMonth);
			params.add(addMonths(beginMonth, 1));
			break;
		case "next week":
			params.add(addWeeks(beginOfWeek, 1));
			params.add(addWeeks(beginOfWeek, 2));
			break;
		case "next month":
			params.add(addMonths(beginMonth, 1));
			params.add(addMonths(beginMonth, 2));
			break;
		case "previous week":
			params.add(addWeeks(beginOfWeek, -1));
			params.add(beginOfWeek);
			break;
		case "previous month":
			params.add(addMonths(beginMonth, -1));
			params.add(beginMonth);
			break;
		case "last week":
			params.add(addDays(todayTruncated, -6));
			params.add(addDays(todayTruncated, 1));
			break;
		case "last month":
			params.add(addDays(todayTruncated, -29));
			params.add(addDays(todayTruncated, 1));
			break;
		default:
			break;
		}
	}

	public enum ValueType {
		BOOLEAN, STRING, CURRENT_USER, DATE_IDENTIFIER, UNASSIGNED
	}

	public static class SearchFilterValue {
		private final ValueType type;
		private final Object value;

        @java.beans.ConstructorProperties({ "type", "value" }) public SearchFilterValue(ValueType type,
            Object value) {
            this.type = type;
            this.value = value;
        }

        public ValueType getType() {
            return this.type;
        }

        public Object getValue() {
            return this.value;
        }
    }

	private static void addUserToParam(UserWithPermission userWithPermission, List<Object> params,
			Map<String, Integer> userNameToId, SearchFilter searchFilter) {
		if (searchFilter.value.type == ValueType.CURRENT_USER && "me".equals(searchFilter.value.value)) {
			params.add(userWithPermission.getId());
		} else {
			params.add(from(userNameToId, searchFilter.value.value));
		}
	}

	private static Integer from(Map<String, Integer> f, Object key) {
		return key == null ? null : f.get(key);
	}

	private static Integer tryParse(String value) {
		try {
			return Integer.valueOf(value, 10);
		} catch (NullPointerException | NumberFormatException e) {
			return null;
		}
	}

	public FilterType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public SearchFilterValue getValue() {
		return value;
	}

}
