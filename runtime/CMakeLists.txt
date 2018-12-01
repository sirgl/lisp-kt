cmake_minimum_required(VERSION 3.12)

project(runtime)
enable_language(CXX ASM)

set(CMAKE_CXX_STANDARD 14)

add_executable(runtime main.cpp memory/Memory.cpp memory/Memory.h Gc.cpp Gc.h Error.h Error.cpp
        utils/Utils.cpp utils/Utils.h
        memory/Symbol.cpp memory/Symbol.h
        memory/List.h memory/List.cpp
        memory/Allocation.cpp memory/Allocation.h stdlib/StdLib.cpp stdlib/StdLib.h
        gc/RootContributor.h
        asm.s
        memory/String.cpp memory/String.h memory/Types.h memory/Types.cpp)