Refactoring 1.2

# Introduction #

Refactoring of the directory structure and the build system for pcmsmdi.

# Details #

The library was in quite a state with multiple copies of files all over the place. To improve the situation I I tried to put everything in its correct place and remove any duplicate files I could find. Also changed the pcmsmdi build system so it now uses a Makefile. I also checked the entire code and libraries into SVN, this will allow for changes to be tracked should changes be made to the source code.