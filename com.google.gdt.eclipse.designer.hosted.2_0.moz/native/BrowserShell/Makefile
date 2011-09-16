# Makefile for creating SWT libraries for Linux GTK

JAVA_HOME = /usr/lib/jvm/java-6-sun

ARCH_BITS = $(shell getconf LONG_BIT)

# Define the various shared libraries to be build.
WS_PREFIX = gtk
SWT_PREFIX = swt
MOZILLA_PREFIX = gwt-ll

MOZILLA_LIB = lib$(MOZILLA_PREFIX).so

# Do not use pkg-config to get libs because it includes unnecessary dependencies (i.e. pangoxft-1.0)
GTKCFLAGS = `pkg-config --cflags gtk+-2.0`
GTKLIBS = `pkg-config --libs-only-L gtk+-2.0 gthread-2.0` -lgtk-x11-2.0 -lgthread-2.0 -L/usr/X11R6/lib $(XLIB64) -lXtst

ifeq ($(ARCH_BITS),32)
# 32-bit paths on Ubuntu 8.04 x86
#    MOZILLA_DIR = /home/mitin_aa/mozilla/firefox-2.0.0.4/obj-i686-pc-linux-gnu/dist
    MOZILLA_DIR = /usr/lib/xulrunner-devel-1.9.2.13
    MOZILLA_DIR_BIN = $(MOZILLA_DIR)/bin
    MOZILLA_DIR_LIB = $(MOZILLA_DIR)/lib
    MOZILLA_INCLUDE_DIR = $(MOZILLA_DIR)/include
    OUTPUT_DIR=../../os/linux/x86
else
# 64-bit paths on SuSE 10.3 x86_64
#    MOZILLA_DIR = /usr/lib64/xulrunner-1.8.1
#    MOZILLA_DIR_BIN = $(MOZILLA_DIR)
#    MOZILLA_DIR_LIB = $(MOZILLA_DIR)
#    MOZILLA_INCLUDE_DIR = /usr/include/xulrunner-1.8.1

    JAVA_HOME = /usr/lib/jvm/java-6-sun
    MOZILLA_DIR = /usr/lib/xulrunner-devel-1.9.2.12
    MOZILLA_DIR_BIN = $(MOZILLA_DIR)/bin
    MOZILLA_DIR_LIB = $(MOZILLA_DIR)/lib
    MOZILLA_INCLUDE_DIR = $(MOZILLA_DIR)/include
    OUTPUT_DIR=../../os/linux/x86_64

endif

COMMONCFLAGS = \
	-fPIC \
	-O2 \
	-Wall \
	-fno-stack-protector \
	-DWBP_ARCH$(ARCH_BITS)

MOZILLACFLAGS = \
	-DMOZILLA_STRICT_API=1 \
	-DXPCOM_GLUE=1 \
	-fno-rtti \
	-fno-exceptions \
	-fno-stack-protector \
	-fshort-wchar \
	-g \
	-Wno-unused \
	-Wconversion \
	-Wpointer-arith \
	-Wcast-align \
	-Woverloaded-virtual \
	-Wsynth \
	-Wno-ctor-dtor-privacy \
	-Wno-non-virtual-dtor  \
	-I.

JAVACFLAGS = \
	-I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/linux

MOZILLA_INCLUDES = -I$(MOZILLA_INCLUDE_DIR) \
		    -I$(MOZILLA_INCLUDE_DIR)/xpcom \
		    -I$(MOZILLA_INCLUDE_DIR)/string \
		    -I/usr/include/nspr \
		    -I$(MOZILLA_INCLUDE_DIR)/widget \
		    -I$(MOZILLA_INCLUDE_DIR)/js \
		    -I$(MOZILLA_INCLUDE_DIR)/dom \
		    -I$(MOZILLA_INCLUDE_DIR)/xpconnect

MOZILLALIBS = -shared -Wl,--as-needed -Wl,--version-script=mozilla_exports -L$(MOZILLA_DIR_LIB) -L$(MOZILLA_DIR_BIN) -Wl,-rpath-link,$(MOZILLA_DIR_BIN) -lxpcomglue_s -lxpcom -lnspr4

MOZILLA_OBJECTS = ScriptObjectProxy.o LowLevelMoz.o JsValueMoz.o Tracer.o NativeWrapper.o JsRootedValue.o rcp.o

ifndef NO_STRIP
	MOZILLALIBS := $(MOZILLALIBS) -s
endif

all: make_mozilla

#
# Mozilla lib
#
make_mozilla:$(MOZILLA_LIB)

$(MOZILLA_LIB): $(MOZILLA_OBJECTS)
	$(CXX) -o $(MOZILLA_LIB) $(COMMONCFLAGS) $(MOZILLACFLAGS) $(MOZILLA_OBJECTS) $(MOZILLALIBS) $(GTKLIBS)

CLASSDIR := ~/workspace/com.swtdesigner.gwt.moz/bin
JAVAH    := $(JAVA_HOME)/bin/javah

##                                                                                                                                                                                                   
# javah-generated headers for native methods                                                                                                                                                         
##                                                                                                                                                                                                   
#LowLevelMoz.h : $(CLASSDIR)/com/google/gwt/dev/shell/moz/LowLevelMoz.class
#	$(JAVAH) -classpath $(CLASSDIR) -o LowLevelMoz.h com.google.gwt.dev.shell.moz.LowLevelMoz

#JsValueMoz.h : $(CLASSDIR)/com/google/gwt/dev/shell/moz/JsValueMoz.class
#	$(JAVAH) -classpath $(CLASSDIR) -o JsValueMoz.h com.google.gwt.dev.shell.moz.JsValueMoz

ScriptObjectProxy.o: ScriptObjectProxy.cpp
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c ScriptObjectProxy.cpp
LowLevelMoz.o: LowLevelMoz.cpp
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c LowLevelMoz.cpp
JsValueMoz.o:  JsValueMoz.cpp 
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c JsValueMoz.cpp
Tracer.o: Tracer.cpp
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c Tracer.cpp
NativeWrapper.o: NativeWrapper.cpp 
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c NativeWrapper.cpp
JsRootedValue.o: JsRootedValue.cpp
	$(CXX) $(COMMONCFLAGS) $(JAVACFLAGS) $(MOZILLACFLAGS) $(MOZILLA_INCLUDES) -c JsRootedValue.cpp
rcp.o: rcp.c
	$(CC) $(COMMONCFLAGS) $(JAVACFLAGS) $(GTKCFLAGS) -c rcp.c

#
# Install
#
install: all
	cp *.so $(OUTPUT_DIR)

#
# Clean
#
clean:
	rm -f *.o *.so *~
