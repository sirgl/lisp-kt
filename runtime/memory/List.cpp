//
// Created by jetbrains on 14/10/2018.
//

#include "List.h"

ListObj::ListObj(const Header &header, const Value &value) : header(header), value(value) {
    next = nullptr;
    last = nullptr;
}

ListObj *asList(Header *header) {
    assert(header->type == ValueType::List);
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
    return reinterpret_cast<ListObj*>(header);
#pragma clang diagnostic pop
}
