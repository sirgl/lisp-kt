//
// Created by jetbrains on 14/10/2018.
//

#include "Allocation.h"

Object* root;
Object* current;

uint64_t allocationCount;
uint64_t deleteCount;

void finishHeapAllocation(Object *next) {
    current->next = next;
    current = next;
    allocationCount++;
}

void initializeGCRoot(Object *r) {
    assert(root == nullptr);
    root = r;
    allocationCount = 1;
    deleteCount = 0;

}

uint64_t getAllocationCount() {
    return allocationCount;
}

