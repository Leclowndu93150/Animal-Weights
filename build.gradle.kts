plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "1.0.4"

prism {
    metadata {
        modId = "animalweights"
        name = "Animal Weights"
        description = "Animals gain weight from good farm conditions and drop more loot when killed."
        license = "MIT"
    }

    publishing {
        changelogFile = "CHANGELOG.md"
        type = STABLE

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1544774"
        }
    }

    curseMaven()

    sharedCommon {
        dependencies {
            compileOnly("com.google.code.gson:gson:2.10.1")
        }
    }

    version("26.1.2") {
        common {
            compileOnly("curse.maven:jade-324717:8068368")
        }
        fabric {
            loaderVersion = "0.19.2"
            fabricApi("0.149.0+26.1.2")
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:8048381")
            }
        }
        neoforge {
            loaderVersion = "26.1.2.59-beta"
            loaderVersionRange = "[4,)"
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:8068368")
            }
        }
    }

    version("1.21.11") {
        common {
            compileOnly("curse.maven:jade-324717:7938498")
        }
        fabric {
            loaderVersion = "0.19.2"
            fabricApi("0.141.4+1.21.11")
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:7886760")
            }
        }
        neoforge {
            loaderVersion = "21.11.42"
            loaderVersionRange = "[4,)"
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:7938498")
            }
        }
    }

    version("1.21.1") {
        common {
            compileOnly("curse.maven:jade-324717:7545219")
        }
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.116.1+1.21.1")
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:7545228")
            }
        }
        neoforge {
            loaderVersion = "21.1.95"
            loaderVersionRange = "[4,)"
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:7545219")
            }
        }
    }

    version("1.20.1") {
        common {
            modCompileOnly("curse.maven:jade-324717:6855440")
        }
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.92.6+1.20.1")
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:6291330")
            }
        }
        forge {
            loaderVersion = "47.4.0"
            dependencies {
                modRuntimeOnly("curse.maven:jade-324717:6855440")
            }
        }
    }

}
