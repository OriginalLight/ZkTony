plugins {
    id("kotlin")
}

java {
    sourceSets {
        main {
            resources {
                srcDir("src/main/proto")
            }
        }
    }
}