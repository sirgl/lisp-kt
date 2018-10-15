#pragma once

#include "List.h"
#include "Vector.h"
#include "Function.h"


ListObj* allocateEmptyList();

VectorObj* allocateEmptyVector();

FunctionObj* allocateFunction(LispFunctionPtr function);