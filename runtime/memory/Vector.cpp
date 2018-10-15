//
// Created by jetbrains on 14/10/2018.
//

#include "Vector.h"

bool VectorObj::isString() {
    return elementType == ValueType::Byte;
}


VectorObj *asVector(Header *header) {
    assert(header->type == ValueType::Vector);
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
    return reinterpret_cast<VectorObj *>(header);
#pragma clang diagnostic pop
}
