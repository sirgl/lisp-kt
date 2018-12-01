#pragma once

#include "Memory.h"

struct String : public Object {
    char* string;
    uint32_t length;

public:
    String(char *string, uint32_t length);

    void print() override;
};
