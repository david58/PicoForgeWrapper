package net.cavoj.picoforgewrapper;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        List<String> argsList = Stream.of(args).collect(Collectors.toList());

        String mcVersion = argsList.get(argsList.indexOf("--fml.mcVersion") + 1);
        String forgeFullVersion = mcVersion + "-" + argsList.get(argsList.indexOf("--fml.forgeVersion") + 1);
        String mainClass = System.getProperty("picomc.mainClass");

        File librariesDirFile = getLibrariesDir();
        Path librariesDir = librariesDirFile.toPath();
        Path forgeDir = librariesDir.resolve("net").resolve("minecraftforge").resolve("forge").resolve(forgeFullVersion);
        Path installerPath = forgeDir.resolve("forge-" + forgeFullVersion + "-installer.jar");
        File installerFile = installerPath.toFile();

        URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {
                Main.class.getProtectionDomain().getCodeSource().getLocation(),
                installerPath.toUri().toURL()
        }, getParentClassLoader());

        Class<?> installer = ucl.loadClass("net.cavoj.picoforgewrapper.Installer");
        boolean result = (boolean)installer.getDeclaredMethod("ensure").invoke(
                installer.getDeclaredConstructor(File.class, File.class).newInstance(librariesDirFile, installerFile));
        if (!result) {
            System.err.println("Could not install Forge");
            return;
        }

        Method mainMethod = Class.forName(mainClass).getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    public static File getLibrariesDir() {
        try {
            File picofw = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            //              /<version>     /picoforgewrapper/cavoj           /net            /libraries
            return picofw.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
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
                System.err.println("No platform classloader: " + System.getProperty("java.version"));
            }
        }
        return null;
    }
}
