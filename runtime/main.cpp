#include <iostream>
#include <cstring>
#include "memory/Allocation.h"
#include "memory/Symbol.h"



extern "C" {
    uint64_t main__init();
}

// TODO
void __entry__() {
//    __main__();
    uint64_t i = main__init();
    std::cout << std::endl << "result = "  <<  i << std::endl;

}

void initRoot() {
    char *rootText = const_cast<char *>("__ROOT__");
    size_t i = strlen(rootText);
    auto *root = new Symbol(rootText, static_cast<uint32_t>(i));
    initializeGCRoot(root);
}

int main() {
    initRoot();
    __entry__();
    return 0;
}