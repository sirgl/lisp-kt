
#include <assert.h>
#include "Types.h"
#include "../Error.h"

Tag parseTag(uint64_t value) {
    switch (value) {
        case 0:
            return Tag::Nil;
        case 0b001L:
            return Tag::Int;
        case 0b010L:
            return Tag::Bool;
        case 0b100L:
            return Tag::Object;
        case 0b110:
            return Tag::Function;
        default:
            printErrorAndExit("Can't convert to tag");
    }
}

uint8_t getPrefix(Tag type) {
    switch (type) {
        case Tag::Nil:
            return 0;
        case Tag::Function:
            return 0b110;
        case Tag::Bool:
            return 0b010;
        case Tag::Int:
            return 0b001;
        case Tag::Object:
            return 0b100;
        default:
            assert(false);
    }
}

Tag convertToTag(ValueType valueType) {
    switch (valueType) {
        case ValueType::List:
        case ValueType::Symbol:
        case ValueType::String:
            return Tag::Object;
        case ValueType::Int:
            return Tag::Int;
        case ValueType::Bool:
            return Tag::Bool;
        case ValueType::Function:
            return Tag::Function;
        default:
            assert(false);
    }
}