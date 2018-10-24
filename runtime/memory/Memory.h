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



static uint8_t PASSED_MASK = 0b00000001;

struct Header {
    Header* next;
    // actually, only heap types are
    ValueType type;
    uint8_t flags;

    inline bool isPassed() {
        return flags & PASSED_MASK;
    }

    inline void setPassed(bool passed = true) {
        if (passed) {
            flags |= PASSED_MASK;
        } else {
            flags &= PASSED_MASK;
        }
    }

    explicit Header(ValueType type) : type(type) {
        flags = 0;
        next = nullptr;
    }
};

