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

import com.google.gson.reflect.TypeToken;
import io.lavagna.common.Read;
import io.lavagna.model.*;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.query.StatisticsQuery;
import io.lavagna.service.PermissionService.RoleAndPermissionsWithUsers;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

import static io.lavagna.common.Read.readMatchingObjects;
import static io.lavagna.common.Read.readObject;
import static java.util.Collections.singletonList;

@Component
class LavagnaImporter {

	private final ConfigurationRepository configurationRepository;
	private final UserRepository userRepository;
	private final PermissionService permissionService;
	private final ProjectService projectService;
	private final BoardRepository boardRepository;
	private final BoardColumnRepository boardColumnRepository;
	private final CardLabelRepository cardLabelRepository;
	private final CardRepository cardRepository;
	private final CardDataRepository cardDataRepository;
	private final StatisticsQuery statisticsQuery;

	private final ImportEvent importEvent;


	public LavagnaImporter(ConfigurationRepository configurationRepository, UserRepository userRepository,
			PermissionService permissionService, ProjectService projectService, BoardRepository boardRepository,
			BoardColumnRepository boardColumnRepository, CardLabelRepository cardLabelRepository,
			CardDataRepository cardDataRepository, CardRepository cardRepository, ImportEvent importEvent,
			StatisticsQuery statisticsQuery) {
		this.configurationRepository = configurationRepository;
		this.userRepository = userRepository;
		this.permissionService = permissionService;
		this.projectService = projectService;
		this.boardRepository = boardRepository;
		this.boardColumnRepository = boardColumnRepository;
		this.cardLabelRepository = cardLabelRepository;
		this.importEvent = importEvent;
		this.cardRepository = cardRepository;
		this.cardDataRepository = cardDataRepository;
		this.statisticsQuery = statisticsQuery;
	}

	public void importData(boolean overrideConfiguration, Path tempFile) {

		importConfiguration(overrideConfiguration, tempFile);

		importMissingUsers(tempFile);
		importBasePermissions(tempFile);

		ImportContext context = new ImportContext();

		importProjects(tempFile, context);

		importBoards(tempFile, context);

		//

		int eventPages = readObject("events-page-count.json", tempFile, new TypeToken<Integer>() {
		});

		for (int i = 0; i < eventPages; i++) {
			processEvents(readObject("events-" + i + ".json", tempFile, new TypeToken<List<EventFull>>() {
			}), context, tempFile);
		}

		orderAll(tempFile, context);
	}

	private void orderAll(Path tempFile, ImportContext context) {

		for (String shortName : context.getImportedBoard()) {
			orderCards(readObject("boards/" + shortName + "/cards.json", tempFile, new TypeToken<List<CardFull>>() {
			}));
		}

		for (CardDataIdAndOrder idOrder : readObject("card-data-types-order.json", tempFile,
				new TypeToken<List<CardDataIdAndOrder>>() {
				})) {

			int oldId = idOrder.getFirst();
			int order = idOrder.getSecond();

			if (context.getActionItemId().containsKey(oldId)) {
				cardDataRepository.updateOrderById(context.getActionItemId().get(oldId), order);
			} else if (context.getActionListId().containsKey(oldId)) {
				cardDataRepository.updateOrderById(context.getActionListId().get(oldId), order);
			}
		}
	}

	private void orderCards(List<CardFull> cards) {
		for (CardFull cf : cards) {
			int cardId = cardRepository.findCardIdByBoardNameAndSeq(cf.getBoardShortName(), cf.getSequence());
			cardRepository.updateCardOrder(cardId, cf.getOrder());
		}
	}

	private void importConfiguration(boolean overrideConfiguration, Path tempFile) {
		if (overrideConfiguration) {
			configurationRepository.updateOrCreate(readObject("config.json", tempFile,
					new TypeToken<List<ConfigurationKeyValue>>() {
					}));
		}
	}

	private void importBoards(Path tempFile, ImportContext context) {
		for (Pair<String, BoardInfo> p : readMatchingObjects("boards/[^/]+\\.json", tempFile,
				new TypeToken<Pair<String, BoardInfo>>() {
				})) {
			String projectShortName = p.getFirst();
			BoardInfo boardInfo = p.getSecond();
			if (context.getImportedProject().contains(projectShortName)) {
				Project project = projectService.findByShortName(projectShortName);
				if (!boardRepository.existsWithShortName(boardInfo.getShortName())) {
					importMissingBoard(project, boardInfo, tempFile, context);
					context.getImportedBoard().add(boardInfo.getShortName());
				}
			}
		}
	}

	private void importProjects(Path tempFile, ImportContext context) {
		for (Project project : readMatchingObjects("projects/[^/]+\\.json", tempFile, new TypeToken<Project>() {
		})) {
			if (importProject(project, tempFile)) {
				context.getImportedProject().add(project.getShortName());
			}
		}
	}

	private void processEvents(List<EventFull> events, ImportContext idMapping, Path tempFile) {
		for (EventFull e : events) {
			processEvent(idMapping, e, tempFile);
		}
	}

	private void processEvent(ImportContext context, EventFull e, Path tempFile) {

		if (!context.getImportedBoard().contains(e.getBoardShortName())) {
			return;
		}

		importEvent.processEvent(e, context, tempFile);
	}

	private void importBasePermissions(Path tempFile) {

		// add missing base permissions
		Map<String, RoleAndPermissionsWithUsers> permissions = readObject("permissions.json", tempFile,
				new TypeToken<Map<String, RoleAndPermissionsWithUsers>>() {
				});

		permissionService.createMissingRolesWithPermissions(from(permissions));

		// add users to roles
		for (RoleAndPermissionsWithUsers v : permissions.values()) {
			List<UserIdentifier> assignedUsers = v.getAssignedUsers();
			Role role = new Role(v.getName());
			permissionService.assignRoleToUsers(role, removeUserWithRole(role, assignedUsers));
		}
		//
	}

	private Set<Integer> removeUserWithRole(Role role, List<UserIdentifier> users) {
		Set<Integer> userIds = new HashSet<>();
		for (UserIdentifier ui : users) {
			User u = userRepository.findUserByName(ui.getProvider(), ui.getUsername());
			if (!permissionService.findBaseRoleAndPermissionByUserId(u.getId()).containsKey(role.getName())) {
				userIds.add(u.getId());
			}
		}
		return userIds;
	}

	private Map<RoleAndPermission, Set<Permission>> from(Map<String, RoleAndPermissionsWithUsers> from) {
		Map<RoleAndPermission, Set<Permission>> res = new HashMap<>();
		for (Entry<String, RoleAndPermissionsWithUsers> kv : from.entrySet()) {

			RoleAndPermissionsWithUsers rpu = kv.getValue();
			RoleAndPermission rap = new RoleAndPermission(rpu.getName(), rpu.isRemovable(), rpu.isHidden(),
					rpu.isReadOnly(), null);
			if (!res.containsKey(rap)) {
				res.put(rap, EnumSet.noneOf(Permission.class));
			}
			for (RoleAndPermission rp : kv.getValue().getRoleAndPermissions()) {
				//we can have null permission enum value if the enum is no more present
				if(rp.getPermission() != null) {
					res.get(rap).add(rp.getPermission());
				}
			}
		}
		return res;
	}

	/**
	 * Import only the users that are not present in the system.
	 */
	private void importMissingUsers(Path tempFile) {
		List<User> users = readObject("users.json", tempFile, new TypeToken<List<User>>() {
		});

		SortedSet<User> usersToImport = new TreeSet<>(new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return new CompareToBuilder().append(o1.getProvider(), o2.getProvider())
						.append(o1.getUsername(), o2.getUsername()).toComparison();
			}
		});

		usersToImport.addAll(users);
		usersToImport.removeAll(userRepository.findAll());
		userRepository.createUsers(usersToImport);

        if(Read.hasMatchingObject("users-password-hash.json", tempFile)) {
            Map<String, String> usersWithPassword = Read.readObject("users-password-hash.json", tempFile, new TypeToken<Map<String, String>>() {
            });

            for(User u : usersToImport) {
                if("password".equals(u.getProvider()) && usersWithPassword.containsKey(u.getUsername())) {
                    userRepository.setUserPassword(userRepository.findUserByName("password", u.getUsername()).getId(), usersWithPassword.get(u.getUsername()));
                }
            }
        }

	}

	private boolean importProject(Project project, Path tempFile) {
		boolean created = projectService.createMissing(singletonList(project)).getRight().isEmpty();
		if (created) {
			Project createdProject = projectService.findByShortName(project.getShortName());

			String projectNameDir = "projects/" + project.getShortName();

			importColumnDefinitionColor(tempFile, createdProject, projectNameDir);
			importLabels(tempFile, createdProject, projectNameDir);
			importProjectPermissions(tempFile, createdProject, projectNameDir);

			return true;
		} else {
			return false;
		}
	}

	private void importProjectPermissions(Path tempFile, Project createdProject, String projectNameDir) {
		Map<String, RoleAndPermissionsWithUsers> permissions = readObject(projectNameDir + "/permissions.json",
				tempFile, new TypeToken<Map<String, RoleAndPermissionsWithUsers>>() {
				});
		permissionService.createMissingRolesWithPermissionForProject(createdProject.getId(), from(permissions));
		// add users to roles
		for (RoleAndPermissionsWithUsers v : permissions.values()) {
			List<UserIdentifier> assignedUsers = v.getAssignedUsers();
			Role role = new Role(v.getName());
			permissionService.assignRoleToUsersInProjectId(role, toUserIds(assignedUsers), createdProject.getId());
		}
	}

	private Set<Integer> toUserIds(List<UserIdentifier> users) {
		Set<Integer> userIds = new HashSet<>();
		for (UserIdentifier ui : users) {
			userIds.add(userRepository.findUserByName(ui.getProvider(), ui.getUsername()).getId());
		}
		return userIds;
	}

	private void importLabels(Path tempFile, Project createdProject, String projectNameDir) {
		List<Pair<CardLabel, List<LabelListValueWithMetadata>>> labels = readObject(projectNameDir + "/labels.json", tempFile,
				new TypeToken<List<Pair<CardLabel, List<LabelListValueWithMetadata>>>>() {
				});

		for (Pair<CardLabel, List<LabelListValueWithMetadata>> pLabel : labels) {
			CardLabel label = pLabel.getFirst();

			if (label.getDomain() == LabelDomain.USER) {
				cardLabelRepository.addLabel(createdProject.getId(), label.getUnique(), label.getType(),
						label.getDomain(), label.getName(), label.getColor());
			}

			// import label list value
			if (label.getType() == LabelType.LIST && !pLabel.getSecond().isEmpty()) {
				CardLabel importedCl = cardLabelRepository.findLabelByName(createdProject.getId(), label.getName(),
						label.getDomain());
				for (LabelListValueWithMetadata llv : pLabel.getSecond()) {
					LabelListValue addedLabeListValue = cardLabelRepository.addLabelListValue(importedCl.getId(), llv.getValue());
					if(llv.getMetadata() != null) {
						for(Entry<String, String> metadataKV : llv.getMetadata().entrySet()) {
							cardLabelRepository.createLabelListMetadata(addedLabeListValue.getId(), metadataKV.getKey(), metadataKV.getValue());
						}
					}
				}
			}
		}
	}

	private void importColumnDefinitionColor(Path tempFile, Project createdProject, String projectNameDir) {
		Map<ColumnDefinition, BoardColumnDefinition> importedColDef = readObject(projectNameDir
				+ "/column-definitions.json", tempFile, new TypeToken<Map<ColumnDefinition, BoardColumnDefinition>>() {
		});
		Map<ColumnDefinition, BoardColumnDefinition> currentColDef = projectService
				.findMappedColumnDefinitionsByProjectId(createdProject.getId());

		for (Entry<ColumnDefinition, BoardColumnDefinition> kv : currentColDef.entrySet()) {
			projectService.updateColumnDefinition(createdProject.getId(), kv.getValue().getId(),
					importedColDef.get(kv.getKey()).getColor());
		}
	}

	private void importMissingBoard(Project project, BoardInfo boardInfo, Path tempFile, ImportContext idMapping) {
		Board createdBoard = boardRepository.createEmptyBoard(boardInfo.getName(), boardInfo.getShortName(), boardInfo.getDescription(),
				project.getId());
		boardRepository.updateBoard(createdBoard.getId(), createdBoard.getName(), createdBoard.getDescription(), boardInfo.getArchived());
		List<BoardColumn> boardColumns = readObject("boards/" + boardInfo.getShortName() + "/columns.json", tempFile,
				new TypeToken<List<BoardColumn>>() {
				});
		int boardId = boardRepository.findBoardIdByShortName(boardInfo.getShortName());

		Map<ColumnDefinition, BoardColumnDefinition> colsDef = projectService
				.findMappedColumnDefinitionsByProjectId(project.getId());

		for (BoardColumn bc : boardColumns) {
			BoardColumn added = boardColumnRepository.addColumnToBoard(bc.getName(), colsDef.get(bc.getStatus())
					.getId(), bc.getLocation(), boardId);
			boardColumnRepository.updateOrder(added.getId(), bc.getOrder());

			// save the old column to new column id mapping.
			idMapping.getColumns().put(bc.getId(), added.getId());
		}

		// Update default column order for side locations
        BoardColumn archiveDefaultColumn = boardColumnRepository.findDefaultColumnFor(createdBoard.getId(), BoardColumn.BoardColumnLocation.ARCHIVE);

		if(archiveDefaultColumn.getOrder() != 0) {
            boardColumnRepository.updateOrder(archiveDefaultColumn.getId(), 0);
        }

        BoardColumn backlogDefaultColumn = boardColumnRepository.findDefaultColumnFor(createdBoard.getId(), BoardColumn.BoardColumnLocation.BACKLOG);

        if(archiveDefaultColumn.getOrder() != 0) {
            boardColumnRepository.updateOrder(backlogDefaultColumn.getId(), 0);
        }

        BoardColumn trashDefaultColumn = boardColumnRepository.findDefaultColumnFor(createdBoard.getId(), BoardColumn.BoardColumnLocation.TRASH);

        if(archiveDefaultColumn.getOrder() != 0) {
            boardColumnRepository.updateOrder(trashDefaultColumn.getId(), 0);
        }

		List<StatisticForExport> stats = readObject("boards/" + boardInfo.getShortName() + "/statistics.json",
				tempFile, new TypeToken<List<StatisticForExport>>() {
				});

		// TODO: not optimal in term of performance, use a bulk insert
		for (StatisticForExport stat : stats) {
			statisticsQuery.addFromImport(stat.getDate(), boardId, colsDef.get(stat.getColumnDefinition()).getId(),
					stat.getLocation().toString(), stat.getCount());
		}
	}

}
