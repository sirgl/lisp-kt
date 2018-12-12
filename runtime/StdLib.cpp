#include "StdLib.h"
#include "memory/List.h"
#include "memory/String.h"


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
//    uint32_t i = v.asInt();
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

extern "C" Value r__withElement(Value list, Value element) {
    ValueType type = list.getType();
    if (type != ValueType::List) {
        printErrorAndExit("withElement: expected list type");
    }
    Object *objRef = list.asObject();
    auto *listRef = dynamic_cast<List*>(objRef);
    List *newList = listRef->withHead(element);
    return Value::fromPtr(newList, ValueType::List);
}

extern "C" Value r__typeAssert(Value value, uint64_t typeId) {
    assert(typeId < 8);
//    Tag tag = typeId;
    return Value(0); // TODO
}

Value r__createString(char *str) {
    String *strPtr = String::allocate(str);
    return Value::fromPtr(strPtr, ValueType::String);
}

Value r__createSymbol(char *str) {
    return Value(0);
}
