/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.vaniglia;

import java.util.PropertyResourceBundle;

public class Information {

    private static String name = "Vaniglia";
    private static String version = "0.5.0f";

    private static String cvsTag = "$Name: VANIGLIA-0_5_0 $";

    public static String getName() {
        return name;
    }

    public static String getVersion() {
        int dashIndex = cvsTag.lastIndexOf("-");
        if (dashIndex < 0) {
            return version;
        }

        int vstart = (dashIndex + 1);
        String tagVersion = cvsTag.substring(vstart, cvsTag.length() - 2);
        String vers = tagVersion.replaceAll("_", ".");
        return vers;
    }

    public static String getBuildProperties() {
        try {
            PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("Build");
            String buildNumber = bundle.getString("build.number");
            String buildTimestamp = bundle.getString("build.timestamp");
            String userName = bundle.getString("user.name");
            String osVersion = bundle.getString("os.name");
            String javaVersion = bundle.getString("java.vm.version");

            StringBuffer buff = new StringBuffer(100);

            buff.append("Build Number: " + buildNumber);
            buff.append('\n');
            buff.append("Build Timestamp: " + buildTimestamp);
            buff.append('\n');
            buff.append("User Name:    " + userName);
            buff.append('\n');
            buff.append("OS Version:   " + osVersion);
            buff.append('\n');
            buff.append("Java Version: " + javaVersion);

            return buff.toString();
        } catch (Exception e) {
        }
        return "N/A";
    }

    public static void main(String[] args) {
        final String all = "all";
//        final String build = "build";
        final String help = "help";
        final String version = "version";

        if (args.length == 0) {
            System.out.println(getName() + " " + getVersion());
            return;
        }

        if (help.equals(args[0])) {
            System.out.println(getName() + " " + getVersion());
            System.out.println();
            System.out.println("Available options:");
            System.out.println("- all     Prints all information.");
            System.out.println("- version Prints version information.");
            System.out.println("- help    Prints this message.");
            return;
        }

        if (all.equals(args[0])) {
            System.out.println(getName() + " " + getVersion());
            System.out.println();
            System.out.println("Build Properties: ");
            System.out.println(getBuildProperties());
            return;
        }

        if (version.equals(args[0])) {
            System.out.println(getVersion());
            return;
        }

//        if (build.equals(args[0])) {
//            System.out.println(getName() + " " + getVersion());
//            System.out.println();
//            System.out.println("Build Properties: ");
//            System.out.println(getBuildProperties());
//            return;
//        }

    }

}
