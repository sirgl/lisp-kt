//
// Created by jetbrains on 14/10/2018.
//

#include "Symbol.h"

Symbol::Symbol(char *string, uint32_t length) : Object(ValueType::Symbol), string(string), length(length) {}

void Symbol::print() {
    std::cout << string << std::endl;
}
