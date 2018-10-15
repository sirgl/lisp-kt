#pragma once

#include "Memory.h"

struct VectorObj {
    Header header;
    unsigned int size;
    ValueType elementType;
//    uint32_t values;

    bool isString();

    // TODO access to elements, size
};

VectorObj* asVector(Header* header);