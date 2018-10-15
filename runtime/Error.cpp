#include "Error.h"

noreturn void printErrorAndExit(const std::string &errorText) {
    std::cerr << errorText << std::endl;
    exit(1);
}
