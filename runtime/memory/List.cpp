//
// Created by jetbrains on 14/10/2018.
//

#include "List.h"

void List::print() {
    std::cout << "(";
    List* current = this;
    while (current != nullptr) {
        current->value.print();
        current = current->next;
        if (current != nullptr) {
            std::cout << " ";
        }
    }
    std::cout << ")";
}

List::List(ValueType type, const Value &value, List *next) : Object(type), value(value), next(next) {}

List *List::allocate(Value value) {
    List *list = new List(ValueType::List, value, nullptr);
    finishHeapAllocation(list);
    return list;
}
