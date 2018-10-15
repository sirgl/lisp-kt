#include "Memory.h"

ValueType Value::getType() {
    uint64_t shifted = value >> 61;
    switch (shifted) {
        case 0b000:
            return ValueType::Nil;
        case 0b010:
            return ValueType::List;
        case 0b001:
            return ValueType::Vector;
        case 0b100:
            return ValueType::Symbol;
        case 0b110:
            return ValueType::Int;
        default:
            printErrorAndExit("Unexpected type " + std::to_string(value));
    }
}

Value::Value(uint64_t value, ValueType type) {
    uint8_t prefix = getPrefix(type);
    uint64_t fullPrefix = static_cast<uint64_t >(prefix) << 61;
    this->value = value | fullPrefix;
}

Value::Value(uint64_t value) : value(value){};

uint8_t getPrefix(ValueType type) {
    switch (type) {
        case ValueType::Nil:
            return 0b00000000;
        case ValueType::List:
            return 0b00000010;
        case ValueType::Vector:
            return 0b00000001;
        case ValueType::Symbol:
            return 0b00000100;
        case ValueType::Int:
            return 0b00000110;
        case ValueType::Bool:
            return 0b00000011;
        case ValueType::Byte:
            return 0b00000101;
        case ValueType::Function:
            return 0b00000111;
    }
}
