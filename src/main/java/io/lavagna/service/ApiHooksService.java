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

import io.lavagna.common.Json;
import io.lavagna.model.*;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.apihook.From;
import io.lavagna.model.apihook.Label;
import io.lavagna.query.ApiHookQuery;
import io.lavagna.service.EventEmitter.LavagnaEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class ApiHooksService {

    private static final Logger LOG = LogManager.getLogger();

    private final Compilable engine;
    private final Executor executor;
    private final Map<String, Triple<ApiHook, Map<String, String>, CompiledScript>> compiledScriptCache = new ConcurrentHashMap<>();
    private final Map<String, Triple<ApiHook, Map<String, String>, CompiledScript>> compiledScriptCacheWebHook = new ConcurrentHashMap<>();
    private final ProjectService projectService;
    private final CardService cardService;
    private final ApiHookQuery apiHookQuery;
    private final LabelService labelService;
    private final UserService userService;
    private final ConfigurationRepository configurationRepository;

    public ApiHooksService(ProjectService projectService,
                           CardService cardService,
                           ApiHookQuery apiHookQuery,
                           LabelService labelService,
                           UserService userService,
                           ConfigurationRepository configurationRepository) {
        this.projectService = projectService;
        this.cardService = cardService;
        this.apiHookQuery = apiHookQuery;
        this.labelService = labelService;
        this.userService = userService;
        this.configurationRepository = configurationRepository;
        engine = (Compilable) new ScriptEngineManager().getEngineByName("javascript");
        executor = Executors.newFixedThreadPool(4);
    }

    private static boolean executeScript(String name, CompiledScript script, Map<String, Object> scope) {
        try {
            ScriptContext newContext = new SimpleScriptContext();
            Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
            engineScope.putAll(scope);
            engineScope.put("log", LOG);
            engineScope.put("GSON", Json.GSON);
            engineScope.put("restTemplate", new RestTemplate());
            script.eval(newContext);
            return true;
        } catch (ScriptException ex) {
            LOG.warn("Error while executing script " + name, ex);
            return false;
        }
    }

    public ApiHook findByName(String name) {
        return apiHookQuery.findByNames(Collections.singletonList(name)).get(0);
    }

    private static class EventToRun implements Runnable {

        private final ApiHooksService apiHooksService;
        private final LavagnaEvent eventName;
        private final String projectName;
        private final Map<String, Object> env;
        private final io.lavagna.model.apihook.User user;

        EventToRun(ApiHooksService apiHooksService, LavagnaEvent eventName, String projectName, User user, Map<String, Object> env) {
            this.apiHooksService = apiHooksService;
            this.eventName = eventName;
            this.projectName = projectName;
            this.env = env;
            this.user = From.from(user);
        }

        @Override
        public void run() {

            List<ApiHookNameAndVersion> nameAndVersions = apiHooksService.apiHookQuery.findAllEnabled(ApiHook.Type.EVENT_EMITTER_HOOK);
            List<String> names = new ArrayList<>(nameAndVersions.size());
            for (ApiHookNameAndVersion nv : nameAndVersions) {
                names.add(nv.getName());
            }

            //remove all disabled scripts
            apiHooksService.compiledScriptCache.keySet().retainAll(names);

            List<String> toAddOrUpdate = new ArrayList<>(0);
            for (ApiHookNameAndVersion hook : nameAndVersions) {
                if (!apiHooksService.compiledScriptCache.containsKey(hook.getName()) || apiHooksService.compiledScriptCache.get(hook.getName()).getLeft().getVersion() < hook.getVersion()) {
                    toAddOrUpdate.add(hook.getName());
                }
            }

            if (!toAddOrUpdate.isEmpty()) {
                for (ApiHook apiHook : apiHooksService.apiHookQuery.findByNames(toAddOrUpdate)) {
                    try {
                        CompiledScript cs = apiHooksService.engine.compile(apiHook.getScript());
                        Map<String, String> configuration = apiHook.getConfiguration() != null ? apiHook.getConfiguration() : Collections.<String, String>emptyMap();
                        apiHooksService.compiledScriptCache.put(apiHook.getName(), Triple.of(apiHook, configuration, cs));
                    } catch (ScriptException ex) {
                        LOG.warn("Error while compiling script " + apiHook.getName(), ex);
                    }
                }
            }

            for (Triple<ApiHook, Map<String, String>, CompiledScript> val : apiHooksService.compiledScriptCache.values()) {
                List<String> projectsFilter = val.getLeft().getProjects();
                if(projectsFilter != null && !projectsFilter.contains(projectName)) {
                    continue;
                }

                Map<String, Object> scope = new HashMap<>(env);

                scope.put("eventName", eventName.name());
                scope.put("project", projectName);
                scope.put("user", user);
                scope.put("data", env);
                scope.put("configuration", val.getMiddle());

                executeScript(val.getLeft().getName(), val.getRight(), scope);
            }

        }
    }

    @Transactional(readOnly = true)
    public List<ApiHook> findAllPlugins() {
        return apiHookQuery.findAll();
    }

    @Transactional
    public void deleteHook(String name) {
        apiHookQuery.delete(name);
    }

    @Transactional
    public void enable(String name, boolean enabled) {
        apiHookQuery.enable(name, enabled);
    }

    @Transactional
    public void createApiHook(String name, String code, Map<String, String> properties, List<String> projects, Map<String, Object> metadata) {
        String propAsJson = properties == null ? null : Json.GSON.toJson(properties, Map.class);
        String projectsAsJson = projects == null ? null : Json.GSON.toJson(projects, List.class);
        String metadataAsJson = metadata == null ? null : Json.GSON.toJson(metadata, Map.class);
        apiHookQuery.insert(name, code, propAsJson, true, ApiHook.Type.EVENT_EMITTER_HOOK, projectsAsJson, metadataAsJson);
    }

    @Transactional
    public void updateApiHook(String name, String code, Map<String, String> properties, List<String> projects, Map<String, Object> metadata) {
        String propAsJson = properties == null ? null : Json.GSON.toJson(properties, Map.class);
        String projectsAsJson = projects == null ? null : Json.GSON.toJson(projects, List.class);
        String metadataAsJson = metadata == null ? null : Json.GSON.toJson(metadata, Map.class);
        apiHookQuery.update(name, code, propAsJson, apiHookQuery.findStatusByName(name), ApiHook.Type.EVENT_EMITTER_HOOK, projectsAsJson, metadataAsJson);
    }

    private Map<String, Object> getBaseDataFor(int cardId) {
        CardFull cf = cardService.findFullBy(cardId);
        String baseUrl = configurationRepository.getValue(Key.BASE_APPLICATION_URL);
        return getBaseDataFor(cf, baseUrl);
    }

    private static Map<String, Object> getBaseDataFor(CardFull cf, String baseUrl) {
        Map<String, Object> res = new HashMap<>();
        res.put("card", From.from(cf, baseUrl));
        res.put("board", cf.getBoardShortName());
        return res;
    }

    private Map<String, Object> updateForObj(int cardId, Object previous, Object updated) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("previous", previous);
        payload.put("updated", updated);
        payload.putAll(getBaseDataFor(cardId));
        return payload;
    }

    private Map<String, Object> updateFor(int cardId, String previous, String updated) {
        return updateForObj(cardId, previous, updated);
    }

    private Map<String, Object> updateFor(int cardId, CardData previous, String updated) {
        return updateForObj(cardId, From.from(previous), updated);
    }

    private Map<String, Object> updateFor(int cardId, CardType type, CardDataHistory previous, CardDataHistory updated) {
        return updateForObj(cardId, From.from(type, previous), From.from(type, updated));
    }

    private Map<String, Object> payloadForObj(int cardId, String name, Object object) {
        Map<String, Object> r = getBaseDataFor(cardId);
        r.put(name, object);
        return r;
    }

    private Map<String, Object> payloadFor(int cardId, String name, CardData cardData) {
        return payloadForObj(cardId, name, From.from(cardData));
    }

    private Map<String, Object> payloadFor(int cardId, String name, String value) {
        return payloadForObj(cardId, name, value);
    }

    private Map<String, Object> payloadFor(int cardId, String name, Collection<?> value) {
        return payloadForObj(cardId, name, value);
    }

    public void handleHook(String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleHook(null, name, request, response);
    }

    public void handleHook(String projectShortName, String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ApiHookNameAndVersion> apiHooks = apiHookQuery.findEnabledByNameAndType(name, ApiHook.Type.WEB_HOOK);
        if(apiHooks.size() == 1) {
            ApiHook apiHook = apiHookQuery.findByNames(Collections.singletonList(name)).get(0);

            // check project access if necessary
            if (projectShortName != null && (apiHook.getProjects() == null || !apiHook.getProjects().contains(projectShortName))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String metadataKey = (String) apiHook.getMetadata().get("key");
            String paramKey = request.getParameter("key");

            // check access
            if (!(metadataKey != null && paramKey !=null && metadataKey.equals(paramKey))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if(!compiledScriptCacheWebHook.containsKey(name) || (compiledScriptCacheWebHook.containsKey(name) && compiledScriptCacheWebHook.get(name).getLeft().getVersion() < apiHook.getVersion())) {
                try {
                    CompiledScript script = engine.compile(apiHook.getScript());
                    Map<String, String> configuration = apiHook.getConfiguration() != null ? apiHook.getConfiguration() : Collections.<String, String>emptyMap();
                    compiledScriptCacheWebHook.put(apiHook.getName(), Triple.of(apiHook, configuration, script));
                } catch (ScriptException se) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            }

            Triple<ApiHook, Map<String, String>, CompiledScript> compiledScript = compiledScriptCacheWebHook.get(name);

            Map<String, Object> scope = new HashMap<>();

            scope.put("project", projectShortName);
            scope.put("configuration", compiledScript.getMiddle());
            scope.put("request", request);
            scope.put("response", response);

            boolean res = executeScript(name, compiledScript.getRight(), new HashMap<String, Object>());
            if(!res) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void createdProject(String projectShortName, User user, LavagnaEvent event) {
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

    public void updatedProject(String projectShortName, User user, LavagnaEvent event) {
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

    public void createdBoard(String boardShortName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void updatedBoard(String boardShortName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void createdColumn(String boardShortName, String columnName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        Map<String, Object> payload = new HashMap<>();
        payload.put("board", boardShortName);
        payload.put("columnName", columnName);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void updateColumn(String boardShortName, BoardColumn oldColumn, BoardColumn updatedColumn, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        Map<String, Object> payload = new HashMap<>();
        payload.put("board", boardShortName);
        payload.put("previous", From.from(oldColumn));
        payload.put("updated", From.from(updatedColumn));
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void createdCard(String boardShortName, Card card, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(card.getId())));
    }

    public void updatedCardName(String boardShortName, Card beforeUpdate, Card newCard, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, updateForObj(beforeUpdate.getId(), beforeUpdate.getName(), newCard.getName())));
    }

    public void updateCardDescription(int cardId, CardDataHistory previousDescription, CardDataHistory newDescription, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, updateFor(cardId, CardType.DESCRIPTION, previousDescription, newDescription)));
    }

    public void createdComment(int cardId, CardData comment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, payloadFor(cardId, "comment", comment)));
    }

    public void updatedComment(int cardId, CardData previousComment, String newComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, updateFor(cardId, previousComment, newComment)));
    }

    public void deletedComment(int cardId, CardData deletedComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, payloadFor(cardId, "comment", deletedComment)));
    }

    public void undeletedComment(int cardId, CardData undeletedComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, payloadFor(cardId, "comment", undeletedComment)));
    }

    public void uploadedFile(int cardId, List<String> fileNames, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "files", fileNames);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void deletedFile(int cardId, String fileName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "file", fileName);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void undoDeletedFile(int cardId, String fileName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "file", fileName);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void removedLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        handleLabelValue(affectedCards, labelId, labelValue, user, event);
    }

    public void addLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        handleLabelValue(affectedCards, labelId, labelValue, user, event);
    }

    public void updateLabelValueToCards(List<CardFull> updated, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        handleLabelValue(updated, labelId, labelValue, user, event);
    }

    private void handleLabelValue(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        if (affectedCards.isEmpty()) {
            return;
        }

        String projectShortName = projectService.findRelatedProjectShortNameByLabelId(labelId);
        Map<String, Object> payload = new HashMap<>();
        Label label = from(labelService.findLabelById(labelId), labelValue);
        payload.put("affectedCards", toList(affectedCards));
        payload.put("label", label);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    private Label from(CardLabel cardLabel, LabelValue labelValue) {
        Object value = null;
        switch (cardLabel.getType()) {
            case CARD:
                value = From.from(cardService.findFullBy(labelValue.getValueCard()), baseUrl());
                break;
            case INT:
                value = labelValue.getValueInt();
                break;
            case LIST:
                value = labelService.findLabelListValueById(labelValue.getValueList()).getValue();
                break;
            case STRING:
                value = labelValue.getValueString();
                break;
            case TIMESTAMP:
                value = Json.formatDate(labelValue.getValueTimestamp());
                break;
            case USER:
                value = From.from(userService.findUserWithPermission(labelValue.getValueUser()));
                break;
        }
        return new Label(cardLabel.getType().toString(), cardLabel.getDomain().toString(), cardLabel.getName(),  value);
    }

    private String baseUrl() {
        return configurationRepository.getValue(Key.BASE_APPLICATION_URL);
    }

    private List<io.lavagna.model.apihook.Card> toList(List<CardFull> cards) {
        List<io.lavagna.model.apihook.Card> res = new ArrayList<>(cards.size());
        String baseUrl = baseUrl();
        for(CardFull cf : cards) {
            res.add(From.from(cf, baseUrl));
        }
        return res;
    }

    private void handleActionList(int cardId, String name, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "actionList", name);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void createActionList(int cardId, String name, User user, LavagnaEvent event) {
        handleActionList(cardId, name, user, event);
    }

    public void deleteActionList(int cardId, String name, User user, LavagnaEvent event) {
        handleActionList(cardId, name, user, event);
    }

    public void updatedNameActionList(int cardId, String oldName, String newName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, updateFor(cardId, oldName, newName)));
    }

    public void undeletedActionList(int cardId, String name, User user, LavagnaEvent event) {
        handleActionList(cardId, name, user, event);
    }

    public void createActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        handleActionItem(cardId, actionItemListName, actionItem, user, event);
    }

    public void deletedActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        handleActionItem(cardId, actionItemListName, actionItem, user, event);
    }

    public void toggledActionItem(int cardId, String actionItemListName, String actionItem, boolean toggle, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "actionList", actionItemListName);
        payload.put("actionItem", actionItem);
        payload.put("toggled", toggle);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void updatedActionItem(int cardId, String actionItemListName, String oldActionItem, String newActionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = updateFor(cardId, oldActionItem, newActionItem);
        payload.put("actionList", actionItemListName);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void undoDeleteActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        handleActionItem(cardId, actionItemListName, actionItem, user, event);
    }

    private void handleActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "actionList", actionItemListName);
        payload.put("actionItem", actionItem);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void movedActionItem(int cardId, String fromActionItemListName, String toActionItemListName,
                                String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        Map<String, Object> payload = payloadFor(cardId, "actionItem", actionItem);
        payload.put("from", fromActionItemListName);
        payload.put("to", toActionItemListName);
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

    public void moveCards(BoardColumn from, BoardColumn to, Collection<Integer> cardIds, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardIds.iterator().next());
        Map<String, Object> payload = new HashMap<>();
        payload.put("affectedCards", toList(cardService.findFullBy(cardIds)));
        payload.put("from", From.from(from));
        payload.put("to", From.from(to));
        executor.execute(new EventToRun(this, event, projectShortName, user, payload));
    }

}
