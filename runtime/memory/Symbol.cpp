//
// Created by jetbrains on 14/10/2018.
//

#include <cstring>
#include "Symbol.h"
#include "Allocation.h"

Symbol::Symbol(char *string, uint32_t length) : Object(ValueType::Symbol), string(string), length(length) {}

void Symbol::print() {
    std::cout << string << std::endl;
}

Symbol *Symbol::allocate(char *string) {
    auto *symbol = new Symbol(string, static_cast<uint32_t>(strlen(string)));
    finishHeapAllocation(symbol);
    return symbol;
}
