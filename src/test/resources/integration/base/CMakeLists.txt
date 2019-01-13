cmake_minimum_required(VERSION 3.10)
project(runtime)
enable_language(C CXX ASM)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base)

set(library_src
    "/home/roman/IdeaProjects/lisp-kt/runtime/Error.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Error.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Gc.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Gc.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/main.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/StdLib.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/StdLib.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/gc/RootContributor.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Allocation.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Allocation.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/List.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/List.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Memory.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Memory.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/String.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/String.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Symbol.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Symbol.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Types.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Types.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/utils/Utils.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/utils/Utils.h"
)

set(SRC_FILES
    /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base/src/test/resources/integration/base/Main.lisp.S
    /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base/src/main/resources/stdlib.lisp.S
)

add_executable(runtime ${SRC_FILES} ${library_src})