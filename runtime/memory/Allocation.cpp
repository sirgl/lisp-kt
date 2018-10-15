//
// Created by jetbrains on 14/10/2018.
//

#include "Allocation.h"

Header* root;
Header* current;

uint64_t allocationCount;
uint64_t deleteCount;

ListObj *allocateEmptyList() {
    Header header(ValueType::List);
    auto *obj = new ListObj(header, nil);
    current = &obj->header;
    allocationCount++;
    return obj;
}


//VectorObj *allocateEmptyVector() {
//    Header header(ValueType::List);
//    auto *obj = new VectorObj(header, nil);
//    current = &obj->header;
//    allocationCount++;
//    return obj;
//}

FunctionObj *allocateFunction(LispFunctionPtr function) {
    Header header(ValueType::Function);
    auto *obj = new FunctionObj(header, function);
    current = &obj->header;
    allocationCount++;
    return nullptr;
}
