PRE_RULE = (echo "=== Building $@ ==="; ls -ld $@; true) && ls -ld $+
POST_RULE = ls -ld $@

#############################################################################
# jvm-sbt-plugins

# HEAD as of 2022-01-13
$(SRCDIR)/jvm-sbt-plugins:
	git clone https://github.com/toktok/jvm-sbt-plugins $@
	cd $@ && git checkout 4104763df188d237c21a372f0aa2cf6ef92cdf92

$(DESTDIR)/jvm-sbt-plugins.stamp: $(SRCDIR)/jvm-sbt-plugins
	@$(PRE_RULE)
	cd $< && sbt publishLocal
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# jvm-macros

# HEAD as of 2022-01-13
$(SRCDIR)/jvm-macros:
	git clone https://github.com/toktok/jvm-macros $@
	cd $@ && git checkout 8e8991581bec396861678012cab302ba09ced629

$(DESTDIR)/jvm-macros.stamp: $(SRCDIR)/jvm-macros $(DESTDIR)/jvm-sbt-plugins.stamp
	@$(PRE_RULE)
	cd $< && sbt publishLocal
	cd $< && sbt publishM2
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# jvm-toxcore-api

# HEAD as of 2022-01-13
$(SRCDIR)/jvm-toxcore-api:
	git clone https://github.com/toktok/jvm-toxcore-api $@
	cd $@ && git checkout c0f37cfd77d79d5826ea566127f60fce838858c2

$(DESTDIR)/jvm-toxcore-api.stamp: $(SRCDIR)/jvm-toxcore-api $(DESTDIR)/jvm-sbt-plugins.stamp
	@$(PRE_RULE)
	cd $< && sbt publishLocal
	cd $< && sbt publishM2
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# tox4j

# HEAD as of 2022-01-13
$(SRCDIR)/tox4j:
	git clone https://github.com/toktok/jvm-toxcore-c $@
	cd $@ && git checkout 959e226809e4446c97679f090ef5df905bf627a4

$(BUILDDIR)/tox4j/Makefile: $(SRCDIR)/tox4j $(TOOLCHAIN_FILE) $(foreach i,protobuf toxcore,$(PREFIX)/$i.stamp)
	@$(PRE_RULE)
	mkdir -p $(@D)
	cd $(@D) && cmake $</cpp $($(notdir $(@D))_CONFIGURE)
	@$(POST_RULE)

$(PREFIX)/tox4j.stamp: $(BUILDDIR)/tox4j/Makefile
	@$(PRE_RULE)
	$(MAKE) -C $(<D) install
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

$(DESTDIR)/tox4j-c.stamp: $(SRCDIR)/tox4j $(foreach i,jvm-toxcore-api jvm-macros jvm-sbt-plugins,$(DESTDIR)/$i.stamp)
	@$(PRE_RULE)
	cd $< && sbt publishM2
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# protobuf

$(SRCDIR)/protobuf:
	git clone --depth=1 --branch=v3.19.3 https://github.com/google/protobuf $@

$(PREFIX)/protobuf.stamp: $(SRCDIR)/protobuf $(TOOLCHAIN_FILE) $(PROTOC)
	@$(PRE_RULE)
	cd $< && autoreconf -fi
	mkdir -p $(BUILDDIR)/$(notdir $<)
	cd $(BUILDDIR)/$(notdir $<) && $(SRCDIR)/$(notdir $<)/configure $($(notdir $<)_CONFIGURE)
	$(MAKE) -C $(BUILDDIR)/$(notdir $<) install V=0
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# toxcore

$(SRCDIR)/toxcore:
	git clone --depth=1 --branch=v0.2.13 https://github.com/TokTok/c-toxcore $@;

$(PREFIX)/toxcore.stamp: $(foreach f,$(shell cd $(SRCDIR)/toxcore && git ls-files),$(SRCDIR)/toxcore/$f)
$(PREFIX)/toxcore.stamp: $(SRCDIR)/toxcore $(TOOLCHAIN_FILE) $(foreach i,libsodium opus libvpx,$(PREFIX)/$i.stamp)
	@$(PRE_RULE)
	mkdir -p $(BUILDDIR)/$(notdir $<)
	cd $(BUILDDIR)/$(notdir $<) && cmake $(SRCDIR)/$(notdir $<) $($(notdir $<)_CONFIGURE) -DMUST_BUILD_TOXAV=ON -DBOOTSTRAP_DAEMON=OFF
	$(MAKE) -C $(BUILDDIR)/$(notdir $<) install
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# libsodium

$(SRCDIR)/libsodium:
	git clone --depth=1 --branch=1.0.18 https://github.com/jedisct1/libsodium $@

$(PREFIX)/libsodium.stamp: $(SRCDIR)/libsodium $(TOOLCHAIN_FILE)
	@$(PRE_RULE)
	cd $< && autoreconf -fi
	mkdir -p $(BUILDDIR)/$(notdir $<)
	cd $(BUILDDIR)/$(notdir $<) && $(SRCDIR)/$(notdir $<)/configure $($(notdir $<)_CONFIGURE)
	$(MAKE) -C $(BUILDDIR)/$(notdir $<) install V=0
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# opus

$(SRCDIR)/opus:
	git clone --depth=1 --branch=v1.3.1 https://github.com/xiph/opus $@

$(PREFIX)/opus.stamp: $(SRCDIR)/opus $(TOOLCHAIN_FILE)
	@$(PRE_RULE)
	cd $< && autoreconf -fi
	mkdir -p $(BUILDDIR)/$(notdir $<)
	cd $(BUILDDIR)/$(notdir $<) && $(SRCDIR)/$(notdir $<)/configure $($(notdir $<)_CONFIGURE)
	$(MAKE) -C $(BUILDDIR)/$(notdir $<) install V=0
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)

#############################################################################
# libvpx

$(SRCDIR)/libvpx:
	git clone --depth=1 --branch=v1.11.0 https://github.com/webmproject/libvpx $@
	cd $@ && patch -p1 < $(CURDIR)/scripts/patches/libvpx.patch

$(PREFIX)/libvpx.stamp: $(SRCDIR)/libvpx $(TOOLCHAIN_FILE)
	@$(PRE_RULE)
	mkdir -p $(BUILDDIR)/$(notdir $<)
	cd $(BUILDDIR)/$(notdir $<) && $(SRCDIR)/$(notdir $<)/configure $($(notdir $<)_CONFIGURE)
	$(MAKE) -C $(BUILDDIR)/$(notdir $<) install
	mkdir -p $(@D) && touch $@
	@$(POST_RULE)
