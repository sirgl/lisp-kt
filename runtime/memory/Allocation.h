#pragma once


#include "Memory.h"

void finishHeapAllocation(Object *next);

void initializeGCRoot(Object *root);

uint64_t getAllocationCount();