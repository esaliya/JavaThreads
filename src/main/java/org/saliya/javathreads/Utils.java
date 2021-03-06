package org.saliya.javathreads;

import java.io.IOException;

public class Utils {

    public static String getProcAffinityMask(int pid) throws IOException {
        byte[] bo = new byte[100];
        String pidString = String.valueOf(pid);
        String[] cmd = {"bash", "-c", "taskset -pc " + pidString};
        Process p = Runtime.getRuntime().exec(cmd);
        p.getInputStream().read(bo);
        return new String(bo);
    }
}
