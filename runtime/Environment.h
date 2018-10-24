#pragma once

#include <string>
#include <map>
#include "memory/Function.h"


// contains all functions
class Environment {
    std::map<std::string, LispFunctionPtr> globalFunctionMap;

    std::map<std::string, Header*> globals;

public:
    void add

    void addFunction(std::string name, LispFunctionPtr function);

    LispFunctionPtr getFunction(std::string name);
    const std::map<std::string, LispFunctionPtr>& getAllFunctions() const {
        return globalFunctionMap;
    }
};
