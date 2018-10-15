#pragma once

#include "Memory.h"

struct ListEntry {
    Value value;
    ListEntry* next;
};

struct ListObj {
    Header header;
    Value value;
    ListObj* next;
    ListObj* last;

    ListObj(const Header &header, const Value &value);

    void add(Value value) {
        if (last == nullptr) {
            // TODO
        }
    }
};

ListObj* asList(Header* header);