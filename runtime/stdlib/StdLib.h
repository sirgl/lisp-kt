#pragma once

#include "../memory/Memory.h"

Value __add(unsigned int count, ...);

Value r__print(Value value);

Value r__add(Value left, Value right);

uint64_t r__untag(Value value);