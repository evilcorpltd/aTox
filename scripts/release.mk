_build/$(TARGET)/tox4j/libtox4j-c$(DLLEXT): $(PREFIX)/tox4j.stamp
	ls -l $@
	touch $@

release: _build/$(TARGET)/tox4j/libtox4j-c$(DLLEXT)
	rm -rf $(wildcard $(SRCDIR)/tox4j/cpp/src/main/resources/im/tox/tox4j/impl/jni/*/)
	mkdir -p $(SRCDIR)/tox4j/cpp/src/main/resources/im/tox/tox4j/impl/jni/$(TOX4J_PLATFORM)/
	cp $< $(SRCDIR)/tox4j/cpp/src/main/resources/im/tox/tox4j/impl/jni/$(TOX4J_PLATFORM)/
	cd $(SRCDIR)/tox4j/cpp && sbt publishM2
