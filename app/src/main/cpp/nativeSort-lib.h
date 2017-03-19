//
// Created by maximka on 23.10.16.
//

#ifndef NEWSAPP_NATIVESORT_LIB_H
#define NEWSAPP_NATIVESORT_LIB_H

#include <jni.h>

extern "C"
JNIEXPORT jdouble JNICALL
        Java_com_newsapp_maximka_newsapp_sorting_CSort_nativeSortNews(JNIEnv *, jobject, jobjectArray);

#endif //NEWSAPP_NATIVESORT_LIB_H
