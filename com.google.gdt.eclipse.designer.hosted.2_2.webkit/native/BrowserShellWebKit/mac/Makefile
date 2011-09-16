# Makefile for creating WBP library for Mac (Cocoa & Carbon)

GWT_LIB = libwbp-gwt-webkit.jnilib
BS_LIB_COCOA = libwbp-gwt-webkit-bs-cocoa.jnilib
BS_LIB_CARBON = libwbp-gwt-webkit-bs-carbon.jnilib

OUTPUT_DIR = ../../../os/macosx/x86
OUTPUT_DIR64 = ../../../os/macosx/x86_64
OUTPUT_DIRPPC = ../../../os/macosx/ppc

ARCHS_COCOA = -arch i386 -arch x86_64 -arch ppc
ARCHS_CARBON = -arch i386 -arch ppc

CORE_DIR = ../core
COCOA_DIR = cocoa
COMMON_DIR = common
CARBON_DIR = carbon

CFLAGS = -O2 \
	-Wall \
	-fPIC \
	-g \
	-I. \
	-I$(CORE_DIR) \
	-I$(COCOA_DIR) \
	-I$(CARBON_DIR) \
	-I$(COMMON_DIR) \
	-I/System/Library/Frameworks/JavaVM.framework/Headers \
	-isysroot /Developer/SDKs/MacOSX10.5.sdk \
	-mmacosx-version-min=10.5 \
	-D_WBP_MACOSX
	
LFLAGS_COCOA = -bundle -framework JavaScriptCore -framework Cocoa -framework WebKit -mmacosx-version-min=10.5
LFLAGS_CARBON = $(LFLAGS_COCOA) -framework Carbon -mmacosx-version-min=10.5
	
GWT_OBJECTS = utils.o java-dispatch.o gwt-webkit.o trace.o
BS_OBJECTS_COCOA = utils.o MyFrameLoadAdapter.o NSBrowserShell.o bs-webkit-cocoa.o
BS_OBJECTS_CARBON = utils.o MyFrameLoadAdapter.o TWindow.o TWebWindow.o TCarbonEvent.o bs-webkit-carbon.o

all: make_bs_cocoa make_bs_carbon make_gwt

#
# GWT lib
#
make_gwt:$(GWT_LIB)

$(GWT_LIB): $(GWT_OBJECTS)
	$(CXX) -o $(GWT_LIB) $(ARCHS_COCOA) $(LFLAGS_COCOA) $(GWT_OBJECTS) 

#
# BS Cocoa lib
#
make_bs_cocoa:$(BS_LIB_COCOA)

$(BS_LIB_COCOA): $(BS_OBJECTS_COCOA)
	$(CC) -o $(BS_LIB_COCOA) $(ARCHS_COCOA) $(LFLAGS_COCOA) $(BS_OBJECTS_COCOA)
#
# BS Carbon lib
#
make_bs_carbon:$(BS_LIB_CARBON)

$(BS_LIB_CARBON): $(BS_OBJECTS_CARBON)
	$(CXX) -o $(BS_LIB_CARBON) $(ARCHS_CARBON) $(LFLAGS_CARBON) $(BS_OBJECTS_CARBON)

##
# General coff target.
##
%.o : $(COCOA_DIR)/%.m
	$(CC) $(ARCHS_COCOA) $(CFLAGS) -c $<

%.o : $(COMMON_DIR)/%.m
	$(CC) $(ARCHS_COCOA) $(CFLAGS) -c $<

%.o : $(CARBON_DIR)/%.cp
	$(CXX) $(ARCHS_CARBON) $(CFLAGS) -xobjective-c++ -c $<

%.o : $(CORE_DIR)/%.c
	$(CC) $(ARCHS_COCOA) $(CFLAGS) -c $<

%.o : $(CORE_DIR)/%.cpp
	$(CXX) $(ARCHS_COCOA) $(CFLAGS) -c $<

#
# Install
#
install: all
	mkdir -p $(OUTPUT_DIR)
	cp *.jnilib $(OUTPUT_DIR)
	mkdir -p $(OUTPUT_DIR64)
	cp *.jnilib $(OUTPUT_DIR64)
	mkdir -p $(OUTPUT_DIRPPC)
	cp *.jnilib $(OUTPUT_DIRPPC)
# there is no carbon for 64-bit	
	rm -f $(OUTPUT_DIR64)/$(BS_LIB_CARBON)
#
# Clean
#
clean:
	rm -f *.o *.jnilib
