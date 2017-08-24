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

import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.model.UserMetadata;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.*;
import io.lavagna.web.api.model.DisplayNameEmail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventEmitter eventEmitter;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserWithPermission user;

    @Mock
    private ProjectService projectService;

    private UserController userController;

    @Before
    public void prepare() {
        userController = new UserController(userRepository, userService, eventEmitter, eventRepository,
            projectService);
    }

    @Test
    public void findAllUsers() {
        userController.findAllUsers();
        verify(userRepository).findAll();
    }

    @Test
    public void keepAlive() {
        Assert.assertTrue(userController.keepAlive());
    }

    @Test
    public void updateUserProfile() {
        DisplayNameEmail d = new DisplayNameEmail();
        d.setDisplayName("displayName");
        d.setEmail("email");
        d.setEmailNotification(true);
        d.setSkipOwnNotifications(true);

        userController.updateUserProfile(user, d);

        verify(userRepository).updateProfile(user, "email", "displayName", true, true);
    }

    @Test
    public void updateMetadata() {
        UserMetadata metadata = new UserMetadata(true, false);

        userController.updateMetadata(user, metadata);

        verify(userRepository).updateMetadata(user.getId(), metadata);
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(user.getId())).thenReturn(user);

        User u = userController.getUser(user.getId());
        Assert.assertEquals(user, u);
    }

    @Test
    public void testGetUserByName() {
        when(userRepository.findUserByName(user.getProvider(), user.getUsername())).thenReturn(user);

        User u = userController.getUser(user.getProvider(), user.getUsername());
        Assert.assertEquals(user, u);
    }

    @Test
    public void userProfile() {
        Assert.assertEquals(user, userController.userProfile(user));
    }

    @Test
    public void testGetUserProfileWithoutGlobalRead() {
        User testUser = mock(User.class);
        when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

        //
        userController.getUserProfile("test", "test", user, 1);
        //

        // check that we are going in the correct branch
        verify(user).projectsIdWithPermission(Permission.READ);
        verify(eventRepository).getUserActivityForProjects(eq(testUser.getId()), Mockito.<Date>any(),
            Mockito.<Collection<Integer>>any());
        verify(projectService).findProjectsActivityByUserInProjects(eq(testUser.getId()),
            Mockito.<Collection<Integer>>any());
        verify(eventRepository, Mockito.never()).getLatestActivityByProjects(eq(testUser.getId()), Mockito.<Date>any(),
            Mockito.<Collection<Integer>>any());
    }

    @Test
    public void testGetUserProfileWithGlobalRead() {
        User testUser = mock(User.class);
        when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

        Map<Permission, Permission> permission = new EnumMap<>(Permission.class);
        permission.put(Permission.READ, Permission.READ);
        when(user.getBasePermissions()).thenReturn(permission);

        //
        userController.getUserProfile("test", "test", user, 1);
        //

        // check that we are going in the correct branch
        verify(user, Mockito.never()).projectsWithPermission(Permission.READ);
        verify(eventRepository).getUserActivity(eq(testUser.getId()), Mockito.<Date>any());
        verify(projectService).findProjectsActivityByUser(eq(testUser.getId()));
        verify(eventRepository, Mockito.never()).getLatestActivity(eq(testUser.getId()), Mockito.<Date>any());
    }

    @Test
    public void testClearAllTokens() {

        userController.clearAllTokens(user);

        verify(userRepository).clearAllTokens(eq(user));
    }

    @Test
    public void testGetUserActivity() {
        User testUser = mock(User.class);
        when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

        userController.getUserActivity("test", "test", user);

        verify(eventRepository).getLatestActivityByProjects(eq(testUser.getId()), Mockito.<Date>any(),
            Mockito.<Collection<Integer>>any());
    }

    @Test
    public void testGetUserActivityWithGlobalRead() {
        User testUser = mock(User.class);
        when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

        Map<Permission, Permission> permission = new EnumMap<>(Permission.class);
        permission.put(Permission.READ, Permission.READ);
        when(user.getBasePermissions()).thenReturn(permission);

        userController.getUserActivity("test", "test", user);

        verify(eventRepository).getLatestActivity(eq(testUser.getId()), Mockito.<Date>any());
    }
}
