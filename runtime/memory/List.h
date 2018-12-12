#pragma once

#include "Memory.h"
#include "Allocation.h"


class List : public Object {
public:
    Value value;
    List* next;

    List(ValueType type, const Value &value, List *next);

    List* withHead(Value value) {
        List *headObj = allocate(value);
        headObj->next = this;
        return headObj;
    }

    void print() override;

    static List* allocate(Value value);
};

