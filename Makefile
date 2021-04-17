#
# GNU Make file.
#
#
# Set this if  'make install' should install its files into a
# user directory - useful for package systems that will grab
# all the files they see.  Setting this will allow a package
# to be built without requiring root permissions.
#
DESTDIR :=

JROOT := $(shell while [ ! -d src -a `pwd` != / ] ; do cd .. ; done ; pwd)

include VersionVars.mk

ANIM2D_DIR = /usr/share/doc/libbzdev-doc/api/org/bzdev/anim2d
ANIM2D_DIR_SED = $(shell echo $(ANIM2D_DIR) | sed  s/\\//\\\\\\\\\\//g)
ALT_ANIM2D_DIR = ../../../bzdev/doc/api/org/bzdev/anim2d
ALT_ANIM2D_DIR_SED = $(shell echo $(ALT_ANIM2D_DIR) | sed  s/\\//\\\\\\\\\\//g)

DRAMA_DIR = /usr/share/doc/libbzdev-doc/api/org/bzdev/drama
DRAMA_DIR_SED =  $(shell echo $(DRAMA_DIR) | sed  s/\\//\\\\\\\\\\//g)
ALT_DRAMA_DIR = ../../../bzdev/doc/api/org/bzdev/drama
ALT_DRAMA_DIR_SED = $(shell echo $(ALT_DRAMA_DIR) | sed  s/\\//\\\\\\\\\\//g)


TMPSRC = $(JROOT)/tmpsrc
#
# System directories (that contains JAR files, etc.)
#
SYS_LIBJARDIR = /usr/share/java
SYS_BZDEVDIR = /usr/share/bzdev
SYS_API_DOCDIR = /usr/share/doc/libbikeshr-doc
SYS_JAVADOCS = $(SYS_API_DOCDIR)/api
SYS_EXAMPLES = $(SYS_API_DOCDIR)/examples

EXTDIR = $(SYS_BZDEVDIR)

ALL = jarfile javadocs

all: $(ALL)

# Target for the standard Java extension directory
LIBJARDIR = $(DESTDIR)$(SYS_LIBJARDIR)

LIBJARDIR_SED=$(shell echo $(SYS_LIBJARDIR) | sed  s/\\//\\\\\\\\\\//g)

# Other target directories

API_DOCDIR = $(DESTDIR)$(SYS_API_DOCDIR)
JAVADOCS = $(DESTDIR)$(SYS_JAVADOCS)
EXAMPLES = $(DESTDIR)$(SYS_EXAMPLES)
BZDEVDIR = $(DESTDIR)$(SYS_BZDEVDIR)

JROOT_JARDIR = $(JROOT)/BUILD
JROOT_LIBJARDIR = $(JROOT_JARDIR)

JROOT_JAVADOCS = $(JROOT)/BUILD/api
JROOT_ALT_JAVADOCS = $(JROOT)/BUILD/alt-api
JROOT_EXAMPLES = $(JROOT)/examples

JDOCS = *.html stylesheet.css package-list
RDOCS = *.gif

BZDEV = org/bzdev
BIKESHR_DIR = ./src/org.bzdev.bikeshr

$(JROOT_JARDIR)/libbzdev.jar: $(EXTDIR)/libbzdev.jar
	mkdir -p $(JROOT_JARDIR)
	ln -s $(EXTDIR)/libbzdev.jar $(JROOT_JARDIR)/libbzdev.jar

JFILES = $(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/*.java)

RES1 = $(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/lpack/*.properties)
RESOURCES = $(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/lpack/*.properties) \
	$(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/*.yaml) \
	$(RES1)


include MajorMinor.mk

$(TMPSRC):
	mkdir -p $(TMPSRC)

FILES = $(JFILES) $(JFILES1) $(RESOURCES)

JARFILE = $(JROOT_LIBJARDIR)/libbikeshr.jar
BJARFILE = $(JROOT_JARDIR)/libbikeshr.jar

jarfile: $(JARFILE)

NOF_SERVICE = org.bzdev.obnaming.NamedObjectFactory


BIKESHR_MODINFO = $(BIKESHR_DIR)/module-info.java
BIKESHR_JFILES = $(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/*.java)
BIKESHR_JFILES1 = \
	$(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/*.java)
BTMP1 = $(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/lpack
BIKESHR_RESOURCES1 = \
	$(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/*.yaml) \
	$(wildcard $(BTMP1)/*.properties) \
	$(wildcard $(BIKESHR_DIR)/$(BZDEV)/bikeshare/lpack/*.properties)
BIKESHR_RESOURCES = $(subst ./src/,,$(BIKESHR_RESOURCES1))

JDOC_MODULES = org.bzdev.bikeshr
BSHR_PKG = org.bzdev.bikeshare
JDOC_EXCLUDE = \
	$(BSHR_PKG).lpack:$(BSHR_PKG).provider:$(BSHR_PKG).provider.lpack

LSNOF0= $(SYS_BZDEVDIR)/libbzdev-
LSNOF1 = $(LSNOF0)base.jar:$(LSNOF0)math.jar:$(LSNOF0)obnaming.jar
LSNOF2 = $(LSNOF0)devqsim.jar:$(LSNOF0)drama.jar
LSNOF3 = $(SYS_BZDEVDIR)/lsnof.jar

# need a custom version because librdanim.jar is in $(SYS_BZDEVDIR)
# after the package is installed.
LSNOF = java -p $(LSNOF1):$(LSNOF2):$(LSNOF3) -m org.bzdev.lsnof

DEFAULTCLASS1 = $(BIKESHR_DIR)/$(BZDEV)/bikeshare/lpack/DefaultClass.java
DEFAULTCLASS2 = \
	$(BIKESHR_DIR)/$(BZDEV)/bikeshare/provider/lpack/DefaultClass.java

$(JARFILE): $(FILES)  $(TMPSRC) $(JROOT_JARDIR)/libbzdev.jar \
	    META-INF/services/org.bzdev.obnaming.NamedObjectFactory
	mkdir -p mods/org.bzdev.bikeshr
	mkdir -p BUILD
	javac -d mods/org.bzdev.bikeshr -p $(EXTDIR) \
		-Xlint:deprecation \
		--processor-module-path $(EXTDIR) \
		-s tmpsrc/org.bzdev.bikeshr \
		$(BIKESHR_MODINFO) $(BIKESHR_JFILES) $(BIKESHR_JFILES1) \
		 $(DEFAULTCLASS1) $(DEFAULTCLASS2)
	for i in $(BIKESHR_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	mkdir -p mods/org.bzdev.bikeshr/META-INF/services
	cp META-INF/services/* mods/org.bzdev.bikeshr/META-INF/services
	jar --create --file $(JARFILE) --manifest=$(BIKESHR_DIR)/manifest.mf \
		copyright -C mods/org.bzdev.bikeshr .

clean:
	@[ -d mods ] && rm -rf mods || echo -n
	@[ -d tmpsrc ] && rm -rf tmpsrc || echo -n
	rm -rf BUILD

jdclean:
	@ [ -d BUILD/api ] && rm -rf BUILD/api  || echo -n
	@ [ -d BUILD/alt-api ] && rm -rf BUILD/alt-api || echo -n


DIAGRAMS = $(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/simclasses.png \
	$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/factories1.png \
	$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/factories2.png \
	$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/instrument.png

diagrams: $(DIAGRAMS)

$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/simclasses.png: \
		diagrams/simclasses.dia
	mkdir -p $(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files
	dia -s 700x -e $@ $<

$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/factories1.png: \
		diagrams/factories1.dia
	mkdir -p $(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files
	dia -s 700x -e $@ $<

$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/factories2.png: \
		diagrams/factories2.dia
	mkdir -p $(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files
	dia -s 700x -e $@ $<

$(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files/instrument.png: \
		diagrams/instrument.dia
	mkdir -p $(BIKESHR_DIR)/org/bzdev/bikeshare/doc-files
	dia -s 700x -e $@ $<

javadocs: $(JROOT_JAVADOCS)/index.html

altjavadocs: $(JROOT_ALT_JAVADOCS)/index.html

JAVA_VERSION=11
JAVADOC_LIBS = BUILD/libbikeshr.jar:$(EXTDIR)

$(JROOT_JAVADOCS)/index.html: $(JFILES)	overview.html  $(DIAGRAMS) $(JARFILE)
	mkdir -p $(JROOT_JAVADOCS)
	rm -rf $(JROOT_JAVADOCS)/*
	javadoc -d $(JROOT_JAVADOCS) --module-path $(JAVADOC_LIBS) \
		--module-source-path src:tmpsrc \
		--add-modules org.bzdev.bikeshr \
		-link file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api \
		-link file:///usr/share/doc/libbzdev-doc/api/ \
		-overview overview.html \
		--module $(JDOC_MODULES) -exclude $(JDOC_EXCLUDE)
	$(LSNOF) -d $(JROOT_JAVADOCS) -p $(JROOT_JARDIR) \
	      --link file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api/ \
	      --link file:///usr/share/doc/libbzdev-doc/api/ \
	      --overview src/FactoryOverview.html 'org.bzdev.bikeshare.*'

$(JROOT_ALT_JAVADOCS)/index.html: $(RDANIM_JFILES) overview.html $(JARFILE) \
		$(JROOT_JAVADOCS)/index.html
	mkdir -p $(JROOT_ALT_JAVADOCS)
	rm -rf $(JROOT_ALT_JAVADOCS)/*
	javadoc -d $(JROOT_ALT_JAVADOCS) --module-path $(JAVADOC_LIBS) \
		--module-source-path src:tmpsrc \
		--add-modules org.bzdev.bikeshr \
		-linkoffline ../../../bzdev/doc/api \
			file:///usr/share/doc/libbzdev-doc/api/ \
		-linkoffline \
	   https://docs.oracle.com/en/java/javase/$(JAVA_VERSION)/docs/api/ \
		    file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api \
		-overview overview.html \
		--module $(JDOC_MODULES) -exclude $(JDOC_EXCLUDE)
	lsnof -d $(JROOT_ALT_JAVADOCS) -p $(JARFILE) \
	      --link-offline \
	   https://docs.oracle.com/en/java/javase/$(JAVA_VERSION)/docs/api/ \
			file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api/ \
	      --link-offline ../../../bzdev/doc/api \
			file:///usr/share/doc/libbzdev-doc/api/ \
	      --overview src/FactoryOverview.html 'org.bzdev.bikeshare.*'

# overview.html: overview.tpl
#	sed s/ANIM2D/$(ANIM2D_DIR_SED)/ < overview.tpl \
#	| sed s/DRAMA/$(DRAMA_DIR_SED)/ > overview.html

#altoverview.html: overview.tpl
#	sed s/ANIM2D/$(ALT_ANIM2D_DIR_SED)/ < overview.tpl \
#	| sed s/DRAMA/$(ALT_DRAMA_DIR_SED)/  > altoverview.html

install: install-lib install-links install-docs

install-lib: $(JARFILE)
	install -d $(LIBJARDIR)
	install -m 0644 $(JARFILE) $(LIBJARDIR)/libbikeshr-$(VERSION).jar

install-links:
	rm -f $(LIBJARDIR)/libbikeshr.jar
	ln -s $(LIBJARDIR)/libbikeshr-$(VERSION).jar \
		$(LIBJARDIR)/libbikeshr.jar
	rm -f $(BZDEVDIR)/libbikeshr.jar
	ln -s $(LIBJARDIR)/libbikeshr-$(VERSION).jar \
		$(BZDEVDIR)/libbikeshr.jar

install-docs: javadocs
	install -d $(API_DOCDIR)
	install -d $(JAVADOCS)
	for i in `cd $(JROOT_JAVADOCS); find . -type d -print ` ; \
		do install -d $(JAVADOCS)/$$i ; done
	for i in `cd $(JROOT_JAVADOCS); find . -type f -print ` ; \
		do j=`dirname $$i`; install -m 0644 $(JROOT_JAVADOCS)/$$i \
			$(JAVADOCS)/$$j ; \
		done
	install -d $(EXAMPLES)
	install -m 0644 $(JROOT_EXAMPLES)/example1.js $(EXAMPLES)/example1.js
	install -m 0644 $(JROOT_EXAMPLES)/example2.js $(EXAMPLES)/example2.js

uninstall-lib:
	rm -f $(LIBJARDIR)/libbikeshr-$(VERSION).jar

uninstall-links:
	rm -f $(LIBJARDIR)/libbikeshr.jar $(BZDEVDIR)/libbikeshr.jar

uninstall-docs:
	rm -rf $(JAVADOCS)
	rm -rf $(EXAMPLES)
	rmdir --ignore-fail-on-non-empty $(API_DOCDIR)
