/** Copyright (C) 2009 by Aleksey Surkov.
 **
 ** Permission to use, copy, modify, and distribute this software and its
 ** documentation for any purpose and without fee is hereby granted, provided
 ** that the above copyright notice appear in all copies and that both that
 ** copyright notice and this permission notice appear in supporting
 ** documentation.  This software is provided "as is" without express or
 ** implied warranty.
 */

#include <math.h>
#include <android/log.h>

#include "fft.h"

#define LOGV(v) \
  __android_log_write(ANDROID_LOG_VERBOSE, "fft-jni" , (v))

#define LOGE(v) \
  __android_log_write(ANDROID_LOG_ERROR, "fft-jni", (v))

template<class T> inline void swap(T &x, T&y) {
	T z;
	z = x; x = y; y = z;
}

// Taken from http://www.ddj.com/cpp/199500857
// which took it from Numerical Recipes in C++, p.513
void DoFFTInternal(jdouble* data, jint nn) {
	unsigned long n, mmax, m, j, istep, i;
	jdouble wtemp, wr, wpr, wpi, wi, theta;
	jdouble tempr, tempi;

	// reverse-binary reindexing
	n = nn<<1;
	j=1;
	for (i=1; i<n; i+=2) {
		if (j>i) {
			swap(data[j-1], data[i-1]);
			swap(data[j], data[i]);
		}
		m = nn;
		while (m>=2 && j>m) {
			j -= m;
			m >>= 1;
		}
		j += m;
	};


	// here begins the Danielson-Lanczos section
	mmax=2;
	while (n>mmax) {
		istep = mmax<<1;
		theta = -(2*M_PI/mmax);
		wtemp = sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = sin(theta);
		wr = 1.0;
		wi = 0.0;
		for (m=1; m < mmax; m += 2) {
			for (i=m; i <= n; i += istep) {
				j=i+mmax;
				tempr = wr*data[j-1] - wi*data[j];
				tempi = wr * data[j] + wi*data[j-1];


				data[j-1] = data[i-1] - tempr;
				data[j] = data[i] - tempi;
				data[i-1] += tempr;
				data[i] += tempi;
			}
			wtemp=wr;
			wr += wr*wpr - wi*wpi;
			wi += wi*wpr + wtemp*wpi;
		}
		mmax=istep;
	}
}

int jniRegisterNativeMethods(JNIEnv* env, const char* className,
    const JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    LOGV("Registering natives:");
    LOGV(className);
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class:");
        LOGE(className);
        return -1;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for:");
        LOGE(className);
        return -1;
    }
    LOGV("Successfully registered natives.");
    return 0;
}

static JNINativeMethod gMethods[] = {
    {"DoFFT", "([DI)V", (void *)Java_com_example_AndroidTuner_PitchDetector_DoFFT},
};

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    jniRegisterNativeMethods(env, "com/example/AndroidTuner/PitchDetector", gMethods, 1);
    return JNI_VERSION_1_4;
}

void Java_com_example_AndroidTuner_PitchDetector_DoFFT(
            JNIEnv* env,
	        jobject thiz,
            jdoubleArray data,
            jint size) {
  jdouble *source_data = env->GetDoubleArrayElements(data, JNI_FALSE);
  DoFFTInternal(source_data, size);
}
