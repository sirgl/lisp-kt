#include "StdLib.h"

Value __add(unsigned int count, ...) {
    int accumulator = 0;
    va_list l;
    va_start(l, count);
    for (int i = 0; i < count; ++i) {
        const uint64_t rawValue = va_arg(l, uint64_t);
        Value value(rawValue);
        if (value.getType() != ValueType::Int) {
            printErrorAndExit("add: Expected type int");
        }
        accumulator += value.asInt();
    }
    return Value(static_cast<uint64_t>(accumulator), ValueType::Int);
}
