cmake_minimum_required(VERSION 3.12)

project(runtime)
enable_language(C CXX ASM)

set(CMAKE_CXX_STANDARD 14)

set(SRC_FILES Main.lisp.S main.cpp memory/Memory.cpp memory/Memory.h Gc.cpp Gc.h Error.h Error.cpp
        utils/Utils.cpp utils/Utils.h
        memory/Symbol.cpp memory/Symbol.h
        memory/List.h memory/List.cpp
        memory/Allocation.cpp memory/Allocation.h StdLib.cpp StdLib.h
        gc/RootContributor.h
        memory/String.cpp memory/String.h memory/Types.h memory/Types.cpp)

add_executable(runtime ${SRC_FILES})