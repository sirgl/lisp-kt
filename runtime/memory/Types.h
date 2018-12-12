#pragma once


#include <cstdint>

enum class Tag {
    Nil,
    Function,
    Bool,
    Int,
    Object,
};

Tag parseTag(uint64_t value);


enum class ValueType {
    List,
    Symbol,
    String,
    Int,
    Bool,
    Function
};


uint8_t getPrefix(Tag type);

Tag convertToTag(ValueType valueType);
