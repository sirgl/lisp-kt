//
// Created by jetbrains on 14/10/2018.
//

#include "Allocation.h"

Object* root;
Object* current;

uint64_t allocationCount;
uint64_t deleteCount;

//List *allocateListObj() {
//    auto *list = new List(ValueType::List, nil, nil);
//    current->next = list;
//    current = list;
//    allocationCount++;
//    return list;
//}


//
//FunctionObj *allocateFunction(LispFunctionPtr function) {
//    Object header(ValueType::Function);
//    auto *obj = new FunctionObj(header, function);
//    current = &obj->header;
//    allocationCount++;
//    return nullptr;
//}
void finishHeapAllocation(Object *next) {
    current->next = next;
    current = next;
    allocationCount++;
}
