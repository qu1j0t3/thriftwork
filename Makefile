# Copyright (C) 2011 Toby Thain, toby@telegraphics.com.au

# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by  
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License  
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


# path to your thrift distribution
THRIFT = $(HOME)/Downloads/thrift-0.6.1
THRIFTJAR = $(THRIFT)/lib/java/build/libthrift-0.6.1-snapshot.jar


build/JavaServer.class : src/JavaServer.java gen-java/SimonSays.java
	ant

# the Java needs stripping of 1.6-isms to build with my on my javac 1.5.0_19
gen-java/SimonSays.java : simonsays.thrift
	thrift -r --gen java $<
	sed -i.bak -e /@Override/d -e 's/IOException(te)/IOException()/' $@

run-server : build/JavaServer.class
	java -cp build:$(THRIFTJAR):slf4j-1.6.1/slf4j-api-1.6.1.jar:slf4j-1.6.1/slf4j-simple-1.6.1.jar JavaServer

# the Ruby bindings don't build on my OS X 10.4.11 system
$(THRIFT)/Makefile :
	cd $(THRIFT) && ./configure --without-ruby

$(THRIFTJAR) : $(THRIFT)/Makefile 
	cd $(THRIFT) && make

clean :
	ant clean
	rm -fr gen-java

realclean : clean
	cd $(THRIFT) && make clean
