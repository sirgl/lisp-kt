#include "EnvironmentRootContributor.h"

void EnvironmentRootContributor::contribute(std::function<void(Header*)> consumer) {
    for (const auto& nameToFunction : environment.getAllFunctions()) {
        std::string name = nameToFunction.first;
        nameToFunction.second
    }
}

EnvironmentRootContributor::EnvironmentRootContributor(Environment &environment) : environment(environment) {}
