plugins {
    java
    id ("io.freefair.lombok") version "9.2.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.github.zamponimarco"
version = "0.1.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("https://jitpack.io")
        metadataSources {
            artifact()
        }
    }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.1.1.build.+")
    compileOnly("com.github.CubesCocktail:CubesCocktail:7463f203bc")
}