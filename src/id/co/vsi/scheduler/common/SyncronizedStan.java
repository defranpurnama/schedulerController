package id.co.vsi.scheduler.common;

import id.co.vsi.common.settings.SystemConfig;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * @author  $Author:: didik                                                     $: Author of last revision
 * @version $Revision:: 720                                                     $: Last revision number
 * @since   $LastChangedDate:: 2013-10-04 16:05:13 +0700 (Fri, 04 Oct 2013)     $: Date of last revision
 */
public class SyncronizedStan {
    public static final String     cRevisionNumber   = "$Revision: 9507 $";
    private static final String    cModuleNameSpace  = "stan-config";
    private static final String    cStanKey          = "stan";
    private static final String    cConfigSystemName = "system-config";
    private static final String    cStartStanKey     = "start-stan";
    private static final String    cEndStanKey       = "end-stan";
    private static SyncronizedStan INSTANCE;
    private AtomicInteger          mStan             = new AtomicInteger(1);

    /**
     * Construct Syncronized Stan
     */
    public SyncronizedStan() {
        String tStan        = readSTANFromFile();
        int    tStanCounter = Integer.valueOf(tStan);

        int    min          = SystemConfig.getNameSpace(cConfigSystemName).getIntParameter(cStartStanKey, 0);
        int    max          = SystemConfig.getNameSpace(cConfigSystemName).getIntParameter(cEndStanKey, 999999);

        if (tStanCounter > max) {
            tStanCounter     = min;
            tStan            = padString(tStan, '0', 7, 0);

            saveSTANToFile(tStan);
        }

        mStan.set(tStanCounter);
        attachShutDownHook();
    }

    /**
     * Get singleton Syncronized Stan
     * @return SyncronizedStan
     */
    public static synchronized SyncronizedStan getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SyncronizedStan();
        }

        return INSTANCE;
    }

    /**
     * Get Last STAN
     * @return
     * @Return String
     */
    public String getStan() {
        final int tStanNumber = increamentStan();

        String    tStan       = padString(String.valueOf(tStanNumber), '0', 7, 0);

        return tStan;
    }

    /**
     * Increment STAN
     * @return
     */
    protected int increamentStan() {
        final int tStanumber = mStan.incrementAndGet();

        int min = SystemConfig.getNameSpace(cConfigSystemName).getIntParameter(cStartStanKey, 0);
        int max = SystemConfig.getNameSpace(cConfigSystemName).getIntParameter(cEndStanKey, 999999);

        if (tStanumber > max) {
            mStan.set(min);
        }

        return tStanumber;
    }

    private void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    String tStan = padString(String.valueOf(mStan.get()), '0', 7, 0);
                    saveSTANToFile(tStan);
                }
            });
    }

    /**
     * Read STAN from file
     * @Return string
     */
    private String readSTANFromFile() {
        return SystemConfig.getNameSpace(cModuleNameSpace).getStringParameter(cStanKey);
    }

    /**
     * Save STAN to file
     */
    private void saveSTANToFile(String pStan) {
        SystemConfig.getNameSpace(cModuleNameSpace).setParameter(cStanKey, pStan);
        SystemConfig.getSingleton().saveConfigToFile(cModuleNameSpace);
    }

    private String padString(String pInput, char pPad, int pLen, int pPos) {
        String tTemp = pInput;

        while (tTemp.length() < pLen) {
            if (pPos == 0) {
                tTemp = pPad + tTemp;
            } else if (pPos == 1) {
                tTemp = tTemp + pPad;
            }
        }

        return tTemp;
    }
}
