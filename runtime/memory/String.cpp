#include "String.h"


void String::print() {
    std::cout << "\"" << string << "\"";
}

String::String(char *string, uint32_t length) : Object(ValueType::String), string(string), length(length) {}
