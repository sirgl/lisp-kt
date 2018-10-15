#pragma once

#include "Memory.h"
#include "Vector.h"

struct Symbol {
    Header header;
    VectorObj* name; // must hold only byte as elementType
};
