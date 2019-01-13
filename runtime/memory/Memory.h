#pragma once

#include <iomanip>
#include "../Error.h"
#include "Memory.h"
#include "Types.h"
#include "../utils/Utils.h"
#include <cassert>

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



struct Value {
    uint64_t value;
//    Tag tag;


    ValueType getType();
    
    Tag getTag() {
        return parseTag(value >> 61);
    }

    uint32_t asInt() {
        assert(getType() == ValueType::Int);
        return asIntUnchecked();
    }

    uint32_t asIntUnchecked() {
        return (uint32_t)value & 0xFFFFFFFF;
    }

    bool asBool() {
        assert(getType() == ValueType::Bool);
        return asIntUnchecked() != 0;
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
            return Value(1, Tag::Bool);
        } else {
            return Value(0, Tag::Bool);
        }
    }
    
    static Value fromPtr(void* ptr) {
        if (ptr == nullptr) {
            return Value(0);
        }
        Value resultPtrValue = Value((uint64_t)ptr, Tag::Object);
        return resultPtrValue;
    }

    static Value fromFunctionPtr(void* ptr) {
        if (ptr == nullptr) {
            return Value(0);
        }
        Value resultPtrValue = Value((uint64_t)ptr, Tag::Function);
        return resultPtrValue;
    }

    // Must be aligned to 8
    void* asPointer() {
        Tag tag = getTag();
        assert(tag == Tag::Object || tag == Tag::Function);
        uint64_t mask = ~((uint64_t)0b111 << 61);
        uint64_t resultPtr = value & mask;
        return reinterpret_cast<void *>(resultPtr);
    }

    void print();

    explicit Value(uint64_t value);
    
    Value(uint64_t untagged, Tag tag) {
        uint64_t prefixShifted = (uint64_t)getPrefix(tag) << 61;
        value = untagged | prefixShifted;
    }

    uint64_t untag();

    static Value fromInt(uint32_t i);
};

static const Value nil = Value(0); // NOLINT(cert-err58-cpp)


static uint8_t HEAP_OBJECT_TYPE_MASK = 0b00001110; // 8 different heap types allowed



