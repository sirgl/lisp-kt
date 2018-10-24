#pragma once


#include "RootContributor.h"
#include "../Environment.h"

class EnvironmentRootContributor : public RootContributor {
public:
    void contribute(std::function<void(Header*)> consumer) override;

private:
    const Environment& environment;

public:
    explicit EnvironmentRootContributor(Environment &environment);
};

