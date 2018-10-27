cmake_minimum_required(VERSION 3.12)

project(runtime)

set(CMAKE_CXX_STANDARD 14)

add_executable(runtime main.cpp memory/Memory.cpp memory/Memory.h Gc.cpp Gc.h Error.h Error.cpp
        StackWalker.cpp StackWalker.h Runtime.cpp Runtime.h utils/Utils.cpp utils/Utils.h
        memory/Symbol.cpp memory/Symbol.h
        memory/List.h memory/List.cpp memory/Vector.cpp memory/Vector.h memory/Function.cpp memory/Function.h
        memory/Allocation.cpp memory/Allocation.h Environment.cpp Environment.h stdlib/StdLib.cpp stdlib/StdLib.h gc/RootContributor.h gc/EnvironmentRootContributor.cpp gc/EnvironmentRootContributor.h)