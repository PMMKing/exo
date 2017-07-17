
#include <string.h>
#include <jni.h>
#include <android/log.h>
/* Header for class com_tcgch_historyclient_utils_SecureUtil */

#ifndef _Included_com_haolb_client_utils_SecureUtil
#define _Included_com_haolb_client_utils_SecureUtil
#ifdef __cplusplus
extern "C" {
#endif

	static char* hex = "0123456789ABCDEF" ;
//凯撒算法密码表,随机生成
	static char* caesarHexTable[] = {
		"F4C67DBE892A0351",
		"8E6B4FA59731C20D",
		"0853DC4EA6972F1B",
		"9301845CA7DB62EF",
		"A6C1BE270F53D849",
		"2BCF4E90738165AD",
		"1230A49B78C6D5EF",
		"B3F1806752A9ED4C",
		"CD041B792EF635A8",
		"6CD14A5F37892B0E",
		"54AC179B8632EDF0",
		"ECB9D3A2F7564018",
		"4E5AD18067B329CF",
		"3B4C9AF8507E126D",
		"79438F1EA560D2CB",
		"D72AE63BFC590841"
};

//java字符串转C字符串
char* jstringTostr(JNIEnv* env, jstring jstr)
{        
    char* pStr = NULL;
    jclass     jstrObj   = (*env)->FindClass(env, "java/lang/String");
    jstring    encode    = (*env)->NewStringUTF(env, "utf-8");
    jmethodID  methodId  = (*env)->GetMethodID(env, jstrObj, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray byteArray = (jbyteArray)(*env)->CallObjectMethod(env, jstr, methodId, encode);
    jsize      strLen    = (*env)->GetArrayLength(env, byteArray);
    jbyte      *jBuf     = (*env)->GetByteArrayElements(env, byteArray, JNI_FALSE);
    if (jBuf > 0)
    {
        pStr = (char*)malloc(strLen + 1);
        if (!pStr)
        {
            return NULL;
        }
        memcpy(pStr, jBuf, strLen);
        pStr[strLen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, byteArray, jBuf, 0);
    return pStr;
}

//C字符串转java字符串
jstring strToJstring(JNIEnv* env, const char* pStr)
{
    int        strLen    = strlen(pStr);
    jclass     jstrObj   = (*env)->FindClass(env, "java/lang/String");
    jmethodID  methodId  = (*env)->GetMethodID(env, jstrObj, "<init>", "([BLjava/lang/String;)V");
    jbyteArray byteArray = (*env)->NewByteArray(env, strLen);
    jstring    encode    = (*env)->NewStringUTF(env, "utf-8");

    (*env)->SetByteArrayRegion(env, byteArray, 0, strLen, (jbyte*)pStr);
    
    return (jstring)(*env)->NewObject(env, jstrObj, methodId, byteArray, encode);
}

//字节流转换为十六进制字符串

void ByteToHexStr(const unsigned char* source, char* dest, int sourceLen)

{
    short i;
    unsigned char highByte, lowByte;


    for (i = 0; i < sourceLen; i++)
    {
        highByte = source[i] >> 4;
        lowByte = source[i] & 0x0f ;


        highByte += 0x30;


        if (highByte > 0x39)
                dest[i * 2] = highByte + 0x07;
        else
                dest[i * 2] = highByte;


        lowByte += 0x30;
        if (lowByte > 0x39)
            dest[i * 2 + 1] = lowByte + 0x07;
        else
            dest[i * 2 + 1] = lowByte;
    }
    return ;
}

//十六进制字符串转换为字节流
void HexStrToByte(const char* source, unsigned char* dest, int sourceLen)
{
    int i;
    unsigned char highByte, lowByte;
    
    for (i = 0; i < sourceLen; i += 2)
    {
        highByte = toupper(source[i]);
        lowByte  = toupper(source[i + 1]);


        if (highByte > 0x39)
            highByte -= 0x37;
        else
            highByte -= 0x30;


        if (lowByte > 0x39)
            lowByte -= 0x37;
        else
            lowByte -= 0x30;

        dest[i / 2] = (highByte << 4) | lowByte;
		
		//__android_log_print(ANDROID_LOG_INFO, "HexStrToByte","i / 2 is %d", i / 2);
    }
	//__android_log_print(ANDROID_LOG_INFO, "HexStrToByte","sourceLen is %d", sourceLen);
    return ;
}


/*
 * Class:     com_tcgch_client_utils_SecureUtil
 * Method:    encode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_haolb_client_utils_SecureUtil_encode
  (JNIEnv *env, jclass jcls, jstring jcontent, jstring jkey){
  if(jcontent == NULL || jkey == NULL) {
	return NULL;
  }
  
  char* content = jstringTostr(env,jcontent);
  char* key = jstringTostr(env,jkey);
  
  int len = strlen(content);
  
  int i = 0,j = 0;
  for (i = 0; i < len; i++) {
	content[i] ^= key[i % strlen(key)];
  }

  char dest[len * 2 + 1];
  ByteToHexStr(content,dest,len);
  dest[len * 2] = '\0';
 
  
	int index = 0;
	for (i = 0; i < strlen(dest); i++) {
		char k = key[index++];
		if(index >= strlen(key)) {
			index = 0;
		}
		if(k < 0) {
			k = -1 * k;
		}
		int idx = k % 16;	
		for (j = 0; j < 16; j++) {
			if(dest[i] == hex[j]) {
				dest[i] = caesarHexTable[idx][j];
				break;
			}
		}
	}
	
  return strToJstring(env,dest);
}
/*
 * Class:     com_tcgch_client_utils_SecureUtil
 * Method:    decode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_haolb_client_utils_SecureUtil_decode
  (JNIEnv *env, jclass jcls, jstring jcontent, jstring jkey){
  if(jcontent == NULL || jkey == NULL) {
	return NULL;
  }
  char* content = jstringTostr(env,jcontent);
  char* key = jstringTostr(env,jkey);
  int len = strlen(content);
  
  //__android_log_print(ANDROID_LOG_INFO, "JNIMsg", content);
  //__android_log_print(ANDROID_LOG_INFO, "JNIMsg","content length is %d", len);
  
  int i = 0, j = 0;
  int index = 0;
	for (i = 0; i < len; i++) {
	
		char k = key[index++];
		if(index >= strlen(key)) {
			index = 0;
		}
		if(k < 0) {
			k = -1 * k;
		}
		
		int idx = k % 16;
		for (j = 0; j < 16; j++) {
			if(content[i] == caesarHexTable[idx][j]) {
				content[i] = hex[j];
				break;
			}
		}
	}
	//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", content + len - 32);
	//__android_log_print(ANDROID_LOG_INFO, "JNIMsg","content length is %d", len);
	
	char dest[len / 2 + 1];
	HexStrToByte(content,dest,len);
	dest[len / 2] = '\0';
	
	for (i = 0; i < len / 2; i++) {
		dest[i] ^= key[i % strlen(key)];
		//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "dest[i] %d",dest[i]);
	}
	dest[len / 2] = '\0';

  //__android_log_print(ANDROID_LOG_INFO, "JNIMsg", dest + len / 2 - 32);
  //__android_log_print(ANDROID_LOG_INFO, "JNIMsg","dest length is %d", strlen(dest));
  return strToJstring(env,dest);
}

#ifdef __cplusplus
}
#endif
#endif
