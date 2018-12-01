#pragma once

#include <cstdint>
#include <iomanip>
#include "../Error.h"
#include "Memory.h"
#include "Types.h"


static uint8_t PASSED_MASK = 0b00000001;

struct Object {
    Object* next;
    // actually, only heap types can be here
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

    explicit Object(ValueType type) : type(type) {
        flags = 0;
        next = nullptr;
    }

    virtual ~Object() = default;

    virtual void print() = 0;
};


uint8_t getPrefix(ValueType type);

struct Value {
    uint64_t value;
    Tag tag;


    ValueType getType();
    
    Tag getTag() {
        return parseTag(value >> 61);
    }

    uint32_t asInt() {
        assert(getType() == ValueType::Int);
        return (uint32_t)value & 0xFFFFFFFF;
    }

    bool asBool() {
        assert(getType() == ValueType::Bool);
        return asInt() != 0;
    }

    Object* asObject() {
        Tag tag = getTag();
        assert(tag == Tag::Object || tag == Tag::Nil);
        if (tag == Tag::Nil) {
            return nullptr;
        }
        return (Object*)asPointer();
    }

    static Value fromBool(bool value) {
        if (value) {
            return Value(1);
        } else {
            return Value(0);
        }
    }
    
    static Value fromPtr(void* ptr, ValueType type) {
        return Value(getPrefix(type) | (uint64_t)ptr);
    }

    // Must be aligned to 8
    void* asPointer() {
        ValueType type = getType();
        assert(type == ValueType::List || type ==  ValueType::Function || type == ValueType::Symbol);
        return reinterpret_cast<void *>(value << 3);
    }

    void print();

    Value(uint64_t value, Tag tag);

    explicit Value(uint64_t value);

    uint64_t untag();
};

static const Value nil = Value(0, Tag::Nil); // NOLINT(cert-err58-cpp)


static uint8_t HEAP_OBJECT_TYPE_MASK = 0b00001110; // 8 different heap types allowed



