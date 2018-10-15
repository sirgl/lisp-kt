#pragma once

#include "Memory.h"


typedef Value (*LispFunctionPtr)(unsigned int, ...);

struct FunctionObj {
    Header header;
    LispFunctionPtr function;

        FunctionObj(Header header, LispFunctionPtr);
};

FunctionObj* asFunction(Header* header);
