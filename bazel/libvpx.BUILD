load("@rules_cc//cc:defs.bzl", "cc_library")

[genrule(
    name = "%s_rtcd" % mod,
    srcs = [
        "build/make/rtcd.pl",
        "@atox//bazel/libvpx:vpx_config.mk",
        defs,
    ],
    outs = ["%s_rtcd.h" % mod],
    cmd = " ".join([
        "$(location build/make/rtcd.pl)",
        "--arch=arm64",
        "--sym=%s_rtcd" % mod,
        "--disable-avx512",
        "--config=$(location @atox//bazel/libvpx:vpx_config.mk)",
        "$(location %s)" % defs,
        "> $@",
    ]),
) for mod, defs in [
    ("vp8", "vp8/common/rtcd_defs.pl"),
    ("vp9", "vp9/common/vp9_rtcd_defs.pl"),
    ("vpx_dsp", "vpx_dsp/vpx_dsp_rtcd_defs.pl"),
    ("vpx_scale", "vpx_scale/vpx_scale_rtcd.pl"),
]]

cc_library(
    name = "headers",
    hdrs = glob(["**/*.h"]) + [
        "vp8_rtcd.h",
        "vp9_rtcd.h",
        "vpx_dsp_rtcd.h",
        "vpx_scale_rtcd.h",
    ],
    deps = ["@atox//bazel/libvpx:vpx_config"],
)

cc_library(
    name = "libvpx",
    srcs = glob(
        include = ["vp*/**/*.c"],
        exclude = [
            "**/arm/**",
            "**/mips/**",
            "**/ppc/**",
            "**/x86/**",
            "vp8/encoder/mr_dissim.c",
            "vpx_ports/arm_cpudetect.c",
            "vpx_ports/ppc_cpudetect.c",
            "vpx_ports/emms_mmx.c",
        ],
    ),
    copts = [
        "-I$(GENDIR)/external/libvpx",
        "-Iexternal/libvpx",
        "-fvisibility=protected",
        "-Wno-overflow",
    ],
    includes = ["."],
    linkopts = ["-lm"],
    visibility = ["//visibility:public"],
    deps = [
        ":headers",
        "@atox//bazel/libvpx:vpx_config",
        "@atox//bazel/libvpx:vpx_version",
    ],
)
