package net.cavoj.picoforgewrapper;

import net.minecraftforge.installer.actions.ClientInstall;
import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.InstallV1;

import java.io.File;

public class PicoClientInstall extends ClientInstall {
    public PicoClientInstall(InstallV1 profile, ProgressCallback monitor) {
        super(profile, monitor);
    }

    public boolean runPico(File librariesDir, File clientTarget, File root, File installer) {
        return this.processors.process(librariesDir, clientTarget, root, installer);
    }
}
