#include "StdLib.h"
#include "memory/List.h"
#include "memory/String.h"

void typeAssert(Value value, ValueType expected) {
    if (value.getType() != expected) {
        printErrorAndExit("Unexpected value type");   
    }
}

extern "C" Value r__add(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    Value value = Value::fromInt(left.asInt() + right.asInt());
    Tag tag = value.getTag();
    return value;
}

extern "C" Value r__sub(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    return Value::fromInt(left.asInt() - right.asInt());
}

extern "C" Value r__mul(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    return Value::fromInt(left.asInt() * right.asInt());
}

extern "C" Value r__div(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    uint32_t rightVal = right.asInt();
    if (rightVal == 0) {
        printErrorAndExit("Division by zero");
    }
    return Value::fromInt(left.asInt() * rightVal);
}

extern "C" Value r__rem(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    uint32_t rightVal = right.asInt();
    if (rightVal == 0) {
        printErrorAndExit("Division by zero");
    }
    return Value::fromInt(left.asInt() % rightVal);
}

extern "C" Value r__gt(Value left, Value right) {
    typeAssert(left, ValueType::Int);
    typeAssert(right, ValueType::Int);
    return Value::fromBool(left.asInt() > right.asInt());
}

extern "C" Value r__eq(Value left, Value right) {
    Tag leftTag = left.getTag();
    Tag rightTag = right.getTag();
    if (leftTag == Tag::Int && rightTag == Tag::Int) {
        return Value::fromBool(left.asInt() == right.asIntUnchecked());
    }
    if (leftTag == Tag::Bool && rightTag == Tag::Bool) {
        return Value::fromBool(left.asBool() == right.asBool());
    }
    printErrorAndExit("Unexpected value type in equality comparison");
}


extern "C" uint64_t r__untag(Value value) {
    return value.value & (~(0b111L << 61));
}

extern "C" Value r__print(Value value) {
    auto v = Value(value);
    v.print();
    return nil;
}

extern "C" Value r__getType(Value value) {
    // TODO
    assert(false);
    return nil;
}

extern "C" Value r__isList(Value value) {
    bool isList = value.getType() == ValueType::List;
    return Value::fromBool(isList);
}

extern "C" Value r__printErrorAndExit(Value errorText) {
    typeAssert(errorText, ValueType::String);
    errorText.asObject()->print();
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
    return Value::fromPtr(newList);
}

Value r__createString(char *str) {
    String *strPtr = String::allocate(str);
    return Value::fromPtr(strPtr);
}

Value r__createSymbol(char *str) {
    // TODO
    assert(false);
    return Value(0);
}

Value r__first(Value list) {
    typeAssert(list, ValueType::List);
    List *pList = dynamic_cast<List*>(list.asObject());
    return pList->value;
}

Value r__tail(Value list) {
    typeAssert(list, ValueType::List);
    List *pList = dynamic_cast<List*>(list.asObject());
    return Value::fromPtr(pList->next);
}

Value r__size(Value list) {
    if (list.getTag() == Tag::Nil) {
        return Value::fromInt(0);
    }
    typeAssert(list, ValueType::List);
    List *pList = dynamic_cast<List*>(list.asObject());
    return Value::fromInt(pList->size());
}

Value r__tagFunction(void *function) {
    auto value = Value::fromFunctionPtr(function);
    auto tag = value.getTag();
    auto converted = value.asPointer();
    assert(converted == function);
    return value;
}
