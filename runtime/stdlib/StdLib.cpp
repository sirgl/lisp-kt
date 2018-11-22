#include <cstdarg>
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

Value r__add(Value left, Value right) {
    // TODO typecheck?
    return Value(left.asInt() + right.asInt());
}

uint64_t r__untag(Value value) {
    return value.value & (~(0b111L << 61));
}

Value r__print(Value value) {
    switch (value.getType()) {
        case ValueType::Nil:
            std::cout << "()";
            break;
        case ValueType::List:
            std::cout << "TODO";
            break;
        case ValueType::Symbol:
            // TODO
            break;
        case ValueType::String:
            // TODO
            break;
        case ValueType::Int:
            std::cout << std::to_string(value.asInt());
        case ValueType::Bool:
            if (value.asBool()) {
                std::cout << "#t";
            } else {
                std::cout << "#f";
            }
        case ValueType::Function:
            // TODO extract name from binary?
            std::cout << r__untag(value);
            break;
    }
    return nil;
}

Value r__getType(Value value) {
    // TODO
    return nil;
}

Value r__isList(Value value) {
    bool isList = value.getType() == ValueType::Nil || value.getType() == ValueType::List;
    return Value::fromBool(isList);
}

Value r__printErrorAndExit(Value errorText) {
    // TODO
    return nil;
}

