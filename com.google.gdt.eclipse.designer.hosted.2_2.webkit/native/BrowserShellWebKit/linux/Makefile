# Makefile for creating WBP library for Linux GTK
JAVA_HOME = /usr/lib/jvm/java-7-openjdk-i386
# determine arch bits
ARCH_BITS = $(shell getconf LONG_BIT)

WBP_LIB = libwbp-gwt-webkit.so
BS_LIB = libwbp-gwt-webkit-bs.so

OUTPUT_DIR = ../../../os/linux/x86$(shell ./arch_dir.sh)

CORE_DIR = ../core

# Do not use pkg-config to get libs because it includes unnecessary dependencies (i.e. pangoxft-1.0)
GTKCFLAGS = `pkg-config --cflags gtk+-2.0`
GTKLIBS = `pkg-config --libs-only-L gtk+-2.0 gthread-2.0` -lgtk-x11-2.0 -L/usr/X11R6/lib $(XLIB64)
WEBKITCFLAGS = `pkg-config --cflags webkit-1.0`
WEBKITLIBS = `pkg-config --libs webkit-1.0`

DEFINES = -DWBP_ARCH$(ARCH_BITS)

CFLAGS = -O2 \
	-Wall \
	-fPIC \
	-fno-stack-protector \
	-g \
	-I. \
	-I$(CORE_DIR) \
	-I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/linux
	
LIBS = -shared -Wl,--as-needed
	
WBP_OBJECTS = java-dispatch.o gwt-webkit.o trace.o utils.o
BS_OBJECTS = bs-webkit.o utils.o

ifndef NO_STRIP
	LIBS := $(LIBS) -s
endif

all: make_bs make_wbp

#
# WBP lib
#
make_wbp:$(WBP_LIB)

$(WBP_LIB): $(WBP_OBJECTS)
	$(CC) -o $(WBP_LIB) $(GTKCFLAGS) $(CFLAGS) $(WBP_OBJECTS) $(GTKLIBS) $(WEBKITLIBS) $(LIBS)

#
# BS lib
#
make_bs:$(BS_LIB)

$(BS_LIB): $(BS_OBJECTS)
	$(CC) -o $(BS_LIB) $(GTKCFLAGS) $(CFLAGS) $(BS_OBJECTS) $(GTKLIBS) $(WEBKITLIBS) $(LIBS)

##
# General coff target.
##
%.o : %.c
	$(CC) $(GTKCFLAGS) $(WEBKITCFLAGS) $(CFLAGS) $(DEFINES) -c $<

%.o : $(CORE_DIR)/%.c
	$(CC) $(GTKCFLAGS) $(WEBKITCFLAGS) $(CFLAGS) $(DEFINES) -c $<

%.o : $(CORE_DIR)/%.cpp
	$(CXX) $(GTKCFLAGS) $(WEBKITCFLAGS) $(CFLAGS) $(DEFINES) -c $<

#
# Install
#
install: all
	mkdir -p $(OUTPUT_DIR)
	cp *.so $(OUTPUT_DIR)

#
# Clean
#
clean:
	rm -f *.o *.so
