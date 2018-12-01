#include <iostream>
//#include "memory/Memory.h"
//#include "utils/Utils.h"
//#include "memory/Allocation.h"
//#include "stdlib/StdLib.h"


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

int main() {
//    // TODO add functions from stdlib to env
//    Environment env = Environment();
//    env.addFunction("+", __add);
////    entry();
////    allocateFunction(nullptr);
//    auto v1 = Value(1, ValueType::Int);
//    auto v2 = Value(4, ValueType::Int);
//    auto v3 = Value(3, ValueType::Int);
//    std::cout << __add(3, v1.value, v2.value, v3.value).asInt() << std::endl;

    __entry__();
    return 0;
}