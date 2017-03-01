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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.lavagna.common.Json;
import io.lavagna.model.ApiHook;
import io.lavagna.model.ApiHookNameAndVersion;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.Card;
import io.lavagna.model.CardData;
import io.lavagna.model.CardDataHistory;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.User;
import io.lavagna.query.ApiHookQuery;
import io.lavagna.service.EventEmitter.LavagnaEvent;

@Service
public class ApiHooksService {

    private static final Logger LOG = LogManager.getLogger();

    private final Compilable engine;
    private final Executor executor;
    private final Map<String, Triple<ApiHook, Map<Object, Object>, CompiledScript>> compiledScriptCache = new ConcurrentHashMap<>();
    private final ProjectService projectService;
    private final CardService cardService;
    private final ApiHookQuery apiHookQuery;

    public ApiHooksService(ProjectService projectService, CardService cardService, ApiHookQuery apiHookQuery) {
        this.projectService = projectService;
        this.cardService = cardService;
        this.apiHookQuery = apiHookQuery;
        engine = (Compilable) new ScriptEngineManager().getEngineByName("javascript");
        executor = Executors.newFixedThreadPool(4);
    }

    private static void executeScript(String name, CompiledScript script, Map<String, Object> scope) {
        try {
            ScriptContext newContext = new SimpleScriptContext();
            Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
            engineScope.putAll(scope);
            engineScope.put("log", LOG);
            script.eval(newContext);
        } catch (ScriptException ex) {
            LOG.warn("Error while executing script " + name, ex);
        }
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
            this.user = new io.lavagna.model.apihook.User(user.getId(), user.getProvider(), user.getUsername(), user.getEmail(), user.getDisplayName());
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
                        @SuppressWarnings("unchecked")
                        Map<Object, Object> configuration = apiHook.getConfiguration() != null ? Json.GSON.fromJson(apiHook.getConfiguration(), Map.class) : Collections.emptyMap();
                        apiHooksService.compiledScriptCache.put(apiHook.getName(), Triple.of(apiHook, configuration, cs));
                    } catch (ScriptException ex) {
                        LOG.warn("Error while compiling script " + apiHook.getName());
                    }
                }
            }

            for (Triple<ApiHook, Map<Object, Object>, CompiledScript> val : apiHooksService.compiledScriptCache.values()) {

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
    public void createOrUpdateApiHook(String name, String code, Map<String, Object> properties, List<String> projects, Map<String, Object> metadata) {
        if (apiHookQuery.findByNames(Collections.singletonList(name)).isEmpty()) {
            apiHookQuery.insert(name, code, Json.GSON.toJson(properties), true, ApiHook.Type.EVENT_EMITTER_HOOK, Json.GSON.toJson(projects), Json.GSON.toJson(metadata));
        } else {
            apiHookQuery.update(name, code, Json.GSON.toJson(properties), true, ApiHook.Type.EVENT_EMITTER_HOOK, Json.GSON.toJson(projects));
        }
    }

    private Map<String, Object> getBaseDataFor(int cardId) {
        Map<String, Object> res = new HashMap<>();
        CardFull cf = cardService.findFullBy(cardId);
        res.put("card", new io.lavagna.model.apihook.Card(cf.getBoardShortName(), cf.getSequence(), cf.getProjectShortName()));
        res.put("board", cf.getBoardShortName());
        return res;
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
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void updateColumn(String boardShortName, BoardColumn oldColumn, BoardColumn updatedColumn, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void createdCard(String boardShortName, Card card, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void updatedCard(String boardShortName, Card beforeUpdate, Card newCard, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByBoardShortname(boardShortName);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>singletonMap("board", boardShortName)));
    }

    public void updateCardDescription(int cardId, CardDataHistory previousDescription, CardDataHistory newDescription, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);

        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void createdComment(int cardId, CardData comment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void updatedComment(int cardId, CardData previousComment, String newComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void deletedComment(int cardId, CardData deletedComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void undeletedComment(int cardId, CardData undeletedComment, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void uploadedFile(int cardId, List<String> fileNames, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void deletedFile(int cardId, String fileName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void undoDeletedFile(int cardId, String fileName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void removedLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByLabelId(labelId);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

    public void addLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByLabelId(labelId);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

    public void updateLabelValueToCards(List<CardFull> updated, int labelId, LabelValue labelValue, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByLabelId(labelId);
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

    public void createActionList(int cardId, String name, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void deleteActionList(int cardId, String name, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void updatedNameActionList(int cardId, String oldName, String newName, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void undeletedActionList(int cardId, String name, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void createActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void deletedActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void toggledActionItem(int cardId, String actionItemListName, String actionItem, boolean toggle, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void updatedActionItem(int cardId, String actionItemListName, String oldActionItem, String newActionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void undoDeleteActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void movedActionItem(int cardId, String fromActionItemListName, String toActionItemListName,
                                String actionItem, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardId);
        executor.execute(new EventToRun(this, event, projectShortName, user, getBaseDataFor(cardId)));
    }

    public void moveCards(BoardColumn from, BoardColumn to, Collection<Integer> cardIds, User user, LavagnaEvent event) {
        String projectShortName = projectService.findRelatedProjectShortNameByCardId(cardIds.iterator().next());
        executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
    }

}
