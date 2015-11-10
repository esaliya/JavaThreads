package org.saliya.javathreads;

import java.io.IOException;

public class Utils {
    public static String getPid() throws IOException {
        byte[] bo = new byte[100];
        String[] cmd = {"bash", "-c", "echo $PPID"};
        Process p = Runtime.getRuntime().exec(cmd);
        p.getInputStream().read(bo);
        return new String(bo).trim();
    }

    public static String getProcAffinityMask(int pid) throws IOException {
        byte[] bo = new byte[100];
        String pidString = String.valueOf(pid);
        String[] cmd = {"bash", "-c", "taskset -p " + pidString};
        Process p = Runtime.getRuntime().exec(cmd);
        p.getInputStream().read(bo);
        return new String(bo);
    }
}
