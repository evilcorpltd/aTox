SRCDIR			:= $(CURDIR)/_git
DESTDIR			:= $(CURDIR)/_install
BUILDDIR		:= $(CURDIR)/_build/$(TARGET)

export CFLAGS		:= -O3 -pipe
export CXXFLAGS		:= -O3 -pipe
export LDFLAGS		:=

export PATH		:= $(DESTDIR)/host/bin:$(PATH)

# Android NDK
NDK_DIR		:= android-ndk-r21
NDK_PACKAGE	:= $(NDK_DIR)-$(shell perl -e 'print $$^O')-x86_64.zip
NDK_URL		:= http://dl.google.com/android/repository/$(NDK_PACKAGE)

NDK_COMMON_FILES :=						\
	sources/android/cpufeatures				\
	toolchains/llvm/prebuilt/linux-x86_64/bin/clang		\
	toolchains/llvm/prebuilt/linux-x86_64/bin/clang++	\
	toolchains/llvm/prebuilt/linux-x86_64/lib/lib64		\
	toolchains/llvm/prebuilt/linux-x86_64/lib64		\
	toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include
