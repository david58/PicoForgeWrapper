package net.cavoj.picoforgewrapper;

import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.Install;
import net.minecraftforge.installer.json.Util;

public class Installer {
    public static boolean install() {
        ProgressCallback monitor = ProgressCallback.withOutputs(System.out);
        Install install = Util.loadInstallProfile();
        return new PicoClientInstall(install, monitor).run(null, input -> true);
    }
}
