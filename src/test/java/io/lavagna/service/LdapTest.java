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

import io.lavagna.model.Key;
import io.lavagna.service.LdapConnection.InitialDirContextCloseable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LdapTest {

	private static final String MANAGER_PWD = "secret";
	private static final String MANAGER_DN = "uid=admin,ou=system";
	private static final String PROVIDER_URL = "ldap://localhost:10389";

	@Mock
	private ConfigurationRepository configurationRepository;
	@Mock
	private LdapConnection ldapConnection;

	private Ldap ldap;

	@Before
	public void prepare() {
		ldap = new Ldap(configurationRepository, ldapConnection);

		Map<Key, String> conf = new EnumMap<>(Key.class);
		conf.put(Key.LDAP_SERVER_URL, PROVIDER_URL);
		conf.put(Key.LDAP_MANAGER_DN, MANAGER_DN);
		conf.put(Key.LDAP_MANAGER_PASSWORD, MANAGER_PWD);
		conf.put(Key.LDAP_USER_SEARCH_BASE, "ou=system");
		conf.put(Key.LDAP_USER_SEARCH_FILTER, "uid={0}");

		when(
				configurationRepository.findConfigurationFor(EnumSet.of(Key.LDAP_SERVER_URL, Key.LDAP_MANAGER_DN,
						Key.LDAP_MANAGER_PASSWORD, Key.LDAP_USER_SEARCH_BASE, Key.LDAP_USER_SEARCH_FILTER)))
				.thenReturn(conf);
	}

	@Test
	public void failOnFirstOpen() throws NamingException {
		Throwable throwable = new NamingException("unit test :D");
		when(ldapConnection.context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD)).thenThrow(throwable);

		Assert.assertFalse(ldap.authenticate("user", "password"));
		verify(ldapConnection).context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void nothingFound() throws NamingException {

		InitialDirContextCloseable ctx = mock(InitialDirContextCloseable.class);
		when(ldapConnection.context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD)).thenReturn(ctx);

		NamingEnumeration<SearchResult> searchRes = mock(NamingEnumeration.class);
		when(ctx.search(eq("ou=system"), eq("uid=user"), any(SearchControls.class))).thenReturn(searchRes);
		when(searchRes.hasMore()).thenReturn(false);

		Assert.assertFalse(ldap.authenticate("user", "password"));
		// first call
		verify(ldapConnection).context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void wrongPassword() throws NamingException {

		InitialDirContextCloseable ctx = mock(InitialDirContextCloseable.class);
		when(ldapConnection.context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD)).thenReturn(ctx);

		NamingEnumeration<SearchResult> searchRes = mock(NamingEnumeration.class);
		when(ctx.search(eq("ou=system"), eq("uid=user\\5c\\2a\\28\\29\\00"), any(SearchControls.class))).thenReturn(
				searchRes);
		when(searchRes.hasMore()).thenReturn(true, false);
		SearchResult sr = mock(SearchResult.class);
		when(sr.getNameInNamespace()).thenReturn("HOLOYOLO");
		when(searchRes.next()).thenReturn(sr);

		Throwable throwable = new NamingException("unit test :D");
		when(ldapConnection.context(PROVIDER_URL, "HOLOYOLO", "password")).thenThrow(throwable);

		// we check the escape too..
		Assert.assertFalse(ldap.authenticate("user\\*()\u0000", "password"));
		verify(ctx).search(eq("ou=system"), eq("uid=user\\5c\\2a\\28\\29\\00"), any(SearchControls.class));
		verify(sr).getNameInNamespace();
		// first call
		verify(ldapConnection).context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD);
		// second call with the user dn
		verify(ldapConnection).context(PROVIDER_URL, "HOLOYOLO", "password");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void authenticate() throws NamingException {

		InitialDirContextCloseable ctx = mock(InitialDirContextCloseable.class);
		when(ldapConnection.context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD)).thenReturn(ctx);

		NamingEnumeration<SearchResult> searchRes = mock(NamingEnumeration.class);
		when(ctx.search(eq("ou=system"), eq("uid=user"), any(SearchControls.class))).thenReturn(searchRes);
		when(searchRes.hasMore()).thenReturn(true, false);
		SearchResult sr = mock(SearchResult.class);
		when(sr.getNameInNamespace()).thenReturn("HOLOYOLO");
		when(searchRes.next()).thenReturn(sr);

		Assert.assertTrue(ldap.authenticate("user", "password"));
		verify(sr).getNameInNamespace();
		// first call
		verify(ldapConnection).context(PROVIDER_URL, MANAGER_DN, MANAGER_PWD);
		// second call with the user dn
		verify(ldapConnection).context(PROVIDER_URL, "HOLOYOLO", "password");
	}
}
