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

PLATFORMS_TAG = "0.0.10"

# https://github.com/bazelbuild/platforms
http_archive(
    name = "platforms",
    integrity = "sha256-IY7+juc20mo1cmY7N0olPAErcW2K8MB+hC6C8jigp+4=",
    url = "https://github.com/bazelbuild/platforms/releases/download/%s/platforms-%s.tar.gz" % (PLATFORMS_TAG, PLATFORMS_TAG),
)

RULES_PKG_TAG = "1.0.1"

# https://github.com/bazelbuild/rules_pkg
http_archive(
    name = "rules_pkg",
    integrity = "sha256-0gyVGWDtd8t7NBwqWUiFNOSU1a0dMMSBjHNtV3cqn+8=",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (RULES_PKG_TAG, RULES_PKG_TAG),
)

# >=8.6.0 requires a newer version of com_google_protobuf which breaks rules_scala.
RULES_JAVA_TAG = "7.12.3"

# https://github.com/bazelbuild/rules_java
http_archive(
    name = "rules_java",
    integrity = "sha256-wO5g+HV/FAwVf8LHr3A9gZUU3m4CXr9wOG04vdhfzoM=",
    url = "https://github.com/bazelbuild/rules_java/releases/download/%s/rules_java-%s.tar.gz" % (RULES_JAVA_TAG, RULES_JAVA_TAG),
)

# >=7.0.0 requires a newer version of com_google_protobuf which breaks rules_scala.
RULES_PROTO_TAG = "6.0.2"

# https://github.com/bazelbuild/rules_proto
http_archive(
    name = "rules_proto",
    integrity = "sha256-b7Z2fRvvU1MQVH4DJH91GLA0h3QMEbbGrbeVIDP+EpU=",
    strip_prefix = "rules_proto-%s" % RULES_PROTO_TAG,
    url = "https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_TAG,
)

RULES_PYTHON_TAG = "0.40.0"

# https://github.com/bazelbuild/rules_python
http_archive(
    name = "rules_python",
    sha256 = "690e0141724abb568267e003c7b6d9a54925df40c275a870a4d934161dc9dd53",
    strip_prefix = "rules_python-%s" % RULES_PYTHON_TAG,
    url = "https://github.com/bazelbuild/rules_python/releases/download/%s/rules_python-%s.tar.gz" % (RULES_PYTHON_TAG, RULES_PYTHON_TAG),
)

RULES_ANDROID_TAG = "0.1.1"

# https://github.com/bazelbuild/rules_android
http_archive(
    name = "rules_android",
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-%s" % RULES_ANDROID_TAG,
    url = "https://github.com/bazelbuild/rules_android/archive/v%s.zip" % RULES_ANDROID_TAG,
)

STARDOC_TAG = "0.7.2"

# https://github.com/bazelbuild/stardoc
http_archive(
    name = "io_bazel_stardoc",
    integrity = "sha256-Dh7UqY8m5xh3a9ZNBT0CuzTZhXLM0D1ro1URKhIFcGs=",
    url = "https://github.com/bazelbuild/stardoc/releases/download/%s/stardoc-%s.tar.gz" % (STARDOC_TAG, STARDOC_TAG),
)

BAZEL_SKYLIB_TAG = "1.7.1"

# https://github.com/bazelbuild/bazel-skylib
http_archive(
    name = "bazel_skylib",
    integrity = "sha256-vCg8381SalLDIBJ5zaS8KYZS76iYsQtNsIN9xRZSdW8=",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/%s/bazel-skylib-%s.tar.gz" % (BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

RULES_KOTLIN_TAG = "v2.1.0"

# https://github.com/bazelbuild/rules_kotlin
http_archive(
    name = "io_bazel_rules_kotlin",
    integrity = "sha256-3TLxnnPHDzLMuaFmxhXAykrtjifnLEpjMMNSPq+hqlU=",
    url = "https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin-%s.tar.gz" % (RULES_KOTLIN_TAG, RULES_KOTLIN_TAG),
)

RULES_SCALA_TAG = "6.6.0"

# https://github.com/bazelbuild/rules_scala
http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "e734eef95cf26c0171566bdc24d83bd82bdaf8ca7873bec6ce9b0d524bdaf05d",
    strip_prefix = "rules_scala-%s" % RULES_SCALA_TAG,
    url = "https://github.com/bazelbuild/rules_scala/releases/download/v%s/rules_scala-v%s.tar.gz" % (RULES_SCALA_TAG, RULES_SCALA_TAG),
)

RULES_FUZZING_TAG = "0.5.2"

# https://github.com/bazelbuild/rules_fuzzing
http_archive(
    name = "rules_fuzzing",
    integrity = "sha256-5rwhm/rJ4fg7Mn3QkPcoqflz7pm5tdjloYSicy7whiM=",
    strip_prefix = "rules_fuzzing-%s" % RULES_FUZZING_TAG,
    urls = ["https://github.com/bazelbuild/rules_fuzzing/releases/download/v%s/rules_fuzzing-%s.zip" % (RULES_FUZZING_TAG, RULES_FUZZING_TAG)],
)

# Bazel contrib
# =========================================================

RULES_JVM_EXTERNAL_TAG = "5.3"

# https://github.com/bazel-contrib/rules_jvm_external
http_archive(
    name = "rules_jvm_external",
    integrity = "sha256-0x42m4VDIspQmOoSxp1xdd7ZcUNeVcGN2d1fKcxSSaw=",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazel-contrib/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

# https://github.com/bazel-contrib/bazel_features
http_archive(
    name = "bazel_features",
    integrity = "sha256-ixybdVhJgAD1reu8WEt78VtrK/GBRIpm9rL8W0yEIxw=",
    strip_prefix = "bazel_features-1.23.0",
    url = "https://github.com/bazel-contrib/bazel_features/releases/download/v1.23.0/bazel_features-v1.23.0.tar.gz",
)

# Third-party
# =========================================================

# Using protobuf 28.3 causes ProtoScalaPBRule to hang. Looking at issues in
# rules_scala, it seems like 25.5 is the newest version of protobuf that
# rules_scala can handle.
# See: https://github.com/bazelbuild/rules_scala/issues/1647
PROTOBUF_TAG = "25.5"

# https://github.com/protocolbuffers/protobuf
http_archive(
    name = "com_google_protobuf",
    integrity = "sha256-PPfVsXxP8E/p8DgQTp0Mrm2gm4ziccE+RPisafUeTg8=",
    strip_prefix = "protobuf-%s" % PROTOBUF_TAG,
    url = "https://github.com/protocolbuffers/protobuf/releases/download/v%s/protobuf-%s.tar.gz" % (PROTOBUF_TAG, PROTOBUF_TAG),
)

DAGGER_TAG = "2.54"

# https://github.com/google/dagger
http_archive(
    name = "dagger",
    integrity = "sha256-9/0XTvm7D+eyO1DFAPeOGbZM52IWn6epfHFxZY2Cg0w=",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    url = "https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG,
)

ROBOLECTRIC_TAG = "4.7.3"

# https://github.com/robolectric/robolectric-bazel
http_archive(
    name = "robolectric",
    sha256 = "97f169d39f19412bdd07fd6c274dcda0a7c8f623f7f00aa5a3b94994fc6f0ec4",
    strip_prefix = "robolectric-bazel-%s" % ROBOLECTRIC_TAG,
    url = "https://github.com/robolectric/robolectric-bazel/archive/%s.tar.gz" % ROBOLECTRIC_TAG,
)

LIBSODIUM_TAG = "1.0.18-RELEASE"

# https://github.com/jedisct1/libsodium
http_archive(
    name = "libsodium",
    build_file = "//bazel:libsodium.BUILD",
    integrity = "sha256-b1BEkLNCpPikxKAvybhmy++GItXfTlRStGvhIeRmNsE=",
    strip_prefix = "libsodium-%s" % LIBSODIUM_TAG.replace("-RELEASE", ""),
    url = "https://github.com/jedisct1/libsodium/releases/download/%s/libsodium-%s.tar.gz" % (
        LIBSODIUM_TAG,
        LIBSODIUM_TAG.replace("-RELEASE", ""),
    ),
)

OPUS_TAG = "5c94ec3205c30171ffd01056f5b4622b7c0ab54c"

# https://github.com/xiph/opus
http_archive(
    name = "opus",
    build_file = "//bazel:opus.BUILD",
    sha256 = "09366bf588b02b76bda3fd1428a30b55ca995d6d2eac509a39919f337690329e",
    strip_prefix = "opus-%s" % OPUS_TAG,
    url = "https://github.com/xiph/opus/archive/%s.zip" % OPUS_TAG,
)

LIBVPX_TAG = "3d28ff98039134325cf689d8d08996fc8dabb225"

# https://github.com/webmproject/libvpx
http_archive(
    name = "libvpx",
    build_file = "//bazel:libvpx.BUILD",
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
        "androidx.activity:activity:1.7.1",
        "androidx.annotation:annotation:1.1.0",
        "androidx.appcompat:appcompat:1.6.1",
        # TODO(robinlinden): androidx.core:core >1.5.0 causes
        # com.google.devtools.build.android.AndroidFrameworkAttrIdProvider$AttrLookupException: Android attribute not found: lStar
        "androidx.core:core-ktx:1.5.0",
        "androidx.core:core:1.5.0",
        "androidx.databinding:databinding-adapters:3.4.2",
        "androidx.databinding:databinding-common:3.4.2",
        "androidx.databinding:databinding-runtime:8.8.1",
        "androidx.fragment:fragment:1.8.6",
        "androidx.lifecycle:lifecycle-livedata-core:2.8.7",
        "androidx.lifecycle:lifecycle-livedata-ktx:2.8.7",
        "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7",
        "androidx.lifecycle:lifecycle-runtime:2.8.7",
        "androidx.lifecycle:lifecycle-service:2.8.7",
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7",
        "androidx.lifecycle:lifecycle-viewmodel:2.8.7",
        "androidx.navigation:navigation-fragment-ktx:2.8.7",
        "androidx.navigation:navigation-ui-ktx:2.8.7",
        "androidx.preference:preference:1.2.1",
        "androidx.room:room-compiler:2.6.1",
        "androidx.room:room-ktx:2.6.1",
        "androidx.room:room-runtime:2.6.1",
        "androidx.room:room-testing:2.6.1",
        "androidx.test.ext:junit:1.2.1",
        "com.google.android.material:material:1.12.0",
        "com.google.code.gson:gson:2.8.6",
        "com.google.guava:guava:33.4.0-android",
        "com.squareup.picasso:picasso:2.8",
        "com.typesafe.scala-logging:scala-logging_2.11:3.7.2",
        "javax.inject:javax.inject:1",
        "junit:junit:4.13.1",
        "org.jetbrains.kotlin:kotlin-test-junit:1.7.20",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0",
        "org.jetbrains:annotations:13.0",
        "org.robolectric:robolectric:4.7.3",
        "org.scala-lang:scala-library:2.11.12",
        "org.slf4j:slf4j-api:1.7.25",
    ],
    repositories = DAGGER_REPOSITORIES + [
        "https://repo1.maven.org/maven2/",
        "https://dl.google.com/dl/android/maven2/",
    ],
    version_conflict_policy = "pinned",
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

# https://github.com/TokTok/jvm-toxcore-api
http_archive(
    name = "jvm-toxcore-api",
    build_file = "//bazel:jvm-toxcore-api.BUILD",
    sha256 = "ab129f7d845d87e1b6ee0a2b4bc34acede45480dd32a15f85a08e9dfca7cedf6",
    strip_prefix = "jvm-toxcore-api-%s" % JVM_TOXCORE_API_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-api/archive/%s.tar.gz" % JVM_TOXCORE_API_TAG,
)

JVM_TOXCORE_C_TAG = "f697eef5d0a16a025b187c3369288986e89bde2b"

# https://github.com/TokTok/jvm-toxcore-c
http_archive(
    name = "jvm-toxcore-c",
    build_file = "//bazel:jvm-toxcore-c.BUILD",
    sha256 = "93fb5cd0a1f45561e52cb585287cec98415d80b655d847278aa51c8d26f80124",
    strip_prefix = "jvm-toxcore-c-%s" % JVM_TOXCORE_C_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-c/archive/%s.tar.gz" % JVM_TOXCORE_C_TAG,
)

JVM_MACROS_TAG = "8e8991581bec396861678012cab302ba09ced629"

# https://github.com/TokTok/jvm-macros
http_archive(
    name = "jvm-macros",
    build_file = "//bazel:jvm-macros.BUILD",
    sha256 = "3f2e7c024347085596ad3c90d236e0e6fddf5c7c18c03a66a058c4d334f24888",
    strip_prefix = "jvm-macros-%s" % JVM_MACROS_TAG,
    url = "https://github.com/TokTok/jvm-macros/archive/%s.tar.gz" % JVM_MACROS_TAG,
)

C_TOXCORE_TAG = "0.2.20"

# https://github.com/TokTok/c-toxcore
http_archive(
    name = "c-toxcore",
    integrity = "sha256-qciaja6nRdU+XXjnqsuZx7R5LEQApaaccSOPRdYWT0w=",
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
        "sed -i /no_undefined/d third_party/BUILD.bazel",

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
        "sed -i 's|//c-toxcore:|@c-toxcore//:|g' third_party/BUILD.bazel",

        # Flatten the gendir structure to deal with c-toxcore having its own workspace.
        "sed -i 's|$(GENDIR)/c-toxcore/|$(RULEDIR)/|g' BUILD.bazel",
    ],
    strip_prefix = "c-toxcore-%s" % C_TOXCORE_TAG,
    url = "https://github.com/TokTok/c-toxcore/releases/download/v%s/c-toxcore-%s.tar.gz" % (C_TOXCORE_TAG, C_TOXCORE_TAG),
)

# Transitive dependencies and toolchain setup
# =========================================================
#
# These go last since we override a bunch of them.

load("@bazel_features//:deps.bzl", "bazel_features_deps")

bazel_features_deps()

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")

kotlin_repositories(
    compiler_release = kotlinc_version(
        release = "2.1.10",
        sha256 = "c6e9e2636889828e19c8811d5ab890862538c89dc2a3101956dfee3c2a8ba6b1",
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

load("@rules_fuzzing//fuzzing:repositories.bzl", "rules_fuzzing_dependencies")

rules_fuzzing_dependencies()

load("@rules_fuzzing//fuzzing:init.bzl", "rules_fuzzing_init")

rules_fuzzing_init()
