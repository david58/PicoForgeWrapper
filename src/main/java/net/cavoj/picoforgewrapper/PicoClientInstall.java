package net.cavoj.picoforgewrapper;

import net.minecraftforge.installer.actions.ClientInstall;
import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.Install;

import java.io.File;
import java.util.function.Predicate;

public class PicoClientInstall extends ClientInstall {
    public PicoClientInstall(Install profile, ProgressCallback monitor) {
        super(profile, monitor);
    }

    @Override
    public boolean run(File target, Predicate<String> optionals) {
        File librariesDir = Main.getLibrariesDir();
        File clientTarget = Main.getClientJarFile();
        return this.processors.process(librariesDir, clientTarget);
    }
}
