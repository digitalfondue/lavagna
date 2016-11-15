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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import io.lavagna.common.Json;
import io.lavagna.model.ApiHook;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.Card;
import io.lavagna.model.CardData;
import io.lavagna.model.CardDataHistory;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.User;
import io.lavagna.service.EventEmitter.LavagnaEvent;

@Service
public class ApiHooksService {
	
	private static final Logger LOG = LogManager.getLogger();
	
	private final ScriptEngine engine;
	private final Executor executor;
	private final Map<String, Pair<String, CompiledScript>> compiledScriptCache = new ConcurrentHashMap<>();
 
	public ApiHooksService() {
		engine =  new ScriptEngineManager().getEngineByName("javascript");
		executor = Executors.newFixedThreadPool(4);
	}
	
	private void executeScript(String name, String script, Map<String, Object> scope) {
		try {
			String key = DigestUtils.sha1Hex(script);
			Pair<String, CompiledScript> compiled = compiledScriptCache.get(name);
			if(compiled == null || (compiled != null && !compiled.getKey().equals(key))) {
				Compilable compEngine = (Compilable)engine;
                CompiledScript cs = compEngine.compile(script);
                compiled = Pair.of(key, cs);
                compiledScriptCache.put(name, compiled);
			}
			
			ScriptContext newContext = new SimpleScriptContext();
			Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScope.putAll(scope);
			compiled.getRight().eval(newContext);
		} catch (ScriptException ex) {
			LOG.warn("Error while executing script " + name, ex);
		}
	}
	
	private List<ApiHook> getScripts() {
		return Collections.emptyList();
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
			for(ApiHook hook: apiHooksService.getScripts()) {
				Map<String, Object> scope = new HashMap<>(env);
				
				scope.put("eventName", eventName.name());
				scope.put("project", projectName);
				scope.put("user", user);
				
				if(hook.getConfiguration() != null) {
					scope.put("configuration", Json.GSON.fromJson(hook.getConfiguration(), Map.class));
				} else {
					scope.put("configuration", Collections.emptyMap());
				}
				
				scope.put("data", env);
				
				apiHooksService.executeScript(hook.getName(), hook.getScript(), scope);
			}
		}
	}

	public void createdProject(String projectShortName, User user, LavagnaEvent event) {
		executor.execute(new EventToRun(this, event, projectShortName, user, Collections.<String, Object>emptyMap()));
	}

	public void updatedProject(String projectShortName, User user, LavagnaEvent updateProject) {
		//FIXME run scripts
	}

	public void createdBoard(String boardShortName, User user, LavagnaEvent createBoard) {
		//FIXME run scripts
	}

	public void updatedBoard(String boardShortName, User user, LavagnaEvent updateBoard) {
		//FIXME run scripts
	}

	public void createdColumn(String boardShortName, String columnName, User user, LavagnaEvent createColumn) {
		//FIXME run scripts
	}

	public void updateColumn(String boardShortName, BoardColumn oldColumn, BoardColumn updatedColumn, User user, LavagnaEvent updateColumn) {
		//FIXME run scripts
	}

	public void createdCard(String boardShortName, Card card, User user, LavagnaEvent createCard) {
		//FIXME run scripts
	}

	public void updatedCard(String boardShortName, Card beforeUpdate, Card newCard, User user, LavagnaEvent updateCard) {
		//FIXME run scripts
	}

	public void updateCardDescription(int cardId, CardDataHistory previousDescription, CardDataHistory newDescription, User user, LavagnaEvent updateDescription) {
		//FIXME run scripts
	}

	public void createdComment(int cardId, CardData comment, User user, LavagnaEvent createComment) {
		//FIXME run scripts
	}

	public void updatedComment(int cardId, CardData previousComment, String newComment, User user, LavagnaEvent updateComment) {
		//FIXME run scripts
	}

	public void deletedComment(int cardId, CardData deletedComment, User user, LavagnaEvent deleteComment) {
		//FIXME run scripts
	}

	public void undeletedComment(int cardId, CardData undeletedComment, User user, LavagnaEvent undoDeleteComment) {
		//FIXME run scripts
	}

	public void uploadedFile(int cardId, List<String> fileNames, User user, LavagnaEvent createFile) {
		//FIXME run scripts		
	}

	public void deletedFile(int cardId, String fileName, User user, LavagnaEvent deleteFile) {
		//FIXME run scripts
	}

	public void undoDeletedFile(int cardId, String fileName, User user, LavagnaEvent undoDeleteFile) {
		//FIXME run scripts
	}

	public void removedLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent removeLabelValue) {
		//FIXME run scripts
	}

	public void addLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user, LavagnaEvent addLabelValueToCard) {
		//FIXME run scripts
	}

	public void updateLabelValueToCards(List<CardFull> updated, int labelId, LabelValue labelValue, User user, LavagnaEvent updateLabelValue) {
		//FIXME run scripts
	}

	public void createActionList(int cardId, String name, User user, LavagnaEvent createActionList) {
		//FIXME run scripts	
	}

	public void deleteActionList(int cardId, String name, User user, LavagnaEvent deleteActionList) {
		//FIXME run scripts
	}

	public void updatedNameActionList(int cardId, String oldName, String newName, User user, LavagnaEvent updateActionList) {
		//FIXME run scripts		
	}
	
	public void undeletedActionList(int cardId, String name, User user, LavagnaEvent undoDeleteActionList) {
		//FIXME run scripts		
	}

	public void createActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent createActionItem) {
		//FIXME run scripts
	}

	public void deletedActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent deleteActionItem) {
		//FIXME run scripts
	}

	public void toggledActionItem(int cardId, String actionItemListName, String actionItem, boolean toggle, User user, LavagnaEvent toggleActionItem) {
		//FIXME run scripts
	}

	public void updatedActionItem(int cardId, String actionItemListName, String oldActionItem, String newActionItem, User user, LavagnaEvent updateActionItem) {
		//FIXME run scripts		
	}

	public void undoDeleteActionItem(int cardId, String actionItemListName, String actionItem, User user, LavagnaEvent undoDeleteActionItem) {
		//FIXME run scripts
	}

	public void movedActionItem(int cardId, String fromActionItemListName, String toActionItemListName,
			String actionItem, User user, LavagnaEvent moveActionItem) {
		//FIXME run scripts
	}

	public void moveCards(BoardColumn from, BoardColumn to, Collection<Integer> cardIds, User user, LavagnaEvent updateCardPosition) {
		//FIXME run scripts		
	}
}
