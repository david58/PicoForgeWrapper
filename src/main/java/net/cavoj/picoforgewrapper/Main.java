package net.cavoj.picoforgewrapper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cpw.mods.modlauncher.Launcher;


public class Main {
    public static void main(String[] args) throws Exception {
        List<String> argsList = Stream.of(args).collect(Collectors.toList());

        String mcVersion = argsList.get(argsList.indexOf("--fml.mcVersion") + 1);
        String mcpFullVersion = mcVersion + "-" + argsList.get(argsList.indexOf("--fml.mcpVersion") + 1);
        String forgeFullVersion = mcVersion + "-" + argsList.get(argsList.indexOf("--fml.forgeVersion") + 1);

        File librariesDirFile = getLibrariesDir();
        Path librariesDir = librariesDirFile.toPath();
        Path forgeDir = librariesDir.resolve("net").resolve("minecraftforge").resolve("forge").resolve(forgeFullVersion);

        URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {
                Main.class.getProtectionDomain().getCodeSource().getLocation(),
                Launcher.class.getProtectionDomain().getCodeSource().getLocation(),
                forgeDir.resolve("forge-" + forgeFullVersion + "-installer.jar").toUri().toURL()
        }, getParentClassLoader());

        Class<?> installer = ucl.loadClass("net.cavoj.picoforgewrapper.Installer");
        boolean result = (boolean)installer.getDeclaredMethod("ensure").invoke(
                installer.getDeclaredConstructor(File.class).newInstance(librariesDirFile));
        if (!result) {
            LogManager.getLogger().error("Could not install Forge");
            return;
        }

        Launcher.main(args);
    }

    public static File getLibrariesDir() {
        try {
            File laucnher = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            //              /<version>      /modlauncher    /mods           /cpw            /libraries
            return laucnher.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // https://github.com/MinecraftForge/Installer/blob/fe18a164b5ebb15b5f8f33f6a149cc224f446dc2/src/main/java/net/minecraftforge/installer/actions/PostProcessors.java#L287-L303
    private static ClassLoader getParentClassLoader() {
        if (!System.getProperty("java.version").startsWith("1.")) {
            try {
                return (ClassLoader) ClassLoader.class.getDeclaredMethod("getPlatformClassLoader").invoke(null);
            } catch (Exception e) {
                System.out.println("No platform classloader: " + System.getProperty("java.version"));
            }
        }
        return null;
    }
}
