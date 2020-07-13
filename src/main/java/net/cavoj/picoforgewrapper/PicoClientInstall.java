package net.cavoj.picoforgewrapper;

import net.minecraftforge.installer.actions.ClientInstall;
import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.Install;

import java.io.File;

public class PicoClientInstall extends ClientInstall {
    public PicoClientInstall(Install profile, ProgressCallback monitor) {
        super(profile, monitor);
    }

    public boolean runPico(File librariesDir, File clientTarget) {
        return this.processors.process(librariesDir, clientTarget);
    }
}
