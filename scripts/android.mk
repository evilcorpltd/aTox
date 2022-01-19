ANDROID_NDK_HOME ?= $(SRCDIR)/$(NDK_DIR)
NDK_HOME := $(ANDROID_NDK_HOME)

DLLEXT		:= .so
TOOLCHAIN	:= $(NDK_HOME)/toolchains/llvm/prebuilt/linux-x86_64
SYSROOT		:= $(TOOLCHAIN)/sysroot
PREFIX		:= $(DESTDIR)/$(TARGET)
TOOLCHAIN_FILE	:= $(SRCDIR)/$(TARGET).cmake
TOOLCHAIN_CLANG_BIN	:= $(TOOLCHAIN)/bin/$(TARGET)$(NDK_API)
TOOLCHAIN_BIN	:= $(TOOLCHAIN)/bin/$(BASE_TARGET)
PROTOC		:= $(DESTDIR)/host/bin/protoc

export CC		:= $(TOOLCHAIN_CLANG_BIN)-clang
export CXX		:= $(TOOLCHAIN_CLANG_BIN)-clang++
export AR 		:= $(TOOLCHAIN_BIN)-ar
export LD 		:= $(CC)
export AS		:= $(TOOLCHAIN_BIN)-as
export STRIP	:= $(TOOLCHAIN_BIN)-strip
export NM		:= $(TOOLCHAIN_BIN)-nm
export LDFLAGS		:= -static-libstdc++ -llog
export PKG_CONFIG_LIBDIR:= $(PREFIX)/lib/pkgconfig
export PKG_CONFIG_PATH	:= $(PREFIX)/lib/pkgconfig
export PATH		:= $(TOOLCHAIN)/bin:$(PATH)
export TOX4J_PLATFORM	:= $(TARGET)

ifeq ($(TARGET),i686-linux-android)
undefine AS
else ifeq ($(TARGET),x86_64-linux-android)
undefine AS
endif

protobuf_CONFIGURE	:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared --with-protoc=$(PROTOC)
libsodium_CONFIGURE	:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared
opus_CONFIGURE		:= --prefix=$(PREFIX) --host=$(TARGET) --with-sysroot=$(SYSROOT) --disable-shared
libvpx_CONFIGURE	:= --prefix=$(PREFIX) --libc=$(SYSROOT) --target=$(VPX_TARGET) --disable-examples --disable-unit-tests --enable-pic
toxcore_CONFIGURE	:= -DCMAKE_INSTALL_PREFIX:PATH=$(PREFIX) -DCMAKE_TOOLCHAIN_FILE=$(TOOLCHAIN_FILE) -DANDROID_CPU_FEATURES=$(NDK_HOME)/sources/android/cpufeatures/cpu-features.c -DENABLE_STATIC=ON -DENABLE_SHARED=OFF
tox4j_CONFIGURE		:= -DCMAKE_INSTALL_PREFIX:PATH=$(PREFIX) -DCMAKE_TOOLCHAIN_FILE=$(TOOLCHAIN_FILE) -DANDROID_CPU_FEATURES=$(NDK_HOME)/sources/android/cpufeatures/cpu-features.c

build: $(PREFIX)/tox4j.stamp $(foreach i,jvm-macros jvm-toxcore-api tox4j-c,$(DESTDIR)/$i.stamp)

test: build
	@echo "No tests for Android builds"

$(NDK_HOME):
	@echo "Downloading NDK..."
	@$(PRE_RULE)
	@mkdir -p $(@D)
	test -f $(NDK_PACKAGE) || curl -s $(NDK_URL) -o $(NDK_PACKAGE)
	7z x $(NDK_PACKAGE) -o$(SRCDIR) > /dev/null
	@$(POST_RULE)

$(TOOLCHAIN_FILE): scripts/android.mk | $(NDK_HOME)
	@$(PRE_RULE)
	mkdir -p $(@D)
	echo 'set(CMAKE_SYSTEM_NAME Linux)' > $@
	echo >> $@
	echo 'set(CMAKE_BUILD_TYPE Release CACHE STRING "")' >> $@
	echo >> $@
	echo 'set(CMAKE_SYSROOT $(SYSROOT))' >> $@
	echo >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH $(PREFIX))' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)' >> $@
	echo 'set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)' >> $@
	@$(POST_RULE)

include scripts/release.mk
