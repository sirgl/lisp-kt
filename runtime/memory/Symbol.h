#pragma once


#include "Memory.h"

struct Symbol : Object {
    char* string;
    uint32_t length;

    Symbol(char *string, uint32_t length);

    void print() override;

    static Symbol* allocate(char *string);
};