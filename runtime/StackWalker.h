//
// Created by jetbrains on 14/10/2018.
//

#ifndef RUNTIME_STACKWALKER_H
#define RUNTIME_STACKWALKER_H


#include <functional>
#include "memory/Memory.h"

class StackWalker {
public:
    // Walks stack until main function (from runtime)
    void walkStack(std::function<void(Value)> consumer);
};


#endif //RUNTIME_STACKWALKER_H
