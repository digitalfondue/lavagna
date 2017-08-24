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
package io.lavagna.web.api;

import io.lavagna.model.CardLabel;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchControllerTest {

	@Mock
	private ProjectService projectService;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private CardLabelRepository cardLabelRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private SearchService searchService;
	@Mock
	private UserWithPermission user;

	private SearchController searchController;

	@Before
	public void prepare() {
		searchController = new SearchController(userRepository, cardRepository, cardLabelRepository, searchService,
				projectService);

	}

	@Test
	public void testFindUsersId() {
		List<String> users = Arrays.asList("test");

		searchController.findUsersId(Arrays.asList("test"));

		verify(userRepository).findUsersId(users);
	}

	@Test
	public void testFindCardsIs() {
		List<String> cards = Arrays.asList("test");

		searchController.findCardsIds(Arrays.asList("test"));

		verify(cardRepository).findCardsIds(cards);
	}

	@Test
	public void testFindLabelListValueMapping() {
		List<String> labelValues = Arrays.asList("test");

		searchController.findLabelListValueMapping(Arrays.asList("test"));

		verify(cardLabelRepository).findLabelListValueMapping(labelValues);
	}

	@Test
	public void testFindMilestones() {
		String term = "test";

		searchController.findMilestones("test", null, user);

		verify(cardLabelRepository).findListValuesBy(eq(CardLabel.LabelDomain.SYSTEM), eq(SYSTEM_LABEL_MILESTONE), eq(term), isNull(Integer.class), eq(user));
	}

	@Test
	public void testFindUsers() {
		String term = "test";

		searchController.findUsers("test", null, user);

		verify(userRepository).findUsers(eq(term));
	}

	@Test
	public void testFindLabelNames() {
		String term = "test";

		searchController.findLabel("test", null, user);

		verify(cardLabelRepository).findUserLabelNameBy(eq(term), isNull(Integer.class), eq(user));
	}

    @Test
    public void testFindLabelValues() {
        String term = "test";

        searchController.findLabel("test", null, user);

        verify(cardLabelRepository).findUserLabelNameBy(eq(term), isNull(Integer.class), eq(user));
    }

	@Test
	public void testSearch() {
		when(projectService.findIdByShortName("SHORT")).thenReturn(42);

		searchController.search(null, "SHORT", 0, user);

		verify(projectService).findIdByShortName("SHORT");
		verify(searchService).find((List<SearchFilter>)isNull(List.class), eq(42), isNull(Integer.class), eq(user), eq(0));
	}
}
