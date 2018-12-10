#include "StdLib.h"
#include "../memory/List.h"



extern "C" Value r__add(Value left, Value right) {
    // TODO typecheck?
    return Value(left.asInt() + right.asInt());
}

extern "C" uint64_t r__untag(Value value) {
    return value.value & (~(0b111L << 61));
}

extern "C" Value r__print(Value value) {
//    value.print();
    auto v = Value(value);
    uint32_t i = v.asInt();
    v.print();
    return nil;
}

extern "C" Value r__getType(Value value) {
    // TODO
    return nil;
}

extern "C" Value r__isList(Value value) {
    bool isList = value.getType() == ValueType::List;
    return Value::fromBool(isList);
}

extern "C" Value r__printErrorAndExit(Value errorText) {
    // TODO
//    std::cout << errorText.
    exit(1);
}

extern "C" Value r__withElement(Value value) {
    ValueType type = value.getType();
    if (!(type == ValueType::List)) {
        printErrorAndExit("withElement: expected list type");
    }
    auto *list = dynamic_cast<List*>(value.asObject());
    return Value::fromPtr(list->withHead(value), ValueType::List);
}

extern "C" Value r__typeAssert(Value value, uint64_t typeId) {
    assert(typeId < 8);
//    Tag tag = typeId;
    return Value(0); // TODO
}