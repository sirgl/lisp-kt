#include <iostream>
#include <cstring>
#include "memory/Allocation.h"
#include "memory/Symbol.h"

extern "C" {
    extern Value __entry__();
}

void initRoot() {
    char *rootText = const_cast<char *>("__ROOT__");
    size_t i = strlen(rootText);
    auto *root = new Symbol(rootText, static_cast<uint32_t>(i));
    initializeGCRoot(root);
}

int main() {
    initRoot();
    Value result = __entry__();
    std::cout << std::endl;
    result.print();
    return 0;
}