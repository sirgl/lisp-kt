//
// Created by jetbrains on 14/10/2018.
//

#include "Utils.h"

std::string bits(uint64_t num) {
    unsigned int size = sizeof(num);
    auto maxPow = static_cast<unsigned int>(1 << (size * 8 - 1));
    std::string s;
    while(maxPow){
        s.append(num&maxPow ? "1" : "0");
        maxPow >>= 1;
    }
    return s;
}
