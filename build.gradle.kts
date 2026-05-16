plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "1.0.1"

prism {
    metadata {
        modId = "animalweights"
        name = "Animal Weights"
        description = "Animals gain weight from good farm conditions and drop more loot when killed."
        license = "MIT"
    }

    publishing {
        changelogFile = "CHANGELOG.md"
        type = BETA

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1544774"
        }
    }

    sharedCommon {
        dependencies {
            compileOnly("com.google.code.gson:gson:2.10.1")
        }
    }

    version("26.1.2") {
        fabric {
            loaderVersion = "0.19.2"
            fabricApi("0.149.0+26.1.2")
        }
        neoforge {
            loaderVersion = "26.1.2.59-beta"
            loaderVersionRange = "[4,)"
        }
    }

    version("1.21.11") {
        fabric {
            loaderVersion = "0.19.2"
            fabricApi("0.141.4+1.21.11")
        }
        neoforge {
            loaderVersion = "21.11.42"
            loaderVersionRange = "[4,)"
        }
    }

    version("1.21.1") {
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.116.1+1.21.1")
        }
        neoforge {
            loaderVersion = "21.1.95"
            loaderVersionRange = "[4,)"
        }
    }

    version("1.20.1") {
        fabric {
            loaderVersion = "0.16.10"
            fabricApi("0.92.6+1.20.1")
        }
        forge {
            loaderVersion = "47.4.0"
        }
    }

}
