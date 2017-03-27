/** Copyright (C) 2009 by Aleksey Surkov.
 **
 ** Permission to use, copy, modify, and distribute this software and its
 ** documentation for any purpose and without fee is hereby granted, provided
 ** that the above copyright notice appear in all copies and that both that
 ** copyright notice and this permission notice appear in supporting
 ** documentation.  This software is provided "as is" without express or
 ** implied warranty.
 */

#ifndef FFT_H_
#define FFT_H_

#include <jni.h>

// Taken from http://www.ddj.com/cpp/199500857
// which took it from Numerical Recipes in C++, p.513

// The 'data' should be an array of length 'size' * 2,
// where each even element corresponds
// to the real part and each odd element to the imaginary part of a
// complex number.
// For an incoming stream, all imaginary parts should be zero.
void Java_com_example_AndroidTuner_PitchDetector_DoFFT(
    JNIEnv* env, jobject thiz, jdoubleArray data, jint size);

jint JNI_OnLoad(JavaVM* vm, void* reserved);

#endif /* FFT_H_ */
