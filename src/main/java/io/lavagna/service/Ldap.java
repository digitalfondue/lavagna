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
import io.lavagna.model.Pair;
import io.lavagna.service.LdapConnection.InitialDirContextCloseable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.EnumSet.of;
import static java.util.Objects.requireNonNull;

@Service
public class Ldap {

	private static final Logger LOG = LogManager.getLogger();

	private final ConfigurationRepository configurationRepository;
	private final LdapConnection ldapConnection;


	public Ldap(ConfigurationRepository configurationRepository, LdapConnection ldapConnection) {
		this.configurationRepository = configurationRepository;
		this.ldapConnection = ldapConnection;
	}

	public boolean authenticate(String username, String password) {

		Map<Key, String> conf = configurationRepository
				.findConfigurationFor(of(Key.LDAP_SERVER_URL, Key.LDAP_MANAGER_DN, Key.LDAP_MANAGER_PASSWORD,
						Key.LDAP_USER_SEARCH_BASE, Key.LDAP_USER_SEARCH_FILTER));

		String providerUrl = requireNonNull(conf.get(Key.LDAP_SERVER_URL));
		String ldapManagerDn = requireNonNull(conf.get(Key.LDAP_MANAGER_DN));
		String ldapManagerPwd = requireNonNull(conf.get(Key.LDAP_MANAGER_PASSWORD));
		String base = requireNonNull(conf.get(Key.LDAP_USER_SEARCH_BASE));
		String filter = requireNonNull(conf.get(Key.LDAP_USER_SEARCH_FILTER));

		return authenticateWithParams(providerUrl, ldapManagerDn, ldapManagerPwd, base, filter, username, password)
				.getFirst();
	}

	public Pair<Boolean, List<String>> authenticateWithParams(String providerUrl, String ldapManagerDn,
			String ldapManagerPwd, String base, String filter, String username, String password) {
		requireNonNull(username);
		requireNonNull(password);
		List<String> msgs = new ArrayList<>();

		msgs.add(format("connecting to %s with managerDn %s", providerUrl, ldapManagerDn));
		try (InitialDirContextCloseable dctx = ldapConnection.context(providerUrl, ldapManagerDn, ldapManagerPwd)) {
			msgs.add(format("connected [ok]"));
			msgs.add(format("now searching user \"%s\" with base %s and filter %s", username, base, filter));

			SearchControls sc = new SearchControls();
			sc.setReturningAttributes(null);
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

			List<SearchResult> srs = Ldap.search(dctx, base,
					new MessageFormat(filter).format(new Object[] { Ldap.escapeLDAPSearchFilter(username) }), sc);
			if (srs.size() != 1) {
				String msg = format("error for username \"%s\" we have %d results instead of 1 [error]", username,
						srs.size());
				msgs.add(msg);
				LOG.info(msg, username, srs.size());
				return Pair.Companion.of(false, msgs);
			}

			msgs.add("user found, now will connect with given password [ok]");

			SearchResult sr = srs.get(0);

			try (InitialDirContextCloseable uctx = ldapConnection.context(providerUrl, sr.getNameInNamespace(),
					password)) {
				msgs.add("user authenticated, everything seems ok [ok]");
				return Pair.Companion.of(true, msgs);
			} catch (NamingException e) {
				String msg = format("error while checking with username \"%s\" with message: %s [error]", username,
						e.getMessage());
				msgs.add(msg);
				LOG.info(msg, e);
				return Pair.Companion.of(false, msgs);
			}
		} catch (Throwable e) {
			String errMsg = format(
					"error while opening the connection with message: %s [error], check the logs for a more complete trace",
					e.getMessage());
			msgs.add(errMsg);
			msgs.add("Full stacktrace is:");
			msgs.add(ExceptionUtils.getStackTrace(e));
			LOG.error(errMsg, e);
			return Pair.Companion.of(false, msgs);
		}
	}

    public boolean checkUserAvailability(String username){

        try (InitialDirContextCloseable dctx = getLdapContext()) {
            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(null);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            List<SearchResult> srs = Ldap.search(dctx, getLdapBase(),
                new MessageFormat(getLdapFilter()).format(new Object[] { Ldap.escapeLDAPSearchFilter(username) }), sc);
            if (srs.size() != 1) {
                String msg = format("error for username \"%s\" we have %d results instead of 1 [error]", username, srs.size());
                LOG.info(msg, username, srs.size());
                return false;
            }

            return true;

        } catch (Throwable e) {
            String errMsg = format(
                "error while opening the connection with message: %s [error], check the logs for a more complete trace",
                e.getMessage());
            LOG.error(errMsg, e);
            return false;
        }
    }

    private InitialDirContextCloseable getLdapContext() throws NamingException {
        Map<Key, String> conf = configurationRepository
            .findConfigurationFor(of(Key.LDAP_SERVER_URL, Key.LDAP_MANAGER_DN, Key.LDAP_MANAGER_PASSWORD,
                Key.LDAP_USER_SEARCH_BASE, Key.LDAP_USER_SEARCH_FILTER));

        String providerUrl = requireNonNull(conf.get(Key.LDAP_SERVER_URL));
        String ldapManagerDn = requireNonNull(conf.get(Key.LDAP_MANAGER_DN));
        String ldapManagerPwd = requireNonNull(conf.get(Key.LDAP_MANAGER_PASSWORD));
        return ldapConnection.context(providerUrl, ldapManagerDn, ldapManagerPwd);
    }

    private String getLdapBase(){
        Map<Key, String> conf = configurationRepository
            .findConfigurationFor(of(Key.LDAP_SERVER_URL, Key.LDAP_MANAGER_DN, Key.LDAP_MANAGER_PASSWORD,
                Key.LDAP_USER_SEARCH_BASE, Key.LDAP_USER_SEARCH_FILTER));
        return requireNonNull(conf.get(Key.LDAP_USER_SEARCH_BASE));
    }

    private String getLdapFilter(){
        Map<Key, String> conf = configurationRepository
            .findConfigurationFor(of(Key.LDAP_SERVER_URL, Key.LDAP_MANAGER_DN, Key.LDAP_MANAGER_PASSWORD,
                Key.LDAP_USER_SEARCH_BASE, Key.LDAP_USER_SEARCH_FILTER));
        return requireNonNull(conf.get(Key.LDAP_USER_SEARCH_FILTER));
    }

	private static List<SearchResult> search(DirContext dctx, String base, String filter, SearchControls sc)
			throws NamingException {
		List<SearchResult> res = new ArrayList<>();
		NamingEnumeration<SearchResult> search = dctx.search(base, filter, sc);
		while (search.hasMore()) {
			res.add(search.next());
		}
		return res;
	}


	// imported from
	// https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java .
	// Checked against spring implementation too...
	private static final String escapeLDAPSearchFilter(String filter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filter.length(); i++) {
			char curChar = filter.charAt(i);
			switch (curChar) {
			case '\\':
				sb.append("\\5c");
				break;
			case '*':
				sb.append("\\2a");
				break;
			case '(':
				sb.append("\\28");
				break;
			case ')':
				sb.append("\\29");
				break;
			case '\u0000':
				sb.append("\\00");
				break;
			default:
				sb.append(curChar);
			}
		}
		return sb.toString();
	}
}
