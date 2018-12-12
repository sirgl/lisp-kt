#pragma once

#include "memory/Memory.h"

extern "C" Value __add(unsigned int count, ...);

extern "C" Value r__print(Value value);

extern "C" Value r__printErrorAndExit(Value errorText);

extern "C" Value r__add(Value left, Value right);

extern "C" uint64_t r__untag(Value value);

extern "C" Value r__withElement(Value list, Value element);

extern "C" Value r__createString(char* str);

extern "C" Value r__typeAssert(Value value, uint64_t typeId);