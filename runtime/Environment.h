#pragma once

#include <string>
#include <map>
#include "memory/Function.h"


// contains all functions
class Environment {
    std::map<std::string, LispFunctionPtr> globalFunctionMap;

public:
    void addFunction(std::string name, LispFunctionPtr function);
    LispFunctionPtr getFunction(std::string name);
};
