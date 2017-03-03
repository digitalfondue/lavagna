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

import com.google.gson.reflect.TypeToken;
import io.lavagna.common.Json;
import io.lavagna.model.*;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.service.*;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_MILESTONE;

@ExpectPermission(Permission.SEARCH)
@RestController
public class SearchController {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final SearchService searchService;
    private final CardLabelRepository cardLabelRepository;
    private final ProjectService projectService;

    private static final Type LIST_OF_SEARCH_FILTERS = new TypeToken<List<SearchFilter>>() {
    }.getType();


    public SearchController(UserRepository userRepository, CardRepository cardRepository,
        CardLabelRepository cardLabelRepository, SearchService searchService, ProjectService projectService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.cardLabelRepository = cardLabelRepository;
        this.searchService = searchService;
        this.projectService = projectService;
    }

    /**
     * Given a list of user identifier "provider:username" it return a map name -> id.
     *
     * @return
     */
    @RequestMapping(value = "/api/search/user-mapping", method = RequestMethod.GET)
    public Map<String, Integer> findUsersId(@RequestParam("from") List<String> users) {
        return userRepository.findUsersId(users);
    }

    /**
     * Given a list of card identifier "PROJECT-CARD_SEQUENCE" it return a map card identifier -> id Used by search.js
     *
     * @return
     */
    @RequestMapping(value = "/api/search/card-mapping", method = RequestMethod.GET)
    public Map<String, Integer> findCardsIds(@RequestParam("from") List<String> cards) {
        return cardRepository.findCardsIds(cards);
    }

    /**
     * Given a list of label list value, return a map value -> card label id -> card label list value id
     *
     * @param labelValues
     * @return
     */
    @RequestMapping(value = "/api/search/label-list-value-mapping", method = RequestMethod.GET)
    public Map<String, Map<Integer, Integer>> findLabelListValueMapping(
        @RequestParam("from") List<String> labelValues) {
        return cardLabelRepository.findLabelListValueMapping(labelValues);
    }

    @RequestMapping(value = "/api/search/card", method = RequestMethod.GET)
    public SearchResults search(@RequestParam("q") String queryAsJson,
        @RequestParam(value = "projectName", required = false) String projectName,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        UserWithPermission userWithPermission) {
        List<SearchFilter> searchFilters = Json.GSON.fromJson(queryAsJson, LIST_OF_SEARCH_FILTERS);
        Integer projectId = toProjectId(projectName);
        return searchService.find(searchFilters, projectId, null, userWithPermission, page);
    }

    @RequestMapping(value = "/api/search/user", method = RequestMethod.GET)
    public List<User> findUsers(@RequestParam("term") String term,
        @RequestParam(value = "projectName", required = false) String projectName,
        UserWithPermission userWithPermission) {

        // TODO: ugly code
        boolean useProjectSearch = StringUtils.isNotBlank(projectName);
        if (useProjectSearch && !hasReadAccessToProject(userWithPermission, projectName)) {
            return Collections.emptyList();
        }

        String[] splitted = StringUtils.split(term, ':');

        if (term != null && splitted.length > 0) {
            String value = splitted.length == 1 ? splitted[0] : splitted[1];
            return useProjectSearch ?
                userRepository.findUsers(value, projectService.findIdByShortName(projectName), Permission.READ) :
                userRepository.findUsers(value);
        } else {
            return Collections.emptyList();
        }
    }

    private static boolean hasReadAccessToProject(UserWithPermission userWithPermission, String projectName) {
        return (userWithPermission.getBasePermissions().containsKey(Permission.READ) || userWithPermission
            .projectsWithPermission(Permission.READ).contains(projectName));
    }

    @RequestMapping(value = "/api/search/autocomplete-card", method = RequestMethod.GET)
    public List<CardFull> searchCardForAutocomplete(@RequestParam("term") String term,
        UserWithPermission userWithPermission) {

        boolean hasGlobalAccess = userWithPermission.getBasePermissions().containsKey(Permission.READ);
        Set<Integer> projectReadAccess = userWithPermission.projectsIdWithPermission(Permission.READ);
        Validate.isTrue(hasGlobalAccess || !projectReadAccess.isEmpty());

        return cardRepository.findCardBy(term, hasGlobalAccess ? null : projectReadAccess);
    }

    @RequestMapping(value = "/api/search/milestone", method = RequestMethod.GET)
    public List<String> findMilestones(@RequestParam("term") String term, @RequestParam(value = "projectName",
        required = false) String projectName, UserWithPermission userWithPermission) {
        Integer projectId = toProjectId(projectName);
        return cardLabelRepository
            .findListValuesBy(LabelDomain.SYSTEM, SYSTEM_LABEL_MILESTONE, term, projectId, userWithPermission);
    }

    @RequestMapping(value = "/api/search/labels", method = RequestMethod.GET)
    public List<CardLabel> findLabel(@RequestParam("term") String term,
        @RequestParam(value = "projectName", required = false) String projectName,
        UserWithPermission userWithPermission) {
        Integer projectId = toProjectId(projectName);
        return cardLabelRepository.findUserLabelNameBy(term, projectId, userWithPermission);
    }

    @RequestMapping(value = "/api/search/label-values", method = RequestMethod.GET)
    public List<String> findLabelValues(@RequestParam("term") String term,
        @RequestParam(value = "projectName", required = false) String projectName,
        UserWithPermission userWithPermission) {
        Integer projectId = toProjectId(projectName);
        List<String> res = new ArrayList<>();
        for (CardLabel label : cardLabelRepository.findUserLabelNameBy(term, projectId, userWithPermission)) {
            if (label.getType().equals(CardLabel.LabelType.LIST)) {
                for (LabelListValue llv : cardLabelRepository.findListValuesByLabelId(label.getId())) {
                    res.add(llv.getValue());
                }
            }
        }
        return res;
    }

    private Integer toProjectId(String projectName) {
        return projectName == null ? null : projectService.findIdByShortName(projectName);
    }
}
