# Libraries

The jar files here are not part of Avade. They are the work of others, are
used unchanged and come under their own licenses. Avade itself is under the
GNU General Public License, version 2 or later (`LICENSE` in the top folder).

| File | What | License | Where the text is |
|---|---|---|---|
| `mysql-connector-j-26.7.0.jar` | MySQL Connector/J, the database driver | GPL version 2 with the Universal FOSS Exception 1.0 | `LICENSE` inside the jar |
| `snakeyaml-2.7.jar` | SnakeYAML, reads the `.conf` files | Apache License 2.0 | `LICENSE-snakeyaml.txt` here (the jar has none) |
| `mail/jakarta.mail-2.0.5.jar` | Jakarta Mail, used by AvadeMailer | Eclipse Public License 2.0, or GPL version 2 with the Classpath Exception | `META-INF/LICENSE.md` inside the jar |
| `mail/jakarta.activation-api-2.1.4.jar` | Jakarta Activation API, needed by Jakarta Mail | Eclipse Distribution License 1.0 (BSD 3-Clause) | `META-INF/LICENSE.md` inside the jar |
| `mail/angus-activation-2.0.3.jar` | Angus Activation, needed by Jakarta Mail | Eclipse Distribution License 1.0 (BSD 3-Clause) | `META-INF/LICENSE.md` inside the jar |

`CopyLibs/org-netbeans-modules-java-j2seproject-copylibstask.jar` is a build
task of NetBeans 8.2 (CDDL 1.0, or GPL version 2 with the Classpath
Exception). Only the ant build uses it, it is not installed.

`src/core/CIDRUtils.java` is by Edin Dazdarevic, under the MIT license in
its header.

A new library has to be free software with a license that can be combined
with the GPL, and gets a row here.
