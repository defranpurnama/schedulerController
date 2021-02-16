package id.co.vsi.scheduler.plugin;

import id.co.vsi.common.etc.VersionChecker;


/**
 *
 * $Rev::                                                                       $:Revision of last commit
 * $Author::                                                                    $:Author of last commit
 * $Date::                                                                      $:Date of last commit
 *
 */

public class Main {

    public static final String cRevisionNumber = "$Revision: 0 $";

    public static void main(String[] args) {
        VersionChecker.checkVersion(Main.class);
    }

}
