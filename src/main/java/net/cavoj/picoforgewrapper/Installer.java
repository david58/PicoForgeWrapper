package net.cavoj.picoforgewrapper;

import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.Artifact;
import net.minecraftforge.installer.json.Install;
import net.minecraftforge.installer.json.Util;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;

public class Installer {
    private final File librariesDir;
    private final File clientTarget;

    public static File getClientJarFile() {
        try {
            Class<?> client = ClassLoader.getSystemClassLoader().loadClass("net.minecraft.client.main.Main");
            return new File(client.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (ClassNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Installer(File librariesDir) {
        this.librariesDir = librariesDir;
        this.clientTarget = getClientJarFile();
    }

    public boolean ensure() {
        Install install = Util.loadInstallProfile();
        if (checkLibraries(install)) {
            System.err.println("PicoForgeWrapper: Found cached Forge artifacts, continuing");
            return true;
        };
        System.err.println("PicoForgeWrapper: Some artifacts were missing, running Forge installer");
        ProgressCallback monitor = ProgressCallback.withOutputs(System.err);
        return new PicoClientInstall(install, monitor).runPico(this.librariesDir, this.clientTarget);
    }

    private boolean checkLibraries(Install install) {
        Map<String, String> data = install.getData(true);
        for (String key : data.keySet()) {
            String value = data.get(key);
            // We are only interested in artifacts
            if (value.charAt(0) == '[' && value.charAt(value.length() - 1) == ']') {
                data.put(key, Artifact.from(value.substring(1, value.length() -1)).getLocalPath(this.librariesDir).getAbsolutePath());
            }
        }
        boolean allGood = true;
        HashSet<String> artifactFiles = new HashSet<>();

        // Check all processor outputs
        for (Install.Processor proc : install.getProcessors("client")) {
            Map<String, String> outputs = proc.getOutputs();
            for (String key : outputs.keySet()) {
                String value = outputs.get(key);
                if (key.charAt(0) == '{' && key.charAt(key.length() - 1) == '}')
                    key = data.get(key.substring(1, key.length() - 1));
                else if (key.charAt(0) == '[' && key.charAt(key.length() - 1) == ']')
                    key = Artifact.from(key.substring(1, key.length() - 1)).getLocalPath(librariesDir).getAbsolutePath();
                else continue;
                artifactFiles.add(key);
            }
        }

        // MC_SRG is not listed as an output of any processor for some reason
        if (data.containsKey("MC_SRG")) {
            artifactFiles.add(data.get("MC_SRG"));
        }

        // Check artifacts
        for (String path : artifactFiles) {
            File artifact = new File(path);
            // TODO Maybe check hashes as well. Would be cool if it depended on whether picomc ... --verify was used.
            if (!artifact.exists()) {
                System.err.println("PicoForgeWrapper: Missing artifact: " + artifact.getName());
                allGood = false;
            }
        }
        return allGood;
    }
}
