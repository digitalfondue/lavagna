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
package io.lavagna.config;

import io.lavagna.common.CookieNames;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

public class Launcher {

    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<Integer> portOption = parser.accepts("port", "Create an HTTP listener on port n (default 8080)").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> bindAddressOption = parser.accepts("bindAddress", "Accept connections only on address addr (default: accept on any address)").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec<String> tmpDirOption = parser.accepts("tmpDir", "Temporary directory").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec<String> contextPathOption = parser.accepts("contextPath", "Set context path (default: /)").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec<String> cookiePrefixOption = parser.accepts("cookiePrefix", "Prefix the cookies").withRequiredArg().ofType(String.class);
        OptionSpecBuilder helpOption = parser.accepts("help", "Print this help message");
        parser.accepts("headless", "legacy parameter, ignored");
        parser.accepts("forwarded", "legacy parameter, ignored");
        parser.accepts("sslProxied", "legacy parameter, ignored");

        OptionSet options = parser.parse(args);

        if (options.has(helpOption)) {
            parser.printHelpOn(System.out);
            return;
        }

        int port = options.has(portOption) ? options.valueOf(portOption) : 8080;
        String bindAddress = options.has(bindAddressOption) ? options.valueOf(bindAddressOption) : "0.0.0.0";
        String contextPath = options.has(contextPathOption) ? options.valueOf(contextPathOption) : "/";

        if (options.has(cookiePrefixOption)) {
        	String cookiePrefixValue = options.valueOf(cookiePrefixOption);
        	System.out.println("Using cookie prefix " + cookiePrefixValue);
        	System.setProperty(CookieNames.PROPERTY_NAME, cookiePrefixValue);
        }

        InetSocketAddress address = new InetSocketAddress(bindAddress, port);
        Server server = new Server(address);

        WebAppContext webapp = new WebAppContext();

        if (options.has(tmpDirOption)) {
            webapp.setTempDirectory(new File(options.valueOf(tmpDirOption)));
        }

        webapp.setContextPath(contextPath);
        webapp.setServer(server);
        webapp.setWar(war());
        webapp.setConfigurations(new Configuration[] {
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new AnnotationConfiguration(),
                new JettyWebXmlConfiguration()
        });

        server.setHandler(webapp);
        System.out.println("Starting jetty server " + Server.getVersion());
        System.out.println("Server is listening at " + address.toString());
        server.start();
        server.join();
    }

    private static String war() throws UnsupportedEncodingException {
        String file = Launcher.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        //has form jar:file:/path/to/war/lavagna-jetty-console.war!/WEB-INF/classes!/
        return new File(URLDecoder.decode(file.substring(file.indexOf("file:")+ "file:".length(), file.indexOf("!")), "utf-8")).getAbsolutePath();
    }
}
