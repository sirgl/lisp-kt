#include <cstring>
#include "String.h"
#include "Allocation.h"


void String::print() {
    std::cout << string;
}

String::String(char *string, uint32_t length) : Object(ValueType::String), string(string), length(length) {}

String* String::allocate(char *str) {
    String *strPtr = new String(str, static_cast<uint32_t>(strlen(str)));
    finishHeapAllocation(strPtr);
    return strPtr;
}
