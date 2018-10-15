//
// Created by jetbrains on 15/10/2018.
//

#include "Environment.h"

void Environment::addFunction(std::string name, LispFunctionPtr function) {
    globalFunctionMap[name] = function;
}

LispFunctionPtr Environment::getFunction(std::string name) {
    return globalFunctionMap[name];
}
