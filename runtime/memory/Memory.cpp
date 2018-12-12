#include "Memory.h"
#include "../utils/Utils.h"

ValueType Value::getType() {
    Tag tag = getTag();
    switch (tag) {
        case Tag::Nil:
            return ValueType::List;
        case Tag::Function:
            return ValueType::Function;
        case Tag::Bool:
            return ValueType::Bool;
        case Tag::Int:
            return ValueType::Int;
        case Tag::Object:
            return this->asObject()->type;
    }
}


Value::Value(uint64_t value) : value(value){}

uint64_t Value::untag() {
    return value & (~(0b111L << 61));
}


template<typename I>
std::string intAsHex(I w, size_t hex_len = sizeof(I)) {
    static const char* digits = "0123456789ABCDEF";
    std::string rc(hex_len,'0');
    for (size_t i=0, j=(hex_len-1)*4 ; i<hex_len; ++i,j-=4)
        rc[i] = digits[(w>>j) & 0x0f];
    return rc;
}


void Value::print() {
    Tag tag = getTag();
    switch (tag) {
        case Tag::Nil:
            std::cout << "()";
            break;
        case Tag::Function:
            std::cout << "Compiled function 0x" << intAsHex(untag());
            break;
        case Tag::Bool: {
            bool val = asBool();
            if (val) {
                std::cout << "#t";
            } else {
                std::cout << "#f";
            }
        }
            break;
        case Tag::Int:
            std::cout << asInt();
            break;
        case Tag::Object: {
            Object *obj = asObject();
            obj->print();
        }
        break;
    }
}

Value Value::fromInt(uint32_t i) {
    return Value(i, Tag::Int);
}