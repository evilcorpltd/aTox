workspace(name = "atox")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Android SDK/NDK setup
# =========================================================

android_sdk_repository(name = "androidsdk")

android_ndk_repository(
    name = "androidndk",
    api_level = 19,
)

# Bazel
# =========================================================

PLATFORMS_TAG = "0.0.6"

http_archive(
    name = "platforms",
    sha256 = "5308fc1d8865406a49427ba24a9ab53087f17f5266a7aabbfc28823f3916e1ca",
    url = "https://github.com/bazelbuild/platforms/releases/download/%s/platforms-%s.tar.gz" % (PLATFORMS_TAG, PLATFORMS_TAG),
)

RULES_PKG_TAG = "1.0.1"

http_archive(
    name = "rules_pkg",
    integrity = "sha256-0gyVGWDtd8t7NBwqWUiFNOSU1a0dMMSBjHNtV3cqn+8=",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (RULES_PKG_TAG, RULES_PKG_TAG),
)

RULES_PROTO_TAG = "97d8af4dc474595af3900dd85cb3a29ad28cc313"

http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-%s" % RULES_PROTO_TAG,
    url = "https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_TAG,
)

RULES_PYTHON_TAG = "0.40.0"

http_archive(
    name = "rules_python",
    sha256 = "690e0141724abb568267e003c7b6d9a54925df40c275a870a4d934161dc9dd53",
    strip_prefix = "rules_python-%s" % RULES_PYTHON_TAG,
    url = "https://github.com/bazelbuild/rules_python/releases/download/%s/rules_python-%s.tar.gz" % (RULES_PYTHON_TAG, RULES_PYTHON_TAG),
)

RULES_ANDROID_TAG = "0.1.1"

http_archive(
    name = "rules_android",
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-%s" % RULES_ANDROID_TAG,
    url = "https://github.com/bazelbuild/rules_android/archive/v%s.zip" % RULES_ANDROID_TAG,
)

STARDOC_TAG = "0.4.0"

http_archive(
    name = "io_bazel_stardoc",
    sha256 = "36b8d6c2260068b9ff82faea2f7add164bf3436eac9ba3ec14809f335346d66a",
    strip_prefix = "stardoc-%s" % STARDOC_TAG,
    url = "https://github.com/bazelbuild/stardoc/archive/%s.zip" % STARDOC_TAG,
)

BAZEL_SKYLIB_TAG = "fd75066f159234265efb8f838b056be5a2e00a59"

http_archive(
    name = "bazel_skylib",
    sha256 = "37fbe6e229f28dfda55d9c9a305235b882a1cf6cff746ce448b8b870ecfdf620",
    strip_prefix = "bazel-skylib-%s" % BAZEL_SKYLIB_TAG,
    url = "https://github.com/bazelbuild/bazel-skylib/archive/%s.tar.gz" % BAZEL_SKYLIB_TAG,
)

RULES_JVM_EXTERNAL_TAG = "4.1"

http_archive(
    name = "rules_jvm_external",
    sha256 = "f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = "5766f1e599acf551aa56f49dab9ab9108269b03c557496c54acaf41f98e2b8d6",
    url = "https://github.com/bazelbuild/rules_kotlin/releases/download/v1.9.0/rules_kotlin-v1.9.0.tar.gz",
)

RULES_SCALA_TAG = "6.6.0"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "e734eef95cf26c0171566bdc24d83bd82bdaf8ca7873bec6ce9b0d524bdaf05d",
    strip_prefix = "rules_scala-%s" % RULES_SCALA_TAG,
    url = "https://github.com/bazelbuild/rules_scala/releases/download/v%s/rules_scala-v%s.tar.gz" % (RULES_SCALA_TAG, RULES_SCALA_TAG),
)

# Third-party
# =========================================================

# Using protobuf 28.3 causes ProtoScalaPBRule to hang. Looking at issues in
# rules_scala, it seems like 25.5 is the newest version of protobuf that
# rules_scala can handle.
# See: https://github.com/bazelbuild/rules_scala/issues/1647
PROTOBUF_TAG = "25.5"

http_archive(
    name = "com_google_protobuf",
    integrity = "sha256-PPfVsXxP8E/p8DgQTp0Mrm2gm4ziccE+RPisafUeTg8=",
    strip_prefix = "protobuf-%s" % PROTOBUF_TAG,
    url = "https://github.com/protocolbuffers/protobuf/releases/download/v%s/protobuf-%s.tar.gz" % (PROTOBUF_TAG, PROTOBUF_TAG),
)

DAGGER_TAG = "2.44.2"

http_archive(
    name = "dagger",
    sha256 = "cbff42063bfce78a08871d5a329476eb38c96af9cf20d21f8b412fee76296181",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    url = "https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG,
)

ROBOLECTRIC_TAG = "4.7.3"

http_archive(
    name = "robolectric",
    sha256 = "97f169d39f19412bdd07fd6c274dcda0a7c8f623f7f00aa5a3b94994fc6f0ec4",
    strip_prefix = "robolectric-bazel-%s" % ROBOLECTRIC_TAG,
    url = "https://github.com/robolectric/robolectric-bazel/archive/%s.tar.gz" % ROBOLECTRIC_TAG,
)

LIBSODIUM_TAG = "1.0.18"

http_archive(
    name = "libsodium",
    build_file = "//bazel:BUILD.libsodium",
    sha256 = "1b72c0cdbc535ce42e14ac15e8fc7c089a3ee9ffe5183399fd77f0f3746ea794",
    strip_prefix = "libsodium-%s" % LIBSODIUM_TAG,
    url = "https://github.com/jedisct1/libsodium/archive/%s.zip" % LIBSODIUM_TAG,
)

OPUS_TAG = "5c94ec3205c30171ffd01056f5b4622b7c0ab54c"

http_archive(
    name = "opus",
    build_file = "//bazel:BUILD.opus",
    sha256 = "09366bf588b02b76bda3fd1428a30b55ca995d6d2eac509a39919f337690329e",
    strip_prefix = "opus-%s" % OPUS_TAG,
    url = "https://github.com/xiph/opus/archive/%s.zip" % OPUS_TAG,
)

LIBVPX_TAG = "3d28ff98039134325cf689d8d08996fc8dabb225"

http_archive(
    name = "libvpx",
    build_file = "//bazel:BUILD.libvpx",
    sha256 = "27d082899b60dea79c596affc68341522db1f72c241f6d6096fc46bcf774f217",
    strip_prefix = "libvpx-%s" % LIBVPX_TAG,
    url = "https://github.com/webmproject/libvpx/archive/%s.zip" % LIBVPX_TAG,
)

# aTox maven dependencies
# =========================================================

load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")
load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = DAGGER_ARTIFACTS + [
        "androidx.activity:activity-ktx:1.2.3",
        "androidx.activity:activity:1.2.3",
        "androidx.annotation:annotation:1.1.0",
        "androidx.appcompat:appcompat:1.3.0",
        "androidx.core:core-ktx:1.5.0",
        "androidx.databinding:databinding-adapters:3.4.2",
        "androidx.databinding:databinding-common:3.4.2",
        "androidx.databinding:databinding-runtime:3.4.2",
        "androidx.fragment:fragment-ktx:1.3.5",
        "androidx.fragment:fragment:1.3.5",
        "androidx.multidex:multidex:2.0.1",
        "androidx.navigation:navigation-fragment-ktx:2.3.5",
        "androidx.navigation:navigation-ui-ktx:2.3.5",
        "androidx.preference:preference:1.1.1",
        "androidx.room:room-compiler:2.2.6",
        "androidx.room:room-ktx:2.2.6",
        "androidx.room:room-runtime:2.2.6",
        "androidx.room:room-testing:2.2.6",
        "androidx.test.ext:junit:1.2.1",
        "com.google.android.material:material:1.4.0",
        "com.google.code.gson:gson:2.8.6",
        "com.google.guava:guava:19.0",
        "com.squareup.picasso:picasso:2.8",
        "com.typesafe.scala-logging:scala-logging_2.11:3.7.2",
        "javax.inject:javax.inject:1",
        "junit:junit:4.13.1",
        "org.jetbrains.kotlin:kotlin-test-junit:1.7.20",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0",
        "org.jetbrains:annotations:13.0",
        "org.slf4j:slf4j-api:1.7.25",
        "org.robolectric:robolectric:4.7.3",
        "org.scala-lang:scala-library:2.11.12",
        "androidx.lifecycle:lifecycle-extensions:2.2.0",
        "androidx.lifecycle:lifecycle-livedata-ktx:2.2.0",
        "androidx.lifecycle:lifecycle-service:2.2.0",
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0",
    ],
    repositories = DAGGER_REPOSITORIES + [
        "https://repo1.maven.org/maven2/",
        "https://dl.google.com/dl/android/maven2/",
    ],
)

# Tox
# =========================================================

local_repository(
    name = "pthread",
    path = "bazel/pthread",
)

local_repository(
    name = "psocket",
    path = "bazel/psocket",
)

JVM_TOXCORE_API_TAG = "c0f37cfd77d79d5826ea566127f60fce838858c2"

http_archive(
    name = "jvm-toxcore-api",
    build_file = "//bazel:BUILD.jvm-toxcore-api",
    sha256 = "ab129f7d845d87e1b6ee0a2b4bc34acede45480dd32a15f85a08e9dfca7cedf6",
    strip_prefix = "jvm-toxcore-api-%s" % JVM_TOXCORE_API_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-api/archive/%s.tar.gz" % JVM_TOXCORE_API_TAG,
)

JVM_TOXCORE_C_TAG = "f697eef5d0a16a025b187c3369288986e89bde2b"

http_archive(
    name = "jvm-toxcore-c",
    build_file = "//bazel:BUILD.jvm-toxcore-c",
    sha256 = "93fb5cd0a1f45561e52cb585287cec98415d80b655d847278aa51c8d26f80124",
    strip_prefix = "jvm-toxcore-c-%s" % JVM_TOXCORE_C_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-c/archive/%s.tar.gz" % JVM_TOXCORE_C_TAG,
)

JVM_MACROS_TAG = "8e8991581bec396861678012cab302ba09ced629"

http_archive(
    name = "jvm-macros",
    build_file = "//bazel:BUILD.jvm-macros",
    sha256 = "3f2e7c024347085596ad3c90d236e0e6fddf5c7c18c03a66a058c4d334f24888",
    strip_prefix = "jvm-macros-%s" % JVM_MACROS_TAG,
    url = "https://github.com/TokTok/jvm-macros/archive/%s.tar.gz" % JVM_MACROS_TAG,
)

C_TOXCORE_TAG = "0.2.12"

http_archive(
    name = "c-toxcore",
    patch_cmds = [
        # Delete references to the "project" stuff that lives in toktok-stack.
        "sed -i /project/d BUILD.bazel",

        # Ignore the "other", "testing", and "auto_tests" bonus content.
        "echo other >.bazelignore",
        "echo testing >>.bazelignore",
        "echo auto_tests >>.bazelignore",

        # Delete references to the "no_undefined" cc_library that lives in toktok-stack.
        "sed -i /no_undefined/d toxencryptsave/BUILD.bazel",
        "sed -i /no_undefined/d toxav/BUILD.bazel",
        "sed -i /no_undefined/d toxcore/BUILD.bazel",

        # Replace toktok-stack selects w/ more standard versions.
        "sed -i 's|//tools/config:linux|@platforms//os:linux|g' toxcore/BUILD.bazel",

        # Replace "//c-toxcore/" w/ "@c-toxcore//" to get internal dependencies between libraries working.
        "sed -i 's|//c-toxcore/|@c-toxcore//|g' BUILD.bazel",
        "sed -i 's|//c-toxcore/|@c-toxcore//|g' toxcore/BUILD.bazel",
        "sed -i 's|//c-toxcore/|@c-toxcore//|g' toxav/BUILD.bazel",
        "sed -i 's|//c-toxcore/|@c-toxcore//|g' toxencryptsave/BUILD.bazel",

        # Fix some "//c-toxcore:" -> "@c-toxcore:" references.
        "sed -i 's|//c-toxcore:|@c-toxcore//:|g' toxcore/BUILD.bazel",
        "sed -i 's|//c-toxcore:|@c-toxcore//:|g' toxav/BUILD.bazel",
        "sed -i 's|//c-toxcore:|@c-toxcore//:|g' toxencryptsave/BUILD.bazel",

        # Flatten the gendir structure to deal with c-toxcore having its own workspace.
        "sed -i 's|$(GENDIR)/c-toxcore/|$(RULEDIR)/|g' BUILD.bazel",
    ],
    sha256 = "6d21fcd8d505e03dcb302f4c94b4b4ef146a2e6b79d4e649f99ce4d9a4c0281f",
    strip_prefix = "c-toxcore-%s" % C_TOXCORE_TAG,
    url = "https://github.com/TokTok/c-toxcore/archive/v%s.zip" % C_TOXCORE_TAG,
)

# Transitive dependencies and toolchain setup
# =========================================================
#
# These go last since we override a bunch of them.

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")

kotlin_repositories(
    compiler_release = kotlinc_version(
        release = "1.9.22",
        sha256 = "88b39213506532c816ff56348c07bbeefe0c8d18943bffbad11063cf97cac3e6",
    ),
)

register_toolchains("//:kotlin_toolchain")

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config(scala_version = "2.11.12")

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
load("@io_bazel_rules_scala//scala_proto:scala_proto.bzl", "scala_proto_repositories")
load("@io_bazel_rules_scala//scala_proto:toolchains.bzl", "scala_proto_register_enable_all_options_toolchain")

scala_register_toolchains()

scala_repositories()

scala_proto_repositories()

scala_proto_register_enable_all_options_toolchain()
