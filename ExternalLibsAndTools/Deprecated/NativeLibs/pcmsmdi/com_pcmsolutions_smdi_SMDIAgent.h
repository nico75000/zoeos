/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_pcmsolutions_smdi_SMDIAgent */

#ifndef _Included_com_pcmsolutions_smdi_SMDIAgent
#define _Included_com_pcmsolutions_smdi_SMDIAgent
#ifdef __cplusplus
extern "C" {
#endif
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIM_ENDOFPROCEDURE
#define com_pcmsolutions_smdi_SMDIAgent_SMDIM_ENDOFPROCEDURE 17039360L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIM_ERROR
#define com_pcmsolutions_smdi_SMDIAgent_SMDIM_ERROR 0L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIM_SAMPLEHEADER
#define com_pcmsolutions_smdi_SMDIAgent_SMDIM_SAMPLEHEADER 18939904L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIE_OUTOFRANGE
#define com_pcmsolutions_smdi_SMDIAgent_SMDIE_OUTOFRANGE 2097152L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIE_NOSAMPLE
#define com_pcmsolutions_smdi_SMDIAgent_SMDIE_NOSAMPLE 2097154L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIE_NOMEMORY
#define com_pcmsolutions_smdi_SMDIAgent_SMDIE_NOMEMORY 2097156L
#undef com_pcmsolutions_smdi_SMDIAgent_SMDIE_UNSUPPSAMBITS
#define com_pcmsolutions_smdi_SMDIAgent_SMDIE_UNSUPPSAMBITS 2097158L
#undef com_pcmsolutions_smdi_SMDIAgent_FE_OPENERROR
#define com_pcmsolutions_smdi_SMDIAgent_FE_OPENERROR 65537L
#undef com_pcmsolutions_smdi_SMDIAgent_FE_UNKNOWNFORMAT
#define com_pcmsolutions_smdi_SMDIAgent_FE_UNKNOWNFORMAT 65538L
#undef com_pcmsolutions_smdi_SMDIAgent_FT_WAV
#define com_pcmsolutions_smdi_SMDIAgent_FT_WAV 1L
/* Inaccessible static: MAX_ID */
/* Inaccessible static: numAdapters */
/* Inaccessible static: versionStr */
/* Inaccessible static: version */
/* Inaccessible static: deviceInfos */
/* Inaccessible static: deviceCouplings */
/* Inaccessible static: smdiListeners */
/* Inaccessible static: pcl */
/* Inaccessible static: class_00024com_00024pcmsolutions_00024smdi_00024SMDIAgent */
/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    smdiGetVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiGetVersion
  (JNIEnv *, jclass);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    smdiInit
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiInit
  (JNIEnv *, jclass);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    smdiGetDeviceInfo
 * Signature: (BBLcom/pcmsolutions/smdi/Impl_ScsiDeviceInfo;)V
 */
JNIEXPORT void JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiGetDeviceInfo
  (JNIEnv *, jclass, jbyte, jbyte, jobject);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    smdiSendFile
 * Signature: (BBILjava/lang/String;Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiSendFile
  (JNIEnv *, jclass, jbyte, jbyte, jint, jstring, jstring, jboolean);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    smdiRecvFile
 * Signature: (BBILjava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiRecvFile
  (JNIEnv *, jclass, jbyte, jbyte, jint, jstring, jboolean);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    getSampleHeader
 * Signature: (BBILcom/pcmsolutions/smdi/Impl_SmdiSampleHeader;)I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_getSampleHeader
  (JNIEnv *, jclass, jbyte, jbyte, jint, jobject);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    getFileSampleHeader
 * Signature: (Ljava/lang/String;Lcom/pcmsolutions/smdi/Impl_SmdiSampleHeader;)I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_getFileSampleHeader
  (JNIEnv *, jclass, jstring, jobject);

/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    masterIdentify
 * Signature: (BB)V
 */
JNIEXPORT void JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_masterIdentify
  (JNIEnv *, jclass, jbyte, jbyte);

#ifdef __cplusplus
}
#endif
#endif
