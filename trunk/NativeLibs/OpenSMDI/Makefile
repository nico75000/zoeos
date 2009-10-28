# Makefile of SMDI.DLL for MS VC++

VCPPBASEDIR="C:\Program Files\Microsoft Visual Studio 9.0\VC"
SDK="C:\Program Files\Microsoft SDKs\Windows\v6.0A\Include"

OBJS=
LIBS=wnaspi32.vcpp.lib
BINDIR=bin\win32\vcpp
SMDI_DLL=$(BINDIR)\smdi.dll
SMDI_OBJ=$(BINDIR)\smdi.obj
ASPI_WIN32_OBJ=$(BINDIR)\aspi.win32.obj

SMDI_SRC=aspi.win32.c wnaspi32.c smdi.c

# The compiler
INCDIR=$(VCPPBASEDIR)\include
#CC=$(VCPPBASEDIR)\bin\cl.exe
CC=cl.exe
CFLAGS=/nologo /I$(INCDIR) /I$(SDK) /Zp1 /O1

# The linker
#LNK=$(VCPPBASEDIR)\bin\link.exe
LNK=link.exe
LIBPATH=$(VCPPBASEDIR)\lib
UUIDPATH="C:\Program Files\Microsoft SDKs\Windows\v6.0A\Lib"
LFLAGS=/nologo /dll /libpath:$(LIBPATH) /libpath:$(UUIDPATH)

# --

$(SMDI_DLL): $(ASPI_WIN32_OBJ) $(SMDI_OBJ) $(OBJS) $(LIBS)
	$(LNK) $(LFLAGS) /out:$(SMDI_DLL) $(SMDI_OBJ) $(ASPI_WIN32_OBJ) $(OBJS) $(LIBS)

$(SMDI_OBJ): smdi.c
	$(CC) -c -Fo$(SMDI_OBJ) $(CFLAGS) smdi.c

$(ASPI_WIN32_OBJ): aspi.win32.c
	$(CC) -c -Fo$(ASPI_WIN32_OBJ) $(CFLAGS) aspi.win32.c

clean:
	del $(SMDI_DLL)
	del $(SMDI_OBJ)
	del $(ASPI_WIN32_OBJ)
	del $(BINDIR)\smdi.exp
	del $(BINDIR)\smdi.lib

