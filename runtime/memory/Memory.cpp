#include "Memory.h"
#include "../utils/Utils.h"

ValueType Value::getType() {
    return ValueType::Int;
    // TODO now not only tag should be considered, but heap object examination required
//    uint64_t shifted = value >> 61;
//    switch (shifted) {
//        case 0b000:
//            return ValueType::Nil;
//        case 0b010:
//            return ValueType::List;
//        case 0b100:
//            return ValueType::Symbol;
//        case 0b110:
//            return ValueType::Int;
//        default:
//            printErrorAndExit("Unexpected type " + std::to_string(value));
//    }
}

//Value::Value(uint64_t value, ValueType type) {
//    uint8_t prefix = getPrefix(type);
//    uint64_t fullPrefix = static_cast<uint64_t >(prefix) << 61;
//    this->value = value | fullPrefix;
//}

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
    switch (getTag()) {
        case Tag::Nil:
            std::cout << "()";
            break;
        case Tag::Function:
            std::cout << "Compiled function 0x" << intAsHex(untag());
            break;
        case Tag::Bool:
            std::cout << asBool();
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
    return;
//    std::cout << std::endl;
}

Value::Value(uint64_t value, Tag tag) : value(value) {}



uint8_t getPrefix(ValueType type) {
    switch (type) {
//        TODO
//        case ValueType::Nil:
//            return 0b00000000;
//        case ValueType::List:
//            return 0b00000010;
//        case ValueType::Vector:
//            return 0b00000001;
//        case ValueType::Symbol:
//            return 0b00000100;
//        case ValueType::Int:
//            return 0b00000110;
//        case ValueType::Bool:
//            return 0b00000011;
//        case ValueType::Byte:
//            return 0b00000101;
//        case ValueType::Function:
//            return 0b00000111;
    }
    return 0;
}
