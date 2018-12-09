#include <iostream>
#include "memory/Allocation.h"
#include "memory/Symbol.h"



extern "C" {
    uint64_t main__init();
}

// TODO
void __entry__() {
//    __main__();
    std::cout <<
    main__init()
    << std::endl;
}

void initRoot() {
    char *rootText = const_cast<char *>("__ROOT__");
    auto *root = new Symbol(rootText, static_cast<uint32_t>(strlen(rootText)));
    initializeGCRoot(root);
}

int main() {
    initRoot();
    __entry__();
    return 0;
}