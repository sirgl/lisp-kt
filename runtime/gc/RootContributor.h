#pragma once

#include "../memory/Memory.h"

class RootContributor {
public:
    virtual void contribute(std::function<void(Header*)> consumer) = 0;
};