#pragma once

#include "memory/Memory.h"

extern "C" Value r__print(Value value);

extern "C" Value r__printErrorAndExit(Value errorText);

extern "C" uint64_t r__untag(Value value);

extern "C" Value r__withElement(Value list, Value element);

extern "C" Value r__createString(char* str);

extern "C" Value r__createSymbol(char* str);

extern "C" Value r__add(Value left, Value right);

extern "C" Value r__sub(Value left, Value right);

extern "C" Value r__mul(Value left, Value right);

extern "C" Value r__div(Value left, Value right);

extern "C" Value r__rem(Value left, Value right);

extern "C" Value r__gt(Value left, Value right);

extern "C" Value r__eq(Value left, Value right);

extern "C" Value r__or(Value left, Value right);

extern "C" Value r__and(Value left, Value right);

extern "C" Value r__first(Value list);

extern "C" Value r__tail(Value list);

extern "C" Value r__size(Value list);

extern "C" Value r__tagFunction(void* function);
