#include "windows.h"
#include "com_pcmsolutions_smdi_SMDIAgent.h"
#include "stdio.h"
#include "smdidynamic.h"

HINSTANCE hSmdiDll;
BOOL SMDI_Initialized = FALSE;

BOOL WINAPI DllMain(HANDLE hModule, 
                      DWORD  ul_reason_for_call, 
                      LPVOID lpReserved)
{
    switch( ul_reason_for_call ) {
	    case DLL_PROCESS_ATTACH:
			
			hSmdiDll = LoadLibrary ( "smdi.dll" ); 
			if (hSmdiDll == NULL) 
			{ 
				printf ("Couldn't open smdi.dll !\nError code: %d\n",GetLastError()); 
				return(FALSE); 
			}

			SMDI_Init = (unsigned char (*) (void))GetProcAddress ( hSmdiDll, "SMDI_Init" ); 
			if (SMDI_Init == NULL) SMDI_Init = (unsigned char (*) (void))GetProcAddress ( hSmdiDll, "_SMDI_Init" );

			SMDI_SendFile = (unsigned long (*) ( SMDI_FileTransfer * ))GetProcAddress(hSmdiDll, "SMDI_SendFile");
			if (SMDI_SendFile == NULL) SMDI_SendFile = (unsigned long (*) ( SMDI_FileTransfer * ))GetProcAddress(hSmdiDll, "_SMDI_SendFile");

			SMDI_ReceiveFile = (unsigned long (*) ( SMDI_FileTransfer * ))GetProcAddress(hSmdiDll, "SMDI_ReceiveFile");
			if (SMDI_ReceiveFile == NULL) SMDI_ReceiveFile = (unsigned long (*) ( SMDI_FileTransfer * ))GetProcAddress(hSmdiDll, "_SMDI_ReceiveFile");

			SMDI_GetDeviceInfo = (void (*) ( unsigned char, unsigned char, SCSI_DevInfo* ))GetProcAddress(hSmdiDll, "SMDI_GetDeviceInfo");
			if (SMDI_GetDeviceInfo == NULL) SMDI_GetDeviceInfo = (void (*) ( unsigned char, unsigned char, SCSI_DevInfo* ))GetProcAddress(hSmdiDll, "_SMDI_GetDeviceInfo");
	
			SMDI_GetVersion = (unsigned long (*) (void))GetProcAddress(hSmdiDll, "SMDI_GetVersion");
			if (SMDI_GetVersion == NULL) SMDI_GetVersion = (unsigned long (*) (void))GetProcAddress(hSmdiDll, "_SMDI_GetVersion");

			SMDI_SampleHeaderRequest = (unsigned long (*) ( unsigned char, unsigned char, unsigned long, SMDI_SampleHeader* ))GetProcAddress(hSmdiDll, "SMDI_SampleHeaderRequest");
			if (SMDI_SampleHeaderRequest == NULL) SMDI_SampleHeaderRequest = (unsigned long (*) ( unsigned char, unsigned char, unsigned long, SMDI_SampleHeader* ))GetProcAddress(hSmdiDll, "_SMDI_SampleHeaderRequest");

			SMDI_GetFileSampleHeader = (unsigned long (*) ( char*, SMDI_SampleHeader* ))GetProcAddress(hSmdiDll, "SMDI_GetFileSampleHeader");
			if (SMDI_GetFileSampleHeader == NULL) SMDI_GetFileSampleHeader = (unsigned long (*) ( char*, SMDI_SampleHeader* ))GetProcAddress(hSmdiDll, "_SMDI_GetFileSampleHeader");

			SMDI_MasterIdentify = (unsigned long (*) ( unsigned char, unsigned char ))GetProcAddress(hSmdiDll, "SMDI_MasterIdentify");
			if (SMDI_MasterIdentify == NULL) SMDI_MasterIdentify = (unsigned long (*) ( unsigned char, unsigned char ))GetProcAddress(hSmdiDll, "_SMDI_MasterIdentify");

			break;
		case DLL_THREAD_ATTACH:
			break;
		case DLL_THREAD_DETACH:
			break;
		case DLL_PROCESS_DETACH:
			FreeLibrary ( &hSmdiDll );
			break;
		}
    return TRUE;
}


/*
(unsigned long (*SMDI_GetVersion) (void))
(unsigned char (* SMDI_Init) (void))
(void (*SMDI_GetDeviceInfo) ( unsigned char, unsigned char, SCSI_DevInfo* ))
(unsigned long (*SMDI_SampleHeaderRequest) ( unsigned char,unsigned char, unsigned long, SMDI_SampleHeader * ))
(unsigned long (*SMDI_SendSampleHeader) ( unsigned char,unsigned char, unsigned long, SMDI_SampleHeader *, unsigned long * ))
(unsigned long (*SMDI_SendBeginSampleTransfer) ( unsigned char,unsigned char, unsigned long, void * ))
(unsigned long (*SMDI_SendDataPacket) ( unsigned char,unsigned char, unsigned long, void *, unsigned long, unsigned long ))
(unsigned long (*SMDI_NextDataPacketRequest) ( unsigned char,unsigned char, unsigned long, void *, unsigned long ))
(unsigned long (*SMDI_DeleteSample) ( unsigned char, unsigned char, unsigned long ))
(BOOL (*SMDI_TestUnitReady) ( unsigned char, unsigned char ))
(unsigned long (*SMDI_GetMessage) ( unsigned char, unsigned char ))
(unsigned long (*SMDI_SampleTransmission) ( SMDI_TransmissionInfo * ))
(unsigned long (*SMDI_InitSampleTransmission) ( SMDI_TransmissionInfo * ))
(unsigned long (*SMDI_InitSampleReception) ( SMDI_TransmissionInfo * ))
(unsigned long (*SMDI_SampleReception) ( SMDI_TransmissionInfo * ))
(unsigned long (*SMDI_GetFileSampleHeader) ( char *, SMDI_SampleHeader * ))
(unsigned long (*SMDI_InitFileSampleTransmission) ( SMDI_FileTransmissionInfo * ))
(unsigned long (*SMDI_FileSampleTransmission) ( SMDI_FileTransmissionInfo * ))
(unsigned long (*SMDI_InitFileSampleReception) ( SMDI_FileTransmissionInfo * ))
(unsigned long (*SMDI_FileSampleReception) ( SMDI_FileTransmissionInfo * ))
(unsigned long (*SMDI_SendFile) ( SMDI_FileTransfer * ))
(unsigned long (*SMDI_ReceiveFile) ( SMDI_FileTransfer * ))
(unsigned long (*SMDI_MasterIdentify) ( unsigned char, unsigned char ))
*/


JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiGetVersion
  (JNIEnv * env, jobject obj)
{
	return SMDI_GetVersion();
}


/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    masterIdentify
 * Signature: (BB)V
 */
JNIEXPORT void JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_masterIdentify
(JNIEnv * env, jobject obj, jbyte ha_id, jbyte scsi_id){
	SMDI_MasterIdentify(ha_id, scsi_id);
}


/*
 * Class:     SMDIAgent
 * Method:    SMDI_Init
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiInit
  (JNIEnv * env, jobject obj)
{
	SMDI_Initialized = TRUE;
	return SMDI_Init();
}

/*
 * Class:     com_pcmsolutions_SMDI_SMDIAgent
 * Method:    smdiGetDeviceInfo
 * Signature: (BB)V
 */
JNIEXPORT void JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiGetDeviceInfo
(JNIEnv * env, jobject obj, jbyte ha_id, jbyte scsi_id, jobject info){

	SCSI_DevInfo di;
		
	jclass cls = (*env)->GetObjectClass(env,info);

	di.dwStructSize = sizeof(SCSI_DevInfo);
	SMDI_GetDeviceInfo(ha_id, scsi_id, &di);

	(*env)->CallVoidMethod(env, info, (*env)->GetMethodID(env,cls,"setDeviceType", "(B)V"), di.DevType);	
	(*env)->CallVoidMethod(env, info, (*env)->GetMethodID(env,cls,"setSMDI", "(Z)V"), di.bSMDI);	
	(*env)->CallVoidMethod(env, info, (*env)->GetMethodID(env,cls,"setManufacturer", "(Ljava/lang/String;)V"), (*env)->NewStringUTF( env, di.cManufacturer));		
	(*env)->CallVoidMethod(env, info, (*env)->GetMethodID(env,cls,"setName", "(Ljava/lang/String;)V"), (*env)->NewStringUTF( env, di.cName) );	

	//free(&di);	
}

/*
 * Class:     com_pcmsolutions_SMDI_SMDIAgent
 * Method:    smdiSendFile
 * Signature: (BBSLjava/lang/String;Ljava/lang/String;Z)I
 */

JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiSendFile
(JNIEnv * env, jobject obj, jbyte ha_id, jbyte scsi_id, jint sampleNum, jstring fileName, jstring sampleName, jboolean async){

	jint retval = 0;	
	jthrowable exc;
	SMDI_FileTransfer sft;
	
	sft.dwStructSize = sizeof(SMDI_FileTransfer);
	sft.HA_ID = ha_id;
	sft.SCSI_ID = scsi_id;
	sft.dwSampleNumber = sampleNum;
	
	sft.lpFileName = (char *)   (*env)->GetStringUTFChars(env, fileName, 0);

	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}
	
	sft.lpSampleName = (char *) (*env)->GetStringUTFChars(env, sampleName, 0);

	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}

	sft.lpCallback = NULL;
	sft.dwUserData = 0;
	sft.bAsync = async;
	sft.lpReturnValue = malloc(sizeof(DWORD));

	retval =  SMDI_SendFile(&sft);
	
	(*env)->ReleaseStringUTFChars(env, fileName, sft.lpFileName);
	
	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}

	(*env)->ReleaseStringUTFChars(env, sampleName, sft.lpSampleName);

	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}
	
	//free(&sft);
	free(sft.lpReturnValue);
	
	return retval;
}

/*
 * Class:     com_pcmsolutions_SMDI_SMDIAgent
 * Method:    smdiRecvFile
 * Signature: (BBSLjava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_smdiRecvFile
(JNIEnv * env, jobject obj, jbyte ha_id, jbyte scsi_id, jint sampleNum, jstring fileName, jboolean async){

	jthrowable exc;
	jint retval;
	SMDI_FileTransfer sft; 
	
	sft.dwStructSize = sizeof(SMDI_FileTransfer);
	sft.HA_ID = ha_id;
	sft.SCSI_ID = scsi_id;
	sft.dwSampleNumber = sampleNum;
	sft.lpFileName = (char *)   (*env)->GetStringUTFChars(env, fileName, 0);
	sft.lpReturnValue = malloc(sizeof(DWORD));

	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}

	
	sft.dwFileType = FT_WAV;
	sft.lpCallback = NULL;
	sft.dwUserData = 0;
	sft.bAsync = async;

	retval = SMDI_ReceiveFile(&sft);
	//retval = SMDIM_ENDOFPROCEDURE;
	
	(*env)->ReleaseStringUTFChars(env, fileName, sft.lpFileName);

	exc = (*env)->ExceptionOccurred(env);
	if ( exc){
		(*env)->ExceptionDescribe(env);		
		(*env)->ExceptionClear(env);
		return 0;
	}

	//free(&sft);
	free(sft.lpReturnValue);

	return retval;
}


/*
 * Class:     com_pcmsolutions_SMDI_SMDIAgent
 * Method:    getFileSampleHeader
 * Signature: (Ljava/lang/String;)Lcom/pcmsolutions/smdi/SmdiSampleHeader;
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_getFileSampleHeader
(JNIEnv * env, jobject obj, jstring fileName, jobject hdr){
	
	jint retval;
	char* fn;	
	SMDI_SampleHeader sh;		
	jclass cls;
		
	cls = (*env)->GetObjectClass(env,hdr);
	
	fn = (char *) (*env)->GetStringUTFChars(env, fileName, 0);

	sh.dwStructSize = sizeof(SMDI_SampleHeader);
		
	retval = SMDI_GetFileSampleHeader(fn, &sh);

	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setDoesExist", "(Z)V"), sh.bDoesExist);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setBitsPerWord", "(B)V"), sh.BitsPerWord);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setNumChannels", "(B)V"), sh.NumberOfChannels);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopControl", "(B)V"), sh.LoopControl);		
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setNameLengthInBytes", "(B)V"), sh.NameLength );		
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPeriodInNS", "(I)V"), sh.dwPeriod );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLengthInSampleFrames", "(I)V"), sh.dwLength );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopStart", "(I)V"), sh.dwLoopStart );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopEnd", "(I)V"), sh.dwLoopEnd );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setName", "(Ljava/lang/String;)V"), (*env)->NewStringUTF( env, sh.cName) );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPitch", "(S)V"), sh.wPitch );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPitchFraction", "(S)V"), sh.wPitchFraction );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setDataOffset", "(I)V"), sh.dwDataOffset );	
	
	(*env)->ReleaseStringUTFChars(env, fileName, fn);

	return retval;
		
}

/*
typedef struct SMDI_SampleHeader
{
  DWORD dwStructSize;                   // (000)
  BOOL bDoesExist;                      // (004)
  BYTE BitsPerWord;                     // (008)
  BYTE NumberOfChannels;                // (009)
  BYTE LoopControl;                     // (010)
  BYTE NameLength;                      // (011)
  DWORD dwPeriod;                       // (012)
  DWORD dwLength;                       // (016)
  DWORD dwLoopStart;                    // (020)
  DWORD dwLoopEnd;                      // (024)
  WORD wPitch;                          // (028)
  WORD wPitchFraction;                  // (030)
  char cName[256];                      // (032)
  DWORD dwDataOffset;                   // (288)
} SMDI_SampleHeader;

*/
/*
   public byte getBitsPerWord() {
        return bitsPerWord;
    }

    public void setBitsPerWord(byte bitsPerWord) {
        this.bitsPerWord = bitsPerWord;
    }

    public boolean isDoesExist() {
        return doesExist;
    }

    public void setDoesExist(boolean doesExist) {
        this.doesExist = doesExist;
    }

    public int getLengthInSampleFrames() { // Number of frames in sample (one sample frame's elementCount is ( NumberOfChannels*BitsPerWord)/8 Bytes)
        return lengthInSampleFrames;
    }

    public void setLengthInSampleFrames(int lengthInSampleFrames) {
        this.lengthInSampleFrames = lengthInSampleFrames;
    }

    public byte getLoopControl() {
        return loopControl;
    }

    public void setLoopControl(byte loopControl) {          // Specifies if and how the defined loop between LoopStart and LoopEnd should be played
        this.loopControl = loopControl;
    }

    public int getLoopEnd() {      // sample frame where sample ends
        return loopEnd;
    }

    public void setLoopEnd(int loopEnd) {
        this.loopEnd = loopEnd;
    }

    public int getLoopStart() {     // sample frame where sample starts
        return loopStart;
    }

    public void setLoopStart(int loopStart) {
        this.loopStart = loopStart;
    }

    public String getName() {           // name of sample
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNameLengthInBytes() {
        return nameLengthInBytes;
    }

    public void setNameLengthInBytes(int nameLengthInBytes) {
        this.nameLengthInBytes = nameLengthInBytes;
    }

    public byte getNumChannels() {              // 1 or 2 usually
        return numChannels;
    }

    public void setNumChannels(byte numChannels) {
        this.numChannels = numChannels;
    }

    public int getPeriodInNS() {            // 10^9 % period = sample frequency
        return periodInNS;
    }

    public void setPeriodInNS(int periodInNS) {
        this.periodInNS = periodInNS;
    }

    public int getPitch() {                  // (0..127) 60 is middle C
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getPitchFraction() {    // Specifies the fine tuning of the sample, measured in cents upward
        return pitchFraction;
    }
        // from the semitone. 60.00 is exactly middle C, 60.32768 is 50 cents above middle C
    // In the case that the two pitch values are unavailable, a default of 60.00 should be used.



    public void setPitchFraction(int pitchFraction) {
        this.pitchFraction = pitchFraction;
    }
*/



/*
 * Class:     com_pcmsolutions_smdi_SMDIAgent
 * Method:    getSampleHeader
 * Signature: (BBSLcom/pcmsolutions/smdi/SmdiSampleHeader;)V
 */
JNIEXPORT jint JNICALL Java_com_pcmsolutions_smdi_SMDIAgent_getSampleHeader
(JNIEnv * env, jobject obj, jbyte ha_id, jbyte scsi_id, jint sample, jobject hdr){
	
	jint retval;
	SMDI_SampleHeader sh;		
	jclass cls;
	
	cls = (*env)->GetObjectClass(env,hdr);

	sh.dwStructSize = sizeof(SMDI_SampleHeader);
		
	retval = 	SMDI_SampleHeaderRequest(ha_id, scsi_id, sample, &sh);

	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setDoesExist", "(Z)V"), sh.bDoesExist);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setBitsPerWord", "(B)V"), sh.BitsPerWord);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setNumChannels", "(B)V"), sh.NumberOfChannels);	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopControl", "(B)V"), sh.LoopControl);		
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setNameLengthInBytes", "(B)V"), sh.NameLength );		
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPeriodInNS", "(I)V"), sh.dwPeriod );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLengthInSampleFrames", "(I)V"), sh.dwLength );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopStart", "(I)V"), sh.dwLoopStart );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setLoopEnd", "(I)V"), sh.dwLoopEnd );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setName", "(Ljava/lang/String;)V"), (*env)->NewStringUTF( env, sh.cName) );
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPitch", "(S)V"), sh.wPitch );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setPitchFraction", "(S)V"), sh.wPitchFraction );	
	(*env)->CallVoidMethod(env, hdr, (*env)->GetMethodID(env,cls,"setDataOffset", "(I)V"), sh.dwDataOffset );	


	return retval;
}



//(unsigned long (*SMDI_GetFileSampleHeader) ( char *, SMDI_SampleHeader * ))
//(unsigned long (*SMDI_SampleHeaderRequest) ( unsigned char,unsigned char, unsigned long, SMDI_SampleHeader * ))

/*
unsigned long GetStructSize ( void * srcStruct )
{
  unsigned long dwTemp;

  memcpy ( &dwTemp, srcStruct, 4 );
  return ( dwTemp );
}
*/
