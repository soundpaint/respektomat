# Makefile for Respektomat
#
# Copyright (C) 2019 Jürgen Reuter
#
# This program is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <https://www.gnu.org/licenses/>.
#
# For updates and more info or contacting the author, visit:
# <https://github.com/soundpaint/art-frame>
#
# Author's web site: www.juergen-reuter.de

ROOT_DIR=..
include ../defs.mak

SRC_TXT=$(wildcard *.txt)
OBJ_TXT=$(patsubst %,$(BUILD_DIR)/%,$(SRC_TXT))
OBJ=$(OBJ_TXT)

all: $(OBJ)

$(BUILD_DIR):
	mkdir -p $@

$(BUILD_DIR)/%.txt: %.txt | $(BUILD_DIR)
	cp -pf $< $@

objclean:
	rm -f $(OBJ)

bkpclean:
	rm -f *~

coreclean:
	rm -f core core.* vgcore.*

clean: objclean

distclean: clean bkpclean

#  Local Variables:
#    coding:utf-8
#    mode:Makefile
#  End:
