//
// Created by jetbrains on 14/10/2018.
//

#include "Function.h"

FunctionObj::FunctionObj(Header header, LispFunctionPtr function) : header(header), function(function) {}

FunctionObj *asFunction(Header *header) {
    assert(header->type == ValueType::Function);
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
    return reinterpret_cast<FunctionObj*>(header);
#pragma clang diagnostic pop
}
