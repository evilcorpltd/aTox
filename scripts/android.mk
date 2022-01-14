
ifndef ANDROID_NDK_HOME
ANDROID_NDK_HOME := $(SRCDIR)/$(NDK_DIR)
else

NDK_COMMON_FILES_MISSING := $(shell $(foreach f,$(NDK_COMMON_FILES),test -e "$(ANDROID_NDK_HOME)/$f" || echo "$f";))

ifneq ($(NDK_COMMON_FILES_MISSING),)
NDK_INVALID := true
ANDROID_NDK_HOME := $(SRCDIR)/$(NDK_DIR)
endif

endif

DLLEXT		:= .so
TOOLCHAIN	:= $(ANDROID_NDK_HOME)/toolchains/llvm/prebuilt/linux-x86_64
SYSROOT		:= $(TOOLCHAIN)/sysroot
PREFIX		:= $(DESTDIR)/$(TARGET)
TOOLCHAIN_FILE	:= $(SRCDIR)/$(TARGET).cmake
PROTOC		:= $(DESTDIR)/host/bin/protoc

export CC		:= $(TOOLCHAIN)/bin/$(TARGET)$(NDK_API)-clang
export CXX		:= $(TOOLCHAIN)/bin/$(TARGET)$(NDK_API)-clang++
export LDFLAGS		:= -static-libstdc++ -llog
export PKG_CONFIG_LIBDIR:= $(PREFIX)/lib/pkgconfig
export PKG_CONFIG_PATH	:= $(PREFIX)/lib/pkgconfig
export PATH		:= $(TOOLCHAIN)/bin:$(PATH)
export TOX4J_PLATFORM	:= $(TARGET)

protobuf_CONFIGURE	:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared --with-protoc=$(PROTOC)
libsodium_CONFIGURE	:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared
opus_CONFIGURE		:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared
libvpx_CONFIGURE	:= --prefix=$(PREFIX) --sdk-path=$(ANDROID_NDK_HOME) --libc=$(SYSROOT) --target=$(VPX_ARCH) --disable-examples --disable-unit-tests --enable-pic
toxcore_CONFIGURE	:= -DCMAKE_INSTALL_PREFIX:PATH=$(PREFIX) -DCMAKE_TOOLCHAIN_FILE=$(TOOLCHAIN_FILE) -DANDROID_CPU_FEATURES=$(ANDROID_NDK_HOME)/sources/android/cpufeatures/cpu-features.c -DENABLE_STATIC=ON -DENABLE_SHARED=OFF
tox4j_CONFIGURE		:= -DCMAKE_INSTALL_PREFIX:PATH=$(PREFIX) -DCMAKE_TOOLCHAIN_FILE=$(TOOLCHAIN_FILE) -DANDROID_CPU_FEATURES=$(ANDROID_NDK_HOME)/sources/android/cpufeatures/cpu-features.c

build: $(PREFIX)/tox4j.stamp $(foreach i,jvm-macros jvm-toxcore-api tox4j-c,$(DESTDIR)/$i.stamp)

test: build
	@echo "No tests for Android builds"

.PHONY: echo_ndk_was_invalid
echo_ndk_was_invalid:
	@if [ "$(NDK_INVALID)" = "true" ]; then \
		echo "NDK configured with ANDROID_NDK_HOME is invalid. Using NDK in source directory."; \
	fi

$(TOOLCHAIN_FILE): scripts/android.mk | echo_ndk_was_invalid $(ANDROID_NDK_HOME)
	@$(PRE_RULE)
	mkdir -p $(TOOLCHAIN)/bin
	ln -f $(CC) $(TOOLCHAIN)/bin/$(VPX_TARGET)-gcc
	ln -f $(CXX) $(TOOLCHAIN)/bin/$(VPX_TARGET)-g++
	mkdir -p $(@D)
	echo 'set(CMAKE_SYSTEM_NAME Linux)' > $@
	echo >> $@
	echo 'set(CMAKE_BUILD_TYPE Release CACHE STRING "")' >> $@
	echo >> $@
	echo 'set(CMAKE_SYSROOT $(SYSROOT))' >> $@
	echo >> $@
	echo 'set(CMAKE_C_COMPILER $(TOOLCHAIN)/bin/$(VPX_TARGET)-gcc)' >> $@
	echo 'set(CMAKE_CXX_COMPILER $(TOOLCHAIN)/bin/$(VPX_TARGET)-g++)' >> $@
	echo >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH $(PREFIX))' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)' >> $@
	@$(POST_RULE)

$(ANDROID_NDK_HOME):
	@echo "Downloading NDK to source directory."
	@$(PRE_RULE)
	@mkdir -p $(@D)
	test -f $(NDK_PACKAGE) || curl -s $(NDK_URL) -o $(NDK_PACKAGE)
	7z x $(NDK_PACKAGE) -o$(SRCDIR) > /dev/null
	@$(POST_RULE)

include scripts/release.mk
