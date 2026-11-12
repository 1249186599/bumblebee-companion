pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
            name = "AliyunGradlePlugin"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "AliyunPublic"
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://artifact.bytedance.com/repository/Volcengine")
            name = ""
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "AliyunPublic"
        }
        google()
        mavenCentral()
        maven {
            url = uri("https://artifact.bytedance.com/repository/Volcengine")
            name = ""
        }
    }
}

rootProject.name = "BumblebeeCompanion"

include(":app")
