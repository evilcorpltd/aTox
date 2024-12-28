load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@rules_java//java:defs.bzl", "java_library")

scala_library(
    name = "jvm-toxcore-api",
    srcs = glob([
        "src/main/java/**/*.java",
        "src/main/java/**/*.scala",
    ]),
    visibility = ["//visibility:public"],
    deps = ["@maven//:org_jetbrains_annotations"],
)

java_library(
    name = "jvm-toxcore-api-java",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":jvm-toxcore-api",
        "@maven//:org_jetbrains_annotations",
    ],
)
