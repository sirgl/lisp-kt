
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