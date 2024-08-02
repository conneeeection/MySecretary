pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // 네이버 지도 API
        maven("https://repository.map.naver.com/archive/maven")
    }
}

rootProject.name = "MySec"
include(":app")
