#pragma once

#include <cstdint>
#include "../Error.h"


enum class ValueType {
    Nil,
    List,
    Vector,
    Symbol,
    Int,
    Bool,
    Byte,
    Function
};

inline uint8_t getPrefix(ValueType type);

struct Value {
    uint64_t value;


    ValueType getType();

    uint32_t asInt() {
        assert(getType() == ValueType::Int);
        return (uint32_t)value & 0xFFFFFFFF;
    }

    // Must be aligned to 8
    void* asPointer() {
        ValueType type = getType();
        assert(type == ValueType::List || type ==  ValueType::Function || type == ValueType::Vector || type == ValueType::Symbol);
        return reinterpret_cast<void *>(value << 3);
    }

    explicit Value(uint64_t value, ValueType type);
    explicit Value(uint64_t value);
};

static const Value nil = Value(0, ValueType::Nil); // NOLINT(cert-err58-cpp)



struct Header {
    Header* next;
    // actually, only heap types are
    ValueType type;

    explicit Header(ValueType type) : type(type) {
        next = nullptr;
    }
};

