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

import io.lavagna.model.*;
import io.lavagna.query.SearchQuery;
import io.lavagna.service.SearchFilter.FilterType;
import io.lavagna.service.SearchFilter.SearchContext;
import io.lavagna.service.SearchFilter.SearchFilterValue;
import io.lavagna.service.SearchFilter.ValueType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;

import static io.lavagna.service.SearchFilter.filter;
import static io.lavagna.service.SearchFilter.filterByColumnDefinition;

/**
 * Service for searching Cards using criterias defined using {@link SearchFilter}.
 */
@Service
@Transactional(readOnly = true)
public class SearchService {

	private static final Logger LOG = LogManager.getLogger();

	private static final int CARDS_PER_PAGE = 50;

	private final NamedParameterJdbcTemplate jdbc;
	private final CardRepository cardRepository;
	private final CardService cardService;
	private final UserRepository userRepository;
	private final ProjectService projectService;
	private final BoardRepository boardRepository;
	private final SearchQuery queries;


	public SearchService(CardRepository cardRepository, CardService cardService, UserRepository userRepository,
			ProjectService projectService, BoardRepository boardRepository, NamedParameterJdbcTemplate jdbc,
			SearchQuery queries) {
		this.cardRepository = cardRepository;
		this.cardService = cardService;
		this.userRepository = userRepository;
		this.projectService = projectService;
		this.boardRepository = boardRepository;
		this.jdbc = jdbc;
		this.queries = queries;
	}

	private List<SearchFilter> filtersAsList(SearchFilter locationFilter,
			SearchFilter statusOpen, boolean excludeArchivedBoards) {
		return excludeArchivedBoards ?
				Arrays.asList(statusOpen, locationFilter,
						filter(FilterType.BOARD_STATUS, SearchFilter.ValueType.BOOLEAN, Boolean.FALSE)) :
				Arrays.asList(statusOpen, locationFilter);
	}

	public Map<ColumnDefinition, Integer> findTaksByColumnDefinition(Integer projectId, Integer boardId,
			boolean excludeArchivedBoards, UserWithPermission user) {
		SearchFilter locationFilter = filter(SearchFilter.FilterType.LOCATION, SearchFilter.ValueType.STRING,
				BoardColumn.BoardColumnLocation.BOARD.toString());

		Map<ColumnDefinition, Integer> results = new EnumMap<>(ColumnDefinition.class);

		SearchFilter statusOpen = filterByColumnDefinition(ColumnDefinition.OPEN);
		SearchResults openTasks = find(filtersAsList(locationFilter, statusOpen, excludeArchivedBoards), projectId,
				boardId, user, 0);
		results.put(ColumnDefinition.OPEN, openTasks.getCount());

		SearchFilter statusClosed = filterByColumnDefinition(ColumnDefinition.CLOSED);
		SearchResults closedTasks = find(filtersAsList(locationFilter, statusClosed, excludeArchivedBoards), projectId,
				boardId, user, 0);
		results.put(ColumnDefinition.CLOSED, closedTasks.getCount());

		SearchFilter statusBacklog = filterByColumnDefinition(ColumnDefinition.BACKLOG);
		SearchResults backlogTasks = find(filtersAsList(locationFilter, statusBacklog, excludeArchivedBoards),
				projectId, boardId, user, 0);

		SearchFilter backlogFilterLocation = filter(SearchFilter.FilterType.LOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.BACKLOG.toString());
		SearchResults backlogSideBarTasks = find(filtersAsList(backlogFilterLocation, statusBacklog, excludeArchivedBoards), projectId, boardId, user, 0);

		results.put(ColumnDefinition.BACKLOG, backlogTasks.getCount() + backlogSideBarTasks.getCount());

		SearchFilter statusDeferred = filterByColumnDefinition(ColumnDefinition.DEFERRED);
		SearchResults deferredTasks = find(filtersAsList(locationFilter, statusDeferred, excludeArchivedBoards),
				projectId, boardId, user, 0);
		results.put(ColumnDefinition.DEFERRED, deferredTasks.getCount());

		return results;
	}

	public SearchResults find(List<SearchFilter> unmergedSearchFilter, Integer projectId, Integer boardId,
			UserWithPermission currentUser) {
		return find(unmergedSearchFilter, projectId, boardId, currentUser, false, 0);
	}

	public SearchResults find(List<SearchFilter> unmergedSearchFilter, Integer projectId, Integer boardId,
			UserWithPermission currentUser, int page) {
		return find(unmergedSearchFilter, projectId, boardId, currentUser, true, page);
	}

	private SearchResults find(List<SearchFilter> unmergedSearchFilter, Integer projectId, Integer boardId,
			UserWithPermission currentUser, boolean paginate, int page) {

		// if a user don't have access to the specified project id we skip the
		// whole search
		final boolean userHasNotProjectAccess = projectId != null
				&& !currentUser.getBasePermissions().containsKey(Permission.READ)
				&& !currentUser.projectsWithPermission(Permission.READ).contains(
				projectService.findById(projectId).getShortName());
		final boolean userHasNoReadAccess = projectId == null
				&& !currentUser.getBasePermissions().containsKey(Permission.READ)
				&& currentUser.projectsWithPermission(Permission.READ).isEmpty();

		final boolean noProjectIdForBoardId = projectId == null && boardId != null;

		final boolean boardIsntInProject = projectId != null && boardId != null
				&& boardRepository.findBoardById(boardId).getProjectId() != projectId;

		if (userHasNotProjectAccess || userHasNoReadAccess || noProjectIdForBoardId || boardIsntInProject) {
			return new SearchResults(Collections.<CardFullWithCounts>emptyList(), 0, page, paginate ? CARDS_PER_PAGE : Integer.MAX_VALUE, paginate);
		}

		List<SearchFilter> searchFilters = mergeFreeTextFilters(unmergedSearchFilter);
		//

		List<Object> params = new ArrayList<>();
		int filteringConditionsCount = 0;
		//

		List<String> usersOrCardToSearch = new ArrayList<>();
		// fetch all possible user->id, card->id in the value types with string
		// (thus unknown use)
		for (SearchFilter searchFilter : searchFilters) {
			if (searchFilter.getValue() != null && searchFilter.getValue().getType() == ValueType.STRING) {
				usersOrCardToSearch.add(searchFilter.getValue().getValue().toString());
			}
		}
		//

		Map<String, Integer> cardNameToId = cardRepository.findCardsIds(usersOrCardToSearch);
		Map<String, Integer> userNameToId = userRepository.findUsersId(usersOrCardToSearch);

		SearchContext searchContext = new SearchContext(currentUser, userNameToId, cardNameToId);

		//

		StringBuilder baseQuery = new StringBuilder(queries.findFirstFrom()).append("SELECT CARD_ID FROM ( ");

		// add filter conditions
		for (int i = 0; i < searchFilters.size(); i++) {
			SearchFilter searchFilter = searchFilters.get(i);

			String filterConditionQuery = searchFilter.getType().toBaseQuery(searchFilter, queries, params,
					searchContext);

			baseQuery.append("( ").append(filterConditionQuery).append(" ) ");
			if (i < searchFilters.size() - 1) {
				baseQuery.append(" UNION ALL ");
			}
			filteringConditionsCount++;
		}
		//

		/* AS CARD_IDS -> table alias for mysql */
		baseQuery.append(" ) AS CARD_IDS GROUP BY CARD_ID HAVING COUNT(CARD_ID) = ?").append(queries.findSecond());
		params.add(filteringConditionsCount);

		if (boardId != null) {
			baseQuery.append(queries.findThirdWhere()).append(queries.findFourthInBoardId());
			params.add(boardId);
		} else if (projectId != null) {
			baseQuery.append(queries.findThirdWhere()).append(queries.findInFifthProjectId());
			params.add(projectId);
		} else if (!currentUser.getBasePermissions().containsKey(Permission.READ)) {

			Set<Integer> projectsWithPermission = currentUser.projectsIdWithPermission(Permission.READ);
			baseQuery.append(queries.findThirdWhere()).append(queries.findSixthRestrictedReadAccess()).append(" (")
					.append(StringUtils.repeat("?", " , ", projectsWithPermission.size())).append(" ) ");

			params.addAll(projectsWithPermission);
		}

		String findCardsQuery = queries.findFirstSelect() + baseQuery.toString() + queries.findSeventhOrderBy();

		if(paginate) {
			params.add(CARDS_PER_PAGE + 1);// limit
			params.add(page * CARDS_PER_PAGE);// offset
			findCardsQuery += queries.findEighthLimit();
		}



		List<Integer> sr = jdbc.getJdbcOperations().queryForList(findCardsQuery, params.toArray(), Integer.class);

		//

		int count = sr.size();
		if (paginate && page == 0 && sr.size() == (CARDS_PER_PAGE + 1) || page > 0) {
			String countCardsQuery = queries.findFirstSelectCount() + baseQuery.toString();
			count = jdbc.getJdbcOperations().queryForObject(countCardsQuery,
					params.subList(0, params.size() - 2).toArray(), Integer.class);
		}

		//
		return new SearchResults(cardFullWithCounts(sr), count, page, paginate ? CARDS_PER_PAGE : Integer.MAX_VALUE, paginate);
	}

	private List<CardFullWithCounts> cardFullWithCounts(List<Integer> sr) {

		if (sr.isEmpty()) {
			return Collections.emptyList();
		}

		// super ugly :(
		Map<Integer, Integer> idToPosition = new HashMap<>();
		for (int i = 0; i < sr.size(); i++) {
			idToPosition.put(sr.get(i), i);
		}

		CardFull[] orderedCf = new CardFull[sr.size()];
		// reorder:
		for (CardFull cf : cardRepository.findAllByIds(sr)) {
			orderedCf[idToPosition.get(cf.getId())] = cf;
		}
		//

		return cardService.fetchCardFull(Arrays.asList(orderedCf));
	}

	private static List<SearchFilter> mergeFreeTextFilters(List<SearchFilter> unmergedSearchFilter) {
		List<SearchFilter> merged = new ArrayList<>(unmergedSearchFilter.size());
		StringBuilder sb = new StringBuilder();
		for (SearchFilter sf : unmergedSearchFilter) {
			if (sf.getType() != FilterType.FREETEXT) {
				merged.add(sf);
			} else {
				sb.append(" ").append(sf.getValue().getValue());
			}
		}

		String freeText = sb.toString().trim();
		if (freeText.length() > 0) {
			merged.add(new SearchFilter(FilterType.FREETEXT, null, new SearchFilterValue(ValueType.STRING, freeText)));
		}

		return merged;
	}

	// used by HSQLDB, obviously not optimized at all (as it's only for dev
	// purpose)
	public static boolean searchText(String data, String toSearch) {
		String[] wordsToSearch = toSearch.split("\\s+");
		String lowerCasedData = data.toLowerCase(Locale.ENGLISH);
		for (String word : wordsToSearch) {
			if (!lowerCasedData.contains(word.toLowerCase(Locale.ENGLISH))) {
				return false;
			}
		}
		return true;
	}

	public static boolean searchTextClob(Clob data, String toSearch) {
		try (InputStream is = data.getAsciiStream()) {
			String res = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
			return searchText(res, toSearch);
		} catch (IOException | SQLException e) {
			LOG.warn("error while reading clob", e);
			return false;
		}
	}
}
