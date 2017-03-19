

#include "nativeSort-lib.h"

#include <string>
#include <algorithm>
#include <vector>
#include <ctime>

inline int qComparator(const void* a, const void* b)
{
    return ((std::pair<size_t , int>*)b)->second - ((std::pair<size_t , int>*)a)->second;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_newsapp_maximka_newsapp_sorting_CSort_nativeSortNews(JNIEnv *env, jobject instance,
                                                              jobjectArray items) {
    // Array type is SortItem[]
    jclass SortItem = env->FindClass("com/newsapp/maximka/newsapp/models/SortItem");
    jfieldID wordCountId = env->GetFieldID(SortItem, "wordCount", "I");
    size_t numberOfObjects = env->GetArrayLength(items);
    std::pair<size_t , int>* objects = new std::pair<size_t , int>[numberOfObjects];

    for (size_t i = 0; i < numberOfObjects; ++i) {
        jobject object = env->GetObjectArrayElement(items, i);
        objects[i] = std::make_pair(i, env->GetIntField(object, wordCountId));
        env->DeleteLocalRef(object);
    }

    std::clock_t start = std::clock();
    qsort(objects, numberOfObjects, sizeof(std::pair<size_t , int>), qComparator);
    double elapsedTime = (std::clock() - start) * 1000.0 / CLOCKS_PER_SEC;

    jobjectArray buffer = env->NewObjectArray(numberOfObjects, SortItem, NULL);
    // copy to buffer
    for (size_t i = 0; i < numberOfObjects; ++i) {
        jobject object = env->GetObjectArrayElement(items, objects[i].first);
        env->SetObjectArrayElement(buffer, i, object);
        env->DeleteLocalRef(object);
    }
    // copy from buffer to items
    for (size_t i = 0; i < numberOfObjects; ++i) {
        jobject object = env->GetObjectArrayElement(buffer, i);
        env->SetObjectArrayElement(items, i, object);
        env->DeleteLocalRef(object);
    }
    delete[] objects;

    return elapsedTime;
}
