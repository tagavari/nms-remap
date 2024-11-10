# NMS Remap Gradle Plugin

A Gradle plugin for remapping artifacts using [SpecialSource](https://github.com/md-5/SpecialSource),
with shortcuts for Spigot NMS plugin developers.

Inspired by the work done by
[SpecialSourceMP](https://github.com/agaricusb/SpecialSourceMP) and
[mojang-spigot-remapper](https://github.com/patrick-choe/mojang-spigot-remapper).

### About the plugin

Mojang now releases their obfuscation maps for Minecraft,
which makes it possible to directly invoke Minecraft's internal code
by using readable member names.

However, these references must be mapped back to their obfuscated
counterparts in order to run on Spigot servers.

For more details, see [the NMS section on the Spigot 1.17 release post](https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/#post-4184317).

An official Maven plugin is available to perform this remapping automatically when assembling a jar file.
This plugin aims to bring the same functionality to Gradle users, while also being easy to use.

### Getting started

Run BuildTools with the `--remapped` option
in order to install Mojang and Spigot obfuscation maps to your local repository.

Then, add the plugin to your project:

#### Kotlin

```kotlin
plugins {
    id("me.tagavari.nmsremap") version "1.0.1"
}
```

#### Groovy

```groovy
plugins {
    id "me.tagavari.nmsremap" version "1.0.1"
}
```

NMS Remap offers 2 usage methods:
a simplified method for remapping Spigot plugins to run on servers,
and an advanced method that mirrors SpecialSource's Maven plugin's API.

For most users, we recommend the simplified method.

### Simplified method

A `remap` task will be added to your project.
By default, it will try to detect which version of Minecraft you're compiling for,
and remap the output of the `jar` task automatically.

```shell
./gradlew remap
```

The `remap` task has these configuration options:

```kotlin
tasks {
    remap {
        //Overrides the Minecraft version to remap against.
        //You may use this option if your version can't automatically be detected.
        //Must match a valid Spigot dependency version.
        version.set("1.18.2-R0.1-SNAPSHOT")

        //Overrides the default input file
        inputFile.set(uberJar.archiveFile)

        //The classifier to add to the end of the output file
        //(if archiveName is not specified)
        archiveClassifier.set("remapped")
        
        //The name to use for the output file
        //(if outputFile is not specified)
        archiveName.set("plugin-remapped.jar")
        
        //The archive file to write to
        outputFile.set(File(buildDir, "plugin-remapped.jar"))
    }
}
```

### Advanced method

You can define your own tasks with an API similar to SpecialSource's Maven plugin.

Please note that Gradle specifies a dependency's type at the end of the dependency string denoted with the `@` symbol,
rather than after the version like Maven.

For example, this is how you would recreate Maven the configuration provided in the Spigot release post:

```kotlin
val mcVersion = "1.18.2-R0.1-SNAPSHOT"

val taskRemapMojangObf = tasks.register<me.tagavari.nmsremap.SSRemapTask>("remapMojangObf") {
    srgIn.set("org.spigotmc:minecraft-server:$mcVersion:maps-mojang@txt")
    remappedDependencies.add("org.spigotmc:spigot:$mcVersion:remapped-mojang")
    reverse.set(true)
    archiveClassifier.set("remapped-obf")
}

tasks.register<me.tagavari.nmsremap.SSRemapTask>("remapObfSpigot") {
    inputFile.set(taskRemapMojangObf.get().outputFile)
    srgIn.set("org.spigotmc:minecraft-server:$mcVersion:maps-spigot@csrg")
    remappedDependencies.add("org.spigotmc:spigot:$mcVersion:remapped-obf")
}
```
