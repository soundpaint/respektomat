# Makefile for Respektomat
#
# Copyright (C) 2019 Jürgen Reuter
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

ROOT_DIR=..
include ../defs.mak

CLASSPATH=$(JAVA_DIR):$(BUILD_DIR)

JAVA_SRC=$(wildcard $(JAVA_DIR)/org/soundpaint/respektomat/*.java)
JAVA_OBJ=$(patsubst $(JAVA_DIR)/%.java,$(BUILD_DIR)/%.class,$(JAVA_SRC))

JAR_OBJ=$(JAR_DIR)/Respektomat.jar

all: obj jar

obj: $(BUILD_DIR) $(JAVA_OBJ)

$(BUILD_DIR):
	mkdir -p $@

$(JAR_DIR):
	mkdir -p $@

jar: $(JAR_OBJ)

$(JAR_OBJ): $(JAR_DIR) $(JAVA_OBJ)
	cd $(BUILD_DIR); \
	#jar -0cvfm $(JAR_DIR)/Respektomat.jar $(JAVA_DIR)/META-INF/MANIFEST.MF .

depend:

tags:
	etags *.java

run: all
	cd $(BUILD_DIR) ; java -jar $(JAR_OBJ)

objclean:
	- rm -rf $(BUILD_DIR)

jarclean:
	- rm -rf $(JAR_DIR)

bkpclean:
	- find $(JAVA_DIR) -name \*~ -exec /bin/rm -f {} \; -print

coreclean:
	rm -f core core.* vgcore.*

clean: objclean jarclean

distclean: clean bkpclean

.SUFFIXES: $(SUFFIXES) .java .class

$(BUILD_DIR)%class: $(JAVA_DIR)%java
	javac -Xlint:all -d $(BUILD_DIR) -classpath $(CLASSPATH) $<

#  Local Variables:
#    coding:utf-8
#    mode:Makefile
#  End:
