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

RULES_PKG_TAG = "0.2.5"

http_archive(
    name = "rules_pkg",
    sha256 = "352c090cc3d3f9a6b4e676cf42a6047c16824959b438895a76c2989c6d7c246a",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (RULES_PKG_TAG, RULES_PKG_TAG),
)

RULES_PROTO_TAG = "97d8af4dc474595af3900dd85cb3a29ad28cc313"

http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-%s" % RULES_PROTO_TAG,
    url = "https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_TAG,
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

RULES_KOTLIN_TAG = "cbc4ced96a6685236c398cc2554fa4abefec1087"

http_archive(
    name = "io_bazel_rules_kotlin",
    patch_args = ["-p1"],
    patches = ["//bazel/io_bazel_rules_kotlin:kotlin-1.5-support.patch"],
    sha256 = "a8512943dc3f779ca39e887b39fbd4ad4771d7aaf446c69a9d352712b72f2c22",
    strip_prefix = "rules_kotlin-%s" % RULES_KOTLIN_TAG,
    url = "https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % RULES_KOTLIN_TAG,
)

RULES_SCALA_TAG = "73c0dbb55d1ab2905c3d97923efc415623f67ac6"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "efde3cb0feafca7c3939b855c0812bff88657fe2d8f893d912d6ca5180e5bf39",
    strip_prefix = "rules_scala-%s" % RULES_SCALA_TAG,
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.tar.gz" % RULES_SCALA_TAG,
)

# Third-party
# =========================================================

PROTOBUF_TAG = "3.17.3"

http_archive(
    name = "com_google_protobuf",
    sha256 = "c6003e1d2e7fefa78a3039f19f383b4f3a61e81be8c19356f85b6461998ad3db",
    strip_prefix = "protobuf-%s" % PROTOBUF_TAG,
    url = "https://github.com/protocolbuffers/protobuf/archive/v%s.tar.gz" % PROTOBUF_TAG,
)

DAGGER_TAG = "2.38.1"

http_archive(
    name = "dagger",
    sha256 = "d20c81fd622f8bbb714239ea3cb7c963e77fc8ec3c88487f912189a9538071e9",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    url = "https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG,
)

ROBOLECTRIC_TAG = "4.4"

http_archive(
    name = "robolectric",
    sha256 = "d4f2eb078a51f4e534ebf5e18b6cd4646d05eae9b362ac40b93831bdf46112c7",
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
        "androidx.test.ext:junit:1.1.2",
        "com.google.android.material:material:1.4.0",
        "com.google.code.gson:gson:2.8.6",
        "com.google.guava:guava:19.0",
        "com.squareup.picasso:picasso:2.8",
        "com.typesafe.scala-logging:scala-logging_2.11:3.7.2",
        "javax.inject:javax.inject:1",
        "junit:junit:4.13.1",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2",
        "org.jetbrains:annotations:13.0",
        "org.slf4j:slf4j-api:1.7.25",
        "org.robolectric:robolectric:4.4",
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

JVM_TOXCORE_API_TAG = "adb835597e1eac8d2ca80b938b4f37d260cfde36"

http_archive(
    name = "jvm-toxcore-api",
    build_file = "//bazel:BUILD.jvm-toxcore-api",
    sha256 = "31f1ccb76ca267ea49f55b7db3a6a7365a3fdbd9e48d46296d8c87f5de036d88",
    strip_prefix = "jvm-toxcore-api-%s" % JVM_TOXCORE_API_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-api/archive/%s.tar.gz" % JVM_TOXCORE_API_TAG,
)

JVM_TOXCORE_C_TAG = "50d9a6b565de348c00daab83575498fdaec853a8"

http_archive(
    name = "jvm-toxcore-c",
    build_file = "//bazel:BUILD.jvm-toxcore-c",
    sha256 = "3928a2ed2aa35e1129f3313d572f05cd38495cd012f095f9bfc812f452718265",
    strip_prefix = "jvm-toxcore-c-%s" % JVM_TOXCORE_C_TAG,
    url = "https://github.com/TokTok/jvm-toxcore-c/archive/%s.tar.gz" % JVM_TOXCORE_C_TAG,
)

JVM_MACROS_TAG = "f22e243a3192b5d808fac3b1135bb6b8cefef6b3"

http_archive(
    name = "jvm-macros",
    build_file = "//bazel:BUILD.jvm-macros",
    sha256 = "e30d9aa3def22606a411adb7fbca80c52f49199724ffbf23ce2d269bad800230",
    strip_prefix = "jvm-macros-%s" % JVM_MACROS_TAG,
    url = "https://github.com/TokTok/jvm-macros/archive/%s.tar.gz" % JVM_MACROS_TAG,
)

C_TOXCORE_TAG = "0.2.12"

http_archive(
    name = "c-toxcore",
    build_file = "//bazel:BUILD.c-toxcore",
    patch_cmds = [
        "echo toxcore/ > .bazelignore",
        "echo toxencryptsave/ >> .bazelignore",
        "echo toxav/ >> .bazelignore",
    ],
    sha256 = "6d21fcd8d505e03dcb302f4c94b4b4ef146a2e6b79d4e649f99ce4d9a4c0281f",
    strip_prefix = "c-toxcore-%s" % C_TOXCORE_TAG,
    url = "https://github.com/TokTok/c-toxcore/archive/v%s.zip" % C_TOXCORE_TAG,
)

# Transitive dependencies and toolchain setup
# =========================================================
#
# These go last since we override a bunch of them.

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kt_register_toolchains()

kotlin_repositories()

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
load("@io_bazel_rules_scala//scala_proto:scala_proto.bzl", "scala_proto_repositories")
load("@io_bazel_rules_scala//scala_proto:toolchains.bzl", "scala_proto_register_enable_all_options_toolchain")

scala_register_toolchains()

scala_repositories()

scala_proto_repositories()

scala_proto_register_enable_all_options_toolchain()
