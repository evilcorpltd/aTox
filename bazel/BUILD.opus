load("@rules_cc//cc:defs.bzl", "cc_library")

cc_library(
    name = "opus",
    srcs = glob(
        include = [
            "celt/*.c",
            "silk/*.c",
            "silk/float/*.c",
            "**/*.h",
        ],
        exclude = ["celt/opus_custom_demo.c"],
    ) + [
        "src/analysis.c",
        "src/mlp.c",
        "src/mlp_data.c",
        "src/opus.c",
        "src/opus_decoder.c",
        "src/opus_encoder.c",
        "src/opus_multistream.c",
        "src/opus_multistream_decoder.c",
        "src/opus_multistream_encoder.c",
        "src/repacketizer.c",
    ],
    hdrs = glob(["include/*.h"]),
    copts = [
        "-DHAVE_CONFIG_H",
        "-I$(GENDIR)/bazel/opus",
    ],
    includes = [
        "celt",
        "include",
        "silk",
        "silk/float",
    ],
    visibility = ["//visibility:public"],
    deps = ["@atox//bazel/opus:config"],
)
